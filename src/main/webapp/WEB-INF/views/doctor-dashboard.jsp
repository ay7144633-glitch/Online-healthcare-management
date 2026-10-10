<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="shell-top.jspf" %>
<div class="page-heading">
    <div>
        <div class="eyebrow"><span class="eyebrow-dot"></span>CLINICAL WORKSPACE</div>
        <h1>Good to see you, <c:out value="${user.fullName().split(' ')[0]}" />.</h1>
        <p>Your schedule, patient notes and feedback in one place.</p>
    </div>
    <a class="button button-primary" href="${ctx}/doctor/records">
        Add visit note <span aria-hidden="true">&#8594;</span>
    </a>
</div>

<div class="metrics-grid">
    <div class="metric">
        <span class="metric-label">Assigned appointments</span>
        <strong class="metric-value"><c:out value="${fn:length(appointments)}" /></strong>
        <span class="metric-note">Your patient schedule</span>
    </div>
    <div class="metric">
        <span class="metric-label">Patient records</span>
        <strong class="metric-value"><c:out value="${fn:length(records)}" /></strong>
        <span class="metric-note">Notes you have documented</span>
    </div>
    <div class="metric">
        <span class="metric-label">Patient rating</span>
        <strong class="metric-value"><c:out value="${averageRating}" /></strong>
        <span class="metric-note"><c:out value="${feedbackCount}" /> patient reviews</span>
    </div>
</div>

<div class="content-grid">
    <section class="panel">
        <div class="panel-heading">
            <div>
                <h2>Appointment schedule</h2>
                <p>Review your upcoming and recent visits</p>
            </div>
            <a class="text-link" href="${ctx}/appointments">
                Manage appointments <span aria-hidden="true">&#8594;</span>
            </a>
        </div>
        <c:choose>
            <c:when test="${empty appointments}">
                <div class="empty-state">
                    <strong>No appointments assigned</strong>
                    Patient requests will appear here.
                </div>
            </c:when>
            <c:otherwise>
                <div class="table-wrap">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Date &amp; time</th>
                                <th>Patient</th>
                                <th>Reason</th>
                                <th>Status</th>
                                <th>Update</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="appointment" items="${appointments}" begin="0" end="4">
                                <tr>
                                    <td>
                                        <strong>
                                            <c:out value="${appointment.appointmentAt().toLocalDate()}" />
                                        </strong>
                                        <small>
                                            <c:out value="${appointment.appointmentAt().toLocalTime()}" />
                                        </small>
                                    </td>
                                    <td><c:out value="${appointment.patientName()}" /></td>
                                    <td><c:out value="${appointment.reason()}" /></td>
                                    <td>
                                        <span class="status status-${appointment.status().toLowerCase()}">
                                            <c:out value="${appointment.status().toLowerCase()}" />
                                        </span>
                                    </td>
                                    <td>
                                        <form method="post" action="${ctx}/actions/appointments/status"
                                              class="inline-form">
                                            <input type="hidden" name="csrfToken"
                                                   value="<c:out value='${sessionScope.csrfToken}' />">
                                            <input type="hidden" name="returnTo" value="/dashboard">
                                            <input type="hidden" name="appointmentId"
                                                   value="${appointment.id()}">
                                            <select name="status" aria-label="Update appointment status">
                                                <option value="CONFIRMED">Confirm</option>
                                                <option value="COMPLETED">Complete</option>
                                                <option value="CANCELLED">Cancel</option>
                                            </select>
                                            <button class="button button-secondary button-small" type="submit">
                                                Save
                                            </button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </section>

    <section class="panel">
        <div class="panel-heading">
            <div>
                <h2>Schedule management</h2>
                <p>Choose the appointment times patients can request.</p>
            </div>
        </div>
        <p class="muted">
            Publish future availability, then review patient requests and update appointment
            status from your schedule.
        </p>
        <a class="text-link" href="${ctx}/doctor/schedule">
            Manage availability <span aria-hidden="true">&#8594;</span>
        </a>
    </section>
</div>

<div class="content-grid">
    <section class="panel">
        <div class="panel-heading">
            <div>
                <h2>Patient records</h2>
                <p>Recent visit notes in your care</p>
            </div>
            <a class="text-link" href="${ctx}/records">
                View records <span aria-hidden="true">&#8594;</span>
            </a>
        </div>
        <c:choose>
            <c:when test="${empty records}">
                <div class="empty-state">
                    <strong>No visit notes yet</strong>
                    Document a completed visit to build the patient record.
                </div>
            </c:when>
            <c:otherwise>
                <div class="dashboard-record-list">
                    <c:forEach var="record" items="${records}" begin="0" end="3">
                        <article>
                            <span class="record-date">
                                <c:out value="${record.createdAt().toLocalDate()}" />
                            </span>
                            <strong><c:out value="${record.patientName()}" /></strong>
                            <span><c:out value="${record.diagnosis()}" /></span>
                        </article>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </section>

    <section class="panel">
        <div class="panel-heading">
            <div>
                <h2>Patient feedback</h2>
                <p>Recent responses after completed visits</p>
            </div>
        </div>
        <c:choose>
            <c:when test="${empty feedbackItems}">
                <div class="empty-state">
                    <strong>No feedback yet</strong>
                    Patient reviews will appear here.
                </div>
            </c:when>
            <c:otherwise>
                <div class="feedback-list">
                    <c:forEach var="item" items="${feedbackItems}" begin="0" end="3">
                        <article class="feedback-entry">
                            <div class="feedback-meta">
                                <strong><c:out value="${item.patientName()}" /></strong>
                                <span>
                                    <c:out value="${item.rating()}" /> / 5
                                    <span aria-hidden="true">&#9733;</span>
                                </span>
                            </div>
                            <p><c:out value="${item.comments()}" /></p>
                            <small><c:out value="${item.createdAt().toLocalDate()}" /></small>
                        </article>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
</div>

<%@ include file="shell-bottom.jspf" %>
