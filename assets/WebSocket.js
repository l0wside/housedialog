window.HDWebSocket.log("Lalelu");
function WebSocket(uri,protocol) {
	window.HDWebSocket.log("WebSocket initializing");
	if (typeof(WebSocket.instances) == 'undefined') {
		WebSocket.instances = new Array();
	}
	handler = window.HDWebSocket.getHandler(uri,"");
	window.HDWebSocket.log("Handler is: " + handler);
	WebSocket.instances[handler] = this;
	onopen = function() {};
	onclose = function() {};
	onerror = function() {};
	onmessage = function() {};
	send = function(arg) {
		window.HDWebSocket.log("WebSocket sending");
		window.HDWebSocket.send(handler,arg);
	};


	WebSocket._onEvent = function(handler,event,arg) {
		window.HDWebSocket.log("WebSocket processing event " + event);
		ws = WebSocket.instances[handler];
		if (typeof(ws) != 'undefined') {
			if (event == "message") {
				ws.onmessage(arg);
			} else if (event == "open") {
				ws.onopen(arg);
			} else if (event == "close") {
				ws.onclose(arg);
			} else if (event == "error") {
				ws.onerror(arg);
			}
		}

	}
};


