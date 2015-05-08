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
package och.comp.cache;

import static och.api.model.PropKey.*;
import static och.util.StringUtil.*;
import static och.util.Util.*;

import java.util.List;

import och.comp.cache.server.CacheSeverOps;
import och.comp.cache.server.CacheSeverOps.ServersPropsResp;
import och.junit.AssertExt;
import och.service.props.impl.MapProps;
import och.service.props.impl.MultiProps;

import org.junit.Test;

public class CacheSeverOpsTest extends AssertExt {
	
	@Test
	public void test_initProps(){
		
		MapProps startProps = new MapProps();
		
		String dirPath = "./components/cache-remote/extra/";
		
		List<String> cacheFiles = convert(strToList(netProps_cache_files.strDefVal(), " "), (path) -> dirPath + path);
		List<String> frontFiles = convert(strToList(netProps_front_files.strDefVal(), " "), (path) -> dirPath + path);
		List<String> chatsFiles = convert(strToList(netProps_chats_files.strDefVal(), " "), (path) -> dirPath + path);
		
		startProps.putVal(netProps_cache_files, collectionToStr(cacheFiles, ' '));
		startProps.putVal(netProps_front_files, collectionToStr(frontFiles, ' '));
		startProps.putVal(netProps_chats_files, collectionToStr(chatsFiles, ' '));
		
		
		ServersPropsResp resp = CacheSeverOps.getServersProps(startProps);
		{
			MultiProps frontProps = resp.frontProps;
			assertFalse(frontProps.getVal(db_skipDbCreation, true));
		}
	}

}
