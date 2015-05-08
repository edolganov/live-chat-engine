<%@tag import="java.util.Set"%>
<%@tag import="java.util.HashSet"%>
<%@tag import="och.util.Util"%>
<%@tag import="och.api.model.user.UserRole"%>
<%@tag import="och.api.model.user.User"%>
<%@tag trimDirectiveWhitespaces="true" pageEncoding="UTF-8" %>
<%
	User user = (User)request.getAttribute("user");
	if(user != null){
		Set<UserRole> roles = user.getRoles();
		//if ADMIN - put all roles to set
		if(roles != null && roles.contains(UserRole.ADMIN)){
			roles = new HashSet(Util.list(UserRole.values()));
		}
		//set roles vars
		if( ! Util.isEmpty(roles)){
			for(UserRole role : roles){
				request.setAttribute("Role_"+role.toString(), Boolean.TRUE);
			}
		}
	}
%>