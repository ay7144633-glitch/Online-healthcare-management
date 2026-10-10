<%@ include file="shell-top.jspf" %>
<div class="page-heading">
    <div>
        <div class="eyebrow"><span class="eyebrow-dot"></span>PERSONAL HEALTH HISTORY</div>
        <h1>Medical records</h1>
        <p>Visit notes and diagnoses shared by your care team.</p>
    </div>
</div>

<c:if test="${user.role() == 'PATIENT'}">
    <section class="panel">
        <div class="panel-heading">
            <div>
                <h2>My health history</h2>
                <p>Add and maintain health details you want your care team to know.</p>
            </div>
        </div>

        <form class="form-grid" method="post" action="${ctx}/actions/patient-history/create">
            <input type="hidden" name="csrfToken"
                   value="<c:out value='${sessionScope.csrfToken}' />">
            <input type="hidden" name="returnTo" value="/records">
            <label>
                Entry title
                <input name="title" required maxlength="160"
                       placeholder="Allergies, medications, or prior conditions">
            </label>
            <label class="field-wide">
                Details
                <textarea name="details" required maxlength="5000"
                          placeholder="Add relevant dates, details, and context"></textarea>
            </label>
            <div class="form-actions field-wide">
                <button class="button button-primary" type="submit">Add health history</button>
            </div>
        </form>

        <c:choose>
            <c:when test="${empty patientHistory}">
                <div class="empty-state">
                    <strong>No personal entries yet</strong>
                    Use this section for details you maintain yourself.
                </div>
            </c:when>
            <c:otherwise>
                <div class="patient-history-list">
                    <c:forEach var="entry" items="${patientHistory}">
                        <article>
                            <div class="patient-history-entry">
                                <strong><c:out value="${entry.title()}" /></strong>
                                <small>Updated <c:out value="${entry.updatedAt().toLocalDate()}" /></small>
                                <p><c:out value="${entry.details()}" /></p>
                            </div>
                            <details>
                                <summary class="button button-secondary button-small">Edit</summary>
                                <form class="form-stack" method="post"
                                      action="${ctx}/actions/patient-history/update">
                                    <input type="hidden" name="csrfToken"
                                           value="<c:out value='${sessionScope.csrfToken}' />">
                                    <input type="hidden" name="returnTo" value="/records">
                                    <input type="hidden" name="entryId" value="${entry.id()}">
                                    <label>
                                        Entry title
                                        <input name="title" required maxlength="160"
                                               value="<c:out value='${entry.title()}' />">
                                    </label>
                                    <label>
                                        Details
                                        <textarea name="details" required maxlength="5000"><c:out value="${entry.details()}" /></textarea>
                                    </label>
                                    <button class="button button-primary button-small" type="submit">
                                        Save entry
                                    </button>
                                </form>
                            </details>
                            <form method="post" action="${ctx}/actions/patient-history/delete">
                                <input type="hidden" name="csrfToken"
                                       value="<c:out value='${sessionScope.csrfToken}' />">
                                <input type="hidden" name="returnTo" value="/records">
                                <input type="hidden" name="entryId" value="${entry.id()}">
                                <button class="button button-danger button-small" type="submit">Remove</button>
                            </form>
                        </article>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
</c:if>

<section class="panel">
    <div class="panel-heading">
        <div>
            <h2>Visit history</h2>
            <p>Private to your account and authorized care team.</p>
        </div>
    </div>

    <c:choose>
        <c:when test="${empty records}">
            <div class="empty-state">
                <strong>No records yet</strong>
                Notes from completed care visits will appear here.
            </div>
        </c:when>
        <c:otherwise>
            <div class="table-wrap">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Date</th>
                            <th>Clinician</th>
                            <th>Diagnosis</th>
                            <th>Visit notes</th>
                            <c:if test="${user.role() == 'DOCTOR'}"><th>Action</th></c:if>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="record" items="${records}">
                            <tr>
                                <td><c:out value="${record.createdAt().toLocalDate()}" /></td>
                                <td><c:out value="${record.doctorName()}" /></td>
                                <td><strong><c:out value="${record.diagnosis()}" /></strong></td>
                                <td class="record-note"><c:out value="${record.notes()}" /></td>
                                <c:if test="${user.role() == 'DOCTOR'}">
                                    <td>
                                        <details>
                                            <summary class="button button-secondary button-small">Edit note</summary>
                                            <form class="form-stack" method="post"
                                                  action="${ctx}/actions/records/update">
                                                <input type="hidden" name="csrfToken"
                                                       value="<c:out value='${sessionScope.csrfToken}' />">
                                                <input type="hidden" name="returnTo" value="/records">
                                                <input type="hidden" name="recordId" value="${record.id()}">
                                                <label>
                                                    Diagnosis
                                                    <input name="diagnosis" required maxlength="255"
                                                           value="<c:out value='${record.diagnosis()}' />">
                                                </label>
                                                <label>
                                                    Visit notes
                                                    <textarea name="notes" required maxlength="10000"><c:out value="${record.notes()}" /></textarea>
                                                </label>
                                                <button class="button button-primary button-small" type="submit">
                                                    Save changes
                                                </button>
                                            </form>
                                        </details>
                                    </td>
                                </c:if>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</section>

<%@ include file="shell-bottom.jspf" %>
