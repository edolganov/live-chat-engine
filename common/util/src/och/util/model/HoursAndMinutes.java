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
package och.util.model;

import static och.util.StringUtil.*;
import static och.util.Util.*;

import java.util.List;

public class HoursAndMinutes implements Comparable<HoursAndMinutes>{
	
	private int hours;
	private int minutes;
	
	public HoursAndMinutes(int hours, int minutes) {
		super();
		this.hours = hours;
		this.minutes = minutes;
	}

	public int getHours() {
		return hours;
	}

	public int getMinutes() {
		return minutes;
	}
	
	public String getTime(){
		return zeroPrefix(hours)+":"+zeroPrefix(minutes);
	}
	
	private static String zeroPrefix(int num){
		return (num >= 0 && num < 10) ? "0" + num : ""+ num;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + hours;
		result = prime * result + minutes;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HoursAndMinutes other = (HoursAndMinutes) obj;
		if (hours != other.hours)
			return false;
		if (minutes != other.minutes)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getTime();
	}
	
	@Override
	public int compareTo(HoursAndMinutes o) {
		int a = hours * 60 + minutes;
		int b = o.hours * 60 + minutes;
		return Integer.compare(a, b);
	}

	public static HoursAndMinutes tryParseHHmm(String val, HoursAndMinutes defVal) {
		if( ! hasText(val)) return defVal;
		
		List<String> items = strToList(val, ":");
		if(items.size() < 2) return defVal;
		
		int hours = tryParseInt(items.get(0), -1);
		int mins = tryParseInt(items.get(1), -1);
		if(
			hours < 0 
			|| hours > 23 
			|| mins < 0 
			|| mins > 59
				) return defVal;
		
		return new HoursAndMinutes(hours, mins);
	}


	
	
	
	
	
	
}
