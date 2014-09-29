$( document ).ready(function() {
    $( "#submit" ).click(function( event ) {
    	var pattern = new RegExp("\\d{10}");
    	if ($("#phone").val().trim() != "") {
        	if (pattern.test($("#phone").val().trim()) == false) {
        		alert("Phone must be 10 digits all in a row");
    	        event.preventDefault();
        	}
    	}
    });
});

function deleteClicked() {
    if (confirm('Are you sure you want to delete?')) {
    	window.location.href='/toc/admin/player/delete/$player.id';
    }
    return false;
}
