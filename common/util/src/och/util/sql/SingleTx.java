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

import static och.util.ExceptionUtil.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import javax.sql.DataSource;

import och.util.ExceptionUtil;
import och.util.model.CallableVoid;

public class SingleTx {
	
	static class Data {
		SingleTxConnection txConn;
		int extraCalls;
	}
	
	private static ThreadLocal<Data> threadLocal = new ThreadLocal<>();
	
	public static <T> T doInSingleTxMode(CallableVoid c) throws Exception {
		return doInSingleTxMode(()->{
			c.call();
			return null;
		}, null);
	}
	
	public static <T> T doInSingleTxMode(Callable<T> c) throws Exception {
		return doInSingleTxMode(c, null);
	}
	
	public static void doInSingleTxMode(CallableVoid c, ExceptionUtil.ExceptionHander exceptionHander) throws Exception {
		doInSingleTxMode(()->{
			c.call();
			return null;
		}, exceptionHander);
	}
	
	
	public static <T> T doInSingleTxMode(Callable<T> c, ExceptionUtil.ExceptionHander exceptionHander) throws Exception {
		
		setSingleTxMode();
		try {
			
			return c.call();
			
		}catch (Exception e) {
			rollbackSingleTx();
			if(exceptionHander != null) exceptionHander.handle(e);
			throw e;
		} finally {
			closeSingleTx();
		}
	}
	
	
	public static void setSingleTxMode(){
		Data data = threadLocal.get();
		if(data != null) {
			data.extraCalls++;
			return;
		}
		threadLocal.set(new Data());
	}
	
	public static boolean isSingleTxMode(){
		return threadLocal.get() != null;
	}
	
	
	public static Connection getSingleOrNewConnection(DataSource ds) throws SQLException{
		
		Data data = threadLocal.get();
		//not single mode
		if(data == null){
			return ds.getConnection();
		}
		
		if(data.txConn == null) {
			Connection realConn = ds.getConnection();
			realConn.setAutoCommit(false);
			data.txConn = new SingleTxConnection(realConn);
		}
		return data.txConn;
	}
	
	
	public static void saveRealRollbackException(Throwable t){
		Data data = threadLocal.get();
		if(data == null) return;
		if(data.txConn == null) return;
		data.txConn.realRollbackException = t;
	}
	
	public static Throwable getSavedRealRoolbackException(){
		Data data = threadLocal.get();
		if(data == null) return null;
		if(data.txConn == null) return null;
		return data.txConn.realRollbackException;
	}
	
	public static void rollbackSingleTx(){
		Data data = threadLocal.get();
		if(data == null) return;
		if(data.txConn == null) return;
		
		data.txConn.realRollbackException = null;
		data.txConn.txRollbacked = true;
		
	}
	
	
	public static void closeSingleTx() throws Exception {
		Data data = threadLocal.get();
		if(data == null) return;
		if(data.extraCalls > 0){
			data.extraCalls--;
			return;
		}
		
		try {
			
			if( data.txConn == null) return;
			
			Connection realConn =  data.txConn.real;
			boolean txRollbacked =  data.txConn.txRollbacked;
			Throwable realEx = data.txConn.realRollbackException;
			
			try {
				
				//need commit
				if( ! txRollbacked)  {
					realConn.commit();
				}
				//need rollback
				else {
					realConn.rollback();
					if(realEx != null) throw getExceptionOrThrowError(realEx);
				}
				
			}
			catch (Exception e) {
				//can't commit
				if( ! txRollbacked) realConn.rollback();
				throw e;
			} 
			finally {
				//close conn
				try {
					realConn.setAutoCommit(true);
					realConn.close();
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			
		} finally {
			threadLocal.remove();
		}
	}
	
	

	
	
	
//	public static void setSingleConnection(Connection conn){
//		Data data = threadLocal.get();
//		if(data == null) {
//			log.warn("SingleConnectionMode is FALSE");
//			return;
//		}
//		if(data.txConn != null) {
//			log.warn("Single connection is already setted");
//			return;
//		}
//		data.txConn = new SingleTxConnection(conn);
//	}
//	
//	public static Connection getSingleConnection(){
//		Data data = threadLocal.get();
//		return data == null? null : data.txConn;
//	}
	

	


}
