<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="theme-color" content="#f5f7f4">
    <title>Doctor sign in | Carepoint</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&family=Manrope:wght@400;500;600;700;800&display=swap"
          rel="stylesheet">
    <link rel="stylesheet" href="${ctx}/assets/app.css">
    <link rel="stylesheet" href="${ctx}/assets/doctor-login.css">
    <link rel="stylesheet" href="${ctx}/assets/readability.css">
</head>
<body class="doctor-login-body">
<header class="doctor-login-header">
    <a class="brand" href="${ctx}/" aria-label="Carepoint home">
        <span class="brand-mark">C</span>
        <span>carepoint</span>
    </a>
    <nav class="doctor-login-nav" aria-label="Main navigation">
        <a href="${ctx}/">Home</a>
        <a href="${ctx}/for-patients">For patients</a>
        <a class="is-current" href="${ctx}/for-doctors" aria-current="page">For doctors</a>
        <a href="${ctx}/for-clinics">For clinics</a>
        <a href="${ctx}/how-it-works">How it works</a>
    </nav>
    <a class="doctor-login-general-link" href="${ctx}/login">
        Patient / admin sign in <span aria-hidden="true">&#8594;</span>
    </a>
</header>
<main class="doctor-login-main">
    <section class="doctor-login-intro">
        <div class="eyebrow"><span class="eyebrow-dot"></span>CLINICIAN ACCESS</div>
        <h1>Your clinical day, <em>in focus.</em></h1>
        <p>Sign in to coordinate appointments, publish availability and keep patient visits moving.</p>
        <div class="doctor-login-workflows" aria-label="Doctor workspace areas">
            <div><span>01</span><strong>Daily schedule</strong></div>
            <div><span>02</span><strong>Patient visits</strong></div>
            <div><span>03</span><strong>Clinical notes</strong></div>
        </div>
    </section>

    <section class="doctor-login-panel" aria-labelledby="doctor-login-title">
        <div class="eyebrow doctor-login-kicker">
            <span class="eyebrow-dot"></span>SECURE CLINICIAN SIGN IN
        </div>
        <h2 id="doctor-login-title">Welcome back</h2>
        <p class="doctor-login-description">
            Use your doctor account to enter the clinical workspace.
        </p>
        <c:if test="${not empty error}">
            <div class="notice notice-error" role="alert"><c:out value="${error}" /></div>
        </c:if>
        <form class="form-stack doctor-login-form" method="post" action="${ctx}/doctor/login">
            <input type="hidden" name="csrfToken"
                   value="<c:out value='${sessionScope.csrfToken}' />">
            <label>
                Email address
                <input name="email" type="email" autocomplete="username" required
                       maxlength="190" placeholder="you@clinic.com">
            </label>
            <label>
                Password
                <input name="password" type="password" autocomplete="current-password"
                       required minlength="8" placeholder="Your doctor account password">
            </label>
            <button class="button button-primary button-wide" type="submit">
                Sign in to doctor workspace <span aria-hidden="true">&#8594;</span>
            </button>
        </form>
        <p class="doctor-login-switch">
            Patient or administrator? <a href="${ctx}/login">Use general sign in</a>
        </p>
    </section>
</main>
</body>
</html>
