<%@ tag  pageEncoding="UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags/utils" prefix="utils" %>

<utils:propToJs key="ga_account"/>

<script type="text/javascript">

	/* Universal Google Analytics */
	//load script and create window.ga function
	(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
	(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
	m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
	})(window,document,'script','//www.google-analytics.com/analytics.js','ga');
	
	
	var Props = window.Props || {};
	ga('create', Props['ga_account'], 'auto');
	ga('send', 'pageview');
	
</script>
	