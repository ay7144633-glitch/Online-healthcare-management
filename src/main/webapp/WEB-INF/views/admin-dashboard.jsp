<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="shell-top.jspf" %>
<div class="page-heading">
    <div>
        <div class="eyebrow"><span class="eyebrow-dot"></span>ADMINISTRATION</div>
        <h1>Clinic overview</h1>
        <p>Monitor clinic activity, manage access and keep operations moving.</p>
    </div>
    <a class="button button-primary" href="${ctx}/admin/users">
        Manage people <span aria-hidden="true">&#8594;</span>
    </a>
</div>

<div class="metrics-grid admin-metrics">
    <div class="metric">
        <span class="metric-label">All accounts</span>
        <strong class="metric-value"><c:out value="${accountCount}" /></strong>
        <span class="metric-note">Patients, doctors and admins</span>
    </div>
    <div class="metric">
        <span class="metric-label">Active clinicians</span>
        <strong class="metric-value"><c:out value="${activeDoctorCount}" /></strong>
        <span class="metric-note">Doctors with access enabled</span>
    </div>
    <div class="metric">
        <span class="metric-label">Appointments</span>
        <strong class="metric-value"><c:out value="${fn:length(appointments)}" /></strong>
        <span class="metric-note">Across the clinic</span>
    </div>
    <div class="metric">
        <span class="metric-label">Active patients</span>
        <strong class="metric-value"><c:out value="${activePatientCount}" /></strong>
        <span class="metric-note">Registered patient accounts</span>
    </div>
</div>

<div class="content-grid">
    <section class="panel analytics-panel">
        <div class="panel-heading">
            <div>
                <h2>Appointment performance</h2>
                <p>Clinic-wide volume grouped by status</p>
            </div>
            <a class="text-link" href="${ctx}/appointments">
                Open schedule <span aria-hidden="true">&#8594;</span>
            </a>
        </div>
        <div class="analytics-list">
            <c:forEach var="point" items="${analytics}">
                <div class="analytics-row">
                    <span class="analytics-label">
                        <c:out value="${point.label().toLowerCase()}" />
                    </span>
                    <span class="analytics-track">
                        <span class="analytics-bar analytics-${point.label().toLowerCase()}"
                              style="width: ${point.percentage()}%"></span>
                    </span>
                    <strong class="analytics-count"><c:out value="${point.count()}" /></strong>
                </div>
            </c:forEach>
        </div>
    </section>

    <section class="panel">
        <div class="panel-heading">
            <div>
                <h2>Administration</h2>
                <p>Clinic configuration and access</p>
            </div>
        </div>
        <div class="dashboard-action-list">
            <a href="${ctx}/admin/users">
                <span class="action-index">01</span>
                <span>
                    <strong>People and roles</strong>
                    <small>Create clinician accounts and manage access.</small>
                </span>
                <span class="action-arrow" aria-hidden="true">&#8594;</span>
            </a>
            <a href="${ctx}/admin/settings">
                <span class="action-index">02</span>
                <span>
                    <strong>System settings</strong>
                    <small>Update clinic name and appointment length.</small>
                </span>
                <span class="action-arrow" aria-hidden="true">&#8594;</span>
            </a>
        </div>
    </section>
</div>

<section class="panel">
    <div class="panel-heading">
        <div>
            <h2>Six-month clinic activity</h2>
            <p>Appointment volume and new account registrations by month</p>
        </div>
    </div>
    <div class="admin-trend-grid">
        <section>
            <h3>Appointments</h3>
            <div class="analytics-list">
                <c:forEach var="point" items="${monthlyAppointments}">
                    <div class="analytics-row">
                        <span class="analytics-label"><c:out value="${point.label()}" /></span>
                        <span class="analytics-track">
                            <span class="analytics-bar" style="width: ${point.percentage()}%"></span>
                        </span>
                        <strong class="analytics-count"><c:out value="${point.count()}" /></strong>
                    </div>
                </c:forEach>
            </div>
        </section>
        <section>
            <h3>New accounts</h3>
            <div class="analytics-list">
                <c:forEach var="point" items="${monthlyAccounts}">
                    <div class="analytics-row">
                        <span class="analytics-label"><c:out value="${point.label()}" /></span>
                        <span class="analytics-track">
                            <span class="analytics-bar analytics-confirmed"
                                  style="width: ${point.percentage()}%"></span>
                        </span>
                        <strong class="analytics-count"><c:out value="${point.count()}" /></strong>
                    </div>
                </c:forEach>
            </div>
        </section>
    </div>
</section>

<section class="panel">
    <div class="panel-heading">
        <div>
            <h2>Clinic appointments</h2>
            <p>Most recently scheduled visits</p>
        </div>
        <a class="text-link" href="${ctx}/appointments">
            Manage schedule <span aria-hidden="true">&#8594;</span>
        </a>
    </div>
    <c:choose>
        <c:when test="${empty appointments}">
            <div class="empty-state">
                <strong>No appointments yet</strong>
                Patient bookings will appear here.
            </div>
        </c:when>
        <c:otherwise>
            <div class="table-wrap">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Date &amp; time</th>
                            <th>Patient</th>
                            <th>Doctor</th>
                            <th>Reason</th>
                            <th>Status</th>
                            <th>Update</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="appointment" items="${appointments}" begin="0" end="5">
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
                                <td><c:out value="${appointment.doctorName()}" /></td>
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

<%@ include file="shell-bottom.jspf" %>
