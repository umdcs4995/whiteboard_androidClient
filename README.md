# Whiteboard_InterfaceTeam

# Node JS

## Java sending information

When sending information via Java to the NODE JS server you must build a JSON object if you are sending multiple bits of informations. Otherwise you can send it as a string, but we will not parse that information.

So your code in Java would look like:

String action_id = "createWhiteboard";
JSONObject jsonData = new JSONObject();
jsonData.put("name", "testBoard");
jsonData.put("access", "public");

socket.emit(action_id, jsonData.toString());

This allows us to parse the msg (aka the jsonData) in Javascript to grab the information such as "name" and "access"

So in summary:
	action_id to apply the correct logic to the information
	JSONData to send all information for logic 
	
## Java Listeners

For actions that require a response back from the server you will need to build a listener in order to interpret the information that is sent from server to client.

For example in the SocketService you would add:
	

	private void setupListeners(){
		socket.on(action_id, new Emitter.Listener(){
			public void call(Object ...args){
				args can be parsed as one string.
			}
		}
	}

Mitch is adding a method called (SocketService.addListener(action_id, Emitter.Listener anonymous function)) which will add the listener from wherever this is called. You should use this wherever you will need to add a listener, this will make it easier to read through the code and understand the logic that is happening.

This will allow you to handle all the incoming information from the server.

## Node actions
MSG: 'createWhiteboard',function(msg)
    
    Takes a whiteBoard JSON object and will return a confirmation message:

    'status': 100, 'message': 'Successful creation'


MSG: 'joinWhitebboard',function(clientSocket)

    Takes a JSON object containing the mandatory fields

    object{
        name (name of whiteBoard)
        username (name of user)
    }


MSG: 'chat message',function(msg)

    Takes a string message that will be echoed to all members of the whiteBoard.

MSG: 'motionevent',function(msg)

    Takes in a motionEvent JSON Object that will be emitted to all members of the whiteBoard as a motionevent message.

MSG: 'disconnect',function()

    Will disconnect the current client from the whiteboard on the server side.

MSG: 'leave',function()

    Will let the client leave from the whtiebaord on the server side.

MSG: 'listAllClients',funcgion(msg)

    Will return a string of clients in CSV format.

MSG: 'listAllWhiteBoards',function(msg)

    Will return a string of whiteboards in CSV format.
