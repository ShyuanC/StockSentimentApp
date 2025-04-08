<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Operations Dashboard</title>
    <style>
        table, th, td {
            border: 1px solid #000;
            border-collapse: collapse;
        }
        th, td {
            padding: 8px;
        }
    </style>
</head>
<body>
<h1>Operations Analytics</h1>

<h2>Top 5 Average Scores</h2>
<table>
    <tr>
        <th>Ticker</th>
        <th>Average Score</th>
    </tr>
    <c:forEach var="doc" items="${top5}">
        <tr>
            <td>${doc._id}</td>
            <td>${doc.avgScore}</td>
        </tr>
    </c:forEach>
</table>

<h2>Bottom 5 Average Scores</h2>
<table>
    <tr>
        <th>Ticker</th>
        <th>Average Score</th>
    </tr>
    <c:forEach var="doc" items="${bottom5}">
        <tr>
            <td>${doc._id}</td>
            <td>${doc.avgScore}</td>
        </tr>
    </c:forEach>
</table>

<h1>Operation Logs</h1>
<table>
    <tr>
        <th>Date Requested</th>
        <th>User Agent</th>
        <th>API Response Code</th>
        <th>Timestamp Request</th>
        <th>Timestamp Response</th>
        <th>Error</th>
    </tr>
    <c:forEach var="log" items="${logs}">
        <tr>
            <td>${log.date_requested}</td>
            <td>${log.user_agent}</td>
            <td>${log.api_response_code}</td>
            <td>${log.timestamp_request}</td>
            <td>${log.timestamp_response}</td>
            <td>${log.error}</td>
        </tr>
    </c:forEach>
</table>
</body>
</html>