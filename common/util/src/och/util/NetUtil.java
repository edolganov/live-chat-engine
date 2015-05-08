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
package och.util;

import static och.util.StreamUtil.*;
import static och.util.Util.*;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;

public class NetUtil {
	
	private static Log log = getLog(NetUtil.class);
	
	public static final int CONN_TIMEOUT = 30000;
	public static final int READ_TIMEOUT = 30000;

	
	public static final SSLSocketFactory trustAllSocketFactory = getTrustAllSocketFactory();
	public static final HostnameVerifier allHostVerifier = new HostnameVerifier() {
		
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};
	
	private static SSLSocketFactory getTrustAllSocketFactory(){
		
		TrustManager[] trustAllCerts = new TrustManager[]{
		    new X509TrustManager() {
		        @Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		            return null;
		        }
		        @Override
				public void checkClientTrusted(
		            java.security.cert.X509Certificate[] certs, String authType) {
		        }
		        @Override
				public void checkServerTrusted(
		            java.security.cert.X509Certificate[] certs, String authType) {
		        }
		    }
		};
		
		try {
		    SSLContext sc = SSLContext.getInstance("SSL");
		    sc.init(null, trustAllCerts, new java.security.SecureRandom());
		    return sc.getSocketFactory();
		} catch (Exception e) {
			log.error("can't create trustAllSocketFactory", e);
			return null;
		}
	}


	public static String sendRequest(String url) throws Exception {
		return sendRequest(url, CONN_TIMEOUT, READ_TIMEOUT);
	}
	
	public static String sendRequest(String url, int connectTimeout, int readTimeout) throws IOException {
		HttpURLConnection conn = openGetConn(url, connectTimeout, readTimeout);
		return invokeGet(conn);
	}
	
	public static HttpURLConnection openGetConn(String url, int connectTimeout, int readTimeout) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		conn.setConnectTimeout(connectTimeout);
		conn.setReadTimeout(readTimeout);
		conn.setUseCaches(false);
		conn.setDoOutput(false);
		conn.setDoInput(true);
		conn.setRequestMethod("GET");
		return conn;
	}
	
	public static HttpURLConnection setTrustAnyHttps(HttpURLConnection conn){
		
		if(trustAllSocketFactory == null){
			log.error("can't setTrustAnyHttps for "+conn);
			return conn;
		}
		
		if(conn instanceof HttpsURLConnection){
			HttpsURLConnection https = (HttpsURLConnection)conn;
			https.setSSLSocketFactory(trustAllSocketFactory);
			https.setHostnameVerifier(allHostVerifier);
		}
		return conn;
	}
	
	public static String invokeGet(HttpURLConnection conn) throws IOException {
		
		try{
			
            int code = conn.getResponseCode();
            //It may happen due to keep-alive problem http://stackoverflow.com/questions/1440957/httpurlconnection-getresponsecode-returns-1-on-second-invocation
            if (code==-1) {
            	throw new IllegalStateException("NetUtil: conn.getResponseCode() return -1");
            }
            
            InputStream is = new BufferedInputStream(conn.getInputStream());
            String enc = conn.getHeaderField("Content-Encoding");
            if(enc !=null && enc.equalsIgnoreCase("gzip")){
                is = new GZIPInputStream(is);
            }
            
            String response = streamToStr(is);
            return response;
        }
        finally{
            if(conn!=null){
                conn.disconnect();
            }
        }
	}
	
	
	
	public static String sendPost(String url) throws Exception {
		return sendPost(url, null, CONN_TIMEOUT, READ_TIMEOUT);
	}
	
	public static String sendPost(String url, String postBody) throws Exception {
		return sendPost(url, postBody, CONN_TIMEOUT, READ_TIMEOUT);
	}
	
	public static String sendPost(String url, Map<?,?> postParams) throws Exception {
		return sendPost(url, createPostBody(postParams), CONN_TIMEOUT, READ_TIMEOUT);
	}
	
	public static String sendPost(String url, String postBody, int connTimeout, int readTimeout) throws Exception {
		HttpURLConnection conn = openPostConn(url, connTimeout, readTimeout);
		return invokePost(conn, postBody);
	}
	
	public static HttpURLConnection openPostConn(String url, int connTimeout, int readTimeout) throws IOException {
		
		HttpURLConnection conn = (HttpURLConnection)new URL(url).openConnection();
        conn.setConnectTimeout(connTimeout);
        conn.setReadTimeout(readTimeout);
        conn.setUseCaches(false);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");
        
        String agent = System.getProperty("http.agent");
		if(hasText(agent)){
			conn.setRequestProperty("User-Agent", agent);
		}
        
		return conn;
	}
	
	public static String createPostBody(Map<?,?> postParams) throws UnsupportedEncodingException{
		
		if(isEmpty(postParams)) return null;
		
    	StringBuilder sb = new StringBuilder();
    	boolean first = true;
    	for(Entry<?,?> entry : postParams.entrySet()){
    		if(!first)sb.append('&');
    		first = false;
    		sb.append(URLEncoder.encode(String.valueOf(entry.getKey()), UTF8))
    		.append('=')
    		.append(URLEncoder.encode(String.valueOf(entry.getValue()), UTF8));
    	}
    	return sb.toString();
		
	}
	
	public static String invokePost(HttpURLConnection conn, String postBody) throws IOException{
		
		try{
            
            if( Util.isEmpty(postBody)){
            	
            	conn.setDoOutput(false);
            	
            } else {
            	
            	// Send post request
            	conn.setDoOutput(true);
            	
        		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        		wr.writeBytes(postBody);
        		wr.flush();
        		wr.close();
            }
            
            int code = conn.getResponseCode();
            //It may happen due to keep-alive problem http://stackoverflow.com/questions/1440957/httpurlconnection-getresponsecode-returns-1-on-second-invocation
            if (code==-1) {
            	throw new IllegalStateException("NetUtil: conn.getResponseCode() return -1");
            }
            
	            
        	InputStream is = new BufferedInputStream(conn.getInputStream());
            String enc = conn.getHeaderField("Content-Encoding");
            if(enc !=null && enc.equalsIgnoreCase("gzip")){
                is = new GZIPInputStream(is);
            }
            String response = StreamUtil.streamToStr(is);
            return response;
        }
        finally{
            if(conn!=null){
                conn.disconnect();
            }
        }
	}

}
