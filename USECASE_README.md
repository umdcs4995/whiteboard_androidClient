# Whiteboard Use Cases

Here is a quick run-through on how the Whiteboard application will work from starting the application to following each step of the activites inside of the app.

    1.  Start of the app
    2.  Splash screen
        a.  Checks setting for user name (client side)
    3.  Login Screen
        a.  Connects to server
            i.  Client issues login message
                1.  [login<username, password>]
                2.  Relays to Google for login information
                3.  Server sends back confirmation/denial
                    a.  Denial -> [ERR UserOrPasswordInvalid 401]
                    b.  Confirms -> [Success message 202]
                        i.  Load Main Menu
            ii. Client creates new user account
                1. Enter new username and double-enter password
                    a.  Google checks for username
                        i.  if not in use, create account
                        ii. if in use, send err message “username cannot be used”
                    b.  Account creation
                        i.  Google handles most information, but also stores a user token for each account
                        ii. Grab token from Google when account is used and store token on server. user.add<token>
                    c.  Go to main menu, account has been created.
    4.  Main Menu
        a.  Joining a Whiteboard
            i.  Client enters whiteboard name (and passcode)
                1.  Client issues JoinWhiteboard request
                    a.  [JoinWhiteboard <name, passcode>]
                2.  Server cross-reference list and confirms/denies
                    a.  If denied, board.isValid() returns false, server returns request denial
                    b.  If confirmation, board.isValid returns true and connection to respective room is made. server sends request confirmation
                3.  Server confirms request, add to room
                    a.  room.add<user>
        b.  Creating a new Whiteboard
            i.  Client sends whiteboard name (and passcode) request to server
            ii. Server cross-reference current list and confirms/denies request
                1.  call newWhiteboard<name, passcode> and check names
                    a.  if boardList.has<name>, send error “name in use”
                    b.  else, create new whiteboard with boardList.add()
                2.  Server sends back confirmation/denial message based on results of call to newWhiteboard()
                    a.  Client receives confirmation from server
                        i.
                            newWhiteboard<name, passcode> {
                                // first check if name is valid
                                // if valid, add board, else err(“name invalid”)
                                boardList.add(name, passcode)
                            }
                3.  Whiteboard confirmed
                    a.  client receives confirmation from server
        c.  Drawing on board (must first create/join a whiteboard)
            i.  Private view
                1.  Drawing info stored client-side
                2.  Pushing to public view
                    a.  board.pushToPublic()
                    b. 
