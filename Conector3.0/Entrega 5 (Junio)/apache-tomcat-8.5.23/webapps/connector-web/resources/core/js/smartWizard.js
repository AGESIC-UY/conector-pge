/**
 * Created by abrusco on 13/12/17.
 */

function initializeSmartWizard() {
    // Step show event
    $("#smartwizard").on("showStep", function (e, anchorObject, stepNumber, stepDirection, stepPosition) {
        //alert("You are on step "+stepNumber+" now");
        if (stepPosition === 'first') {
            $("#prev-btn").addClass('disabled');
        } else if (stepPosition === 'final') {
            $("#next-btn").addClass('disabled');
        } else {
            $("#prev-btn").removeClass('disabled');
            $("#next-btn").removeClass('disabled');
        }
    });

    // Smart Wizard
    $('#smartwizard').smartWizard({
        selected: 0,
        theme: 'dots',
        transitionEffect: 'fade',
        showStepURLhash: false,
        lang: {
            next: 'Siguiente',
            previous: 'Anterior',
        },
        toolbarSettings: {
            toolbarPosition: 'none',
            toolbarButtonPosition: 'center',
            //toolbarExtraButtons: [btnFinish]
        }
    });
}