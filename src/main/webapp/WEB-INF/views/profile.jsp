<%@ include file="shell-top.jspf" %>
<div class="page-heading">
    <div>
        <div class="eyebrow"><span class="eyebrow-dot"></span>YOUR ACCOUNT</div>
        <h1>Your profile</h1>
        <p>Keep your contact details current so your care team can reach you.</p>
    </div>
</div>

<section class="panel profile-card">
    <div class="profile-initial">
        <c:out value="${profile.fullName().substring(0, 1).toUpperCase()}" />
    </div>
    <div>
        <div class="panel-heading">
            <div>
                <h2>Personal details</h2>
                <p>These details are used for your patient account.</p>
            </div>
        </div>
        <form class="form-stack" method="post" action="${ctx}/actions/profile/save">
            <input type="hidden" name="csrfToken"
                   value="<c:out value='${sessionScope.csrfToken}' />">
            <input type="hidden" name="returnTo" value="/patient/profile">
            <label>
                Full name
                <input name="fullName" required minlength="2" maxlength="120"
                       value="<c:out value='${profile.fullName()}' />">
            </label>
            <label>
                Email address
                <input type="email" name="email" required maxlength="190"
                       value="<c:out value='${profile.email()}' />">
            </label>
            <div class="form-actions">
                <button class="button button-primary" type="submit">
                    Save profile <span aria-hidden="true">&#8594;</span>
                </button>
            </div>
        </form>
    </div>
</section>

<section class="panel">
    <div class="panel-heading">
        <div>
            <h2>Change password</h2>
            <p>Confirm your current password before choosing a new one.</p>
        </div>
    </div>
    <form class="form-stack" method="post" action="${ctx}/actions/profile/password">
        <input type="hidden" name="csrfToken"
               value="<c:out value='${sessionScope.csrfToken}' />">
        <input type="hidden" name="returnTo" value="/patient/profile">
        <label>
            Current password
            <input type="password" name="currentPassword" autocomplete="current-password" required>
        </label>
        <label>
            New password
            <input type="password" name="newPassword" autocomplete="new-password" minlength="8" required>
        </label>
        <label>
            Confirm new password
            <input type="password" name="confirmPassword" autocomplete="new-password"
                   minlength="8" required>
        </label>
        <div class="form-actions">
            <button class="button button-primary" type="submit">Change password</button>
        </div>
    </form>
</section>

<%@ include file="shell-bottom.jspf" %>
