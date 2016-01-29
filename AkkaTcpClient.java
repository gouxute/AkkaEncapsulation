
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.io.Tcp;

import java.net.InetSocketAddress;

/**
 * Created by maotz on 2015-03-18.
 *
 */
public class AkkaTcpClient {
    private final static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AkkaTcpClient.class);

    private final static ActorSystem sys = ActorSystem.create("AkkaTcpClient");
    private final AkkaObjManager objManager;
    private final AkkaListeners<AkkaTcpClient> listeners;
    private ActorRef connectorRef;
    private InetSocketAddress address;

    public AkkaTcpClient(AkkaObjManager _objMgr){
        logger.info("new AkkaTcpClient");
        objManager = _objMgr;
        listeners = new AkkaListeners<AkkaTcpClient>();
        connectorRef = null;
    }

    /**
     * 注册监听器
     * @param _listener 监听器
     */
    public void registerListener(IAkkaObjListener<AkkaTcpClient> _listener){
        listeners.register(_listener);
    }

    public void send(AkkaObj _obj){
        if(null!=connectorRef) {
            logger.info("send {}", _obj);
            connectorRef.tell(_obj, connectorRef);
        }
    }

    /**
     * 取地址
     * @return
     */
    public InetSocketAddress getAddress(){
        return address;
    }

    /**
     * 设置服务器地址
     * @param _host 服务器地址
     * @param _port 服务器端口
     */
    public void setServer(String _host, int _port){
        address = new InetSocketAddress(_host, _port);
    }

    public void setActive(boolean _active){
        if(_active) {
            if(null==address) {
                logger.error("invalid address");
            }else {
                connectorRef = sys.actorOf(Props.create(AkkaTcpConnector.class, Tcp.get(sys).getManager(), objManager, listeners));
                connectorRef.tell(address, connectorRef);
            }
        }else{
            connectorRef.tell(akka.actor.PoisonPill.getInstance(), connectorRef);
            connectorRef = null;
        }
    }

    public boolean getActive(){
        return null!=connectorRef;
    }

    public static void main(String[] _args) throws Exception{
        AkkaObjManager objMgr = new AkkaObjManager();
        objMgr.registerClass(Demo.class);
        AkkaTcpClient client = new AkkaTcpClient(objMgr);
        client.registerListener(new IAkkaObjListener<AkkaTcpClient>() {
            @Override
            public void onLink(AkkaTcpClient _sender) {
                logger.info("onLink {}", _sender.getAddress() );
            }

            @Override
            public void onBrok(AkkaTcpClient _sender) {
                logger.info("onBrok {}", _sender.getAddress() );
            }

            @Override
            public void onData(AkkaTcpClient _sender, AkkaObj _data) {
                logger.info("onData {} {}", _sender.getAddress(), _data );
            }
        });
        client.setServer("127.0.0.1", 6000);
        client.setActive(true);

        Demo obj = new Demo("name1", 1);
        client.send(obj);
    }

}
