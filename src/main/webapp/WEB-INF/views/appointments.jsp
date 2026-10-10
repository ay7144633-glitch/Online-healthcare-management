<%@ include file="shell-top.jspf" %>
<div class="page-heading">
    <div>
        <div class="eyebrow"><span class="eyebrow-dot"></span>CARE CALENDAR</div>
        <h1>Appointments</h1>
        <p>Coordinate upcoming visits and keep every appointment in view.</p>
    </div>
</div>

<c:if test="${user.role() == 'PATIENT'}">
    <section class="panel">
        <div class="panel-heading">
            <div>
                <h2>Request an appointment</h2>
                <p>Choose a clinician and a time that works for you.</p>
            </div>
        </div>
        <c:choose>
            <c:when test="${empty doctors}">
                <div class="empty-state">
                    <strong>No doctors are available yet</strong>
                    Your clinic administrator can add clinicians in People.
                </div>
            </c:when>
            <c:otherwise>
                <form class="form-grid" method="post" action="${ctx}/actions/appointments/book">
                    <input type="hidden" name="csrfToken"
                           value="<c:out value='${sessionScope.csrfToken}' />">
                    <input type="hidden" name="returnTo" value="/appointments">
                    <label>
                        Clinician
                        <select name="doctorId" required>
                            <option value="">Choose a doctor</option>
                            <c:forEach var="doctor" items="${doctors}">
                                <option value="${doctor.id()}">
                                    <c:out value="${doctor.fullName()}" />
                                    <c:if test="${not empty doctor.specialty()}">
                                        &middot; <c:out value="${doctor.specialty()}" />
                                    </c:if>
                                </option>
                            </c:forEach>
                        </select>
                    </label>
                    <label>
                        Date and time
                        <input type="datetime-local" name="appointmentAt"
                               min="<c:out value='${minimumAppointmentDateTime}' />" required>
                    </label>
                    <label class="field-wide">
                        Reason for visit
                        <textarea name="reason" required maxlength="500"
                                  placeholder="Briefly describe what you would like help with"></textarea>
                    </label>
                    <div class="form-actions field-wide">
                        <button class="button button-primary" type="submit">
                            Request appointment <span aria-hidden="true">&#8594;</span>
                        </button>
                    </div>
                </form>
            </c:otherwise>
        </c:choose>
    </section>
</c:if>

<c:if test="${user.role() == 'ADMIN'}">
    <section class="panel">
        <div class="panel-heading">
            <div>
                <h2>Schedule for a patient</h2>
                <p>Choose an active patient, a doctor, and a time inside published availability.</p>
            </div>
        </div>
        <c:choose>
            <c:when test="${empty patients || empty doctors}">
                <div class="empty-state">
                    <strong>Patients and doctors are required</strong>
                    Create active accounts and ask a doctor to publish availability before scheduling.
                </div>
            </c:when>
            <c:otherwise>
                <form class="form-grid" method="post"
                      action="${ctx}/actions/appointments/book-for-patient">
                    <input type="hidden" name="csrfToken"
                           value="<c:out value='${sessionScope.csrfToken}' />">
                    <input type="hidden" name="returnTo" value="/appointments">
                    <label>
                        Patient
                        <select name="patientId" required>
                            <option value="">Choose a patient</option>
                            <c:forEach var="patient" items="${patients}">
                                <option value="${patient.id()}">
                                    <c:out value="${patient.fullName()}" />
                                    &middot; <c:out value="${patient.email()}" />
                                </option>
                            </c:forEach>
                        </select>
                    </label>
                    <label>
                        Clinician
                        <select name="doctorId" required>
                            <option value="">Choose a doctor</option>
                            <c:forEach var="doctor" items="${doctors}">
                                <option value="${doctor.id()}">
                                    <c:out value="${doctor.fullName()}" />
                                    <c:if test="${not empty doctor.specialty()}">
                                        &middot; <c:out value="${doctor.specialty()}" />
                                    </c:if>
                                </option>
                            </c:forEach>
                        </select>
                    </label>
                    <label>
                        Date and time
                        <input type="datetime-local" name="appointmentAt"
                               min="<c:out value='${minimumAppointmentDateTime}' />" required>
                    </label>
                    <label class="field-wide">
                        Reason for visit
                        <textarea name="reason" required maxlength="500"
                                  placeholder="Briefly describe the reason for this visit"></textarea>
                    </label>
                    <div class="form-actions field-wide">
                        <button class="button button-primary" type="submit">
                            Schedule appointment <span aria-hidden="true">&#8594;</span>
                        </button>
                    </div>
                </form>
            </c:otherwise>
        </c:choose>
    </section>
</c:if>

