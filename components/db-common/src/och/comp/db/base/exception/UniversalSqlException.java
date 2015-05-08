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
package och.comp.db.base.exception;

import java.sql.SQLException;

public class UniversalSqlException extends SQLException{
	
	private static final long serialVersionUID = 1L;

	public UniversalSqlException() {
		super();
	}

	public UniversalSqlException(String reason, String sqlState,
			int vendorCode, Throwable cause) {
		super(reason, sqlState, vendorCode, cause);
	}

	public UniversalSqlException(String reason, String SQLState, int vendorCode) {
		super(reason, SQLState, vendorCode);
	}

	public UniversalSqlException(String reason, String sqlState, Throwable cause) {
		super(reason, sqlState, cause);
	}

	public UniversalSqlException(String reason, String SQLState) {
		super(reason, SQLState);
	}

	public UniversalSqlException(String reason, Throwable cause) {
		super(reason, cause);
	}

	public UniversalSqlException(String reason) {
		super(reason);
	}

	public UniversalSqlException(Throwable cause) {
		super(cause);
	}
	
	

}
