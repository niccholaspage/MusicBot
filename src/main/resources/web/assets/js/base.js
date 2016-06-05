window.addEventListener("load", function () {
    var currentPage = window.location.pathname;

    $("a[href='" + currentPage + "']").addClass('is-active');
});