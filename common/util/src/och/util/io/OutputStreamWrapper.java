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
package och.util.io;

import java.io.IOException;
import java.io.OutputStream;

import static och.util.StringUtil.*;

/**
 * for write(String str) method
 */
public class OutputStreamWrapper extends OutputStream {
	
	public final OutputStream os;
	
	public OutputStreamWrapper(OutputStream os) {
		this.os = os;
	}
	
	
	
	public void write(String str) throws IOException {
		os.write(getBytesUTF8(str));
	}
	
	public void write(Object b) throws IOException {
		os.write(getBytesUTF8(String.valueOf(b)));
	}
	
	

	@Override
	public void write(byte b[]) throws IOException {
		os.write(b);
	}

	@Override
	public void write(int b) throws IOException {
		os.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		os.write(b, off, len);
	}

	@Override
	public void flush() throws IOException {
		os.flush();
	}

	@Override
	public void close() throws IOException {
		os.close();
	}
	
	

}
