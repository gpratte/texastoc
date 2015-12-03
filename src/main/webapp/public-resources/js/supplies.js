$( document ).ready(function() {
    $( ".deleteme" ).click(function( event ) {
        if (confirm('Are you sure you want to delete?')) {
            return true;
        }
        return false;
    });

});
