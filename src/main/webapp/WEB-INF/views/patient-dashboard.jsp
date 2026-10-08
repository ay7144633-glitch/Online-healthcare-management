<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="shell-top.jspf" %>
<div class="page-heading">
    <div><div class="eyebrow"><span class="eyebrow-dot"></span>YOUR PERSONAL CARE</div><h1>Good to see you, <c:out value="${user.fullName().split(' ')[0]}" />.</h1><p>Your appointments and health history, ready when you need them.</p></div>
    <a class="button button-primary" href="${ctx}/appointments">Book an appointment <span aria-hidden="true">&#8594;</span></a>
</div>
<div class="metrics-grid">
    <div class="metric"><span class="metric-label">Appointments</span><strong class="metric-value"><c:out value="${fn:length(appointments)}" /></strong><span class="metric-note">Your scheduled care</span></div>
    <div class="metric"><span class="metric-label">Medical records</span><strong class="metric-value"><c:out value="${fn:length(records)}" /></strong><span class="metric-note">Visit notes shared by your doctors</span></div>
    <div class="metric"><span class="metric-label">Available doctors</span><strong class="metric-value"><c:out value="${fn:length(doctors)}" /></strong><span class="metric-note">Choose a clinician for your next visit</span></div>
</div>
<div class="content-grid">
    <section class="panel"><div class="panel-heading"><div><h2>Appointment history</h2><p>Your upcoming and previous visits</p></div><a class="text-link" href="${ctx}/appointments">View all <span aria-hidden="true">&#8594;</span></a></div>
        <c:choose><c:when test="${empty appointments}"><div class="empty-state"><strong>No appointments yet</strong>Book a visit when you are ready to see a doctor.</div></c:when><c:otherwise><div class="table-wrap"><table class="data-table"><thead><tr><th>Date &amp; time</th><th>Doctor</th><th>Reason</th><th>Status</th></tr></thead><tbody><c:forEach var="appointment" items="${appointments}" begin="0" end="4"><tr><td><strong><c:out value="${appointment.appointmentAt().toLocalDate()}" /></strong><small><c:out value="${appointment.appointmentAt().toLocalTime()}" /></small></td><td><c:out value="${appointment.doctorName()}" /></td><td><c:out value="${appointment.reason()}" /></td><td><span class="status status-${appointment.status().toLowerCase()}"><c:out value="${appointment.status().toLowerCase()}" /></span></td></tr></c:forEach></tbody></table></div></c:otherwise></c:choose>
    </section>
    <section class="panel"><div class="panel-heading"><div><h2>Your medical history</h2><p>Notes shared by your care team</p></div><a class="text-link" href="${ctx}/records">Open records <span aria-hidden="true">&#8594;</span></a></div>
        <c:choose><c:when test="${empty records}"><div class="empty-state"><strong>No records yet</strong>Visit notes will appear here after your appointment.</div></c:when><c:otherwise><div class="dashboard-record-list"><c:forEach var="record" items="${records}" begin="0" end="3"><article><span class="record-date"><c:out value="${record.createdAt().toLocalDate()}" /></span><strong><c:out value="${record.diagnosis()}" /></strong><span>Dr. <c:out value="${record.doctorName()}" /></span></article></c:forEach></div></c:otherwise></c:choose>
    </section>
</div>
<section class="panel patient-tools"><div class="panel-heading"><div><h2>Your care tools</h2><p>Keep your information and follow-up in order</p></div></div><div class="dashboard-action-list"><a href="${ctx}/patient/profile"><span class="action-index">01</span><span><strong>Profile management</strong><small>Update your contact details.</small></span><span class="action-arrow" aria-hidden="true">&#8594;</span></a><a href="${ctx}/patient/feedback"><span class="action-index">02</span><span><strong>Patient feedback</strong><small>Share feedback after a completed visit.</small></span><span class="action-arrow" aria-hidden="true">&#8594;</span></a></div></section>
<%@ include file="shell-bottom.jspf" %>