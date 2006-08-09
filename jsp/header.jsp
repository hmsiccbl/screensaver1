<%@ include file="headers.inc" %>

<t:outputText styleClass="title" value="#{pageTitle}" rendered="#{!empty pageTitle}"/>

<h:messages id="allMessages" globalOnly="true" showDetail="true" styleClass="errorMessage" />
