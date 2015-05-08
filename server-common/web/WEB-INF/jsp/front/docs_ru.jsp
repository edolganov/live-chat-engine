<%@page import="och.api.model.PropKey"%>
<%@ page session="false" contentType="text/html;charset=UTF-8" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="layout" %>
<%@ taglib tagdir="/WEB-INF/tags/utils" prefix="utils" %>
<%@ taglib tagdir="/WEB-INF/tags/comp" prefix="comp" %>
<%@ taglib tagdir="/WEB-INF/tags/front/comp" prefix="front" %>

<utils:setupRoles/>


<layout:base useAnalytics="true">
	<jsp:attribute name="title">Документация — Live Chat</jsp:attribute>
	<jsp:attribute name="head">
	
		<utils:css href="/css/front/front.css"/>
		
		<front:chatScript/>
		
		<script>hljs.initHighlightingOnLoad();</script>
		
	</jsp:attribute>
	<jsp:attribute name="content">
	
		<front:enterTopBlock/>
	
		<front:leftTitleLogo/>
		
		<h2 id="mainDesc">
			Документация
		</h2>
		
		
		
		<h4>Links</h4>
		<ul class="unstyled">
			<li>
				<a href="#install">Добавление чата на ваш сайт</a>
			</li>
			<li>
				<a href="#view">Стилизация чата</a>
			</li>
			<li>
				<a href="#lang">Локализация</a>
			</li>
			<li>
				<a href="#configs">Все настройки чата</a>
			</li>
			<li>
				<a href="#js_api">JavaScript АПИ</a>
			</li>
		</ul>
		
		
		
		<br>
		<br>
		<h3>
			<a id="install" href="#install" class="pseudo">Добавление чата на ваш сайт</a>
		</h3>
		<p>Откройте ваш <a href='/cabinet' target="_blank">Кабинет</a>, далее вкладка <b>Аккаунты</b> → <b>Настройки</b>.
		<p>Найдите раздел <b>Клиентский код</b> и скопируйте блок javascript-а. Он начинается как &lt;script> и заканчивается как &lt;/script>.
		Не редактируйте этот блок, просто скопируйте его и добавьте на страницу, где вы хотите показать ваш чат.
		Добавьте его сразу перед закрывающимся тегом &lt;/head>.
		<p>Перезагрузите вашу страницу в браузере. Если появился блок "Задайте вопрос!", значит чат добавлен успешно!
		
		
		<br>
		<br>
		<h3>
			<a id="view" href="#view" class="pseudo">Стилизация чата</a>
		</h3>
		<h4>Готовые темы</h4>
		<p>Добавьте один из этих javascript-блоков сразу после блока инсталяции чата (раздел выше):
		
		<p>Серая тема (<a href="javascript:" onclick="window.open('/html/theme-example.html?theme=app-gray', 'theme example', 'height=530,width=380,toolbar=0,menubar=no,status=no,resizable=no');">демо</a>)
		<pre class="codeBlock uneditable-input">
<code class="html">&lt;script>
	oChatProps.theme = "gray";
&lt;/script></code></pre>
		
		<br>
		<p>Оранжевая тема (<a href="javascript:" onclick="window.open('/html/theme-example.html?theme=app-orange', 'theme example', 'height=530,width=380,toolbar=0,menubar=no,status=no,resizable=no');">демо</a>)
		<pre class="codeBlock uneditable-input">
<code class="html">&lt;script>
	oChatProps.theme = "orange";
&lt;/script></code></pre>

		<br>
		<p>Пурпурная (<a href="javascript:" onclick="window.open('/html/theme-example.html?theme=app-purple', 'theme example', 'height=530,width=380,toolbar=0,menubar=no,status=no,resizable=no');">демо</a>)
		<pre class="codeBlock uneditable-input">
<code class="html">&lt;script>
	oChatProps.theme = "purple";
