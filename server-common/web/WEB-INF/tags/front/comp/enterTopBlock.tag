<%@tag pageEncoding="UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags/front/comp" prefix="comp" %>

<div id="enterTopBlock">
	<a href="${i18n.label('link.docs')}">${i18n.label('link.docsTitle')}</a>
	<sep></sep>
	<a href="${i18n.label('link.enter')}">${i18n.label('link.enterTitle')}</a>
	<sep></sep>
	<a href="${i18n.label('link.enterNew')}">${i18n.label('link.enterNewTitle')}</a>
	<sep></sep>
	| <comp:selectLang />
</div>
