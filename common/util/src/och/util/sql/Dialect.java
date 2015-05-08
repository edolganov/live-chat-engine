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
package och.util.sql;

public class Dialect {
	
	public static final String DB_DEFAULT = "default";
	public static final String DB_POSTGRESQL = "postgresql";
	public static final String DB_H2 = "h2";
	
	
	public static boolean isDefault_Dialect(String val){
		return DB_DEFAULT.equals(val);
	}
	
	public static boolean isPSQL_Dialect(String val){
		return DB_POSTGRESQL.equals(val);
	}
	
	public static boolean isH2_Dialect(String val){
		return DB_H2.equals(val);
	}

}
