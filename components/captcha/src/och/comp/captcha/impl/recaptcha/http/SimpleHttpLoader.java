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
package och.comp.captcha.impl.recaptcha.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import och.comp.captcha.impl.recaptcha.ReCaptchaException;


public class SimpleHttpLoader implements HttpLoader {

	private static final int CONNECT_TIMEOUT = 10000;

	@Override
	public String httpGet(String urlS) {
		InputStream in = null;
		URLConnection connection = null;
		try {
			URL url = new URL(urlS);
			connection = url.openConnection();
			connection.setConnectTimeout(CONNECT_TIMEOUT);
			connection.setReadTimeout(CONNECT_TIMEOUT);
			
			in = connection.getInputStream();

			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			while (true) {
				int rc = in.read(buf);
				if (rc <= 0)
					break;
				else
					bout.write(buf, 0, rc);
			}

			// return the generated javascript.
			return bout.toString();
		}
		catch (IOException e) {
			throw new ReCaptchaException("Cannot load URL: " + e.getMessage(), e);
		}
		finally {
			try {
				if (in != null)
					in.close();
			}
			catch (Exception e) {
				// swallow.
			}
		}
	}

	@Override
	public String httpPost(String urlS, String postdata) {
		InputStream in = null;
		URLConnection connection = null;
		try {
			URL url = new URL(urlS);
			connection = url.openConnection();
			
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setConnectTimeout(CONNECT_TIMEOUT);
			connection.setReadTimeout(CONNECT_TIMEOUT);

			OutputStream out = connection.getOutputStream();
			out.write(postdata.getBytes());
			out.flush();

			in = connection.getInputStream();
			
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			while (true) {
				int rc = in.read(buf);
				if (rc <= 0)
					break;
				else
					bout.write(buf, 0, rc);
			}

			out.close();
			in.close();
			
			// return the generated javascript.
			return bout.toString();
		}
		catch (IOException e) {
			throw new ReCaptchaException("Cannot load URL: " + e.getMessage(), e);
		}
		finally {
			try {
				if (in != null)
					in.close();
			}
			catch (Exception e) {
				// swallow.
			}
		}
	}


}
