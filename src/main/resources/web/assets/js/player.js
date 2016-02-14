if(typeof(EventSource) !== "undefined") {
    var source = new EventSource('play-status');
    source.onmessage = function(event) {
        var obj = JSON.parse(event.data);
        document.getElementById("nowplaying").innerHTML = "Now Playing: " + obj.title;
        document.getElementById("time").innerHTML = obj.time;
    };
} else {
    document.getElementById("nowplaying").innerHTML = "Your browser doesn't support server-sent events.. Get a new one?";
}