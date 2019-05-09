/**
 * Created by abrusco on 13/12/17.
 */

function toggleEnableUserCredentials() {
    toggleDiv('enter_token_credentials_div');
    toggleRequiredUserCredentials();
};
function toggleEnableConfigurations() {
    toggleDiv('enable_local_configuration_div');
    toggleRequiredConfigurations();
};
function toggleEnableSTSLocalUrl() {
    toggleDiv('enable_sts_local_url_div');
    toggleRequiredSTSLocal();
};
function toggleRequiredUserCredentials() {
    toggleRequiredNotRequired('userNameTokenName');
    toggleRequiredNotRequired('userNameTokenPassword');
}

function toggleRequiredSTSLocal() {
    toggleRequiredNotRequired('stsLocalUrl');
}
function preSubmit() {
    $("#type").prop('disabled', false);
};
function toggleDiv(elementId) {
    $(document).ready(function () {
        var element = $("#" + elementId);
        $(element).children().find("input").val("");
        element.toggle();
    });
};
function toggleRequiredNotRequired(elementId) {
    var element = $("#" + elementId);
    if ($(element).prop('required')) {
        $(element).prop('required', false)
    } else {
        $(element).prop('required', true)
    }
};
function removeWhitespaces(object) {
    $(document).ready(function () {
        var element = $("#" + object.id);
        element.val($.trim(element.val()));
    });
};