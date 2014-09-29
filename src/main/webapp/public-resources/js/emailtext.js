$( document ).ready(function() {
    $( "#email" ).click(function( event ) {
        var email = $("#playerEmail").text();
        var url = '/toc/json/admin/email?email=' + email;
        $.getJSON(url).done(function(data) {
            $("#result").html(data.result);
        });
    });
    $( "#text" ).click(function( event ) {
        var email = $("#playerCell").text() + $("#playerCarrier").text();
        var url = '/toc/json/admin/text?email=' + email;
        $.getJSON(url).done(function(data) {
            $("#result").html(data.result);
        });
    });
    $( "#freeformtext" ).click(function( event ) {
        var email = $("#phone").val() + $("#carrier").val();
        var url = '/toc/json/admin/text?email=' + email;
        $.getJSON(url).done(function(data) {
            $("#freeformresult").html(data.result);
        });
    });
    $( "#playerId" ).click(function() {
        var val = $(this).val();
        var url = '/toc/json/admin/player/' + val;
        $.getJSON(url).done(function(data) {
            $("#playerEmail").html(data.email);
            $("#playerCell").html(data.phone);
            $("#playerCarrier").html(data.cellCarrier);
        });
    });
});

