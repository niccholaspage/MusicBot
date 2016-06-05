var webSocket = new WebSocket("ws://" + location.hostname + ":" + location.port + "/play-status");

webSocket.onopen = function (event) {

};

webSocket.onmessage = function (event) {
    var obj = JSON.parse(event.data);

    if (obj.title) {
        document.getElementById("now_playing").innerHTML = "Now Playing: " + obj.title;
    }

    if (obj.time) {
        document.getElementById("time").innerHTML = obj.time;
    }

    if (obj.queue) {
        document.getElementById("queue").innerHTML = "Queue:<br>" + obj.queue;
    }
};

webSocket.onclose = function (event) {
    //Do something.
};

window.addEventListener("load", function () {
    var url = document.getElementById("url");

    url.onkeydown = function (event) {
        if (event.which == 13) {
            play();
        }
    };
});

function play() {
    webSocket.send("play," + document.getElementById("url").value);

    clearURL();
}

function addToQueue() {
    webSocket.send("addtoqueue," + document.getElementById("url").value);

    clearURL();
}

function clearURL() {
    document.getElementById("url").value = "";

    document.getElementById('url-container').MaterialTextfield.checkDirty();
}

function pause() {
    webSocket.send("pause");
}

function stop() {
    webSocket.send("stop");
}