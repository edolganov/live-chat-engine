/*
 * Copyright 2015 Evgeny Dolganov (evgenij.dolganov@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package och.comp.paypal;

import static och.api.model.BaseBean.*;
import static och.api.model.PropKey.*;
import static och.api.model.billing.PaymentProvider.*;
import static och.util.DateUtil.*;
import static och.util.Util.*;
import static urn.ebay.apis.eBLBaseComponents.AckCodeType.*;
import static urn.ebay.apis.eBLBaseComponents.PaymentStatusCodeType.*;

import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import och.api.exception.paypal.PaypalSoapException;
import och.api.model.billing.PayData;
import och.api.model.billing.PaymentBase;
import och.api.model.billing.PaymentStatus;
import och.service.props.Props;
import och.service.props.impl.FileProps;
import och.util.Util;
import och.util.exception.ContinueLoopException;

import org.apache.commons.logging.Log;

import urn.ebay.api.PayPalAPI.DoExpressCheckoutPaymentReq;
import urn.ebay.api.PayPalAPI.DoExpressCheckoutPaymentRequestType;
import urn.ebay.api.PayPalAPI.DoExpressCheckoutPaymentResponseType;
import urn.ebay.api.PayPalAPI.PayPalAPIInterfaceServiceService;
import urn.ebay.api.PayPalAPI.SetExpressCheckoutReq;
import urn.ebay.api.PayPalAPI.SetExpressCheckoutRequestType;
import urn.ebay.api.PayPalAPI.SetExpressCheckoutResponseType;
import urn.ebay.api.PayPalAPI.TransactionSearchReq;
import urn.ebay.api.PayPalAPI.TransactionSearchRequestType;
import urn.ebay.api.PayPalAPI.TransactionSearchResponseType;
import urn.ebay.apis.CoreComponentTypes.BasicAmountType;
import urn.ebay.apis.eBLBaseComponents.AbstractResponseType;
import urn.ebay.apis.eBLBaseComponents.AckCodeType;
import urn.ebay.apis.eBLBaseComponents.CurrencyCodeType;
import urn.ebay.apis.eBLBaseComponents.DoExpressCheckoutPaymentRequestDetailsType;
import urn.ebay.apis.eBLBaseComponents.DoExpressCheckoutPaymentResponseDetailsType;
import urn.ebay.apis.eBLBaseComponents.ErrorType;
import urn.ebay.apis.eBLBaseComponents.ItemCategoryType;
import urn.ebay.apis.eBLBaseComponents.PaymentActionCodeType;
import urn.ebay.apis.eBLBaseComponents.PaymentDetailsItemType;
import urn.ebay.apis.eBLBaseComponents.PaymentDetailsType;
import urn.ebay.apis.eBLBaseComponents.PaymentInfoType;
import urn.ebay.apis.eBLBaseComponents.PaymentTransactionSearchResultType;
import urn.ebay.apis.eBLBaseComponents.SetExpressCheckoutRequestDetailsType;
import urn.ebay.apis.eBLBaseComponents.SeverityCodeType;

public class PaypalSoapClient implements PaypalClient {

	public static final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	
	private Log log = getLog(getClass());
	private Props sysProps;
	private Props conProps;
	private CopyOnWriteArrayList<PaypalClientListener> listeners = new CopyOnWriteArrayList<>();
	
	public PaypalSoapClient(Props systemProps, Props connectProps) {
		
		this.sysProps = systemProps;
		this.conProps = connectProps;
		
	}
	
	@Override
	public String getKey() {
		return sysProps.getStrVal(paypal_key);
	}

	
	@Override
	public void addListener(PaypalClientListener l){
		listeners.add(l);
	}
	
	
	@Override
	public PayData payWithAccReq(BigDecimal val, long userId) throws Exception {
		
		validateState(val.doubleValue() > 0, "val");
		
		String redirectUrl = conProps.findVal("service.ExpressUrl");
		
		SetExpressCheckoutRequestDetailsType details = new SetExpressCheckoutRequestDetailsType();
		details.setReturnURL(sysProps.findVal(httpsServerUrl) + sysProps.getStrVal(paypal_preConfirmUri));
		details.setCancelURL(sysProps.findVal(httpsServerUrl) + sysProps.getStrVal(paypal_failUri));
		details.setPaymentDetails(list(createPaymentDetailsType(val, userId)));
		details.setNoShipping("1");
		details.setBrandName("SomeBrand");
		
		
		SetExpressCheckoutReq req = new SetExpressCheckoutReq();
		req.setSetExpressCheckoutRequest(new SetExpressCheckoutRequestType(details));
		
		//soap call
		SetExpressCheckoutResponseType resp = getApi().setExpressCheckout(req);
		checkForErrors(resp, "payWithAccReq");

		
		String token = resp.getToken();
		return new PayData(token, redirectUrl + token);
		
	}


	
	@Override
	public PaymentBase finishPayment(String token, String payerId, BigDecimal val, long userId) throws Exception {
		
		validateForEmpty(token, "token");
		validateForEmpty(payerId, "payerId");
		validateState(val.doubleValue() > 0, "val");
		
		DoExpressCheckoutPaymentRequestDetailsType details = new DoExpressCheckoutPaymentRequestDetailsType();
		details.setToken(token);
		details.setPayerID(payerId);
		details.setPaymentAction(PaymentActionCodeType.SALE);
		details.setPaymentDetails(list(createPaymentDetailsType(val, userId)));
		
		DoExpressCheckoutPaymentReq req = new DoExpressCheckoutPaymentReq();
		req.setDoExpressCheckoutPaymentRequest(new DoExpressCheckoutPaymentRequestType(details));
		
		DoExpressCheckoutPaymentResponseType resp = getApi().doExpressCheckoutPayment(req);
		checkForErrors(resp, "finishPayment");
		
		AckCodeType ack = resp.getAck();
		if(ack == SUCCESS){
			return createInputPayment(resp);
		}
		else if(ack == SUCCESSWITHWARNING){
			String data = toJson(resp, true);
			for(PaypalClientListener l : listeners) l.onPaymentWarning(data);
			return createInputPayment(resp);
		}
		else {
			throw new PaypalSoapException("Invalid payment: wrong status '"+ack+"': "+toJson(resp));
		}
	}
	

	private PaymentBase createInputPayment(DoExpressCheckoutPaymentResponseType resp) {
		
		DoExpressCheckoutPaymentResponseDetailsType details = resp.getDoExpressCheckoutPaymentResponseDetails();
		List<PaymentInfoType> paymentsList = details.getPaymentInfo();
		if(isEmpty(paymentsList)) throw new PaypalSoapException("Invalid payment: no payment info: "+toJson(resp));
		
		//expected single info
		PaymentInfoType info = paymentsList.get(0);

		PaymentBase out = new PaymentBase();
		out.provider = PAYPAL;
		out.externalId = info.getTransactionID();
		out.created = Util.tryParseDate(info.getPaymentDate(), TIME_FORMAT, new Date());
		out.paymentStatus = getStatus(info.getPaymentStatus().getValue());
		out.amount = getAmountVal(info.getGrossAmount());
		return out;
		
	}

	@Override
	public List<PaymentBase> getPaymentHistory(int daysBefore) throws Exception{
		
		String dayBefore = formatReqDate(addDays(new Date(), -daysBefore));
		
		TransactionSearchReq req = new TransactionSearchReq();
		req.setTransactionSearchRequest(new TransactionSearchRequestType(dayBefore));
		
		//soap call
		TransactionSearchResponseType resp = getApi().transactionSearch(req);
		checkForErrors(resp, "getPaymentHistory");
		
		List<PaymentTransactionSearchResultType> payments = resp.getPaymentTransactions();
		List<PaymentBase> out = convert(payments, (p)-> createPayment(p));
		return out;
		
	}
	
	@Override
	public PaymentBase getPayment(Date startDate, String transactionID) throws Exception {
		
		TransactionSearchRequestType reqType = new TransactionSearchRequestType(formatReqDate(startDate));
		reqType.setTransactionID(transactionID);
		TransactionSearchReq req = new TransactionSearchReq();
		req.setTransactionSearchRequest(reqType);
		
		TransactionSearchResponseType resp = getApi().transactionSearch(req);
		checkForErrors(resp, "getPayment");
		
		List<PaymentTransactionSearchResultType> payments = resp.getPaymentTransactions();
		if(isEmpty(payments)) return null;
		
		PaymentBase out = createPayment(payments.get(0));
		return out;
	}
	
	
	public static String formatReqDate(Date date){
		Date dayStart = dateStart(date);
		return new SimpleDateFormat(TIME_FORMAT).format(dayStart);
	}
	
	public static PaymentBase createPayment(PaymentTransactionSearchResultType p){
		PaymentBase out = new PaymentBase();
		out.provider = PAYPAL;
		out.externalId = p.getTransactionID();
		out.created = Util.tryParseDate(p.getTimestamp(), TIME_FORMAT, null);
		out.paymentStatus = getStatus(p.getStatus());
		out.amount = getAmountVal(p.getGrossAmount());
		return out;
	}
	
	
	
	


	public static PaypalSoapClient create(Props systemProps){
		
		File file = new File(systemProps.findVal(paypal_configPath));
		if( ! file.exists()) throw new IllegalStateException("Can' find file : "+file);
		FileProps connectProps = FileProps.createPropsWithoutUpdate(file);
		
		return new PaypalSoapClient(systemProps, connectProps);
	}
	
	
	
	private PaymentDetailsType createPaymentDetailsType(BigDecimal val, long userId){
		
		BasicAmountType amt = new BasicAmountType(CurrencyCodeType.USD, String.valueOf(val));
		String name = "Online Chat Payment for Account#" + getAccId(userId);
		
		PaymentDetailsItemType item = new PaymentDetailsItemType();
		item.setQuantity(1);
		item.setName(name);
		item.setItemCategory(ItemCategoryType.DIGITAL);
		item.setAmount(amt);
		
		PaymentDetailsType paydtl = new PaymentDetailsType();
		paydtl.setPaymentAction(PaymentActionCodeType.SALE);
		paydtl.setOrderDescription(name);
		paydtl.setOrderTotal(amt);
		paydtl.setItemTotal(amt);
		paydtl.setPaymentDetailsItem(list(item));
		return paydtl;
	}


	
	
	private void checkForErrors(AbstractResponseType resp, String callerMsg) throws PaypalSoapException {
		
		if(resp == null) throw new IllegalStateException("soap resp data is null");
		
		List<ErrorType> errors = resp.getErrors();
		if(isEmpty(errors)) return;
		
		List<String> errMsgs = convert(errors, (error)->{
			
			String json = toJson(error);
			
			SeverityCodeType type = error.getSeverityCode();
			if(type == SeverityCodeType.WARNING){
				log.warn("Warn from paypal in "+callerMsg+": "+json);
				throw new ContinueLoopException();
			}
			return json;
		});
		
		if(isEmpty(errMsgs)) return;
		
		String finalMsg = errMsgs.size() == 1? errMsgs.get(0) : errMsgs.toString();
		throw new PaypalSoapException(finalMsg);
		
	}
	
	

	private PayPalAPIInterfaceServiceService getApi() {
		
		Map<String, String> map = conProps.toMap();
		//lib has url by itself
		map.remove("service.EndPoint");
		
		return new PayPalAPIInterfaceServiceService(map);
	}
	
	
	public static PaymentStatus getStatus(String status) {
		if(CREATED.getValue().equalsIgnoreCase(status)) return PaymentStatus.CREATED;
		if(COMPLETED.getValue().equalsIgnoreCase(status)) return PaymentStatus.COMPLETED;
		if(PENDING.getValue().equalsIgnoreCase(status)) return PaymentStatus.WAIT;
		if(INPROGRESS.getValue().equalsIgnoreCase(status)) return PaymentStatus.WAIT;
		if(REFUNDED.getValue().equalsIgnoreCase(status)) return PaymentStatus.RETURNED;
		return PaymentStatus.ERROR;
	}
	
	public static BigDecimal getAmountVal(BasicAmountType amount){
		return new BigDecimal(amount.getValue());
	}
	
	public static long getAccId(long userId) {
		return userId+1000;
	}
}
