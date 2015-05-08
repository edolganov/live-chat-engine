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

import static och.util.sql.SingleTx.*;


import java.sql.Connection;

import och.util.sql.SingleTxConnectionRollbackException;

import org.junit.Test;

import test.TestException;

public class SingleConnectionTest extends BaseMemSqlTest {
	
	
	@Test
	public void test_inline_rollback() throws Exception {
		try {
			setSingleTxMode();
			
			//TX
			try (Connection conn = getSingleOrNewConnection(ds)){
				insertUser(conn, "name1");
				assertNotNull(selectUserByName(conn, "name1"));	
			}
			
			//other TX
			assertNull(asyncSelectUserByName(async, ds, "name1"));
			
			
			//inline TX
			try {
				setSingleTxMode();
				
				//TX
				try (Connection conn = getSingleOrNewConnection(ds)){
					insertUser(conn, "name2");
					assertNotNull(selectUserByName(conn, "name1"));	
					assertNotNull(selectUserByName(conn, "name2"));	
				}
				
				
				//second inline
				try {
					setSingleTxMode();
					
					//TX
					try (Connection conn = getSingleOrNewConnection(ds)){
						insertUser(conn, "name3");
						assertNotNull(selectUserByName(conn, "name1"));	
						assertNotNull(selectUserByName(conn, "name2"));	
						assertNotNull(selectUserByName(conn, "name3"));	
						throw new TestException();
					}
					
				}catch (Exception e) {
					rollbackSingleTx();
					throw e;
				}
				finally {
					closeSingleTx();
				}
				
			}catch (Exception e) {
				rollbackSingleTx();
				throw e;
			}
			finally {
				closeSingleTx();
			}
			
		}catch (Exception e) {
			assertTrue(e.toString(), e instanceof TestException);
			rollbackSingleTx();
		}finally {
			closeSingleTx();
		}
		
		assertNull(asyncSelectUserByName(async, ds, "name1"));
		assertNull(asyncSelectUserByName(async, ds, "name2"));
		assertNull(asyncSelectUserByName(async, ds, "name3"));
	}
	
	
	@Test
	public void test_inline_tx() throws Exception {
				
		try {
			setSingleTxMode();
			
			//TX
			try (Connection conn = getSingleOrNewConnection(ds)){
				insertUser(conn, "name1");
				assertNotNull(selectUserByName(conn, "name1"));	
			}
			
			//other TX
			assertNull(asyncSelectUserByName(async, ds, "name1"));
			
			
			//inline TX
			try {
				setSingleTxMode();
				
				//TX
				try (Connection conn = getSingleOrNewConnection(ds)){
					insertUser(conn, "name2");
					assertNotNull(selectUserByName(conn, "name1"));	
					assertNotNull(selectUserByName(conn, "name2"));	
				}
				
				
				//second inline
				try {
					setSingleTxMode();
					
					//TX
					try (Connection conn = getSingleOrNewConnection(ds)){
						insertUser(conn, "name3");
						assertNotNull(selectUserByName(conn, "name1"));	
						assertNotNull(selectUserByName(conn, "name2"));	
						assertNotNull(selectUserByName(conn, "name3"));	
					}
					
				}finally {
					closeSingleTx();
				}
				
			}finally {
				closeSingleTx();
			}
			
			//other TX
			assertNull(asyncSelectUserByName(async, ds, "name1"));
			assertNull(asyncSelectUserByName(async, ds, "name2"));
			assertNull(asyncSelectUserByName(async, ds, "name3"));
			
		}finally {
			closeSingleTx();
		}
		
		assertNotNull(asyncSelectUserByName(async, ds, "name1"));
		assertNotNull(asyncSelectUserByName(async, ds, "name2"));
		assertNotNull(asyncSelectUserByName(async, ds, "name3"));
		
		
	}

	
	
	
	@Test
	public void test_rollback_tx() throws Exception {
		

			
		setSingleTxMode();
		try {
			
			//TX
			try (Connection conn = getSingleOrNewConnection(ds)){
				insertUser(conn, "name1");
				assertNotNull(selectUserByName(conn, "name1"));	
			}
			
			//other TX
			assertNull(asyncSelectUserByName(async, ds, "name1"));
			
			
			//TX with some exception
			Connection conn = getSingleOrNewConnection(ds);
			try {
				
				assertNotNull(selectUserByName(conn, "name1"));	
				insertUser(conn, "name2");
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
	public void test_rollback_tx_with_catched_saved_exception() throws Exception {
		
		try {
			
			setSingleTxMode();
			try {
				
				//TX
				try (Connection conn = getSingleOrNewConnection(ds)){
					insertUser(conn, "name1");
					assertNotNull(selectUserByName(conn, "name1"));	
				}
				
				//other TX
				assertNull(asyncSelectUserByName(async, ds, "name1"));
				
				
				//TX with some exception
				Connection conn = getSingleOrNewConnection(ds);
				try {
					
					assertNotNull(selectUserByName(conn, "name1"));	
					insertUser(conn, "name2");
					assertNotNull(selectUserByName(conn, "name2"));
					
					throw new TestException("rollback");
					
				}catch (Exception e) {
					saveRealRollbackException(e);
					conn.rollback();
					throw e;
				}finally {
					conn.close();
				}
				
				
			}
			finally {
				closeSingleTx();
			}
			
		}catch (TestException e) {
			//ok
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
	public void test_rollback_tx_with_catched_exception() throws Exception {
		
		try {
			
			setSingleTxMode();
			try {
				
				//TX
				try (Connection conn = getSingleOrNewConnection(ds)){
					insertUser(conn, "name1");
					assertNotNull(selectUserByName(conn, "name1"));	
				}
				
				//other TX
				assertNull(asyncSelectUserByName(async, ds, "name1"));
				
				
				//TX with some exception
				Connection conn = getSingleOrNewConnection(ds);
				try {
					
					assertNotNull(selectUserByName(conn, "name1"));	
					insertUser(conn, "name2");
					assertNotNull(selectUserByName(conn, "name2"));
					
					throw new TestException("rollback");
					
				}catch (Exception e) {
					conn.rollback();
					throw e;
				}finally {
					conn.close();
				}
				
				
			}
			finally {
				closeSingleTx();
			}
			
		}catch (SingleTxConnectionRollbackException e) {
			//ok
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
	public void test_commit_tx_with_uncatched_exception() throws Exception {
		
		try {
			
			setSingleTxMode();
			try {
				
				//TX
				try (Connection conn = getSingleOrNewConnection(ds)){
					insertUser(conn, "name1");
					assertNotNull(selectUserByName(conn, "name1"));	
				}
				
				//other TX
				assertNull(asyncSelectUserByName(async, ds, "name1"));
				
				
				//TX with some exception
				try (Connection conn = getSingleOrNewConnection(ds)){
					assertNotNull(selectUserByName(conn, "name1"));	
					insertUser(conn, "name2");
					assertNotNull(selectUserByName(conn, "name2"));
					
					throw new TestException("rollback");
				}
				
				
			}
			finally {
				closeSingleTx();
			}
			
		}catch (TestException e) {
			//ok
		}
		
		
		//commited
		try (Connection conn = getSingleOrNewConnection(ds)){
			assertNotNull(selectUserByName(conn, "name1"));		
			assertNotNull(selectUserByName(conn, "name2"));	
		}
		
		assertNotNull(asyncSelectUserByName(async, ds, "name1"));
		assertNotNull(asyncSelectUserByName(async, ds, "name2"));
	}
	
	
	

	@Test
	public void test_commit_tx() throws Exception {
		
		setSingleTxMode();
		try {
			
			//TX
			try (Connection conn = getSingleOrNewConnection(ds)){
				insertUser(conn, "name1");
				assertNotNull(selectUserByName(conn, "name1"));	
			}
			
			//other TX
			assertNull(asyncSelectUserByName(async, ds, "name1"));
			
			//TX
			try (Connection conn = getSingleOrNewConnection(ds)){
				assertNotNull(selectUserByName(conn, "name1"));	
				insertUser(conn, "name2");
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
		

			
		try (Connection conn = getSingleOrNewConnection(ds)){
			insertUser(conn, "name1");
			assertNotNull(selectUserByName(conn, "name1"));	
		}
		
		//other TX
		assertNotNull(asyncSelectUserByName(async, ds, "name1"));
		
	}
	
	

	@Test
	public void test_conn() throws Exception{
		
		try (Connection conn = ds.getConnection()){
			
			long id = insertUser(conn, "name1");
			TestUserRow user = selectUserById(conn, id);
			assertNotNull(user);
			assertEquals("name1", user.name);
		}
		
	}
	



}
