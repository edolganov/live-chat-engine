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
package och.comp.db.base;

import static och.api.model.PropKey.*;
import static och.util.Util.*;
import static och.util.sql.Dialect.*;
import static och.util.sql.SingleTx.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import javax.sql.DataSource;

import och.api.model.PropKey;
import och.comp.db.base.annotation.CreateTablesAfter;
import och.comp.db.base.annotation.SQLDialect;
import och.comp.db.base.mybatis.BaseMapper;
import och.comp.db.base.mybatis.BaseMapperWithTables;
import och.comp.db.base.mybatis.CommitOnCloseSession;
import och.comp.db.base.universal.UniversalQueries;
import och.service.props.Props;

import org.apache.commons.logging.Log;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

public abstract class BaseDb {
	
	protected Log log = getLog(getClass());
	
	protected Collection<Class<?>> mappers;
	protected DataSource ds;
	protected SqlSessionFactory sessionFactory;
	protected Props props;
	boolean isNewTables;
	protected String dialect;
	
	public final UniversalQueries universal;
	
	public BaseDb(DataSource ds) {
		this(ds, null, null);
	}
	
	public BaseDb(DataSource ds, Props props) {
		this(ds, props, null);
	}
	
	
	public BaseDb(DataSource ds, Props props, String url) {
		
		this.ds = ds;
		this.props = props;
		this.dialect = props.getStrVal(PropKey.db_dialect);
		
		String mappersPackageName = getClass().getPackage().getName();
		
		//mybatis
		TransactionFactory txFactory = new JdbcTransactionFactory();
		Environment environment = new Environment("prod", txFactory, ds);
		Configuration config = new Configuration(environment);
		config.addMappers(mappersPackageName, BaseMapper.class);
		mappers = config.getMapperRegistry().getMappers();
		sessionFactory = new SqlSessionFactoryBuilder().build(config);
		
		universal = new UniversalQueries(ds, props, url);
	}
	
	protected void reinitDB() throws SQLException{
		dropTables();
		createTables();
	}
	
	private void dropTables() throws SQLException{
		try(SqlSession session = sessionFactory.openSession()){
			
			Connection conn = session.getConnection();
			Statement st = conn.createStatement();
			if(isDefault_Dialect(dialect)) dropTabelsDefault(st);
			else if(isPSQL_Dialect(dialect)) dropTabelsPSQL(st);
			else if(isH2_Dialect(dialect)) dropTabelsH2(st);
			
		}
	}

	private void dropTabelsDefault(Statement st) throws SQLException {
		st.execute("drop schema public;");
		st.execute("create schema public;");
	}
	
	private void dropTabelsPSQL(Statement st) throws SQLException {
		st.execute("drop schema public cascade;");
		st.execute("create schema public;");
	}
	
	private void dropTabelsH2(Statement st) throws SQLException{
		st.execute("DROP ALL OBJECTS;");
	}
	
	

	protected void createTables() {
		
		HashMap<Class<?>, HashSet<Class<?>>> waiters = new HashMap<>();
		
		//fill waiters
		for(Class<?> type : mappers){
			
			if( ! BaseMapperWithTables.class.isAssignableFrom(type)) continue;
			if(type.equals(BaseMapperWithTables.class)) continue;
			
			HashSet<Class<?>> waitFor = new HashSet<>();
			CreateTablesAfter waitForAnn = type.getAnnotation(CreateTablesAfter.class);
			if(waitForAnn != null){
				waitFor.addAll(list(waitForAnn.value()));
			}
			
			waiters.put(type, waitFor);
		}
		
		//do creates
		int mappersToCreate = 0;
		ArrayList<String> mappersWithErrors = new ArrayList<>();
		while(waiters.size() > 0){
			
			//create
			HashSet<Class<?>> created = new HashSet<>();
			for (Entry<Class<?>, HashSet<Class<?>>> entry : waiters.entrySet()) {
				if(entry.getValue().size() == 0){
					
					Class<?> type = entry.getKey();
					created.add(type);
					
					try(SqlSession session = sessionFactory.openSession()){
						
						Object mapper = session.getMapper(type);
						if(mapper instanceof BaseMapperWithTables){
							
							SQLDialect dialectType = type.getAnnotation(SQLDialect.class);
							String mapperDialect = dialectType == null? null : dialectType.value();
							if(mapperDialect == null) mapperDialect = DB_DEFAULT;
							if(dialect.equals(mapperDialect)){
								mappersToCreate++;
								((BaseMapperWithTables)mapper).createTables();
							}
						}
						
					}catch (Exception e) {
						if(props.getBoolVal(db_debug_LogSql)){
							log.error("can't createTables: " + e);
						}
						mappersWithErrors.add(type.getName());
					}
					
				}
			}
			
			if(created.size() == 0) throw new IllegalStateException("no mapper to create. all mappers is waitnig: "+waiters);
			
			//clean
			for (Class<?> type : created) {
				waiters.remove(type);
				for (Entry<Class<?>, HashSet<Class<?>>> entry : waiters.entrySet()) {
					entry.getValue().remove(type);
				}
			}
		}
		
		//check results
		if( ! isEmpty(mappersWithErrors)) log.info("can't create tables for "+mappersWithErrors);
		isNewTables = mappersToCreate > 0 && isEmpty(mappersWithErrors);
		
	}
	
	public boolean isNewTables(){
		return isNewTables;
	}
	
	public String getDialect(){
		return dialect;
	}
	
	protected CommitOnCloseSession openCommitOnCloseSession(){
		return openCommitOnCloseSession(false);
	}
	
	protected CommitOnCloseSession openCommitOnCloseBatchSession(){
		return openCommitOnCloseSession(true);
	}
	
	private CommitOnCloseSession openCommitOnCloseSession(boolean batch){
		
		ExecutorType executorType = batch? ExecutorType.BATCH : ExecutorType.SIMPLE;
		if( ! isSingleTxMode()){
			return new CommitOnCloseSession(sessionFactory.openSession(executorType));
		}

		//SINGLE CONN MODE
		Environment env = sessionFactory.getConfiguration().getEnvironment();
		DataSource ds = env.getDataSource();
		
		Connection conn = null;
		try {
			conn = getSingleOrNewConnection(ds);
		}catch (Exception e) {
			throw new IllegalStateException("can't get conneciton", e);
		}
		
		return new CommitOnCloseSession(sessionFactory.openSession(executorType, conn));


	}


	public static PooledDataSource createDataSource(Props p) {
		
		String url = p.findVal(db_url);
		
		PooledDataSource ds = new PooledDataSource();
		ds.setDriver(p.findVal(db_driver));         
		ds.setUrl(url);
		ds.setUsername(p.findVal(db_user));                                  
		ds.setPassword(p.findVal(db_psw));
		ds.setPoolMaximumActiveConnections(p.getIntVal(db_maxConnections));
		ds.setPoolMaximumIdleConnections(p.getIntVal(db_idleConnections));
		
		return ds;
	}
	
	
	

}
