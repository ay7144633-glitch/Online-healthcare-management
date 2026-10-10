<%@ include file="public-head.jspf" %>
<main>
    <section class="home-hero">
        <div class="home-hero-copy">
            <div class="eyebrow"><span class="eyebrow-dot"></span>CAREPOINT HEALTHCARE</div>
            <h1>Carepoint</h1>
            <p class="home-subtitle">
                Keep appointments, care teams and health information connected from the first
                request through follow-up.
            </p>
            <div class="home-hero-actions">
                <a class="button button-primary home-primary-action" href="${ctx}/register">
                    Create patient account <span aria-hidden="true">&#8594;</span>
                </a>
                <a class="home-login-action" href="${ctx}/login">I already have an account</a>
            </div>
            <div class="home-promise">
                <span class="promise-mark">&#10003;</span>
                <span>Built around patients, clinicians and clinic teams.</span>
            </div>
        </div>

        <div class="home-hero-signal" aria-hidden="true">
            <div class="signal-meta">
                <span class="signal-mark">C</span>
                <span>CAREPOINT <small>CONNECTED CARE</small></span>
                <span class="signal-live"><i></i> LIVE</span>
            </div>
            <div class="heartbeat-window">
                <svg class="heartbeat-wave-track" viewBox="0 0 1000 100"
                     preserveAspectRatio="none" aria-hidden="true">
                    <path class="heartbeat-wave" pathLength="1000"
                          d="M0 50 H100 L140 47 L180 56 L220 20 L260 80 L300 42 L340 56 L380 48
                             H420 L460 50 L500 26 L540 74 L580 48 L620 52 H660 L700 20
                             L740 80 L780 42 L820 56 L860 48 H900 L940 52 L970 43 L1000 50" />
                </svg>
                <span class="heartbeat-grid-line heartbeat-grid-line-one"></span>
                <span class="heartbeat-grid-line heartbeat-grid-line-two"></span>
            </div>
            <div class="signal-rhythm">
                <span>ECG SIGNAL</span>
                <strong>REGULAR RHYTHM</strong>
                <span class="signal-rhythm-value">72 BPM</span>
            </div>
            <div class="signal-footer">
                <span>APPOINTMENTS</span>
                <i></i>
                <span>CARE TEAM</span>
                <i></i>
                <span>FOLLOW-UP</span>
            </div>
        </div>
    </section>

    <section class="home-overview" aria-labelledby="overview-heading">
        <div class="home-section-heading">
            <div class="eyebrow"><span class="eyebrow-dot"></span>ONE PLACE FOR EVERY ROLE</div>
            <h2 id="overview-heading">The details of care, in sync.</h2>
            <p>Choose your Carepoint experience to see how it supports your day.</p>
        </div>
        <div class="home-overview-links">
            <a href="${ctx}/for-patients">
                <span>01 &nbsp; PATIENTS</span>
                <strong>Find and follow your care</strong>
                <small>Appointments, shared records and your profile.</small>
                <b aria-hidden="true">&#8594;</b>
            </a>
            <a href="${ctx}/for-doctors">
                <span>02 &nbsp; DOCTORS</span>
                <strong>Coordinate your practice</strong>
                <small>Availability, patient visits and clinical notes.</small>
                <b aria-hidden="true">&#8594;</b>
            </a>
            <a href="${ctx}/for-clinics">
                <span>03 &nbsp; CLINICS</span>
                <strong>Manage clinic operations</strong>
                <small>Accounts, settings and appointment activity.</small>
                <b aria-hidden="true">&#8594;</b>
            </a>
        </div>
        <p class="home-how-link">
            <a href="${ctx}/how-it-works">
                See how Carepoint works <span aria-hidden="true">&#8594;</span>
            </a>
        </p>
    </section>

    <section class="home-how" aria-labelledby="home-journey-heading">
        <div class="home-section-heading">
            <div class="eyebrow"><span class="eyebrow-dot"></span>FROM REQUEST TO FOLLOW-UP</div>
            <h2 id="home-journey-heading">One clear care journey.</h2>
            <p>
                Each step moves to the person responsible, keeping appointments and visit
                information together.
            </p>
        </div>
        <ol class="home-steps">
            <li>
                <span class="home-step-number">01</span>
                <div>
                    <h3>Choose an open time</h3>
                    <p>Doctors publish availability. Patients request a visit with a preferred time and reason.</p>
                </div>
            </li>
            <li>
                <span class="home-step-number">02</span>
                <div>
                    <h3>Coordinate the appointment</h3>
                    <p>Doctors review requests and confirm, reschedule or update the appointment.</p>
                </div>
            </li>
            <li>
                <span class="home-step-number">03</span>
                <div>
                    <h3>Keep the follow-up connected</h3>
                    <p>Doctors add visit notes; patients can review shared records and maintain personal history.</p>
                </div>
            </li>
        </ol>
        <a class="home-section-link" href="${ctx}/how-it-works">
            Explore the complete workflow <span aria-hidden="true">&#8594;</span>
        </a>
    </section>
</main>
<%@ include file="public-footer.jspf" %>
