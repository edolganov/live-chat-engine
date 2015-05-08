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

import static java.util.Collections.*;
import static och.api.model.PropKey.*;
import static och.comp.db.base.exception.ConnectionProblemException.*;
import static och.comp.db.base.universal.field.RowField.*;
import static och.util.Util.*;
import static och.util.sql.SingleTx.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import och.comp.db.base.exception.ConnectionProblemException;
import och.comp.db.base.exception.UniversalSqlException;
import och.comp.db.base.universal.field.RowField;
import och.comp.db.base.universal.mapper.ReflectionMapper;
import och.comp.db.base.universal.query.SortCondition;
import och.comp.db.base.universal.query.WhereCondition;
import och.service.props.Props;
import och.util.model.Pair;

import org.apache.commons.logging.Log;

public class UniversalQueries {
	
	private static final ThreadLocal<String> lastQuery = new ThreadLocal<String>();
	
	Log log = getLog(getClass());
	
	String url;
	DataSource ds;
	ReflectionMapper mapper = new ReflectionMapper();
	Props props;
	

	public UniversalQueries(DataSource ds) {
		this(ds, null, null);
	}
	
	public UniversalQueries(DataSource ds, Props props) {
		this(ds, props, null);
	}
	
	public UniversalQueries(DataSource ds, Props props, String url) {
		this.ds = ds;
		this.url = url;
		this.props = props;
	}
	

	public List<Integer> update(BaseUpdateOp... updates) throws SQLException {
		try {
			return tryUpdate(updates);
		}catch (Exception e) {
			ConnectionProblemException connEx = tryFindConnProblem(url, e);
			if(connEx != null) throw connEx;
			else throw createOutException("can't update", lastQuery.get(), e);
		} finally {
			lastQuery.remove();
		}
	}
	
	public Integer updateOne(BaseUpdateOp update) throws SQLException {
		return update(update).get(0);
	}
	
	
	@SuppressWarnings("unchecked")
	public <T> List<T> select(SelectRows<T> select) throws SQLException {
		try {
			return (List<T>)trySelect(select);
		}catch (Exception e) {
			ConnectionProblemException connEx = tryFindConnProblem(url, e);
			if(connEx != null) throw connEx;
			else throw createOutException("can't select", lastQuery.get(), e);
		} finally {
			lastQuery.remove();
		}
	}
	
	private static UniversalSqlException createOutException(String preffix, String query, Exception e){
		if(query == null) query = "";
		query = query.replace('\n', ' ');
		return new UniversalSqlException(preffix+": query='"+query+"', errorMsg="+e.getMessage(), e);
	}
	
	public <T> T selectOne(SelectRows<T> select) throws SQLException {
		List<T> list = select(select);
		return isEmpty(list)? null : list.get(0);
	}
	
	

