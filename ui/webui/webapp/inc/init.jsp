<%@ page import="java.util.*" %>
<%@ page import="eu.sqooss.webui.*" %>
<%@ page session="true" %>
<%!
ResourceBundle configProperties = null;
String initError = null;


public void jspInit() {
    try {
        configProperties = ResourceBundle.getBundle("/config");
    }
    catch (MissingResourceException ex) {
        configProperties = null;
        initError = ex.toString();
    }
}
%>
<%@ include file="/inc/functions.jsp" %>
<%

Long projectId = null;
if (request.getParameter("pid") != null) {
    String req = request.getParameter("pid");
    if (!"none".equals(req)) {
        projectId = getId(req);
        //out.println("PID: " + projectId);
    }
}
/*
    This file instaniates shared objects and defines shared variables
    commonly used by the majority of the WebUI JSP pages. Therefore, it
    should be included at the top of every JSP page.
*/

String title    = "Alitheia";
String msg      = "";

%>


<jsp:useBean id="terrier"
    class="eu.sqooss.webui.Terrier"
    scope="session">
    <%
        // Initialise the Terrier's configuration properties
        terrier.initConfig(configProperties);
    %>
    <jsp:setProperty name="terrier" property="*"/>
</jsp:useBean>

<jsp:useBean
    id="user"
    class="eu.sqooss.webui.User"
    scope="session">
    <jsp:setProperty name="user" property="loggedIn" value="false"/>
</jsp:useBean>

<%
// We also keep a Project as the session state.
// The project is either valid, and can be used to display the
// Project's information.
// It can also be invalid, that means that the id is null.
%>
<jsp:useBean
    id="selectedProject"
    class="eu.sqooss.webui.Project"
    scope="session">
    <jsp:setProperty name="selectedProject" property="id" value="<%= projectId %>"/>
    <%
    if (projectId != null) {
        selectedProject.setId(projectId);
        selectedProject.setTerrier(terrier);
        selectedProject.retrieveData();
    }
    %>
</jsp:useBean>

<%
// Selectedproject might have become valid, reflect that
if (projectId != null && !selectedProject.isValid()) {
    selectedProject.setId(projectId);
    selectedProject.setTerrier(terrier);
    selectedProject.retrieveData();
}

if ("none".equals(request.getParameter("pid"))) {
    selectedProject.setInValid(); // Invalidate
}
%>
<jsp:useBean id="validator"
    class="eu.sqooss.webui.InputValidator"
    scope="session"/>
<jsp:setProperty name="validator" property="*"/>


<%@ include file="/inc/login.jsp" %>