<c:if test="${user.role() == 'PATIENT' || user.role() == 'ADMIN'}">
    <section class="panel">
        <div class="panel-heading">
            <div>
                <h2>Published doctor availability</h2>
                <p>Choose an appointment time within one of these open windows.</p>
            </div>
        </div>
        <c:choose>
            <c:when test="${empty availability}">
                <div class="empty-state">
                    <strong>No open times yet</strong>
                    Doctors will appear here after publishing availability.
                </div>
            </c:when>
            <c:otherwise>
                <div class="table-wrap">
                    <table class="data-table">
                        <thead>
                            <tr><th>Clinician</th><th>Available from</th><th>Available until</th></tr>
                        </thead>
                        <tbody>
                            <c:forEach var="block" items="${availability}">
                                <tr>
                                    <td>
                                        <strong><c:out value="${block.doctorName()}" /></strong>
                                        <c:if test="${not empty block.specialty()}">
                                            <small><c:out value="${block.specialty()}" /></small>
                                        </c:if>
                                    </td>
                                    <td>
                                        <strong><c:out value="${block.availableFrom().toLocalDate()}" /></strong>
                                        <small><c:out value="${block.availableFrom().toLocalTime()}" /></small>
                                    </td>
                                    <td>
                                        <strong><c:out value="${block.availableTo().toLocalDate()}" /></strong>
                                        <small><c:out value="${block.availableTo().toLocalTime()}" /></small>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
</c:if>

<section class="panel">
    <div class="panel-heading">
        <div>
            <h2>Appointment list</h2>
            <p>
                <c:choose>
                    <c:when test="${user.role() == 'PATIENT'}">Your scheduled visits</c:when>
                    <c:when test="${user.role() == 'DOCTOR'}">Visits assigned to you</c:when>
                    <c:otherwise>Clinic-wide appointment schedule</c:otherwise>
                </c:choose>
            </p>
        </div>
    </div>
    <c:choose>
        <c:when test="${empty appointments}">
            <div class="empty-state">
                <strong>Nothing scheduled yet</strong>
                Appointments will appear here as soon as they are requested.
            </div>
        </c:when>
        <c:otherwise>
            <div class="table-wrap">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Date &amp; time</th>
                            <th>Patient</th>
                            <th>Clinician</th>
                            <th>Reason</th>
                            <th>Status</th>
                            <th>Update</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="appointment" items="${appointments}">
                            <tr>
                                <td>
                                    <strong><c:out value="${appointment.appointmentAt().toLocalDate()}" /></strong>
                                    <small><c:out value="${appointment.appointmentAt().toLocalTime()}" /></small>
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
                                    <c:if test="${user.role() == 'DOCTOR' || user.role() == 'ADMIN'}">
                                        <form method="post" action="${ctx}/actions/appointments/status"
                                              class="inline-form">
                                            <input type="hidden" name="csrfToken"
                                                   value="<c:out value='${sessionScope.csrfToken}' />">
                                            <input type="hidden" name="returnTo" value="/appointments">
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
                                    </c:if>
                                    <c:if test="${user.role() == 'PATIENT'
                                            && appointment.status() != 'CANCELLED'
                                            && appointment.status() != 'COMPLETED'}">
                                        <form method="post" action="${ctx}/actions/appointments/status">
                                            <input type="hidden" name="csrfToken"
                                                   value="<c:out value='${sessionScope.csrfToken}' />">
                                            <input type="hidden" name="returnTo" value="/appointments">
                                            <input type="hidden" name="appointmentId"
                                                   value="${appointment.id()}">
                                            <input type="hidden" name="status" value="CANCELLED">
                                            <button class="button button-danger button-small" type="submit">
                                                Cancel
                                            </button>
                                        </form>
                                    </c:if>
                                    <c:if test="${appointment.status() == 'PENDING'
                                            || appointment.status() == 'CONFIRMED'}">
                                        <details class="appointment-reschedule">
                                            <summary class="button button-secondary button-small">
                                                Reschedule
                                            </summary>
                                            <form class="form-stack" method="post"
                                                  action="${ctx}/actions/appointments/reschedule">
                                                <input type="hidden" name="csrfToken"
                                                       value="<c:out value='${sessionScope.csrfToken}' />">
                                                <input type="hidden" name="returnTo" value="/appointments">
                                                <input type="hidden" name="appointmentId"
                                                       value="${appointment.id()}">
                                                <label>
                                                    New date and time
                                                    <input type="datetime-local" name="appointmentAt"
                                                           min="<c:out value='${minimumAppointmentDateTime}' />"
                                                           required>
                                                </label>
                                                <button class="button button-primary button-small" type="submit">
                                                    Save new time
                                                </button>
                                            </form>
                                        </details>
                                    </c:if>
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
