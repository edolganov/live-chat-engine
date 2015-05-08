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
package och.api.model.tariff;

import static och.api.model.tariff.TariffMath.*;
import static och.util.DateUtil.*;

import java.math.BigDecimal;
import java.util.Date;

import och.junit.AssertExt;

import org.junit.Test;

public class TariffMathTest extends AssertExt {
	
	@Test
	public void test_divide(){
		
		BigDecimal price = new BigDecimal(5);
		BigDecimal secPrice = TariffMath.divideForSecPrice(price);
		BigDecimal monthPrice = round(secPrice.multiply(SECONDS_IN_MONTH));
		assertEquals("5.00", monthPrice.toString());
		
	}
	

	@Test
	public void test_calculateBillForPeriod(){
		
		BigDecimal price = new BigDecimal(5);
		BigDecimal minVal = new BigDecimal(0.05);
		
		
		//15 мин
		{
			BigDecimal result = calcForPeriod(price, 
					parseStandartDateTime("15.08.2014 12:00:15"), 
					parseStandartDateTime("15.08.2014 12:15:12"),
					minVal);
			assertEquals("0.05", result.toString());
		}
		
		//1 час
		{
			BigDecimal result = calcForPeriod(price, 
					parseStandartDateTime("15.08.2014 12:00:15"), 
					parseStandartDateTime("15.08.2014 13:15:12"),
					minVal);
			assertEquals("0.05", result.toString());
		}
		
		//2 часа
		{
			BigDecimal result = calcForPeriod(price, 
					parseStandartDateTime("15.08.2014 12:00:15"), 
					parseStandartDateTime("15.08.2014 14:15:12"),
					minVal);
			assertEquals("0.05", result.toString());
		}
		
		
		
		
		//почти весь месяц [.****.]
		{
			BigDecimal result = calcForPeriod(price, 
					parseStandartDateTime("01.08.2014 12:00:15"), 
					parseStandartDateTime("31.08.2014 11:00:12"),
					minVal);
			assertEquals("4.83", result.toString());
		}
		//short month
		{
			BigDecimal result = calcForPeriod(price, 
					parseStandartDateTime("01.02.2014 12:00:15"), 
					parseStandartDateTime("28.02.2014 11:00:12"),
					minVal);
			assertEquals("4.35", result.toString());
		}
		
		
		//начал и закончил в середине месяца [..**..]
		{
			BigDecimal result = calcForPeriod(price, 
					parseStandartDateTime("15.08.2014 12:00:15"), 
					parseStandartDateTime("20.08.2014 11:00:12"),
					minVal);
			assertEquals("0.80", result.toString());
		}
		//short month
		{
			BigDecimal result = calcForPeriod(price, 
					parseStandartDateTime("15.02.2014 12:00:15"), 
					parseStandartDateTime("20.02.2014 11:00:12"),
					minVal);
			assertEquals("0.80", result.toString());
		}
		
		
		//начал в середине, закончил с концом месяца [..****]
		{
			
			Date endDate = dateEnd(parseStandartDateTime("31.08.2014 23:59:59"));
			BigDecimal result = calcForPeriod(price, 
					parseStandartDateTime("15.08.2014 12:00:15"), 
					endDate,
					minVal);
			assertEquals("2.66", result.toString());
		}
		//short month
		{
			
			Date endDate = dateEnd(parseStandartDateTime("28.02.2014 23:59:59"));
			BigDecimal result = calcForPeriod(price, 
					parseStandartDateTime("15.02.2014 12:00:15"), 
					endDate,
					minVal);
			assertEquals("2.18", result.toString());
		}
		
		
		//начал сначала, закончил в середине [****..]
		{
			
			Date beginDate = dateStart(parseStandartDateTime("01.08.2014 00:00:00"));
			BigDecimal result = calcForPeriod(price, 
					beginDate, 
					parseStandartDateTime("20.08.2014 11:00:12"),
					minVal);
			assertEquals("3.14", result.toString());
		}
		//short month
		{
			
			Date beginDate = dateStart(parseStandartDateTime("01.02.2014 00:00:00"));
			BigDecimal result = calcForPeriod(price, 
					beginDate, 
					parseStandartDateTime("20.02.2014 11:00:12"),
					minVal);
			assertEquals("3.14", result.toString());
		}
		
		
		//весь месяц [******]
		{
			
			Date beginDate = dateStart(parseStandartDateTime("01.08.2014 00:00:00"));
			Date endDate = dateEnd(parseStandartDateTime("31.08.2014 23:59:59"));
			BigDecimal result = calcForPeriod(price, 
					beginDate, 
					endDate,
					minVal);
			assertEquals("5.00", result.toString());
		}
		//short month
		{
			
			Date beginDate = dateStart(parseStandartDateTime("01.02.2014 00:00:00"));
			Date endDate = dateEnd(parseStandartDateTime("28.02.2014 23:59:59"));
			BigDecimal result = calcForPeriod(price, 
					beginDate, 
					endDate,
					minVal);
			assertEquals("4.52", result.toString());
		}
		
		
		
		//1,5 месяца
		{
			BigDecimal result = calcForPeriod(price, 
					parseStandartDateTime("01.07.2014 00:00:00"), 
					parseStandartDateTime("15.08.2014 11:00:12"),
					minVal);
			assertEquals("7.33", result.toString());
		}
		
		
		
		//2 месяца
		{
			BigDecimal result = calcForPeriod(price, 
					parseStandartDateTime("01.07.2014 00:00:00"), 
					parseStandartDateTime("31.08.2014 23:59:59"),
					minVal);
			assertEquals("10.00", result.toString());
		}
		
		
	}

}
