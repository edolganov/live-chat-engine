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

import static och.util.Util.*;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import javax.sql.DataSource;


import och.util.concurrent.ExecutorsUtil;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;

import test.BaseTest;


public abstract class BaseMemSqlTest extends BaseTest {
	
	protected DataSource ds;
	protected ExecutorService async;
	
	public BaseMemSqlTest() {
		this.createDir = false;
	}

	@Before
	public void initDB() throws Exception {

		Class.forName("org.h2.Driver");
		
		int logMode = 1; //1-err,2-info,3-debug
		String url = "jdbc:h2:mem:db-"+randomSimpleId() + ";DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT="+logMode;

		JdbcDataSource ds = new JdbcDataSource();
		ds.setUrl(url);
		ds.setUser("sa");
		ds.setPassword("sa");
		this.ds = ds;
		
		//create db
		try (Connection conn = ds.getConnection()){
			
			Statement st = conn.createStatement();
			st.execute("CREATE TABLE user (id INT, name VARCHAR(50)); " 
			+" CREATE SEQUENCE user_seq START WITH 1 INCREMENT BY 1;");
			st.close();
			
		}
		
		async = ExecutorsUtil.newSingleThreadExecutor("async");
		

	}
	
	public static long insertUser(Connection conn, String name) throws SQLException{
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery("SELECT nextval('user_seq')");
		rs.next();
		long id = rs.getLong(1);
		rs.close();
		st.close();
		
		st = conn.createStatement();
		st.executeUpdate("INSERT INTO user (id, name) VALUES ("+id+", '"+name+"')");
		st.close();
		
		return id;
	}
	
	public static TestUserRow selectUserById(Connection conn, long id) throws SQLException{
		
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery("SELECT name FROM user WHERE id="+id);
		
		boolean hasValue = rs.next();
		String name = hasValue? rs.getString(1) : null;
		
		rs.close();
		st.close();
		
		return hasValue? new TestUserRow(id, name) : null;
	}
	
	public static TestUserRow selectUserByName(Connection conn, String name) throws SQLException{
		
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery("SELECT id FROM user WHERE name='"+name+"'");
		
		boolean hasValue = rs.next();
		long id = hasValue? rs.getLong(1) : -1;
		
		rs.close();
		st.close();
		
		return hasValue? new TestUserRow(id, name) : null;
	}
	
	public static TestUserRow asyncSelectUserByName(ExecutorService async, final DataSource ds, final String name) throws Exception {
		
		return async.submit(new Callable<TestUserRow>() {
			
			@Override
			public TestUserRow call() throws Exception {
				
				try(Connection conn = ds.getConnection()){
					return selectUserByName(conn, name);
				}
			}
		}).get();
	}

}
