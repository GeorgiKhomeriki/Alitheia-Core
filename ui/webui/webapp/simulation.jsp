<%@ include file="/inc/init.jsp"
%>
<%@ page session="true"
%>
<%
    title = "Simulation Model's Results";
%>
<%@ include file="/inc/header.jsp"
%>

<p><font size="+2">Simulation Model: </font></p>
<p><font size="+1">Average Scenario | <a href="simulation_best.jsp">Best case Scenario</a> | <a href="simulation_worst.jsp">Worst case Scenario</a></font></p>

<p><center>

<table>
<tr>
	<td><a href="/img/simulation/res_ll.jpg"><img src="/img/simulation/res_ll.jpg" border="0" width="300" height="240" /></a></td>
	<td><a href="/img/simulation/res_bbb.jpg"><img src="/img/simulation/res_bbb.jpg" border="0" width="300" height="240" /></a></td>
	<td><a href="/img/simulation/res_bbbr.jpg"><img src="/img/simulation/res_bbbr.jpg" border="0" width="300" height="240" /></a></td>
</tr>
<tr>
	<td><a href="/img/simulation/res_totb.jpg"><img src="/img/simulation/res_totb.jpg" border="0" width="300" height="240" /></a></td>
	<td><a href="/img/simulation/res_tott.jpg"><img src="/img/simulation/res_tott.jpg" border="0" width="300" height="240" /></a></td>
	<td><a href="/img/simulation/res_totf.jpg"><img src="/img/simulation/res_totf.jpg" border="0" width="300" height="240" /></a></td>
</tr>
<tr>
	<td><a href="/img/simulation/res_occ.jpg"><img src="/img/simulation/res_occ.jpg" border="0" width="300" height="240" /></a></td>
	<td><a href="/img/simulation/res_nb.jpg"><img src="/img/simulation/res_nb.jpg" border="0" width="300" height="240" /></a></td>
</tr>
</table>

</center></p>

<%@ include file="/inc/footer.jsp"
%>