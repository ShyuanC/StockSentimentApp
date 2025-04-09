<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Operations Dashboard</title>
    <style>
        /* Basic reset and typography */
        body {
            margin: 0;
            padding: 20px;
            font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
            background-color: #f5f7fa;
            color: #333;
        }
        h1, h2 {
            margin: 0 0 15px 0;
            font-weight: 400;
        }
        h1 {
            font-size: 2rem;
            margin-bottom: 20px;
        }
        h2 {
            font-size: 1.5rem;
            margin-bottom: 10px;
        }
        /* Container for sections */
        .container {
            max-width: 1200px;
            margin: 0 auto;
        }
        /* Card style for each section */
        .card {
            background-color: #fff;
            border-radius: 6px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            padding: 20px;
            margin-bottom: 30px;
        }
        /* Section for displaying the log table */
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 10px;
        }
        table th, table td {
            padding: 10px;
            border: 1px solid #ddd;
            text-align: left;
            vertical-align: top;
        }
        /* Preformatted text styling */
        pre {
            margin: 0;
            white-space: pre-wrap; /* Allows wrapping */
            word-wrap: break-word;
            font-size: 0.9rem;
        }
        /* Modern button style (if needed later) */
        .btn {
            display: inline-block;
            padding: 10px 20px;
            background-color: #007bff;
            color: #fff;
            border-radius: 4px;
            text-decoration: none;
            transition: background-color 0.2s ease;
        }
        .btn:hover {
            background-color: #0056b3;
        }
    </style>
</head>
<body>
<div class="container">
    <h1>Operations Dashboard</h1>

    <!-- Card for Top 5 Stocks by Average Score -->
    <div class="card">
        <h2>Top 5 Stocks (by Average Score)</h2>
        <div>${top5}</div>
    </div>

    <!-- Card for Bottom 5 Stocks by Average Score -->
    <div class="card">
        <h2>Bottom 5 Stocks (by Average Score)</h2>
        <div>${bottom5}</div>
    </div>

    <!-- Card for Most Frequently Appearing Companies (Top 5) -->
    <div class="card">
        <h2>Most Frequently Appearing Companies (Top 5)</h2>
        <div>${mostFrequent}</div>
    </div>

    <!-- Card for Full Operation Logs -->
    <div class="card">
        <h2>Full Operation Logs</h2>
        <table>
            <thead>
            <tr>
                <th>Date Requested</th>
                <th>User Agent</th>
                <th>API Response Code</th>
                <th>Timestamp Request</th>
                <th>Timestamp Response</th>
                <th>Error</th>
                <th>API Response Data</th>
            </tr>
            </thead>
            <tbody>
            <!-- Iterate over each log entry and display its properties in table rows -->
            <c:forEach var="log" items="${logs}">
                <tr>
                    <td><pre>${log.date_requested}</pre></td>
                    <td><pre>${log.user_agent}</pre></td>
                    <td><pre>${log.api_response_code}</pre></td>
                    <td><pre>${log.timestamp_request}</pre></td>
                    <td><pre>${log.timestamp_response}</pre></td>
                    <td><pre>${log.error}</pre></td>
                    <td><pre>${log.api_response_data}</pre></td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>