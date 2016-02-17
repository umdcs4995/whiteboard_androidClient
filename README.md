# Whiteboard_InterfaceTeam

#Important for SMACK
When building with Smack I wrongly assumed that we had to download the jars and attach them to the project in our finder. However since we are using GRADLE we can add GRADLE packaages to the build path like so:

  - // Optional for XMPPTCPConnection

	 compile "org.igniterealtime.smack:smack-tcp:4.1.0" 
	 
  - // Optional for XMPP-IM (RFC 6121) support (Roster, Threaded Chats, …)
  
	 compile "org.igniterealtime.smack:smack-im:4.1.0"

  - // Optional for XMPP extensions support
  
	 compile "org.igniterealtime.smack:smack-extensions:4.1.0"

Note that you should not include the smack-java7:4.1.0 package as this will cause your project to not build as I have found out. 

Another thing to note is that with our project we can also do it via the application's gradle.build file as well by including:
	
```
	
	maven {
		url 'https://oss.sonatype.org/content/repositories/snapshots'
	}
	mavenCentral()
	
```
under the repository area in the gradle.build


You can find all this information here:

[Smack README] (https://github.com/igniterealtime/Smack/wiki/Smack-4.1-Readme-and-Upgrade-Guide)


#Important for creating an XMPPTCPConnectionConfiguration 
	
This took me a while to figure out and it shouldn't've but in most tutorials you will see a settup where it asks you to do something like:

```
		ConnectionConfiguration config = new ConnectionConfiguration
```

But now this gives us a "Cannot instantiate Abstract Class" error so digging through the source code you can find the real way to do it in their comments which is:

```
		XMPPTCPConnectionConfiguration conf = XMPPTCPConnectionConfiguration.builder()
                        .setServiceName("45.55.183.45").setUsernameAndPassword("hcc", "adminpassword")
                        .setCompressionEnabled(false).build();
```

This allows us to correctly instantiate a config file for use with our connection.


#Important for Connections in our Application

One thing I found out is that you cannot, cannot, CANNOT create a connection in the main application thread (ie the onCreate method) in the main activity. This is because as our connection is made the application thread must then wait for a response and cannot do anything in the mean time. In order to fix this you must make an ASyncTask method that will handle this stuff on a seperate thread. This was shown in the tutorial that Pete posted but it did not explain why it was necessary.

Here is what I did :

```
		public void connect(){
        AsyncTask<Void, Void, Boolean> connectionThread = new AsyncTask<Void, Void, Boolean>(){
            @Override
            protected Boolean doInBackground(Void... arg0){
                boolean isConnected = false;

                XMPPTCPConnectionConfiguration conf = XMPPTCPConnectionConfiguration.builder()
                        .setServiceName("45.55.183.45").setUsernameAndPassword("hcc", "adminpassword")
                        .setCompressionEnabled(false).build();

                connection = new XMPPTCPConnection(conf);

                XMPPConnectionListener connectionListener = new XMPPConnectionListener();
                connection.addConnectionListener(connectionListener);
                try{
                    connection.connect();
                    isConnected = true;
                } catch (IOException e){
                } catch (SmackException e){
                } catch (XMPPException e){
                }

                return isConnected;
            }
        };
        connectionThread.execute();
    }

```
#User Permissions
List all extra permissions added here.

   - \<uses-permission android:name="android.permission.INTERNET"/\>

#TODO

So the only issue I am running into now is that connection to the server is getting dropped due to an :

>           javax.net.ssl.SSLHandshakeException: java.security.cert.CertPathValidatorException: Trust anchor for certification path not found.

which, as far as I have read, means we need to create a certificate that will handle this connection so that this does not bypass Androids security standards


