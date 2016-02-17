###XMPP Protocol


The XMPP protocol is a communcation protocol built around messaging between two enitites using XML forms. An XMPP transmission starts with a *stream*. A stream encapsulstaes everything inside the transmissions, like stanzas. *Stanzas*, or *Packets* in the SmackAPI, are the content of the transmissions. There are three core types: *message*, *iq*, and *presence*. Message handles string meessages, content, payloads, etc. IQ handles all set or get calls to the server. Presence handles meta information about a members presence in the meeting. These do not limit the scope of XMPP because of the provider architecture in Smack giving us the ability to create custom packets and namespace. But the danger of that is that any client that can't understand that custom packet/namespace will treat it as whitespace thus isolating that client from the real "messages". This extended cotent can either take on the form of a packet or can appear as a child of one of the core packets (message,iq,presence). In typical fashion, on the protocol side, you'll just need varying namespace names in the xml form. An example of this could be:

...
<drawing xmlns='drawingEvent:x:umdCS4995' from='julie@capulet.com/balcony'>
  <c xmlns='drawingEvent-child:x:umdCS4995'>
    <drawObj> ...maybe json object... </drawObj>
  </c>
  <x xmlns='userEvent-child:x:umdCS4995'>
    <signal> queue </signal>
    <type> drawevent </type>
  </x>
</drawing>
...

...
<message to='juliet@capulet.com' from='romeo@montague.com'>
  <body>O, wilt thou leave me so unsatisfied?</body>
  <type xmlns='emotionalContext:x:PoolScene'> teenage angst </type>
</message>
...

###XMPP Communication Example with PubSub
Imagine a setup of two clients and a server. The clients will be running our “potential” app that involves custom parsing of our XMPP forms. The server is just the Openire server application. We’ll also assume client 1 is the “host”. Examle: Client 2 is going to press the queue signal on their app. That will activate a listener that is from the SmackAPI to form a transmission to the server. The transmission might look like this:

...
<stream:stream
to= “whiteboard.com”
xmlns="jabber:client"
xmlns:stream="http://etherx.jabber.org/streams">
<iq type='set'
from='client2@whiteboard.com'
to='pubsub.whiteboard'
id='pub1'>
  <pubsub xmlns='http://jabber.org/protocol/pubsub'>
    <publish node='drawingSession1054'>
      <queue> Client2 </queue>
        <timestamp > time </timestamp>
    </publish>
  </pubsub>
</iq>
</stream:stream>
...

This is an IQ stanza, meaning a call that involves a server resource. All this does is publishes an event that to a node on the server that says Client2 has queued up. This event will be delivered to all the clients (in this case Client1 will get it). Client1's app will receive this message:

...
<stream:stream
to= “whiteboard.com”
xmlns="jabber:client"
xmlns:stream="http://etherx.jabber.org/streams">
<message from='pedro@whiteboard.com' to='becky@whiteboard.com' id='fez'>
  <event xmlns='http://jabber.org/protocol/pubsub#event'>
    <items node='drawingSession1054'>
      <item id='ae890ac52d0df67ed7cfdf51b644e901'>
        <publish node='drawingSession1054'>
          <queue> Client2 </queue>
            <timestamp > time </timestamp>
        </publish>
      </item>
    </items>
  </event>
</message>
</stream:stream>
...

From here, the parser from the SmackAPI will parse this information and the listeners connected to that parser that will activate a signal on Client 1’s app saying Client has queued.

Keep this section as an open forum for various things that will need to be handled using custom packet fields. *Please feel free to edit this and add your suggestions.*
* Signaling queues.
* The arrival or departure of a member.
* The drawing events.
* 'type' for every custom stanza/packet

