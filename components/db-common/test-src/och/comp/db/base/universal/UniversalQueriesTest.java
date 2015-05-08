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
package och.comp.db.base.universal;

import static och.util.sql.SingleTx.*;


import java.sql.Connection;


import och.comp.db.base.universal.UniversalQueries;
import och.util.sql.BaseMemSqlTest;
import och.util.sql.SingleTxConnectionRollbackException;

import org.junit.Test;

import test.TestException;

public class UniversalQueriesTest extends BaseMemSqlTest {
	


	@Test
	public void test_rollback_tx() throws Exception {

		UniversalQueries universal = new UniversalQueries(ds);
		
		setSingleTxMode();
		try {
			
			//TX
			long id1 = universal.nextSeqFor("user");
			universal.update(new CreateUser(id1, "name1"));
			try (Connection conn = getSingleOrNewConnection(ds)){
				assertNotNull(selectUserByName(conn, "name1"));	
			}
			
			//other TX
			assertNull(asyncSelectUserByName(async, ds, "name1"));
			
			
			//TX with some exception
			Connection conn = getSingleOrNewConnection(ds);
			try {
				
				assertNotNull(selectUserByName(conn, "name1"));	
				long id2 = universal.nextSeqFor("user");
				universal.update(new CreateUser(id2, "name2"));
				assertNotNull(selectUserByName(conn, "name2"));
				
				throw new TestException("rollback");
				
			}catch (Exception e) {
				conn.rollback();
				throw e;
			}finally {
				conn.close();
			}
			
			
		}
		catch (Exception e) {
			assertTrue(e.toString(), e instanceof SingleTxConnectionRollbackException);
			rollbackSingleTx();
		}
		finally {
			closeSingleTx();
		}
			
		
		
		//rollbacked
		try (Connection conn = getSingleOrNewConnection(ds)){
			assertNull(selectUserByName(conn, "name1"));		
			assertNull(selectUserByName(conn, "name2"));	
		}
		
		assertNull(asyncSelectUserByName(async, ds, "name1"));
		assertNull(asyncSelectUserByName(async, ds, "name2"));

	}
	
	@Test
	public void test_commit_tx() throws Exception {
		
		UniversalQueries universal = new UniversalQueries(ds);
		
		setSingleTxMode();
		try {
			
			//TX
			universal.update(new CreateUser(1, "name1"));
			try (Connection conn = getSingleOrNewConnection(ds)){
				assertNotNull(selectUserByName(conn, "name1"));	
			}
			
			//other TX
			assertNull(asyncSelectUserByName(async, ds, "name1"));
			
			//TX
			try (Connection conn = getSingleOrNewConnection(ds)){
				assertNotNull(selectUserByName(conn, "name1"));	
				universal.update(new CreateUser(2, "name2"));
				assertNotNull(selectUserByName(conn, "name2"));	
			}
			
			//other TX
			assertNull(asyncSelectUserByName(async, ds, "name2"));
			
			
		} finally {
			closeSingleTx();
		}
		
		//after commit
		try (Connection conn = getSingleOrNewConnection(ds)){
			assertNotNull(selectUserByName(conn, "name1"));		
			assertNotNull(selectUserByName(conn, "name2"));	
		}
		
		assertNotNull(asyncSelectUserByName(async, ds, "name1"));
		assertNotNull(asyncSelectUserByName(async, ds, "name2"));
		
		
	}
	
	@Test
	public void test_non_tx() throws Exception {
		
		UniversalQueries universal = new UniversalQueries(ds);
		universal.update(new CreateUser(1, "name1"));
		
		//other TX
		assertNotNull(asyncSelectUserByName(async, ds, "name1"));
		
	}
	
	
	
	@Test
	public void test_seq() throws Exception {
		
		UniversalQueries universal = new UniversalQueries(ds);
		assertEquals(1, universal.nextSeqFor("user"));
		assertEquals(2, universal.nextSeqFor("user"));
		
	}

}
