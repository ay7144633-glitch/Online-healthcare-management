<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="theme-color" content="#f5f7f4">
    <title>Administrator access | Carepoint</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&family=Manrope:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${ctx}/assets/app.css">
    <link rel="stylesheet" href="${ctx}/assets/admin-login.css">
</head>
<body class="admin-login-body">
<header class="admin-login-header">
    <a class="brand" href="${ctx}/" aria-label="Carepoint home"><span class="brand-mark">C</span><span>carepoint</span></a>
    <span class="admin-login-label"><span class="eyebrow-dot"></span>PRIVATE ADMINISTRATION</span>
    <a class="admin-general-link" href="${ctx}/login">General sign in <span aria-hidden="true">&#8594;</span></a>
</header>
<main class="admin-login-main">
    <section class="admin-login-intro">
        <div class="eyebrow"><span class="eyebrow-dot"></span>CLINIC OPERATIONS</div>
        <h1>Keep the whole clinic <em>in view.</em></h1>
        <p>Manage access, appointment activity and system settings from the administrator workspace.</p>
        <div class="admin-login-highlights" aria-label="Administrator workspace areas">
            <div><span>01</span><strong>People and roles</strong></div>
            <div><span>02</span><strong>Appointment activity</strong></div>
            <div><span>03</span><strong>Clinic settings</strong></div>
        </div>
    </section>
    <section class="admin-login-panel" aria-labelledby="admin-login-title">
        <div class="eyebrow admin-login-kicker"><span class="eyebrow-dot"></span>ADMINISTRATOR ACCESS</div>
        <h2 id="admin-login-title">Sign in to operations</h2>
        <p class="admin-login-description">This portal is for active administrator accounts.</p>
        <c:if test="${not empty error}"><div class="notice notice-error" role="alert"><c:out value="${error}" /></div></c:if>
        <form class="form-stack admin-login-form" method="post" action="${ctx}/admin/login">
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />">
            <label>Administrator email<input name="email" type="email" autocomplete="username" required maxlength="190" placeholder="admin@carepoint.local"></label>
            <label>Password<input name="password" type="password" autocomplete="current-password" required minlength="8" placeholder="Your administrator password"></label>
            <button class="button button-primary button-wide" type="submit">Enter admin workspace <span aria-hidden="true">&#8594;</span></button>
        </form>
        <p class="admin-login-note">Sign-in requires an active account with the ADMIN role.</p>
    </section>
</main>
</body>
</html>
