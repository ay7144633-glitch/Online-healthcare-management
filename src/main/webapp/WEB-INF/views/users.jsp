<%@ include file="shell-top.jspf" %>
<div class="page-heading">
    <div>
        <div class="eyebrow"><span class="eyebrow-dot"></span>CLINIC ACCESS</div>
        <h1>People</h1>
        <p>Create accounts for any role and manage access for the clinic team.</p>
    </div>
</div>

<section class="panel">
    <div class="panel-heading">
        <div>
            <h2>Create an account</h2>
            <p>Add a patient, doctor, or administrator. New accounts are active immediately.</p>
        </div>
    </div>

    <form class="form-grid" method="post" action="${ctx}/actions/users/create-account">
        <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />">
        <input type="hidden" name="returnTo" value="/admin/users">
        <label>
            Full name
            <input name="fullName" required minlength="2" maxlength="120" placeholder="Full name">
        </label>
        <label>
            Email address
            <input type="email" name="email" required maxlength="190" placeholder="name@clinic.com">
        </label>
        <label>
            Role
            <select name="role" required>
                <option value="PATIENT">Patient</option>
                <option value="DOCTOR">Doctor</option>
                <option value="ADMIN">Administrator</option>
            </select>
        </label>
        <label>
            Specialty (doctors only)
            <input name="specialty" maxlength="120" placeholder="Family medicine">
        </label>
        <label>
            Temporary password
            <input type="password" name="password" required minlength="8"
                   autocomplete="new-password" placeholder="At least 8 characters">
        </label>
        <div class="form-actions field-wide">
            <button class="button button-primary" type="submit">
                Create account <span aria-hidden="true">&#8594;</span>
            </button>
        </div>
    </form>
</section>

<section class="panel">
    <div class="panel-heading">
        <div>
            <h2>All accounts</h2>
            <p>Patient, doctor and administrator accounts in this system.</p>
        </div>
    </div>

    <c:choose>
        <c:when test="${empty users}">
            <div class="empty-state">
                <strong>No accounts found</strong>
                New accounts will appear here.
            </div>
        </c:when>
        <c:otherwise>
            <div class="table-wrap">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th>Email</th>
                            <th>Role</th>
                            <th>Specialty</th>
                            <th>Access</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="account" items="${users}">
                            <tr>
                                <td><strong><c:out value="${account.fullName()}" /></strong></td>
                                <td><c:out value="${account.email()}" /></td>
                                <td><c:out value="${account.role().toLowerCase()}" /></td>
                                <td><c:out value="${account.specialty()}" /></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${account.active()}">
                                            <span class="status status-confirmed">Active</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="status status-cancelled">Inactive</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <div class="dashboard-action-list">
                                        <c:if test="${account.id() != user.id()}">
                                            <details>
                                                <summary class="button button-secondary button-small">Edit</summary>
                                                <form class="form-stack" method="post"
                                                      action="${ctx}/actions/users/update">
                                                    <input type="hidden" name="csrfToken"
                                                           value="<c:out value='${sessionScope.csrfToken}' />">
                                                    <input type="hidden" name="returnTo" value="/admin/users">
                                                    <input type="hidden" name="userId" value="${account.id()}">
                                                    <label>
                                                        Name
                                                        <input name="fullName" required minlength="2" maxlength="120"
                                                               value="<c:out value='${account.fullName()}' />">
                                                    </label>
                                                    <label>
                                                        Email
                                                        <input name="email" type="email" required maxlength="190"
                                                               value="<c:out value='${account.email()}' />">
                                                    </label>
                                                    <label>
                                                        Role
                                                        <select name="role" required>
                                                            <option value="ADMIN"
                                                                    ${account.role() == 'ADMIN' ? 'selected' : ''}>
                                                                Administrator
                                                            </option>
                                                            <option value="DOCTOR"
                                                                    ${account.role() == 'DOCTOR' ? 'selected' : ''}>
                                                                Doctor
                                                            </option>
                                                            <option value="PATIENT"
                                                                    ${account.role() == 'PATIENT' ? 'selected' : ''}>
                                                                Patient
                                                            </option>
                                                        </select>
                                                    </label>
                                                    <label>
                                                        Specialty
                                                        <input name="specialty" maxlength="120"
                                                               value="<c:out value='${account.specialty()}' />">
                                                    </label>
                                                    <button class="button button-primary button-small" type="submit">
                                                        Save account
                                                    </button>
                                                </form>
                                            </details>
                                        </c:if>

                                        <c:if test="${account.role() != 'ADMIN'}">
                                            <form method="post" action="${ctx}/actions/users/toggle">
                                                <input type="hidden" name="csrfToken"
                                                       value="<c:out value='${sessionScope.csrfToken}' />">
                                                <input type="hidden" name="returnTo" value="/admin/users">
                                                <input type="hidden" name="userId" value="${account.id()}">
                                                <input type="hidden" name="active" value="${!account.active()}">
                                                <button class="button button-secondary button-small" type="submit">
                                                    <c:choose>
                                                        <c:when test="${account.active()}">Deactivate</c:when>
                                                        <c:otherwise>Reactivate</c:otherwise>
                                                    </c:choose>
                                                </button>
                                            </form>
                                        </c:if>

                                        <c:if test="${account.id() != user.id()}">
                                            <form method="post" action="${ctx}/actions/users/delete">
                                                <input type="hidden" name="csrfToken"
                                                       value="<c:out value='${sessionScope.csrfToken}' />">
                                                <input type="hidden" name="returnTo" value="/admin/users">
                                                <input type="hidden" name="userId" value="${account.id()}">
                                                <button class="button button-danger button-small" type="submit">
                                                    Delete
                                                </button>
                                            </form>
                                        </c:if>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</section>

<%@ include file="shell-bottom.jspf" %>
