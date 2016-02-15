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
 * Created by Drax on 2/11/16.
 */
public class XMPP {
    private String serverAddress;
    private XMPPTCPConnection connection;
    private String loginUser;
    private String passwordUser;

    public XMPP(String serverAddress, String user, String password){
        this.serverAddress = serverAddress;
        this.loginUser = user;
        this.passwordUser = password;
    }

    private void login(XMPPConnection connection, final String loginUser, final String passwordUser){
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

    public void connect(){
        AsyncTask<Void, Void, Boolean> connectionThread = new AsyncTask<Void, Void, Boolean>(){
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
        connectionThread.execute();
    }

    public class XMPPConnectionListener implements ConnectionListener {
        @Override
        public void connected(final XMPPConnection connection){
            if(!connection.isAuthenticated())
                login(connection, loginUser, passwordUser);
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
