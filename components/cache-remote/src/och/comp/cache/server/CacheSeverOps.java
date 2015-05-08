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
package och.comp.cache.server;

import static och.api.model.PropKey.*;
import static och.service.props.impl.FileProps.*;
import static och.util.StringUtil.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import och.service.props.Props;
import och.service.props.impl.FileProps;
import och.service.props.impl.MultiProps;

public class CacheSeverOps {
	
	
	public static class ServersPropsResp {
		
		public MultiProps cacheProps;
		public MultiProps frontProps;
		public MultiProps chatsProps;
		
		public ServersPropsResp(MultiProps cacheProps, MultiProps frontProps, MultiProps chatsProps) {
			this.cacheProps = cacheProps;
			this.frontProps = frontProps;
			this.chatsProps = chatsProps;
		}
	}
	
	public static ServersPropsResp getServersProps(Props startProps){
		
		ArrayList<Props> allCacheProps = new ArrayList<Props>();
		allCacheProps.add(startProps);
		
		ArrayList<FileProps> allFrontProps = new ArrayList<FileProps>();
		ArrayList<FileProps> allChatsProps = new ArrayList<FileProps>();
		{
			
			Set<String> cacheFiles = new LinkedHashSet<>(strToList(startProps.getStrVal(netProps_cache_files), " "));
			Set<String> frontFiles = new LinkedHashSet<>(strToList(startProps.getStrVal(netProps_front_files), " "));
			Set<String> chatsFiles = new LinkedHashSet<>(strToList(startProps.getStrVal(netProps_chats_files), " "));
			
			Set<String> allFiles = new LinkedHashSet<>();
			allFiles.addAll(cacheFiles);
			allFiles.addAll(frontFiles);
			allFiles.addAll(chatsFiles);
			
			List<FileProps> allProps = createFileProps(allFiles);
			for (FileProps props : allProps) {
				String path = props.getFile().getPath();
				path = path.replace("\\", "/");
				if(cacheFiles.contains(path)) allCacheProps.add(props);
				if(frontFiles.contains(path)) allFrontProps.add(props);
				if(chatsFiles.contains(path)) allChatsProps.add(props);
			}
		}
		
		MultiProps cacheProps = new MultiProps(allCacheProps);
		MultiProps frontProps = new MultiProps(allFrontProps);
		MultiProps chatsProps = new MultiProps(allChatsProps);
		
		return new ServersPropsResp(cacheProps, frontProps, chatsProps);
	}

}
