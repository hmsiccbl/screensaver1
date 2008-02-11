<%-- includes are commented out to prevent MyEclipse from complaining about
     missing header files
--%>
<%-- <%@include file="header.jspf"%> --%>

<h1>
  Synchronized Fields Test
</h1>

<f:view>
  <h:form id="myform">

      <h:commandButton id="update" value="Update" action="#{SynchronizedFieldsTest.update}"/>
      <h:inputText id="first" value="#{SynchronizedFieldsTest.firstValue}" binding="#{SynchronizedFieldsTest.firstInput}" valueChangeListener="#{SynchronizedFieldsTest.firstListener}" immediate="false" />
      <h:inputText id="second" value="#{SynchronizedFieldsTest.secondValue}" binding="#{SynchronizedFieldsTest.secondInput}" valueChangeListener="#{SynchronizedFieldsTest.secondListener}" immediate="false" />

      <h:outputText id="firstOut">${SynchronizedFieldsTest.firstValue}</h:outputText>
      <h:outputText id="secondOut">${SynchronizedFieldsTest.secondValue}</h:outputText>
  </h:form>

</f:view>

<%-- <%@include file="footer.jspf"%> --%>
