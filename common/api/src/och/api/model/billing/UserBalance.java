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

import static java.math.BigDecimal.*;

import java.math.BigDecimal;

public class UserBalance {
	
	public long userId;
	public BigDecimal balance = ZERO;
	public boolean accsBlocked;

	
	
	public void setId(long id) {
		this.userId = id;
	}

	public void setBalance(BigDecimal balance) {
		if(balance == null) balance = ZERO;
		this.balance = balance;
	}

	public void setAccsBlocked(Boolean accsBlocked) {
		this.accsBlocked = accsBlocked == null? false: accsBlocked;
	}

}
