
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.io.Tcp;

import java.net.InetSocketAddress;

/**
 * Created by maotz on 2015-03-18.
 *
 */
public class AkkaTcpServer {
    private final static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AkkaTcpServer.class);
    private final static ActorSystem sys = ActorSystem.create("AkkaServer");

    private final ActorRef linkMgr;
    private final ActorRef lsnrRef;
    private final AkkaListeners<AkkaTcpLinkObj> listeners;
    private final AkkaObjManager objManager;

    public AkkaTcpServer(AkkaObjManager _objMgr){
        logger.info("AkkaTcpServer()");
        objManager = _objMgr;
        listeners = new AkkaListeners<AkkaTcpLinkObj>();
        linkMgr = sys.actorOf(Props.create(AkkaTcpLinkMgr.class, listeners, objManager));
        lsnrRef = sys.actorOf(Props.create(AkkaTcpListener.class, Tcp.get(sys).getManager(), linkMgr));
    }

    public void setPort(int _port){
        logger.info("setListenPort({})", _port);
        InetSocketAddress address = new InetSocketAddress("0.0.0.0", _port);
        lsnrRef.tell(address, lsnrRef);
    }

    public void registerListener(IAkkaObjListener<AkkaTcpLinkObj> _listener){
        logger.info("registerListener({})", _listener);
        listeners.register(_listener);
    }

    public static void main(String[] _args) throws Exception{
        AkkaObjManager objMgr = new AkkaObjManager();
        objMgr.registerClass(Demo.class);
        AkkaTcpServer server = new AkkaTcpServer(objMgr);
        server.registerListener(new IAkkaObjListener<AkkaTcpLinkObj>() {
            public void onLink(AkkaTcpLinkObj _sender) {
                logger.info("onLink {}", _sender);
            }

            public void onBrok(AkkaTcpLinkObj _sender) {
                logger.info("onBrok {}", _sender);
            }

            public void onData(AkkaTcpLinkObj _sender, AkkaObj _obj) {
                logger.info("onData {} {}", _sender, _obj);
                //_sender.send(akka.util.ByteString.fromString("world!"));
            }

        });
        server.setPort(6000);
    }
}
