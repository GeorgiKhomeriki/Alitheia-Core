<jsp:useBean id="MessageView" class="eu.sqooss.webui.MessageView" scope="session"/>
<jsp:setProperty name="MessageView" property="*"/>
<blockquote>
<%
//MessageView.setMessage("Blabla");
out.println(MessageView.getMessage());

%>
</blockquote>
