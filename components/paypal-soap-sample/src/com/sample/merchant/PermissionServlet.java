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
package com.sample.merchant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.paypal.exception.ClientActionRequiredException;
import com.paypal.exception.HttpErrorException;
import com.paypal.exception.InvalidCredentialException;
import com.paypal.exception.InvalidResponseDataException;
import com.paypal.exception.MissingCredentialException;
import com.paypal.exception.SSLConfigurationException;
import com.paypal.sdk.exceptions.OAuthException;
import com.paypal.svcs.services.PermissionsService;
import com.paypal.svcs.types.common.RequestEnvelope;
import com.paypal.svcs.types.perm.RequestPermissionsRequest;
import com.paypal.svcs.types.perm.RequestPermissionsResponse;
import com.sample.util.Configuration;

public class PermissionServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public PermissionServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(true);
		session.setAttribute("page", request.getParameter("page"));

		getServletConfig().getServletContext()
				.getRequestDispatcher("/RequestPermissions.jsp")
				.forward(request, response);

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		session.setAttribute("url", request.getRequestURI());
		RequestEnvelope env = new RequestEnvelope("en_US");
		List<String> scope = new ArrayList<String>();
		String check[] = request.getParameterValues("api");
		for (int i = 0; i < check.length; i++) {
			scope.add(check[i]);
		}
		response.setContentType("text/html");

		String callback = request.getParameter("callback");
		try {
			if (request.getParameter("PermissionBtn").equals(
					"RequestPermissions")) {
				RequestPermissionsRequest permRequest = new RequestPermissionsRequest(
						scope, callback);
				permRequest.setRequestEnvelope(env);
				
				// Configuration map containing signature credentials and other required configuration.
				// For a full list of configuration parameters refer in wiki page.
				// (https://github.com/paypal/sdk-core-java/wiki/SDK-Configuration-Parameters)
				Map<String,String> configurationMap =  Configuration.getAcctAndConfig();
				
				// Creating service wrapper object to make an API call by loading configuration map.
				PermissionsService perm = new PermissionsService(configurationMap);
				
				RequestPermissionsResponse resp = perm
						.requestPermissions(permRequest);
				response.getWriter()
						.println(
								"<table><tr><td><h3>Step 1:</h3></td><td><h3>Requesting Permissions</h3></td></tr><tr><td><font color=grey><h3>Step 2:</h3></font></td><td><font color=grey><h3>Generate Access Token</h3></font></td></tr></table>");
				if (resp != null) {
					session.setAttribute("RESPONSE_OBJECT", resp);
					session.setAttribute("lastReq", perm.getLastRequest());
					session.setAttribute("lastResp", perm.getLastResponse());
					if (resp.getResponseEnvelope().getAck().toString()
							.equalsIgnoreCase("SUCCESS")) {
						Map<Object, Object> map = new LinkedHashMap<Object, Object>();
						map.put("Ack", resp.getResponseEnvelope().getAck());
						map.put("RequestToken", resp.getToken());
						map.put("Redirect URL",
								"<a href=https://www.sandbox.paypal.com/cgi-bin/webscr?cmd=_grant-permission&request_token="
										+ resp.getToken()
										+ ">Redirect To PayPal</a>");
						session.setAttribute("map", map);
						response.sendRedirect("Response.jsp");
					} else {
						session.setAttribute("Error", resp.getError());
						response.sendRedirect("Error.jsp");
					}
				}
			}
		} catch (OAuthException e) {
			// TODO: handle exception
			e.printStackTrace();
		} catch (SSLConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidCredentialException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HttpErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidResponseDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientActionRequiredException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MissingCredentialException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
