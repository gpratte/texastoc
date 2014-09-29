$( document ).on("pageinit", "#mobilesummary", function() {

    $( "#home" ).click(function( event ) {
    	location.href = '/toc/mobile';
    });
    $( "#gameId" ).change(function( event ) {
    	$( "#formId" ).submit();
    });
});
