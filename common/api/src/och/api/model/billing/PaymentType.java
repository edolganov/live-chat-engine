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
package och.api.model.billing;

import och.util.model.HasIntCode;

public enum PaymentType implements HasIntCode {
	
	/** IN: коррекция счета прибавлением */
	SYSTEM_IN_CORRECTION(1),
	
	/** IN: пополнение баланса */
	REPLENISHMENT(2),
	
	/** IN: стартовый бонус */
	START_BONUS(3),
	
	
	UNKNOWN(0),
	
	/** OUT: коррекция счета убавлением */
	SYSTEM_OUT_CORRECTION(-1),
	
	/** OUT: счет за пользование тарифом в прошлом месяце */
	TARIFF_MONTH_BIll(-2),
	
	/** OUT: счет за смену тарифа с расчетом прошлого в текущем месяце */
	TARIFF_CHANGE_BIll(-3),
	
	
	
	;
	
	public final int code;

	private PaymentType(int code) {
		this.code = code;
	}
	
	@Override
	public int getCode() {
		return code;
	}

}
