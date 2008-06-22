<%@ page import="eu.sqooss.webui.*"
%><%
//============================================================================
// List all files in the selected project version
//============================================================================
if (selectedProject.isValid()) {
%>          <div id="fileslist" class="group">
<%
    Version selectedVersion = selectedProject.getCurrentVersion();
    if (selectedVersion != null) {
        out.println(sp(in) + "<h2>Files "
            + " in version " + selectedVersion.getNumber() + "</h2>");

        // Check if the user has switched to another directory
        if (request.getParameter("did") != null) {
            if (request.getParameter("did").equals("top"))
                selectedVersion.topDir();
            else if (request.getParameter("did").equals("prev"))
                selectedVersion.previousDir();
            else {
                Long directoryId = getId(request.getParameter("did"));
                if (directoryId != null)
                    selectedVersion.switchDir(directoryId);
            }
        }
        // Display some file statistics for the selected version
        out.print(selectedVersion.fileStats(in));
        // Display all files in the selected project version
        out.print(selectedVersion.listFiles(selectedProject, in));
    } else {
        out.print(sp(in) + Functions.warning(
            "Please select a <a href=\"/versions.jsp\">"
            + "project version.</a>"));
    }
}
//============================================================================
// Let the user choose a project, if none was selected
//============================================================================
else {
%>          <div id="projectslist" class="group">
<%
    // Check if the user has selected a project
    if (request.getParameter("pid") != null)
        ProjectsListView.setCurrentProject(
            strToLong(request.getParameter("pid")));
    // Retrieve the list of projects from the connected SQO-OSS framework
    ProjectsListView.retrieveData(terrier);
    // Display the list of evaluated projects (if any)
    if (ProjectsListView.hasProjects()) {
        out.println(sp(in) + "<h2>Please select a project first</h2>");
        ProjectsListView.setServletPath(request.getServletPath());
        out.println(ProjectsListView.getHtml(in));
    }
    // No evaluated projects found
    else {
        out.print(sp(in) + Functions.error(
                "Unable to find any evaluated projects!"));
    }
}
%>          </div>
