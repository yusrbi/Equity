$(document).ready(function () {
    $('div.go button').each(function (index) {
        $(this).ajaxStart(function () {
            $("#ajaxLoad").show();
            $(this).prop('disabled', true);
        });
        $(this).ajaxStop(function () {
            $("#ajaxLoad").hide();
            $(this).prop('disabled', false);
        });
    });
});