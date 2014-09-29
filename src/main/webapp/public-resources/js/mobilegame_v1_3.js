$( document ).ready(function() {

	$("#randomform").submit(function(e) {
		var postData = $(this).serializeArray();
		var formURL = $(this).attr("action");
		$.ajax({
			url : formURL,
			type: "POST",
			data : postData,
			success:function(data, textStatus, jqXHR) 
			{
			    $("#two").html(data);
			    $("#two").find(".send").click(function( event ) {
			    	jQuery.post("http://www.texastoc.com/toc/game/message/" + $(this).attr('id'));
			    	//jQuery.post("http://192.168.1.118:8080/toc/game/message/" + $(this).attr('id'));
			    	$(this).attr("disabled", "disabled");
			    	$(this).prop('value', 'Sent');
			    });
			},
			error: function(jqXHR, textStatus, errorThrown) 
			{
			    //if fails      
			}
		});
		e.preventDefault(); //STOP default action
		e.unbind(); //unbind. to stop multiple form submit.		
		return false; // prevent normal submit
	});

});
