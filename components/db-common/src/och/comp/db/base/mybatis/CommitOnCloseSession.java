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

import org.apache.ibatis.session.SqlSession;

public class CommitOnCloseSession extends SqlSessionWrapper {
	
	private boolean canCommitOnClose = true;

	public CommitOnCloseSession(SqlSession real) {
		super(real);
	}
	
	@Override
	public void close() {
		if(canCommitOnClose){
			canCommitOnClose = false;
			real.commit();
		}
		real.close();
	}
	
	@Override
	public void commit() {
		canCommitOnClose = false;
		real.commit();
	}

	@Override
	public void commit(boolean force) {
		canCommitOnClose = false;
		real.commit(force);
	}

	@Override
	public void rollback() {
		canCommitOnClose = false;
		real.rollback();
	}

	@Override
	public void rollback(boolean force) {
		canCommitOnClose = false;
		real.rollback(force);
	}
	
	

}
