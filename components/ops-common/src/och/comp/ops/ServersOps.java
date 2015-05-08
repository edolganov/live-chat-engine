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
package och.comp.ops;

import static och.util.Util.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import och.api.model.server.ServerRow;
import och.comp.db.base.universal.UniversalQueries;
import och.comp.db.main.table.server.GetAllServers;
import och.util.model.Pair;

public class ServersOps {
	
	
	public static Map<Long, ServerRow> getServersMap(UniversalQueries universal) throws SQLException{
		List<ServerRow> serversList = universal.select(new GetAllServers());
		Map<Long, ServerRow> servers = toMap(serversList, (elem)-> new Pair<Long, ServerRow>(elem.id, elem));
		return servers;
	}
	

}
