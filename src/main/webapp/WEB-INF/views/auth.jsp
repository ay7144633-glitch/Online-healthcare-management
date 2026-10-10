<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="theme-color" content="#f5f7f4">
    <title>
        <c:choose>
            <c:when test="${registerMode}">Create account</c:when>
            <c:otherwise>Sign in</c:otherwise>
        </c:choose>
        | Carepoint
    </title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&family=Manrope:wght@400;500;600;700;800&display=swap"
          rel="stylesheet">
    <link rel="stylesheet" href="${ctx}/assets/app.css">
    <link rel="stylesheet" href="${ctx}/assets/readability.css">
</head>
<body class="auth-body">
<main class="auth-layout">
    <section class="auth-story">
        <a class="brand brand-light" href="${ctx}/login">
            <span class="brand-mark">C</span>
            <span>carepoint</span>
        </a>
        <div class="story-copy">
            <div class="eyebrow"><span class="eyebrow-dot"></span>BETTER CARE, TOGETHER</div>
            <h1>Health care that keeps you <em>connected.</em></h1>
            <p>Appointments, trusted clinicians and your health history, together in one calm place.</p>
        </div>
        <div class="story-bottom">
            <span class="story-line"></span>
            <span>Carepoint Healthcare <span class="story-separator">/</span> Patient services</span>
        </div>
    </section>

    <section class="auth-panel">
        <div class="auth-topline">
            <span>SECURE HEALTH PORTAL</span>
            <span>01 <span class="story-separator">/</span> 03</span>
        </div>
        <div class="auth-form-wrap">
            <div class="eyebrow auth-eyebrow">
                <span class="eyebrow-dot"></span>
                <c:choose>
                    <c:when test="${registerMode}">GET STARTED</c:when>
                    <c:otherwise>WELCOME BACK</c:otherwise>
                </c:choose>
            </div>
            <h2>
                <c:choose>
                    <c:when test="${registerMode}">Create your account</c:when>
                    <c:otherwise>Sign in to Carepoint</c:otherwise>
                </c:choose>
            </h2>
            <p class="auth-intro">
                <c:choose>
                    <c:when test="${registerMode}">Your health journey starts with a few details.</c:when>
                    <c:otherwise>Your care team and health information are ready when you are.</c:otherwise>
                </c:choose>
            </p>
            <c:if test="${not empty error}">
                <div class="notice notice-error" role="alert"><c:out value="${error}" /></div>
            </c:if>
            <form class="form-stack" method="post"
                  action="${ctx}<c:out value='${registerMode ? "/register" : "/login"}' />">
                <input type="hidden" name="csrfToken"
                       value="<c:out value='${sessionScope.csrfToken}' />">
                <c:if test="${registerMode}">
                    <label>
                        Full name
                        <input name="fullName" autocomplete="name" required minlength="2"
                               maxlength="120" placeholder="Your full name">
                    </label>
                </c:if>
                <label>
                    Email address
                    <input name="email" type="email" autocomplete="email" required
                           maxlength="190" placeholder="you@example.com">
                </label>
                <label>
                    Password
                    <input name="password" type="password"
                           autocomplete="${registerMode ? 'new-password' : 'current-password'}"
                           required minlength="8" placeholder="At least 8 characters">
                </label>
                <button class="button button-primary button-wide" type="submit">
                    <c:choose>
                        <c:when test="${registerMode}">
                            Create patient account <span aria-hidden="true">&#8594;</span>
                        </c:when>
                        <c:otherwise>Sign in <span aria-hidden="true">&#8594;</span></c:otherwise>
                    </c:choose>
                </button>
            </form>
            <p class="auth-switch">
                <c:choose>
                    <c:when test="${registerMode}">
                        Already have an account? <a href="${ctx}/login">Sign in</a>
                    </c:when>
                    <c:otherwise>
                        New to Carepoint? <a href="${ctx}/register">Create a patient account</a><br>
                        Doctor? <a href="${ctx}/doctor/login">Open the doctor portal</a>
                    </c:otherwise>
                </c:choose>
            </p>
            <c:if test="${not empty applicationScope.databaseError}">
                <div class="database-hint">
                    <strong>Database not connected</strong>
                    <span>Start MySQL and check the DB settings in the project README.</span>
                </div>
            </c:if>
        </div>
        <div class="auth-legal">
            By continuing, you agree to keep your account credentials private.
        </div>
    </section>
</main>
</body>
</html>
