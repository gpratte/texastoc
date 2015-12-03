$( document ).ready(function() {

    $( "#home" ).click(function( event ) {
    	location.href = '/toc/mobile';
    });
    $( "#gameId" ).change(function( event ) {
    	$( "#formId" ).submit();
    });
    $( "#deleteGameId" ).click(function( event ) {
    	location.href = '/toc/mobile/game/' + $("#gameId").val() + '?deleting=true';
    });

    $("#gamemoreless").hide();
    $("#gamemorelesstoggle").click(function( event ) {
        if ($(this).text() === "Show More") {
            $("#gamemoreless").show();
    		$(this).text("Show Less");
    	} else {
    	    $("#gamemoreless").hide();
        	$(this).text("Show More");
    	}
    });
    $("#seasonmoreless").hide();
    $("#seasonmorelesstoggle").click(function( event ) {
        if ($(this).text() === "Show More") {
            $("#seasonmoreless").show();
    		$(this).text("Show Less");
    	} else {
    	    $("#seasonmoreless").hide();
        	$(this).text("Show More");
    	}
    });
    $("#quartermoreless").hide();
    $("#quartermorelesstoggle").click(function( event ) {
        if ($(this).text() === "Show More") {
            $("#quartermoreless").show();
    		$(this).text("Show Less");
    	} else {
    	    $("#quartermoreless").hide();
        	$(this).text("Show More");
    	}
    });

});
