/*
 * Copyright 2012 Evgeny Dolganov (evgenij.dolganov@gmail.com).
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

import static och.util.ExceptionUtil.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import och.util.model.HoursAndMinutes;

public class DateUtil {
	
	public static final String FULL_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
	public static final String DAY_FORMAT = "yyyy-MM-dd";
	
    public static long ONE_SECOND = 1000L;
    public static long ONE_MINUTE = 60L * ONE_SECOND;
    public static long ONE_HOUR = 60L * ONE_MINUTE;
    public static long ONE_DAY = 24L * ONE_HOUR;
    

    public static Date dateStart(Date date) {
        if (date == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
    
	public static long dateStart(long time) {
		return dateStart(new Date(time)).getTime();
	}

    public static Date dateEnd(Date date) {
        if (date == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    public static Date monthStart(Date date) {
        if (date == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateStart(date));
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTime();
    }

    public static Date monthEnd(Date date) {
        if (date == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateEnd(date));
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return calendar.getTime();
    }

    public static Date previousMonthStart(Date date) {
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(monthStart(date));
        if (calendar.get(Calendar.MONTH) == Calendar.JANUARY) {
            calendar.set(Calendar.MONTH, Calendar.DECEMBER);
            calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 1);
        } else {
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
        }
        return calendar.getTime();
    }

    public static Date previousMonthEnd(Date date) {
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(monthStart(date));
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - 1);
        return dateEnd(calendar.getTime());
    }

    public static Date weekStart(Date date) {
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateStart(date));
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        return calendar.getTime();
    }


    /** абсолютная разница в днях между двумя временами (без учета начала дня) */
    public static long dateDiffInDays(Date dateEnd, Date dateStart) {
        return Math.abs((dateEnd.getTime() - dateStart.getTime()) / ONE_DAY);
    }
    

	public static Date addMonths(Date date, int monthsCount) {
		
        if (date == null) {
            return null;
        }
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.setLenient(false);
        calendar.add(Calendar.MONTH, monthsCount);
        return calendar.getTime();
	}

    public static Date addDays(Date date, int days) {
        if (date == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + days);
        return calendar.getTime();
    }

    public static Date decreaseDays(Date date, int days) {
        if (date == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - days);
        return calendar.getTime();
    }

    public static Date nextDateFromToday() {
        return nextDate(new Date());
    }

    public static Date nextWeekFromToday() {
        return addDays(new Date(), 7);
    }
    
    public static Date nextTwoWeeksFromToday() {
        return addDays(new Date(), 14);
    }

    public static Date nextDate(Date date) {
        return addDays(date, 1);
    }
    
    public static int dayOfMonth(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }
    
    
    public static HoursAndMinutes getHoursAndMinutes(Date date) {
        if (date == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        return new HoursAndMinutes(hours, minutes);
    }

    public static Double getTimeInMilliseconds(int scale){
        return new BigDecimal(System.nanoTime()).divide(new BigDecimal(1000000)).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    public static Float getDeltaTimeInMilliseconds(double startTime, double finishTime, int scale){
        return new BigDecimal(finishTime).subtract(new BigDecimal(startTime)).setScale(scale, RoundingMode.HALF_UP).floatValue();
    }
    
    /**
     * Парсинг даты формата "dd.MM.yyyy"
     */
    public static Date parseStandartDate(String ddMMYYY) {
        return parseDate(ddMMYYY, "dd.MM.yyyy");
    }
    
    /**
     * Парсинг даты формата "dd.MM.yyyy HH:mm:ss"
     */
    public static Date parseStandartDateTime(String ddMMYYY_hhmmsss) {
        return parseDate(ddMMYYY_hhmmsss, "dd.MM.yyyy HH:mm:ss");
    }
    
    public static Date parseDate(String dateStr, String pattern){
    	try {
	        SimpleDateFormat df = new SimpleDateFormat(pattern);
	        return df.parse(dateStr); 
    	}catch (Exception e) {
			throw getRuntimeExceptionOrThrowError(e, "can't parse date");
		}
    }
    
	public static Date tryParseDate(String date, String format, Date defaultDate){
		if( ! Util.hasText(date)) return defaultDate;
		try {
			return parseDate(date, format);
		}catch(Exception e){
			return defaultDate;
		}
	}
    
    public static String formatDate(long date, String pattern){
        return formatDate(new Date(date), pattern) ;
    }
    
    public static String formatDate(long date){
        return formatDate(new Date(date), "yyyy-MM-dd HH:mm:ssZ") ;
    }
    
    public static String formatDate(Date date){
    	return formatDate(date, "yyyy-MM-dd HH:mm:ssZ") ;
    }
    
    public static String formatDate(Date date, String pattern){
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        return df.format(date); 
    }
    
    public static boolean isSameMonth(Date a, Date b){
    	Date normDateA = monthStart(dateStart(a));
    	Date normDateB = monthStart(dateStart(b));
    	return normDateA.equals(normDateB);
    }
    
    public static boolean isSameDay(Date a, Date b){
    	Date normDateA = dateStart(a);
    	Date normDateB = dateStart(b);
    	return normDateA.equals(normDateB);
    }
    


}

