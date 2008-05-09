<%@ page import="eu.sqooss.webui.*" %>

<%!
public static String versionSelector(Long projectId, Long currentVer) {
    String form =
        "<form id=\"selectversion\">"
        + "Current version:"
        + "&nbsp;"
        + "<input type=\"text\" name=\"version" + projectId
            + "\" value=\"" + currentVer+ "\" class=\"form\"/>"
        + "&nbsp;"
        + "<input type=\"submit\" value=\"Change\"  class=\"form\"/>"
        + "</form>";
    //String form = "bl\'\"a";
    return form;
}
%>

<%

// Retrieve the list of evaluated project from the connected SQO-OSS system
ProjectsListView.retrieveData(terrier);

if (ProjectsListView.hasProjects()) {
    // Generate the HTML content dispaying all evaluated projects
    String projects = ProjectsListView.getHtml();

    Project selectedProject = ProjectsListView.getCurrentProject();
    if (selectedProject != null) {
        out.println("<div id=\"selectedproject\" class=\"group\">");
        out.println("<h1>" + selectedProject.getName() + "</h1>");
        out.println("<span class=\"forget\"><a href=\"?pid=none\">(forget)</a></span>");

        out.println(selectedProject.getInfo());

        // Display the number of files in the selected project version
        // TODO: The files number should be cached in the Project object,
        //       instead of calling the Terrier each time.
        if (selectedProject.getCurrentVersionId() != null) {
            Long projectId = selectedProject.getId();
            String inputError = null;
            if (request.getParameter("version" + projectId) != null) {
                Long changeSelected = null;
                try {
                    changeSelected =
                        new Long(request.getParameter("version" + projectId));
                }
                catch (NumberFormatException e) {
                    inputError = new String("Wrong version format!");
                }
                if ((changeSelected != null)
                    && (changeSelected != selectedProject.getCurrentVersionId())) {
                    if (selectedProject.getCurrentVersionId() != null) {
                        selectedProject = ProjectsListView.getCurrentProject();
                    }
                    else {
                        inputError = "No such project version!";
                    }
                }
            }
            //out.println("Number of versions: " + selectedProject.countVersions());
            Long versionNum = selectedProject.getCurrentVersion().getNumber();
            Long versionId = selectedProject.getCurrentVersionId();
            // Display the first and last known project versions
            if (selectedProject.getFirstVersion() != null) {
                if (selectedProject.getFirstVersion()
                    != selectedProject.getLastVersion()) {
                    out.println ("<br />Versions: "
                        + selectedProject.getFirstVersion().getId()
                        + " - "
                        + selectedProject.getLastVersion().getId());
                } else {
                    out.println ("<br />Version: "
                        + selectedProject.getFirstVersion());
                }
            } else {
                out.println("Project doesn't seem to have versions recorded.");
            }
            out.println (versionSelector(projectId, versionId));
            out.println ("<br /><strong>Files:</strong> "
                + terrier.getFilesNumber4ProjectVersion(versionId)
                + " in version " + versionId + " (" + selectedProject.countVersions() + " total)");
            if (inputError != null) {
                out.println(Functions.error(inputError));
            }
        }

        out.println(selectedProject.showMetrics());

        String versionFileList = selectedProject.getCurrentVersion().listFiles();
        String projectFileList = selectedProject.listFiles();
        int v_c = selectedProject.getCurrentVersion().getFileCount();
        int p_c = selectedProject.getFileCount();


        out.println("\n<table width=\"100%\">\n\t<tr><td>");

        out.println("<h3>Files in " + selectedProject.getName() + " (" + p_c + ")</h3>");
        out.println(projectFileList);

        out.println("\n\t\t</td><td>");

        out.println("<h3>Files in Version " + selectedProject.getCurrentVersionId() + " (" + v_c + ")</h3>");
        out.println(versionFileList);

        out.println("\n\t\t</td>\n\t</tr>\n</table>");

        out.println("</div>"); // End of this group
        out.println("<div style=\"margin-bottom: 20px;\"></div>");
    }
    out.println("<div id=\"projectslist\" class=\"group\">");
    out.println(projects);
    out.println("</div>");
}
else {
    out.println("<div id=\"error\" class=\"group\">");
    if (cruncher.isOnline()) {
        out.println(Functions.error("Unable to find any evaluated projects."));
    } else {
        out.println(cruncher.getStatus());
    }
    out.println("</div>");
}
//out.println(Functions.error(terrier.getError()));

%>