&lt;/script></code></pre>
		
		<h4>Свой CSS файл</h4>
		<p>Все наши css-стили можно посмотреть в <a href="/css/app-def.css" target="_blank">этом файле</a>. 
		Любой из них можно переопределить в вашем собственном css-файле. 
		
		<p>Чтобы чат сам добавил ваш css файл на страницу - скопируйте данный javascript-блок, прописав в нем реальный путь до вашего файла:
		<pre class="codeBlock uneditable-input">
<code class="html">&lt;script>
	oChatProps.css = "/path/your.css";
&lt;/script></code></pre>
		
		
		
		
		<br>
		<br>
		<h3>
			<a id="lang" href="#lang" class="pseudo">Локализация</a>
		</h3>
		<h4>Доступные языки</h4>
		<p>Для изменения языка вашего чата используйте данный javascript-блок, добавив его ниже блоки инсталяции (см. первый раздел).
		<pre class="codeBlock uneditable-input">
<code class="html">&lt;script>
	oChatProps.lang = "ru";
&lt;/script></code></pre>
		<p>Сейчас у нас есть Английский ("en") и Русский ("ru") языки.

		<h4>Другие языки и смена лейблов</h4>
		<p>Если вы хотите чат на других языках или же хотите поправить какой-нибудь текст, то используйте следующий javascript-блок:
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
		<p>Список всех доступных лейблов можно посмотреть <a href='/chat/labels.js' target="_blank">здесь</a> (смотреть в кодировке UTF-8).




		<br>
		<br>
		<h3>
			<a id="configs" href="#configs" class="pseudo">Все настройки чата</a>
		</h3>
		<p>Список всех настроек чата (позиция, лейблы, вид и т.д.):
		<pre class="codeBlock uneditable-input">
<code class="html">&lt;script>

//Показывать ли блок 'Задайте вопрос!'
oChatProps.showStartPanel = true;  // true|false (по-умолчанию: true)

//Расположение чата	
oChatProps.chatPosition = "bottom";  // "bottom"|"top" (по-умолчанию: "bottom")

//Чат не пропадает при скроллинге страницы
oChatProps.chatPositionFixed = true;  // true|false (по-умолчанию: true)

//Тема чата
oChatProps.theme = "blue";  // "blue"|"gray"|"orange"|"purple" (по-умолчанию: "blue")

//Подключить чатом свой css-файл
oChatProps.css = "/path/your.css";

//Задать язык чата
oChatProps.lang = "en";  // "en"|"ru" (по-умолчанию: "en")

//Переопределить нужный лейбл чата
oChatProps.labels = {
    "start.Text": "My custom text"
};
// см. раздел 'Локализация'


&lt;/script></code></pre>

		
		
		
		<br>
		<br>
		<h3>
			<a id="js_api" href="#js_api" class="pseudo">JavaScript АПИ</a>
		</h3>
		<p>Вы можете управлять чатом в своем javascript коде через объект <b>window.oChat</b>:
		<pre class="codeBlock uneditable-input">
<code class="js"
>//показать чат
oChat.show();

//скрыть чат
oChat.hide();

//событие показа чата
oChat.onShow(function(){
    alert("You see the chat!");
});

//событие скрытия чата
oChat.onHide(function(){
    alert("Now you don't...");
});
</code></pre>

	<p>Пример использования:
		<pre class="codeBlock uneditable-input">
<code class="html"
>&lt;a href="#" onclick="if(window.oChat) window.oChat.show();">Показать чат&lt;/a>
&lt;a href="#" onclick="if(window.oChat) window.oChat.hide();">Скрыть чат&lt;/a>
</code></pre>
	<a href="javascript:" onclick="if(window.oChat) window.oChat.show();">Показать чат</a>
	<br>
	<a href="javascript:" onclick="if(window.oChat) window.oChat.hide();">Скрыть чат</a>
		
		
		<div id="mainBlockMini">
		</div>
		
		
		<front:copyright/>
		
		
	</jsp:attribute>
	
</layout:base>