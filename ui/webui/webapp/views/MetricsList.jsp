<jsp:useBean id="metricsView"
    class="eu.sqooss.webui.MetricsTableView"
    scope="page"/>

<%@ page import="eu.sqooss.webui.*" %>

<div id="metricslist" class="group">
<% // List metrics per selected project or as total

// Show metric per project, when a project selection exists
if (ProjectsListView.getProjectId() != null) {
    out.println("<h2>Metrics for this Project</h2>");
    metricsView = terrier.getMetrics4Project(ProjectsListView.getProjectId());
    //metricsView = terrier.getAllMetrics();
    if (metricsView != null ) {
        out.println(metricsView.getHtml());
    } else {
        out.println(Functions.error(terrier.getError()));
    }
    metricsView = null;
}


out.println("<h2>All available Metrics</h2>");
// Show all installed metrics available
metricsView = terrier.getAllMetrics();
// We don't provide metric names yet, therefore show the description
if (metricsView != null) {
    metricsView.setShowDescription(true);
}

// Display the accumulated metrics list
if (metricsView != null ) {
    out.println(metricsView.getHtml());
} else {
    out.println(Functions.error(terrier.getError()));
}
if (ProjectsListView.getProjectId() == null) {
%>
<p />
No project is selected. If you want to see metrics applied to a certain project,
<a href="/projects.jsp">choose one</a> first.
<%
}
%>
</div>
