$(document).ready(function() {
    poll();
    
    var url = "https://www.texastoc.com";
    //var url = "http://192.168.1.31:8080";

    ion.sound({
        sounds : [ {
            name : "two_minutes"
        }, {
            name : "tocbuzzer"
        } ],
        path : "/toc/resources/sounds/",
        preload : true,
        volume : 1.0
    });

    setInterval(function() {
        poll();
    }, 900);

    $("#prev").click(function(event) {
    	$.post( url + "/toc/mobile/clock", 
    			{ gameId: $("#gameId").val(), activity: 'prev', round: $("#round").val() });
    	poll();
    });

    $("#play").click(function(event) {
        if ($("#play").text() === 'Pause') {
        	$.post( url + "/toc/mobile/clock", 
        			{ gameId: $("#gameId").val(), activity: 'pause' });
        	poll();
        }
        if ($("#play").text() === 'Play') {
        	$.post( url + "/toc/mobile/clock", 
        			{ gameId: $("#gameId").val(), 
        		      activity: 'play', 
        			  round: $("#round").val(), 
        			  minutes: $("#remainingMinutes").text(), 
        			  seconds: $("#remainingSeconds").text() });
        	poll();
        }
    });
    
	$("#round").change(function() {
    	$.post( url + "/toc/mobile/clock", 
    			{ gameId: $("#gameId").val(), 
    		      activity: 'round', 
    			  round: $("#round").val() });
    	poll();
	});

    $("#next").click(function(event) {
    	$.post( url + "/toc/mobile/clock", 
    			{ gameId: $("#gameId").val(), activity: 'next', round: $("#round").val() });
    	poll();
    });

    $("#reset").click(function(event) {
    	$.post( url + "/toc/mobile/clock", 
    			{ gameId: $("#gameId").val(), activity: 'reset' });
    	poll(true);
    });

    $("#dblUpMinutes").click(function(event) {
    	if ($("#round").val().match("^Break")) {
            $("#remainingMinutes").text("5");
    	} else {
            $("#remainingMinutes").text("20");
    	}
        $("#remainingSeconds").text("0");
    });
    $("#upMinutes").click(function(event) {
        var minutes = parseInt($("#remainingMinutes").text());
        if (minutes < 20) {
            $("#remainingMinutes").text(minutes + 1);
        }
    });
    $("#dblUpSeconds").click(function(event) {
        $("#remainingSeconds").text("59");
    });
    $("#upSeconds").click(function(event) {
        var seconds = parseInt($("#remainingSeconds").text());
        if (seconds < 59) {
            $("#remainingSeconds").text(seconds + 1);
        }
    });
    $("#downMinutes").click(function(event) {
        var minutes = parseInt($("#remainingMinutes").text());
        if (minutes > 0) {
            $("#remainingMinutes").text(minutes - 1);
        }
    });
    $("#dblDownMinutes").click(function(event) {
        $("#remainingMinutes").text("0");
    });
    $("#downSeconds").click(function(event) {
        var seconds = parseInt($("#remainingSeconds").text());
        if (seconds > 0) {
            $("#remainingSeconds").text(seconds - 1);
        }
    });
    $("#dblDownSeconds").click(function(event) {
        $("#remainingSeconds").text("0");
    });
});

function poll(reset) {
    $.getJSON('/toc/clock/' + $("#gameId").val()).done(function(data) {
        if (data.running === true) {
            var currentRound = $("#currentRound").text();
            var updateRound = data.currentLevel.round;
            if (currentRound != updateRound) {
                ion.sound.play("tocbuzzer");
            }

            var currentMinutes = $("#remainingMinutes").text();
            var updateMinutes = data.remainingMinutes;
            if (currentMinutes == 2 && updateMinutes == 1) {
                ion.sound.play("two_minutes");
            }

            $("#currentRound").show();
            $("#currentRound").html(data.currentLevel.round);
            $("#selectRound").hide();
            $("#dblUpMinutes").hide();
            $("#upMinutes").hide();
            $("#dblUpSeconds").hide();
            $("#upSeconds").hide();
            $("#downMinutes").hide();
            $("#dblDownMinutes").hide();
            $("#downSeconds").hide();
            $("#dblDownSeconds").hide();
            $("#resetClock").hide();
            $("#leftarrow").hide();
            $("#rightarrow").hide();
            $("#play").html('Pause');
        } else {
            $("#currentRound").hide();
            $("#selectRound").show();
        	$('#round').val(data.currentLevel.round);
        	$('#round').selectmenu('refresh', true);
            $("#dblUpMinutes").show();
            $("#upMinutes").show();
            $("#dblUpSeconds").show();
            $("#upSeconds").show();
            $("#dblDownMinutes").show();
            $("#downMinutes").show();
            $("#dblDownSeconds").show();
            $("#downSeconds").show();
            $("#resetClock").show();
            $("#leftarrow").show();
            $("#rightarrow").show();
            $("#play").html('Play');
        }

        if (data.running === true || reset) {
            $("#remainingMinutes").html(data.remainingMinutes);
            $("#remainingSeconds").html(data.remainingSeconds);
        }

        $("#currentSmallBlind").html(data.currentLevel.smallBlind);
        $("#currentBigBlind").html(data.currentLevel.bigBlind);
        $("#currentAnte").html(data.currentLevel.ante);

        $("#nextRound").html(data.nextLevel.round);
        $("#nextSmallBlind").html(data.nextLevel.smallBlind);
        $("#nextBigBlind").html(data.nextLevel.bigBlind);
        $("#nextAnte").html(data.nextLevel.ante);
    });
}
