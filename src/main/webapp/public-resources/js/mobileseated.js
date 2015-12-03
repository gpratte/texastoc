$( document ).ready(function() {
    $( "#selectAll" ).click(function( event ) {
    	if ($(this).prop('checked')) {
    		$("#selectAllLabel").text("Deselect all");
        	$('.notyetseated').each(function() {
        		$(this).prop('checked', true).checkboxradio('refresh');
        	});
    	} else {
    		$("#selectAllLabel").text("Select all");
        	$('.notyetseated').each(function() {
        		$(this).prop('checked', false).checkboxradio('refresh');
        	});
    	}
    });
    
});
