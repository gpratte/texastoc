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

    $( "#gameDateText" ).click(function( event ) {
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
    
});
