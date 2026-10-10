<%@ include file="shell-top.jspf" %>
<div class="page-heading">
    <div>
        <div class="eyebrow"><span class="eyebrow-dot"></span>HELP US IMPROVE</div>
        <h1>Share feedback</h1>
        <p>Your feedback helps the clinic make every visit a little better.</p>
    </div>
</div>

<section class="panel">
    <div class="panel-heading">
        <div>
            <h2>Rate a completed visit</h2>
            <p>Feedback is available for doctors you've had a completed appointment with.</p>
        </div>
    </div>
    <c:choose>
        <c:when test="${empty doctors}">
            <div class="empty-state">
                <strong>No clinicians available</strong>
                Feedback options will appear when clinicians are added.
            </div>
        </c:when>
        <c:otherwise>
            <form class="form-stack" method="post" action="${ctx}/actions/feedback/create">
                <input type="hidden" name="csrfToken"
                       value="<c:out value='${sessionScope.csrfToken}' />">
                <input type="hidden" name="returnTo" value="/patient/feedback">
                <label>
                    Doctor
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
                    Rating
                    <select name="rating" required>
                        <option value="">Choose a rating</option>
                        <option value="5">5 - Excellent</option>
                        <option value="4">4 - Very good</option>
                        <option value="3">3 - Good</option>
                        <option value="2">2 - Could be better</option>
                        <option value="1">1 - Poor</option>
                    </select>
                </label>
                <label>
                    Comments
                    <textarea name="comments" maxlength="1000"
                              placeholder="Tell us about your experience"></textarea>
                </label>
                <div class="form-actions">
                    <button class="button button-primary" type="submit">
                        Send feedback <span aria-hidden="true">&#8594;</span>
                    </button>
                </div>
            </form>
        </c:otherwise>
    </c:choose>
</section>

<%@ include file="shell-bottom.jspf" %>
