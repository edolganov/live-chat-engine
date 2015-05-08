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
package och.front.service;

import static och.util.Util.*;
import och.comp.db.base.universal.UniversalQueries;
import och.comp.db.main.MainDb;
import och.service.props.Props;

import org.apache.commons.logging.Log;

public abstract class BaseFrontService {
	
	protected Log log = getLog(getClass());
	
	protected FrontAppContext c;
	protected Props props;
	protected MainDb db;
	protected UniversalQueries universal;

	public BaseFrontService(FrontAppContext c) {
		this.c = c;
		this.props = c.props;
		db = c.db;
		universal = c.db.universal;
	}
	
	public void init() throws Exception {
		
	}

}
