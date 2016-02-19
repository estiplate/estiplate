<%@ page import="helpingsPackage.StatsTimerTask"%>

<html>
<head>
<link href='https://fonts.googleapis.com/css?family=Varela+Round'
	rel='stylesheet' type='text/css'>
<meta name="viewport"
	content="width=device-width, initial-scale=1.0, user-scalable=no" />
<link rel="stylesheet" type="text/css" href="styles.css">
</head>
<body>

	<div id="score">
		<h1><a href="/">ESTIPLATE</a></h1>
		<h2>Leaderboard - Past Week</h2>
		<h3>Average Error Percentage</h3>
		<table>
		<tr>
		<td style="background-color:#dddddd; vertical-align:top; padding:10px; 	margin-left: auto; margin-right: auto;">
		<%= StatsTimerTask.getOutput() %>
		</td>
		</tr>
		</table>
	</div>
</body>
</html>