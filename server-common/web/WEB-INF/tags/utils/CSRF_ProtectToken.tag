<%@ tag trimDirectiveWhitespaces="true" pageEncoding="UTF-8" %>
<% 
	try {
		
		HttpSession httpSession = request.getSession(false);
		String token = httpSession != null? (String)httpSession.getAttribute(och.util.servlet.WebUtil.CSRF_PROTECT_TOKEN) : null;
		if(token != null){
			out.println("<span id='CSRF_ProtectToken' p-val='"+token+"' style='display: none;'></span>");
		}
		
	}catch(Throwable t){
		t.printStackTrace();
	}
%>