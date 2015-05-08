<%@tag import="java.util.Arrays"%>
<%@tag import="java.util.Collections"%>
<%@tag import="java.util.concurrent.ConcurrentHashMap"%>
<%@tag import="java.util.LinkedHashSet"%>
<%@tag import="java.io.File"%>
<%@tag import="org.apache.commons.logging.Log"%>
<%@tag import="och.util.file.FileSortAsc"%>
<%@tag import="och.util.model.Pair"%>
<%@tag import="och.util.FileUtil"%>
<%@tag import="och.util.Util"%>
<%@tag trimDirectiveWhitespaces="true" pageEncoding="UTF-8" %>
<%!
	static Log log = Util.getLog("scriptBundle_tag");

	static ConcurrentHashMap<String, Pair<String, File>> fileById = new ConcurrentHashMap<String, Pair<String, File>>();
	static volatile boolean restored = false;
	
	static String inBundleFlag = "inScriptBundle";
	static String idKey = "scriptBundleId";
	static String urlsKey = "scriptBundleUrls";
	static String parentPath = "/js-comp";
%>
<%@attribute name="id" required="true" %>
<%@attribute name="skipOptimization" type="java.lang.Boolean" %>
<%

	// SKIP if no flag
	if(request.getParameter("devMode") != null 
		|| (System.getProperty("front.filesOptimization") == null && System.getProperty("prodMode") == null)){
%>
<jsp:doBody />	
<%
		return;
	}

	// SKIP if already in other bundle
	Object val = request.getAttribute(inBundleFlag);
	if(val != null){
		log.error("alredy in bundle: "+request.getAttribute(idKey));
%>
<jsp:doBody />
<% 
		return;
	}
	
	// DO LOGIC
	
	String rootPath = request.getServletContext().getRealPath(".");
	File rootFile = new File(rootPath);
	File parentRoot = new File(rootFile.getAbsolutePath()+parentPath);

	
	//if first call - try restore cache (can be concurrent race - it's ok)
	if( ! restored){
		synchronized(fileById){
			if( ! restored) {
				log.info("try to restore all js bundles");
				parentRoot.mkdirs();
				File[] files = parentRoot.listFiles();
				if(files == null) files = new File[0];
				Arrays.sort(files, new FileSortAsc());
				for(File file : files){
					if(file.isDirectory()) continue;
					String name = file.getName();
					if( ! name.endsWith(".js")) continue;
					int sepIndex = name.lastIndexOf('-');
					if(sepIndex < 1) continue;
					String curId = name.substring(0, sepIndex);
					String linkUrl = parentPath + "/" + name;
					//put to cache
					log.info("\t restore bundle '"+curId+"' with file: "+file.getPath());
					fileById.put(curId, new Pair<String, File>(linkUrl, file));
				}
				restored = true;
			}
		}
	}
	
	String appBuild = (String)request.getAttribute("appBuild");
	if(appBuild == null) appBuild = "";
	LinkedHashSet<String> urls = new LinkedHashSet<String>();
	
	try {
		request.setAttribute(inBundleFlag, Boolean.TRUE);
		request.setAttribute(idKey, id);
		request.setAttribute(urlsKey, urls);
%>
<jsp:doBody />
<%

		//try from cache
		Pair<String, File> fromCache = fileById.get(id);
		if(fromCache != null && fromCache.second.exists()){
			out.println("<script type=\"text/javascript\" src=\""+fromCache.first+"?__r="+appBuild+"\"></script>");
			return;
		}	

		//try create and put to cache
		if(urls.size() > 0){
			log.info("create '"+id+"' js bundle");
			StringBuilder sb = new StringBuilder();
			for(String url : urls){
				File file = new File(rootFile.getAbsolutePath()+url);
				if( ! file.exists()){
					log.error("can't find file "+file.getPath());
					continue;
				}
				log.info("\t get file: "+file.getPath());
				String content = FileUtil.readFileUTF8(file);
				sb.append(content).append("\n\n");
			}
			
			//create single file
			parentRoot.mkdirs();
			String singleFileName = id +"-"+System.currentTimeMillis() +".js";
			File singleFile = new File(parentRoot, singleFileName);
			log.info("\t create single file: "+singleFile.getPath());
			FileUtil.writeFileUTF8(new File(parentRoot, singleFileName), sb.toString());
			
			//create min file
			if(skipOptimization == null || skipOptimization.equals(Boolean.FALSE)){
				File compilerJar = new File(rootFile.getAbsolutePath()+"/WEB-INF/js-compiler/compiler.jar");
				if(compilerJar.exists()){
					String minFileName = id +"-"+System.currentTimeMillis() +".min.js";
					File minFile = new File(parentRoot, minFileName);
					log.info("\t create optimized file: "+minFile.getPath());
					
					String realJarPath = compilerJar.getAbsolutePath();
					Process p = Runtime.getRuntime().exec("java -jar "+realJarPath
							+" --compilation_level SIMPLE_OPTIMIZATIONS"
							+" --warning_level QUIET"
							+" --js "+singleFile.getAbsolutePath()
							+" --js_output_file "+minFile.getAbsolutePath());
					int result = p.waitFor();
					if(result == 0){
						singleFileName = minFileName;
						singleFile = minFile;
					}
				}
			}
			
			//create link
			String linkUrl = parentPath + "/" + singleFileName;
			
			//put to cache
			fileById.put(id, new Pair<String, File>(linkUrl, singleFile));
			
			out.println("<script type=\"text/javascript\" src=\""+linkUrl+"?__r="+appBuild+"\"></script>");
		}
		
	} catch(Throwable t){
		//if errors - use old logic
		log.error("can't create bundle: "+t);
		for(String url : urls){
			out.println("<script type=\"text/javascript\" src=\""+url+"?__r="+appBuild+"\"></script>");
		}
	}
	finally {
		request.removeAttribute(inBundleFlag);
		request.removeAttribute(idKey);
		request.removeAttribute(urlsKey);
	}
	
%>