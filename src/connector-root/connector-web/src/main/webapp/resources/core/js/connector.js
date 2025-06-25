/**
 * Created by abrusco on 13/12/17.
 */

function toggleEnableUserCredentials() {
    toggleDiv('enter_token_credentials_div');
    toggleRequiredUserCredentials();
}
function toggleEnableConfigurations() {
    toggleDiv('enable_local_configuration_div');
    toggleRequiredConfigurations();
}
function toggleEnableSTSLocalUrl() {
    toggleDiv('enable_sts_local_url_div');
    toggleRequiredSTSLocal();
}
function toggleEnableLocalPolicyName() {
    toggleDiv('enable_local_policy_name_div');
    toggleRequiredLocalPolicyName();
}
function toggleEnableLocalExpirationNotification() {
    toggleDiv('enable_local_expiration_notice_days_div');
    toggleRequiredLocalExpirationNoticeDays();
}
function toggleEnableLocalTimeout() {
    toggleDiv('enable_local_timeout_div');
    toggleRequiredLocalServiceTimeOut();
}
function toggleRequiredUserCredentials() {
    toggleRequiredNotRequired('userNameTokenName');
    toggleRequiredNotRequired('userNameTokenPassword');
}

function toggleRequiredSTSLocal() {
    toggleRequiredNotRequired('stsLocalUrl');
}
function toggleRequiredLocalPolicyName() {
    toggleRequiredNotRequired('policyName');
}
function toggleRequiredLocalExpirationNoticeDays() {
    toggleRequiredNotRequired('localExpirationNoticeDays');
}
function toggleRequiredLocalServiceTimeOut() {
    toggleRequiredNotRequired('localServiceTimeOut');
}
function preSubmit() {
    $("#type").prop('disabled', false);
}
function toggleDiv(elementId, childrenInputType = "text") {
    $(document).ready(function () {
        const element = $("#" + elementId);
        const value = childrenInputType === "text" ? "" : 0
        $(element).children().find("input").val(value);
        element.toggle();
    });
}
function toggleRequiredNotRequired(elementId) {
    const element = $("#" + elementId);
    if ($(element).prop('required')) {
        $(element).prop('required', false)
    } else {
        $(element).prop('required', true)
    }
}
function removeWhitespaces(object) {
    $(document).ready(function () {
        const element = $("#" + object.id);
        element.val($.trim(element.val()));
    });
}