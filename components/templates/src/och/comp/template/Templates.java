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
package och.comp.template;

import static java.util.Collections.*;
import static och.api.model.PropKey.*;
import static och.service.i18n.I18n.*;
import static och.util.FileUtil.*;
import static och.util.Util.*;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import och.service.i18n.I18n;
import och.service.props.Props;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;

public class Templates {
	
	public final String dirPath;
	
	private Configuration cfg;
	
	public Templates(Props props) throws IOException{
		this(props.findVal(templates_path));
	}
	
	public Templates(String dirPath) throws IOException {
		this.dirPath = dirPath;
		
		cfg = new Configuration();
		cfg.setLocalizedLookup(false);
        cfg.setDirectoryForTemplateLoading(new File(this.dirPath));
        cfg.setObjectWrapper(new DefaultObjectWrapper());
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        cfg.setIncompatibleImprovements(new Version(2, 3, 20));
	}
	
	public String fromTemplate(String templateFileName) throws Exception{
		return fromTemplate(templateFileName, emptyMap());
	}
	
	public String fromTemplate(String templateFileName, Map<?, ?> map) throws Exception{

		
		Template temp = getTemplateByName(templateFileName);
		StringWriter out = new StringWriter();
		temp.process(map, out);
		return out.toString();
	}
	
	
	
	private Template getTemplateByName(String templateFileName) throws Exception{
		
		if(isThreadLang_EN()){
			return cfg.getTemplate(templateFileName);
		}
		
		String lang = getThreadLang();
		if(isEmpty(lang)){
			return cfg.getTemplate(templateFileName);
		}
		
		String baseName = getFileNameWithoutType(templateFileName);
		String type = getFileType(templateFileName);
		String langFile = baseName + I18n.SEP + lang+"."+type;
		try {
			return cfg.getTemplate(langFile);
		}catch(IOException e){
			return cfg.getTemplate(templateFileName);
		}
	}

}
