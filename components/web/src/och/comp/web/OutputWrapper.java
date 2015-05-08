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
package och.comp.web;

import static och.util.StringUtil.*;

import java.io.IOException;
import java.io.OutputStream;

public class OutputWrapper {
	
	public static interface Converter {
		String convert(String out);
	}
	
	public static class DummyConverter implements OutputWrapper.Converter {
		@Override
		public String convert(String out) {
			return out;
		}
	}
	
	public final OutputStream os;
	public final Converter converter;
	
	
	public OutputWrapper(OutputStream os) {
		this(os, new DummyConverter());
	}
	
	public OutputWrapper(OutputStream os, Converter converter) {
		this.os = os;
		this.converter = converter;
	}
	
	public void write(Object b) throws IOException {
		write(String.valueOf(b));
	}
	
	
	public void write(String str) throws IOException {
		str = converter.convert(str);
		os.write(getBytesUTF8(str));
	}
	


}
