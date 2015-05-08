<%@page import="och.api.model.PropKey"%>
<%@ page session="false" contentType="text/html;charset=UTF-8" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout" %>
<%@ taglib tagdir="/WEB-INF/tags/utils" prefix="utils" %>
<%@ taglib tagdir="/WEB-INF/tags/comp" prefix="comp" %>
<%@ taglib tagdir="/WEB-INF/tags/front/comp" prefix="front" %>

<utils:setupRoles/>


<layout:base useAnalytics="true">
	<jsp:attribute name="title">Documentation — Live Chat</jsp:attribute>
	<jsp:attribute name="head">
	
		<utils:css href="/css/front/front.css"/>
		
		<front:chatScript/>
		
		<script>hljs.initHighlightingOnLoad();</script>
		
	</jsp:attribute>
	<jsp:attribute name="content">
	
		<front:enterTopBlock/>
	
		<front:leftTitleLogo/>
		
		<h2 id="mainDesc">
			Documentation
		</h2>
		
		
		
		<h4>Links</h4>
		<ul class="unstyled">
			<li>
				<a href="#install">Installation To Your Site</a>
			</li>
			<li>
				<a href="#view">View Customization</a>
			</li>
			<li>
				<a href="#lang">Localization</a>
			</li>
			<li>
				<a href="#configs">App Configs</a>
			</li>
			<li>
				<a href="#js_api">JavaScript API</a>
			</li>
		</ul>
		
		
		
		<br>
		<br>
		<h3>
			<a id="install" href="#install" class="pseudo">Installation To Your Site</a>
		</h3>
		<p>Open your <a href='/cabinet' target="_blank">Cabinet page</a> and go to <b>Chat Accounts</b> → <b>Account Info</b>.
		<p>Find <b>Client code</b> section and copy javascript code snippet. It starts with &lt;script> and ends with &lt;/script>.
		Don’t edit your snippet. 
		Just copy it. And paste into every web page you want to show chats. 
		Paste it immediately before the closing &lt;/head> tag.
		<p>Check your updated pages in your browser. 
		If you see "Ask a question" block (like on this page) - installation is OK!
		
		
		<br>
		<br>
		<h3>
			<a id="view" href="#view" class="pseudo">View Customization</a>
		</h3>
		<h4>Themes</h4>
		<p>Paste one of this javascript code snippet next after install code snippet:
		
		<p>Grey (<a href="javascript:" onclick="window.open('/html/theme-example.html?theme=app-gray', 'theme example', 'height=530,width=380,toolbar=0,menubar=no,status=no,resizable=no');">see theme</a>)
		<pre class="codeBlock uneditable-input">
<code class="html">&lt;script>
	oChatProps.theme = "gray";
&lt;/script></code></pre>
		
		<br>
		<p>Orange (<a href="javascript:" onclick="window.open('/html/theme-example.html?theme=app-orange', 'theme example', 'height=530,width=380,toolbar=0,menubar=no,status=no,resizable=no');">see theme</a>)
		<pre class="codeBlock uneditable-input">
<code class="html">&lt;script>
	oChatProps.theme = "orange";
&lt;/script></code></pre>

		<br>
		<p>Purple (<a href="javascript:" onclick="window.open('/html/theme-example.html?theme=app-purple', 'theme example', 'height=530,width=380,toolbar=0,menubar=no,status=no,resizable=no');">see theme</a>)
		<pre class="codeBlock uneditable-input">
<code class="html">&lt;script>
	oChatProps.theme = "purple";
&lt;/script></code></pre>
		
		<h4>Custom CSS</h4>
		<p>All styles of <a href="/css/app-def.css" target="_blank">our css file</a> can be rewritten by your own css.
		<p>Chat App can add your css file after our: paste and edit this javascript code snippet next after install code snippet:
		<pre class="codeBlock uneditable-input">
<code class="html">&lt;script>
	oChatProps.css = "/path/your.css";
&lt;/script></code></pre>
		
		
		
		
		<br>
		<br>
		<h3>
			<a id="lang" href="#lang" class="pseudo">Localization</a>
		</h3>
		<h4>Default languages</h4>
		<p>To change language of your Chat Application use this code snippet with special lang code.
		<p>Paste this snippet next after install code snippet:
		<pre class="codeBlock uneditable-input">
<code class="html">&lt;script>
	oChatProps.lang = "ru";
&lt;/script></code></pre>
		<p>Now we have localizations for English ("en") and Russian ("ru") languages. 

		<h4>Other languages and text changes</h4>
		<p>If you want labels in other language or want to replace some text you can use this code snippet:
		<pre class="codeBlock uneditable-input">
<code class="html">&lt;script>
	oChatProps.labels = {
        "start.Text": "Ask a question",
        "app.Close": "Close",
        "app.ConnectingToServer": "Connecting to server...",
        "app.ConnectError": "Connect error",
        "app.TryConnectAgain": "Try again",
        "app.Send": "Send",
        "app.SendLoading": "Sending...",
        "app.SendError": "Send error",
        "app.SendError.NoOperators": "No operators.",
        "app.SendFeedback": "Send Feedback"
	};
&lt;/script></code></pre>
		<p>See all labels in <a href='/chat/labels.js' target="_blank">this file</a>.




		<br>
		<br>
		<h3>
			<a id="configs" href="#configs" class="pseudo">App Configs</a>
		</h3>
		<p>List of all App Configs (position, view, labels etc). Paste this snippet next after install code snippet:
		<pre class="codeBlock uneditable-input">
<code class="html">&lt;script>

//Show 'Ask a question' block
oChatProps.showStartPanel = true;  // true|false (default: true)

//App position 		
oChatProps.chatPosition = "bottom";  // "bottom"|"top" (default: "bottom")

//App will not move even if the window is scrolled
oChatProps.chatPositionFixed = true;  // true|false (default: true)

//App theme
oChatProps.theme = "blue";  // "blue"|"gray"|"orange"|"purple" (default: "blue")

//Custom css to overwrite the themes
oChatProps.css = "/path/your.css";

//App lang
oChatProps.lang = "en";  // "en"|"ru" (default: "en")

//Custom labels
oChatProps.labels = {
    "start.Text": "My custom text"
};
// see 'Localization' section


&lt;/script></code></pre>

		
		
		
		<br>
		<br>
		<h3>
			<a id="js_api" href="#js_api" class="pseudo">JavaScript API</a>
		</h3>
		<p>You can access to loaded Chat App by <b>window.oChat</b> object:
		<pre class="codeBlock uneditable-input">
<code class="js"
>//show Chat window
oChat.show();

//hide Chat window
oChat.hide();

//show event
oChat.onShow(function(){
    alert("You see the chat!");
});

//hide event
oChat.onHide(function(){
    alert("Now you don't...");
});
</code></pre>

	<p>Example of usage:
		<pre class="codeBlock uneditable-input">
<code class="html"
>&lt;a href="#" onclick="if(window.oChat) window.oChat.show();">Show chat&lt;/a>
&lt;a href="#" onclick="if(window.oChat) window.oChat.hide();">Hide chat&lt;/a>
</code></pre>
	<a href="javascript:" onclick="if(window.oChat) window.oChat.show();">Show chat</a>
	<br>
	<a href="javascript:" onclick="if(window.oChat) window.oChat.hide();">Hide chat</a>
		
		
		<div id="mainBlockMini">
		</div>
		
		
		<front:copyright/>
		
		
	</jsp:attribute>
	
</layout:base>