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
package och.api.model.tariff;

import java.math.BigDecimal;
import java.util.Date;

public class TariffMath {

	public static final BigDecimal SECONDS_IN_MONTH = new BigDecimal(31 * 24 * 60 * 60);
	
	
	public static void main(String[] args) {
		BigDecimal sec = new BigDecimal(5).divide(SECONDS_IN_MONTH, 20, BigDecimal.ROUND_HALF_UP);
		System.out.println(sec);
		System.out.println(round(sec.multiply(SECONDS_IN_MONTH)));
	}
	
	public static BigDecimal calcForPeriod(BigDecimal monthPrice, 
			Date begin, 
			Date end,
			BigDecimal minVal) {
		
		if(monthPrice == null) 
			throw new IllegalArgumentException("monthPrice must be not null");
		if(begin.after(end)) 
			throw new IllegalArgumentException("begin must be before end: "+begin+", "+end);
		
		
		BigDecimal secPrice = divideForSecPrice(monthPrice);
		long secCount = (end.getTime() - begin.getTime()) / 1000;
		BigDecimal result = secPrice.multiply(new BigDecimal(secCount));
		result = round(result);
		
		if(result.compareTo(minVal) < 0){
			result = round(minVal);
		}
		
		return result;
	}

	public static BigDecimal divideForSecPrice(BigDecimal monthPrice) {
		return monthPrice.divide(SECONDS_IN_MONTH, 20, BigDecimal.ROUND_HALF_UP);
	}
	
	public static BigDecimal round(BigDecimal val){
		return val.setScale(2, BigDecimal.ROUND_HALF_UP);
	}
	
	public static BigDecimal roundAdd(BigDecimal val1, BigDecimal val2) {
		return round(val1.add(val2));
	}

}
