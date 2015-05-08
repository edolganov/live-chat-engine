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
package com.paypal.core;

import static och.util.Util.*;

/**
 * Replace of default impl
 */
public class LoggingManager {
	
	private LoggingManager() {}

	public static void debug(Class<?> thisClass, Object message) {
		getLog(thisClass).debug(message);
	}

	public static void debug(Class<?> thisClass, Object message, Throwable t) {
		getLog(thisClass).debug(message, t);
	}

	public static void info(Class<?> thisClass, Object message) {
		getLog(thisClass).info(message);
	}

	public static void info(Class<?> thisClass, Object message, Throwable t) {
		getLog(thisClass).info(message, t);
	}

	public static void warn(Class<?> thisClass, Object message) {
		getLog(thisClass).warn(message);
	}

	public static void warn(Class<?> thisClass, Object message, Throwable t) {
		getLog(thisClass).warn(message, t);
	}

	public static void severe(Class<?> thisClass, Object message) {
		getLog(thisClass).error(message);
	}

	public static void severe(Class<?> thisClass, Object message, Throwable t) {
		getLog(thisClass).error(message, t);
	}
}
