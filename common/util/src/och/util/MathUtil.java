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

public class MathUtil {
	
	/**
	 * Округление числа в большую сторону.
	 * Если число отрицательное, то сначала округляем модуль, а потом возвращаем ему знак.
	 * <p>Пример:
	 * <pre> roundUp(119, 30) -> 120
	 * roundUp(120, 30) -> 120
	 * roundUp(121, 30) -> 150
	 * roundUp(-121, 30) -> -150
	 * </pre>
	 * @param val входное число
	 * @param delta число на которое округляем
	 */
	public static long roundUp(long val, long delta){
		boolean isNegative = val < 0;
		val = Math.abs(val);
		
		long old = val;
		val = val  / delta * delta;
		if(val < old) val += delta;
		
		return !isNegative? val : -val;
	}
	
	
	public static long roundDown(long val, long delta){
		boolean isNegative = val < 0;
		val = Math.abs(val);
		val = val  / delta * delta;
		return !isNegative? val : -val;
	}

}