	private List<Integer> tryUpdate(BaseUpdateOp... updates) throws SQLException {
		
		List<Integer> out = new ArrayList<>();
		if(isEmpty(updates)) return out;
		
		Connection conn = getSingleOrNewConnection(ds);
		conn.setAutoCommit(false);
		
		try {

			for (BaseUpdateOp update : updates) {
				Integer opResult;
				if(update instanceof UpdateRows){
					opResult = updateRows((UpdateRows)update, conn);
				}
				else if(update instanceof CreateRow){
					opResult = createRow((CreateRow)update, conn);
				}
				else if(update instanceof DeleteRows){
					opResult = deleteRows((DeleteRows)update, conn);
				} 
				else {
					opResult = 0;
				}
				out.add(opResult);
			}
			
			conn.commit();
			return out;
			
		}catch (SQLException e) {
			saveRealRollbackException(e);
			conn.rollback();
			throw e;
			
		}finally{
			try {
				conn.setAutoCommit(true);
				conn.close();
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private int updateRows(UpdateRows update, Connection conn) throws SQLException {
		
		lastQuery.remove();
		
		int size = update.fields == null? 0 : update.fields.length;
		if(size == 0) return 0;
		
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ").append(update.table);
		sb.append("\n SET");
		boolean isFirst = true;
		for (RowField<?> q : update.fields) {
			if( ! isFirst)sb.append(',');
			isFirst = false;
			sb.append(' ').append(q.fieldName()).append("=?");
		}
		WhereCondition condition = update.whereCondition;
		appendWhereCondition(sb, condition);
		
		String q = sb.toString();
		lastQuery.set(q);
		
		PreparedStatement ps = conn.prepareStatement(q);
		for (int i = 0; i < size; i++) {
			ps.setObject(i+1, update.fields[i].value);
		}
		if( ! isEmpty(condition) && ! isEmpty(condition.values())){
			RowField<?>[] values = condition.values();
			for (int i = 0; i < values.length; i++) {
				ps.setObject(size+i+1, values[i].value);
			}
		}
		
		
		long start = System.currentTimeMillis();
		logQueryStart(q);
		
		int out = ps.executeUpdate();
		ps.close();
		
		logQueryEnd(start);
		
		return out;
		
	}
	
	
	private int deleteRows(DeleteRows update, Connection conn) throws SQLException {
		
		lastQuery.remove();
		
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ").append(update.table);
		WhereCondition condition = update.whereCondition;
		appendWhereCondition(sb, condition);
		
		String q = sb.toString();
		lastQuery.set(q);
		
		PreparedStatement ps = conn.prepareStatement(q);
		if( ! isEmpty(condition)){
			RowField<?>[] values = condition.values();
			for (int i = 0; i < values.length; i++) {
				ps.setObject(i+1, values[i].value);
			}
		}
		
		long start = System.currentTimeMillis();
		logQueryStart(q);
		
		int out = ps.executeUpdate();
		ps.close();
		
		logQueryEnd(start);
		
		return out;
		
		
	}
	
	private int createRow(CreateRow update, Connection conn) throws SQLException {
		
		lastQuery.remove();
		
		int size = update.fields == null? 0 : update.fields.length;
		if(size == 0) return 0;
		
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ").append(update.table).append(" (");
		{
			boolean isFirst = true;
			for (RowField<?> q : update.fields) {
				if( ! isFirst)sb.append(',');
				isFirst = false;
				sb.append(' ').append(q.fieldName());
			}
		}
		sb.append(")\n VALUES (");
		{
			boolean isFirst = true;
			for (int i = 0; i < size; i++) {
				if( ! isFirst)sb.append(',');
				isFirst = false;
				sb.append('?');
			}
		}
		sb.append(")");
		
		String q = sb.toString();
		lastQuery.set(q);
		
		PreparedStatement ps = conn.prepareStatement(q);
		for (int i = 0; i < size; i++) {
			ps.setObject(i+1, update.fields[i].value);
		}
		
		long start = System.currentTimeMillis();
		logQueryStart(q);
		
		int out = ps.executeUpdate();
		ps.close();
		
		logQueryEnd(start);
		
		return out;
		
		
	}
	
	
	private List<Object> trySelect(SelectRows<?> select) throws Exception {
		
		lastQuery.remove();
		
		Class<?>[] fieldTypes = select.selectFields.fieldTypes;
		WhereCondition condition = select.whereCondition;

		if(isEmpty(fieldTypes)) return emptyList();
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT");
		{
			boolean isFirst = true;
			for (Class<?> type : fieldTypes) {
				if( ! isFirst) sb.append(',');
				else isFirst = false;
				sb.append(' ').append(fieldName(type));
			}
		}
		sb.append("\n FROM ").append(select.table);
		appendWhereCondition(sb, condition);
		if(select.sortCondition != null) appendSortCondition(sb, select.sortCondition);
		if(select.limit != null) sb.append("\n LIMIT ").append(select.limit);
		if(select.offset != null) sb.append("\n OFFSET ").append(select.offset);
		sb.append(';');
		
		try(Connection conn = getSingleOrNewConnection(ds)){
			
			String q = sb.toString();
			lastQuery.set(q);
			
			PreparedStatement ps = conn.prepareStatement(q);
			if( ! isEmpty(condition)){
				RowField<?>[] values = condition.values();
				for (int i = 0; i < values.length; i++) {
					ps.setObject(i+1, values[i].value);
				}
			}
			
			long startTime = System.currentTimeMillis();
			logQueryStart(q);
			
			ResultSet rs = ps.executeQuery();
			
			logQueryEnd(startTime);
			
			ArrayList<Object> out = new ArrayList<>();
			while (rs.next()) {
				Object entity = mapper.createEntity(rs, select.resultType, fieldTypes);
				out.add(entity);
			}
			
			ps.close();
			
			return out;
		}
		
		
	}




	public long nextSeqFor(Object tableName) throws SQLException {
		return nextSeq(tableName+"_seq");
	}
	
	public long nextSeq(String seqName) throws SQLException {
		try(Connection conn = getSingleOrNewConnection(ds)){
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT nextval('"+seqName+"')");
			if( ! rs.next()) throw new IllegalStateException("ResultSet has no Sequence data for "+seqName);
			long out = rs.getLong(1);
			st.close();
			return out;
		}
	}
	
	private void appendSortCondition(StringBuilder sb, SortCondition sortCondition) {
		Pair<Class<? extends RowField<?>>, Boolean>[] columns = sortCondition.values();
		if(isEmpty(columns)) return;
		sb.append("\n ORDER BY");
		boolean first = true;
		for (Pair<Class<? extends RowField<?>>, Boolean> pair : columns) {
			if(first) first = false;
			else sb.append(",");
			
			sb.append(' ').append(RowField.fieldName(pair.first)).append(" ");
			if(pair.second) sb.append("ASC");
			else sb.append("DESC");
		}
		
	}
	
	
	

	private void logQueryStart(String q) {
		if(printLogs()) log.info("[SQL] Execute query: \n"+q);
	}
	
	private void logQueryEnd(long startTime) {
		if(printLogs()) log.info("[SQL] Work time: "+(System.currentTimeMillis() - startTime)+"ms");
	}
	

	private boolean printLogs() {
		return props != null && props.getBoolVal(db_debug_LogSql);
	}
	

	private static void appendWhereCondition(StringBuilder sb, WhereCondition condition) {
		if( ! isEmpty(condition)){
			sb.append("\n WHERE ");
			condition.setWhereQuery(sb);
		}
	}
	




}
