<%@ include file="shell-top.jspf" %>
<div class="page-heading">
    <div>
        <div class="eyebrow"><span class="eyebrow-dot"></span>WORKSPACE PREFERENCES</div>
        <h1>Clinic settings</h1>
        <p>Set the clinic identity and standard appointment length.</p>
    </div>
</div>

<section class="panel">
    <div class="panel-heading">
        <div>
            <h2>System configuration</h2>
            <p>These settings apply to the clinic workspace.</p>
        </div>
    </div>

    <form class="form-stack settings-form" method="post" action="${ctx}/actions/settings/save">
        <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />">
        <input type="hidden" name="returnTo" value="/admin/settings">

        <label>
            Clinic name
            <input name="clinicName" required maxlength="120"
                   value="<c:out value='${settings.clinic_name}' />">
        </label>
        <label>
            Public contact email
            <input type="email" name="clinicEmail" maxlength="190"
                   value="<c:out value='${settings.clinic_email}' />">
        </label>
        <label>
            Public phone number
            <input type="tel" name="clinicPhone" maxlength="40"
                   value="<c:out value='${settings.clinic_phone}' />">
        </label>
        <label>
            Clinic address
            <input name="clinicAddress" maxlength="250"
                   value="<c:out value='${settings.clinic_address}' />">
        </label>
        <label>
            Appointment duration
            <select name="appointmentDuration">
                <option value="10" ${settings.appointment_duration == '10' ? 'selected' : ''}>10 minutes</option>
                <option value="15" ${settings.appointment_duration == '15' ? 'selected' : ''}>15 minutes</option>
                <option value="20" ${settings.appointment_duration == '20' ? 'selected' : ''}>20 minutes</option>
                <option value="30" ${settings.appointment_duration == '30' ? 'selected' : ''}>30 minutes</option>
                <option value="45" ${settings.appointment_duration == '45' ? 'selected' : ''}>45 minutes</option>
                <option value="60" ${settings.appointment_duration == '60' ? 'selected' : ''}>60 minutes</option>
                <option value="90" ${settings.appointment_duration == '90' ? 'selected' : ''}>90 minutes</option>
                <option value="120" ${settings.appointment_duration == '120' ? 'selected' : ''}>120 minutes</option>
                <option value="180" ${settings.appointment_duration == '180' ? 'selected' : ''}>180 minutes</option>
            </select>
        </label>

        <div class="form-actions">
            <button class="button button-primary" type="submit">
                Save settings <span aria-hidden="true">&#8594;</span>
            </button>
        </div>
    </form>
</section>

<%@ include file="shell-bottom.jspf" %>