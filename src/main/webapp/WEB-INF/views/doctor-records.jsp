<%@ include file="shell-top.jspf" %>
<div class="page-heading">
    <div>
        <div class="eyebrow"><span class="eyebrow-dot"></span>CLINICAL DOCUMENTATION</div>
        <h1>Add a visit note</h1>
        <p>Document a patient visit linked to one of your appointments.</p>
    </div>
</div>

<section class="panel">
    <div class="panel-heading">
        <div>
            <h2>New medical record</h2>
            <p>Only you and authorized administrators can access your patient notes.</p>
        </div>
    </div>
    <c:choose>
        <c:when test="${empty appointments}">
            <div class="empty-state">
                <strong>No appointments to document</strong>
                Patient appointments will be listed here.
            </div>
        </c:when>
        <c:otherwise>
            <form class="form-stack" method="post" action="${ctx}/actions/records/create">
                <input type="hidden" name="csrfToken"
                       value="<c:out value='${sessionScope.csrfToken}' />">
                <input type="hidden" name="returnTo" value="/doctor/records">
                <label>
                    Patient appointment
                    <select name="appointmentId" required>
                        <option value="">Choose an appointment</option>
                        <c:forEach var="appointment" items="${appointments}">
                            <option value="${appointment.id()}">
                                <c:out value="${appointment.patientName()}" />
                                &middot; <c:out value="${appointment.appointmentAt().toLocalDate()}" />
                                &middot; <c:out value="${appointment.status().toLowerCase()}" />
                            </option>
                        </c:forEach>
                    </select>
                </label>
                <label>
                    Diagnosis
                    <input name="diagnosis" required maxlength="255"
                           placeholder="Diagnosis or reason for visit">
                </label>
                <label>
                    Visit notes
                    <textarea name="notes" required maxlength="10000"
                              placeholder="Add relevant observations and care instructions"></textarea>
                </label>
                <div class="form-actions">
                    <button class="button button-primary" type="submit">
                        Save medical record <span aria-hidden="true">&#8594;</span>
                    </button>
                </div>
            </form>
        </c:otherwise>
    </c:choose>
</section>

<%@ include file="shell-bottom.jspf" %>
