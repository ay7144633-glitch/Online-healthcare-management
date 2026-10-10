<%@ include file="shell-top.jspf" %>
<div class="page-heading">
    <div>
        <div class="eyebrow"><span class="eyebrow-dot"></span>CONNECTION STATUS</div>
        <h1>We couldn't load your data</h1>
        <p>Carepoint needs a working database connection for this view.</p>
    </div>
</div>

<div class="notice notice-error"><c:out value="${error}" /></div>

<div class="panel">
    <h2>Check your MySQL connection</h2>
    <p class="muted">
        Start the MySQL service and confirm DB_HOST, DB_PORT, DB_NAME, DB_USER, and DB_PASSWORD
        are set as described in README.md.
    </p>
    <a class="button button-secondary" href="${ctx}/dashboard">Try again</a>
</div>

<%@ include file="shell-bottom.jspf" %>
