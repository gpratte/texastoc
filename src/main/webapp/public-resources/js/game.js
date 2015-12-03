$( document ).ready(function() {
    $( "#submit" ).click(function( event ) {
    	var pattern = new RegExp("\\d{2}/\\d{2}/\\d{4}");
       	var alerted = false;
        $(".date").each(function(){
        	if (pattern.test($(this).val().trim()) == false) {
        		if (alerted == false) {
	        		alert("Date values must be dd/mm/yyyy");
	        		alerted = true;
			        event.preventDefault();
        		}
        	}
        });
    });
    
    $( "#gameDateImg" ).click(function( event ) {
        var gameDate = $("#gameDateText").val();
        $("#gameDatePicker").datepicker("setDate", new Date(gameDate));
        $("#gameDatePicker").datepicker().toggle();
    });

   	$("#gameDatePicker").datepicker({
    	onSelect: function(dateText, inst) {
       		$("#gameDateText").val(dateText); 
       		$("#gameDatePicker").toggle();
    	}
   	});

    $("#gameDatePicker").datepicker().hide();
    
    $(".send").click(function( event ) {
    	jQuery.post("https://www.texastoc.com/toc/game/message/" + $(this).attr('id'));
    	$(this).attr("disabled", "disabled");
    	$(this).prop('value', 'Sent');
    });

    $("#delete").click(function( event ) {
        if (confirm('Are you sure you want to delete?')) {
        	return true;
        }
    	window.location.href='/toc/admin/home';
        return false;
    });


});
