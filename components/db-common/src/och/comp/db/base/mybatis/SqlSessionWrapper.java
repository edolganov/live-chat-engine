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
package och.comp.db.base.mybatis;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;

public abstract class SqlSessionWrapper implements SqlSession {
	
	protected SqlSession real;

	public SqlSessionWrapper(SqlSession real) {
		this.real = real;
	}
	
	
	@Override
	public void close() {
		real.close();
	}
	
	@Override
	public void commit() {
		real.commit();
	}

	@Override
	public void commit(boolean force) {
		real.commit(force);
	}

	@Override
	public void rollback() {
		real.rollback();
	}

	@Override
	public void rollback(boolean force) {
		real.rollback(force);
	}
	

	@Override
	public <T> T selectOne(String statement) {
		return real.selectOne(statement);
	}

	@Override
	public <T> T selectOne(String statement, Object parameter) {
		return real.selectOne(statement, parameter);
	}

	@Override
	public <E> List<E> selectList(String statement) {
		return real.selectList(statement);
	}

	@Override
	public <E> List<E> selectList(String statement, Object parameter) {
		return real.selectList(statement, parameter);
	}

	@Override
	public <E> List<E> selectList(String statement, Object parameter,
			RowBounds rowBounds) {
		return real.selectList(statement, parameter, rowBounds);
	}

	@Override
	public <K, V> Map<K, V> selectMap(String statement, String mapKey) {
		return real.selectMap(statement, mapKey);
	}

	@Override
	public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey) {
		return real.selectMap(statement, parameter, mapKey);
	}

	@Override
	public <K, V> Map<K, V> selectMap(String statement, Object parameter,
			String mapKey, RowBounds rowBounds) {
		return real.selectMap(statement, parameter, mapKey, rowBounds);
	}

	@Override
	public void select(String statement, Object parameter, ResultHandler handler) {
		real.select(statement, parameter, handler);
	}

	@Override
	public void select(String statement, ResultHandler handler) {
		real.select(statement, handler);
	}

	@Override
	public void select(String statement, Object parameter, RowBounds rowBounds,
			ResultHandler handler) {
		real.select(statement, parameter, rowBounds, handler);
	}

	@Override
	public int insert(String statement) {
		return real.insert(statement);
	}

	@Override
	public int insert(String statement, Object parameter) {
		return real.insert(statement, parameter);
	}

	@Override
	public int update(String statement) {
		return real.update(statement);
	}

	@Override
	public int update(String statement, Object parameter) {
		return real.update(statement, parameter);
	}

	@Override
	public int delete(String statement) {
		return real.delete(statement);
	}

	@Override
	public int delete(String statement, Object parameter) {
		return real.delete(statement, parameter);
	}



	@Override
	public List<BatchResult> flushStatements() {
		return real.flushStatements();
	}



	@Override
	public void clearCache() {
		real.clearCache();
	}

	@Override
	public Configuration getConfiguration() {
		return real.getConfiguration();
	}

	@Override
	public <T> T getMapper(Class<T> type) {
		return real.getMapper(type);
	}

	@Override
	public Connection getConnection() {
		return real.getConnection();
	}
	
	

}
