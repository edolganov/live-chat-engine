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
package och.util.json;

public class JsonUtil {

	//from com.google.gson.stream.JsonWriter
	
	/*
	 * From RFC 4627, "All Unicode characters may be placed within the quotation
	 * marks except for the characters that must be escaped: quotation mark,
	 * reverse solidus, and the control characters (U+0000 through U+001F)."
	 * 
	 * We also escape '\u2028' and '\u2029', which JavaScript interprets as
	 * newline characters. This prevents eval() from failing with a syntax
	 * error. http://code.google.com/p/google-gson/issues/detail?id=341
	 */
	public static final String[] REPLACEMENT_CHARS;
	public static final String[] HTML_SAFE_REPLACEMENT_CHARS;
	static {
		REPLACEMENT_CHARS = new String[128];
		for (int i = 0; i <= 0x1f; i++) {
			REPLACEMENT_CHARS[i] = String.format("\\u%04x", (int) i);
		}
		REPLACEMENT_CHARS['"'] = "\\\"";
		REPLACEMENT_CHARS['\\'] = "\\\\";
		REPLACEMENT_CHARS['\t'] = "\\t";
		REPLACEMENT_CHARS['\b'] = "\\b";
		REPLACEMENT_CHARS['\n'] = "\\n";
		REPLACEMENT_CHARS['\r'] = "\\r";
		REPLACEMENT_CHARS['\f'] = "\\f";
		HTML_SAFE_REPLACEMENT_CHARS = REPLACEMENT_CHARS.clone();
		HTML_SAFE_REPLACEMENT_CHARS['<'] = "\\u003c";
		HTML_SAFE_REPLACEMENT_CHARS['>'] = "\\u003e";
		HTML_SAFE_REPLACEMENT_CHARS['&'] = "\\u0026";
		HTML_SAFE_REPLACEMENT_CHARS['='] = "\\u003d";
		HTML_SAFE_REPLACEMENT_CHARS['\''] = "\\u0027";
	}

	public static String escapeStr(String str, boolean htmlSafe) {

		StringBuilder st = new StringBuilder();

		String[] replacements = htmlSafe ? HTML_SAFE_REPLACEMENT_CHARS : REPLACEMENT_CHARS;
		int last = 0;
		int length = str.length();
		for (int i = 0; i < length; i++) {
			char c = str.charAt(i);
			String replacement = getReplacement(c, replacements);
			if (replacement == null) {
				continue;
			}
			if (last < i) {
				st.append(str.substring(last, last + i - last));
			}
			st.append(replacement);
			last = i + 1;
		}
		if (last < length) {
			st.append(str.substring(last, last + length - last));
		}
		
		return st.toString();

	}
	
	public static String getReplacement(char c, boolean htmlSafe){
		String[] replacements = htmlSafe ? HTML_SAFE_REPLACEMENT_CHARS : REPLACEMENT_CHARS;
		return getReplacement(c, replacements);
	}
	
	private static String getReplacement(char c, String[] replacements){
		if (c < 128) {
			return replacements[c];
		}
		if (c == '\u2028') {
			return "\\u2028";
		}
		if (c == '\u2029') {
			return "\\u2029";
		}
		return null;
	}

}
