<%@ page import="eu.sqooss.webui.*" %>

<div id="fileslist" class="group">
<% // List files per selected project and version

out.println("<table><tr>");
if (selectedProject.isValid()) {
    out.println("<td valign=\"top\">");

    Version selectedVersion = selectedProject.getCurrentVersion();
    if (selectedVersion != null) {
        out.println("<h2>Files "
            + " in version " + selectedVersion.getNumber()
            + "</h2>");

        // Display all files in the selected project version
        out.println(selectedVersion.fileStats());
        out.println(selectedVersion.listFiles());
    } else {
        out.println(Functions.error(
            "Please select a <a href=\"/versions.jsp\">"
            + "project version.</a>"));
    }
    out.println("</td>");
}
else {
    // Check if the user has selected a project
    ProjectsListView.setProjectId(request.getParameter("pid"));
    // Retrieve the list of evaluated project from the connected SQO-OSS
    // system OR the selected project's file list when a project is selected.
    ProjectsListView.retrieveData(terrier);
    // Display the list of evaluated projects (if any)
    if (ProjectsListView.hasProjects()) {
        out.println("<td valign=\"top\">");
        out.println("<h2>Please select a project first</h2>");
        // Generate the HTML content dispaying all evaluated projects
        out.println(ProjectsListView.getHtml(request.getServletPath()));
    }
    // No evaluated projects found
    else {
        out.println("<div id=\"error\">");
        if (cruncher.isOnline()) {
            out.println(Functions.error(
                "Unable to find any evaluated projects."));
        } else {
            out.println(cruncher.getStatus());
        }
        out.println("</div>");
    }
    out.println("</td>");
}
out.println("</tr></table>");

%>
</div>
