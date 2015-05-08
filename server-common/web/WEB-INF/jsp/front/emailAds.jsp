<%@page import="och.api.model.PropKey"%>
<%@ page session="false" contentType="text/html;charset=UTF-8" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout" %>
<%@ taglib tagdir="/WEB-INF/tags/utils" prefix="utils" %>
<%@ taglib tagdir="/WEB-INF/tags/comp" prefix="comp" %>
<%@ taglib tagdir="/WEB-INF/tags/front/comp" prefix="front" %>

<utils:setupRoles/>


<layout:base useAnalytics="true">
	<jsp:attribute name="title">Форма рассылки коммерческого предложения</jsp:attribute>
	<jsp:attribute name="head">
	
		<utils:css href="/css/front/front.css"/>
		<utils:css href="/css/front/emailAds.css"/>
		
		<utils:script src="/js/front/cabinet/cabLabels.js"/>
		<utils:script src="/js/front/emailAds.js"/>
		
	</jsp:attribute>
	<jsp:attribute name="content">
	
		<h3>Форма рассылки коммерческого предложения</h3>
		
		<br>
		<br>
		
		Сайт клиента:<br>
		<input id="url" type="text" placeholder="Введите урл сайта"/>
		
		<h4 id="info"></h4>
		
		<div id="demoForm" class="genView" style="display: none;">
			Превью демо (если сайт не отображается, то можно не ставить ссылку на демо):
			<br><br>
			<iframe id="demoPreview" src="" width="100%" height="100%" frameBorder="0" sandbox="allow-scripts">Browser not compatible.</iframe>
		</div>
		
		<div id="genData" class="genView" style="display: none;">
			
					
			Язык письма:
			<label class="radio">
				<input name="lang" class="langSel" type="radio"  value="en" checked>
				ENG
			</label>
			<label class="radio">
				<input name="lang" class="langSel" type="radio" value="ru">
				РУС
			</label>
			
			<br>
			<label class="checkbox">
				<input id="showDemo" type="checkbox" name="linkToDemo" checked> Ссылка на Демо
			</label>
			<button id="openDemo" class="btn btn-info" type="button">Посмотреть Демо в отдельном окне</button>
			
			<br><br><br>
			Email получателя:<br>
			<input id="to" type="text" placeholder="Введите email получателя"/>
			
			
			<br><br><br>
			<br><br><br>
			<br><br><br>
			<br>
			
			<h3>Итоговый текст</h3>
			<br>Кому: <b><span id="toLabel"></span></b>
			<br>Заголовок: <b><span id="subject"></span></b>
			<br>
			<br>
			<div id="mailText">
			</div>
			
			<br>
			<button id="sendReq" class="btn btn-info" type="button">Отправить письмо клиенту</button>
		
		</div>
		
		<div style="display: none;">
			<div id="textTmpls_en">
				<div bind="subject">${i18n.labelFor('en', 'emailAds.subject')}</div>
				<div bind="begin">${i18n.labelFor('en', 'emailAds.begin')}</div>
				<div bind="demo">${i18n.labelFor('en', 'emailAds.demo')}</div>
				<div bind="end">${i18n.labelFor('en', 'emailAds.end')}</div>
				<div bind="endSig">${i18n.labelFor('en', 'emailAds.endSig')}</div>
			</div>
			<div id="textTmpls_ru">
				<div bind="subject">${i18n.labelFor('ru', 'emailAds.subject')}</div>
				<div bind="begin">${i18n.labelFor('ru', 'emailAds.begin')}</div>
				<div bind="demo">${i18n.labelFor('ru', 'emailAds.demo')}</div>
				<div bind="end">${i18n.labelFor('ru', 'emailAds.end')}</div>
				<div bind="endSig">${i18n.labelFor('ru', 'emailAds.endSig')}</div>
			</div>
		</div>
		
		<hr>
		
		<br><br><br><br>
		
		<h3>Список уже отправленных предложений</h3>
		<button id="updateSendedList" class="btn" type="button">Обновить</button>
		<br>
		<br>
		<pre id="sendedList"></pre>
					
		<br><br><br><br>
		<br><br><br><br>
		<br><br><br><br>
		<br><br><br><br>
		<br><br><br><br>
		
		
	</jsp:attribute>
	
</layout:base>