$(document).ready(function() {
    poll();

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
//        $("#activity").val('prev');
//        $("#clockForm").submit();
    	//$.post( "https://www.texastoc.com/toc/mobile/clock", 
    	$.post( "http://192.168.1.31:8080/toc/mobile/clock", 
    			{ gameId: $("#gameId").val(), activity: 'prev' });
    });

    $("#play").click(function(event) {
        if ($("#play").val() === 'Pause') {
            $("#activity").val('pause');
        }
        if ($("#play").val() === 'Play') {
            $("#activity").val('play');
            var minutes = $("#remainingMinutes").text();
            $("#minutes").val(minutes);
            var seconds = $("#remainingSeconds").text();
            $("#seconds").val(seconds);
        }
        $("#clockForm").submit();
    });

    $("#next").click(function(event) {
//        $("#activity").val('next');
//        $("#clockForm").submit();
    	//$.post( "https://www.texastoc.com/toc/mobile/clock", 
    	$.post( "http://192.168.1.31:8080/toc/mobile/clock", 
    			{ gameId: $("#gameId").val(), activity: 'next' });
    });
    $("#reset").click(function(event) {
        $("#activity").val('reset');
        $("#clockForm").submit();
    });

    $("#upMinutes").click(function(event) {
        var minutes = parseInt($("#remainingMinutes").text());
        if (minutes < 20) {
            $("#remainingMinutes").text(minutes + 1);
        }
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
    $("#downSeconds").click(function(event) {
        var seconds = parseInt($("#remainingSeconds").text());
        if (seconds > 0) {
            $("#remainingSeconds").text(seconds - 1);
        }
    });
});

function poll() {
    //$.getJSON('https://www.texastoc.com/toc/clock').done(function(data) {
    $.getJSON('/toc/clock/' + $("#gameId").val()).done(function(data) {
        if (data.running === true) {
            var currentRound = $("#currentRound").text();
            var updateRound = data.currentLevel.round;
            if (currentRound != updateRound) {
                ion.sound.play("tocbuzzer");
            }

            $("#currentRound").html(data.currentLevel.round);

            var currentMinutes = $("#remainingMinutes").text();
            var updateMinutes = data.remainingMinutes;
            if (currentMinutes == 2 && updateMinutes == 1) {
                ion.sound.play("two_minutes");
            }

            $("#remainingMinutes").html(data.remainingMinutes);
            $("#remainingSeconds").html(data.remainingSeconds);
            $("#currentSmallBlind").html(data.currentLevel.smallBlind);
            $("#currentBigBlind").html(data.currentLevel.bigBlind);
            $("#currentAnte").html(data.currentLevel.ante);

            $("#nextRound").html(data.nextLevel.round);
            $("#nextSmallBlind").html(data.nextLevel.smallBlind);
            $("#nextBigBlind").html(data.nextLevel.bigBlind);
            $("#nextAnte").html(data.nextLevel.ante);

            $("#upMinutes").hide();
            $("#upSeconds").hide();
            $("#downMinutes").hide();
            $("#downSeconds").hide();
            $("#play").val('Pause');
        } else {
            $("#upMinutes").show();
            $("#upSeconds").show();
            $("#downMinutes").show();
            $("#downSeconds").show();
            $("#play").val('Play');
        }
    });
}
