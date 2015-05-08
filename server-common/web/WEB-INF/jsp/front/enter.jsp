<%@ page session="false" contentType="text/html;charset=UTF-8" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout" %>
<%@ taglib tagdir="/WEB-INF/tags/utils" prefix="utils" %>
<%@ taglib tagdir="/WEB-INF/tags/front/comp" prefix="front" %>

<utils:setupRoles/>

<layout:base>
	<jsp:attribute name="title">${i18n.label('enter.title')}</jsp:attribute>
	<jsp:attribute name="head">
	
		<utils:css href="/css/front/front.css"/>
		<utils:css href="/css/front/enter.css"/>
		
		<utils:propToJs key="captcha_enabled"/>
		<utils:propToJs key="captcha_publicKey"/>
		<utils:propToJs key="users_autoActivation"/>
		
	
		<utils:scriptBundle id="login">
			<utils:script src="/js/comp/errLabels.js"/>
			<utils:script src="/js/front/frontApp.js"/>
			<utils:script src="/js/front/enter/enterLabels.js"/>
			<utils:script src="/js/front/enter/enterPage.js"/>
			<utils:script src="/js/front/enter/user-sign-in.js"/>
			<utils:script src="/js/front/enter/user-add.js"/>
			<utils:script src="/js/front/enter/restore-psw.js"/>
		</utils:scriptBundle>
		
	</jsp:attribute>
	<jsp:attribute name="content">
	
		<front:leftTitleLogo/>
	
		<div class="loginWrapper">
			
			<div id="userActions">
	
				<ul class="nav nav-pills">
				  <li bind="signIn"><a href="javascript:">${i18n.label('enter.signIn')}</a></li>
				  <li bind="restore"><a href="javascript:">${i18n.label('enter.restorePsw')}</a></li>
				  <li bind="newUser"><a href="javascript:">${i18n.label('enter.newUser')}</a></li>
				</ul>
	
				<div bind="content"></div>
				
			</div>
			
		</div>
		
		<front:copyright/>
		
	</jsp:attribute>
	<jsp:attribute name="footer">
	
		<front:baseFooterData/>
		
		<div id="enterTemplates" style="display: none;">
		

			<div id="signInForm">
				<strong i18n="signIn.welcome">It's nice to see you again!</strong>
				<br>
				<br>
				<div class="form-group">
					
					<form id="fakeSubmitFormForLoginSave" action="/cabinet" method="post">
						<input 
							bind="login" 
							name="login"
							formData 
							type="text" 
							class="block" 
							autocomplete="off" 
							placeholder-i18n="signIn.login"
							placeholder="Enter email or login" 
							title="email">
						
						<input 
							bind="psw" 
							name="psw"
							formData 
							type="password" 
							class="block" 
							autocomplete="off" 
							placeholder-i18n="signIn.psw"
							placeholder="Enter Password" 
							title="password">
						
						<label>
							<input 
								bind="rememberMe"
								name="remMe" 
								formData 
								type="checkbox"> <span i18n="signIn.remMe">Remember me</span>
						</label>
						
					</form>
					
				</div>
				<div class="captcha"></div>
				<button bind="signIn" class="btn btn-default" i18n="signIn.btn">Sign in</button>
				<div bind="infoMsg" class="infoMsg"></div>
			</div>
			
			<div id="newUserForm">
				<strong i18n="newUser.welcome">To begin using Live Chat fill this welcome form</strong>
				<br>
				<br>
				<div class="form-group">
					<input bind="email" 
						formData type="text" 
						class="block" 
						autocomplete="off" 
						placeholder-i18n="newUser.email"
						placeholder="Enter email" 
						title="email">
					
					<input bind="login" 
						formData type="text" 
						class="block" 
						autocomplete="off" 
						placeholder-i18n="newUser.login"
						placeholder="Enter login" 
						title="login">
					
					<input bind="psw" 
						formData 
						type="password" 
						class="block" 
						autocomplete="off" 
						placeholder-i18n="newUser.psw"
						placeholder="Enter Password" 
						title="password">
				</div>
				<div class="captcha"></div>
				<button bind="create" class="btn btn-default" i18n="newUser.btn">Sign up</button>
				<div bind="infoMsg" class="infoMsg"></div>
				
				<div bind="termOfUseBlock" class="termOfUseBlock" style="display: none;">
					By clicking "Sign up" button you agree with <span class="pseudo" collapseTitle>Terms of Use</span>.
					<div collapseContent class="termOfUse" style="display: none;">
					</div>
				</div>
			</div>
			
			
			<div id="restorePswForm">
				<strong i18n="rem.welcome">Enter your email and we will send you a new password</strong>
				<br>
				<br>
				<div class="form-group">
				  <input id="userLoginRestore" 
				  	bind="email" 
				  	formData 
				  	type="text" 
				  	class="block" 
				  	autocomplete="off" 
				  	placeholder-i18n="rem.email" 
				  	placeholder="Enter email" 
				  	title="email">
				</div>
				<button bind="restore" class="btn btn-default" i18n="rem.btn">Send a new password</button>
				<div bind="infoMsg" class="infoMsg"></div>
			</div>
			
		</div>

		
	</jsp:attribute>
	
</layout:base>