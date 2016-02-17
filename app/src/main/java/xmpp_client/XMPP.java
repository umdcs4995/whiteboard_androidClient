package xmpp_client;

/**
 * Created by LauraKrebs on 2/15/16.
 */
import android.os.AsyncTask;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 *  Class: XMPP
 *  This class handles connection to the XMPP server. As of right now provides a good basis for testing
 *  any connections made to the server. Currently uses a static username and password to connect to the XMPP server
 *  that Maz has set up.
 */
public class XMPP {
    private String serverAddress;
    private XMPPTCPConnection connection;
    private String loginUser;
    private String passwordUser;

    /**
     * Constructor that populates these fields with the information needed to connect to the server
     * @param serverAddress server IP that we will be connecting too.
     * @param user Username for server
     * @param password Password for server
     */
    public XMPP(String serverAddress, String user, String password){
        this.serverAddress = serverAddress;
        this.loginUser = user;
        this.passwordUser = password;
    }

    /**
     * Login works on reconnection to the server if not authenticated via connection.login
     * @param connection XMPPConnection used to connect to server
     * @param loginUser Username for server
     * @param passwordUser  Password for server
     */
    private void login(final String loginUser, final String passwordUser){
        try{
            this.connection.login(loginUser, passwordUser);
        } catch (SmackException.NotConnectedException e){
            // If is not connected, a timer is schelude and a it will try to reconnect
            new Timer().schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    connect();
                }
            }, 5 * 1000);
        } catch (XMPPException e){
        } catch (SmackException e){
        } catch (IOException e){
        }
    }

    /**
     * Connect is an important method that runs on it's own thread so that it does not cause the main system thread to
     * wait on it's connection.
     *
     * This method creates our connection configuration with the correct server information
     *      -Will need to be updated to use non static username, password, and IP eventually-
     * This utilizes the ConnectionListener with the connection so that we are informed of when the connection is made.
     */
    public void connect(){
        AsyncTask<Void, Void, Boolean> connectionThread = new AsyncTask<Void, Void, Boolean>(){
            /**
             * doInBackground (from Andriod Doc):  This step is used to perform background computation that can take a long time
             * @param arg0
             * @return
             */
            @Override
            protected Boolean doInBackground(Void... arg0){
                boolean isConnected = false;

                /**
                 * Added '.setSecurityMode(SecurityMode.disabled) and it no longer blows up. - LauraKrebs 2/15
                 */

                XMPPTCPConnectionConfiguration conf = XMPPTCPConnectionConfiguration.builder()
                        .setServiceName("45.55.183.45").setUsernameAndPassword("admin", "admin")
                        .setCompressionEnabled(false).setSecurityMode(SecurityMode.disabled).build();



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
        connectionThread.execute(); //Runs the thread
    }

    /**
     * Notifies us about the status of the connection
     */
    public class XMPPConnectionListener implements ConnectionListener {
        @Override
        public void connected(final XMPPConnection connection){
            if(!connection.isAuthenticated())
                login(loginUser, passwordUser);
        }
        @Override
        public void authenticated(XMPPConnection connection, boolean resumed) {
            System.out.println("Connected");
        }
        @Override
        public void connectionClosed(){}
        @Override
        public void connectionClosedOnError(Exception arg0){}
        @Override
        public void reconnectingIn(int arg0){}
        @Override
        public void reconnectionFailed(Exception arg0){}
        @Override
        public void reconnectionSuccessful(){}
    }
}
