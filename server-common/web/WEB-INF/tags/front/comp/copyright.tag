<%@tag pageEncoding="UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags/front/comp" prefix="comp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<hr>

<div id="copyright-wrapper">
	<div class="float-left">
		<div id="copyright">
			<a href="/">Live Chat Engine</a>
			<p>
			Powered by 
			<a href='https://github.com/edolganov/live-chat-engine' target="_blank">GitHub Project</a>,
			<a target="_blank" href="http://getbootstrap.com/">Bootstrap</a>,
			<a target="_blank" href="http://www.free-patterns.info/">Free-patterns.info</a>,
			<a target="_blank" href="https://highlightjs.org/">highlight.js</a>
		</div>
	</div>
	<div class="float-right right-align">
		<comp:selectLang />
	</div>
	<div class="clearfix"></div>	
</div>


