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
package och.util.socket.pool;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import static och.util.socket.SocketUtil.*;

public abstract class SocketConn implements Closeable {
	
	public abstract InputStream getInputStream();
	
	public abstract OutputStream getOutputStream();
	
	public abstract void invalidate();
	
	public PrintWriter getWriter() throws IOException{
		return getWriterUTF8(this);
	}
	
	public BufferedReader getReader() throws IOException{
		return getReaderUTF8(this);
	}

}
