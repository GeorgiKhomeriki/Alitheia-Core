<%!
public static String loginForm() {
    String form = "<form id=\"loginform\">" +
    "<input type=\"text\" name=\"user_id\" /><br />" +
    "<input type=\"password\" name=\"password\" /><br />" +
    "<input type=\"submit\" value=\"Log in\" />" +
    "</form>";
    //String form = "bl\'\"a";
    return form;
}
%>

<%@ include file="/inc/init.jsp" %>
<%
    title = "Users";
%>
<%@ page import="java.util.*" %>
<%@ include file="/inc/header.jsp" %>

<jsp :useBean id="user" class="eu.sqooss.webui.User" scope="session"/> 
<jsp :setProperty name="user" property="*"/>

<h1>Users</h1>

    <%

    String uid = request.getParameter("user_id");
    if ( (uid!=null) && uid.length() > 0 ) {
        out.println("You are:");
        out.println("<ul><li> Foobar [" + uid + "]</li></ul>");
    } else if ((uid!=null) && uid.length() < 3)  {
        out.println(loginForm());
        out.println("Invalid User ID.");
    } else {
        out.println(loginForm());
    }
    %>

<%@ include file="/inc/footer.jsp" %>
