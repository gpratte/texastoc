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
    
    $( "#startDateImg" ).click(function( event ) {
        var startDate = $("#startDateText").val();
        $("#startDatePicker").datepicker("setDate", new Date(startDate));
        $("#endDatePicker").datepicker().hide();
        $("#startDatePicker").datepicker().toggle();
    });

   	$("#startDatePicker").datepicker({
    	onSelect: function(dateText, inst) {
       		$("#startDateText").val(dateText); 
       		$("#startDatePicker").toggle();
    	}
   	});

    $( "#endDateImg" ).click(function( event ) {
        var endDate = $("#endDateText").val();
        $("#endDatePicker").datepicker("setDate", new Date(endDate));
        $("#startDatePicker").datepicker().hide();
        $("#endDatePicker").datepicker().toggle();
    });

   	$("#endDatePicker").datepicker({
    	onSelect: function(dateText, inst) {
       		$("#endDateText").val(dateText); 
       		$("#endDatePicker").toggle();
    	}
   	});

    $("#startDatePicker").datepicker().hide();
    $("#endDatePicker").datepicker().hide();
    
    for (var i=1; i<5; i++) {
    	var quarterly = "#quarterly" + i;
    	var quarterlyGames = "#quarterlyGames" + i;
    	
    	$(quarterly).hide();
        $(quarterlyGames).click(function( event ) {
            if ($(this).text() === "View") {
            	$(this).siblings("[id^=quarterly]").show();
        		$(this).text("Hide");
        	} else {
            	$(this).siblings("[id^=quarterly]").hide();
            	$(this).text("View");
        	}
        });

    	var quarterlyLeaders = "#quarterlyLeaders" + i;
    	var quarterlyLeadersToggle = "#quarterlyLeadersToggle" + i;
    	
    	$(quarterlyLeaders).hide();
        $(quarterlyLeadersToggle).click(function( event ) {
            if ($(this).text() === "View") {
            	$(this).siblings("[id^=quarterlyLeaders]").show();
        		$(this).text("Hide");
        	} else {
            	$(this).siblings("[id^=quarterlyLeaders]").hide();
            	$(this).text("View");
        	}
        });

    }
});
