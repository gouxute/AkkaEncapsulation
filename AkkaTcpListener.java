
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.io.Tcp;
import akka.io.TcpMessage;

import java.net.InetSocketAddress;

/**
 * Created by maotz on 2015-03-18.
 *
 */
class AkkaTcpListener extends UntypedActor{
    private final static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AkkaTcpListener.class);

    private final ActorRef manager;
    private final ActorRef linkMgrRef;
    private InetSocketAddress address;

    /**
     * 构造函数
     * @param _tcpMgrRef 管理器
     */
    public AkkaTcpListener(ActorRef _tcpMgrRef, ActorRef _linkMgrRef){
        manager = _tcpMgrRef;
        linkMgrRef = _linkMgrRef;
    }

    /**
     * 接收消息
     * @param msg 消息
     * @throws Exception
     */
    public void onReceive(Object msg) throws Exception{
        if(msg instanceof Tcp.Bound){
            logger.info("listen on : {}", address);
            manager.tell(msg, self());
        }else if(msg instanceof Tcp.CommandFailed){
            logger.error("receive failed {}", msg);
            //if(raw.toString().indexOf("Bind2DB")>0)  preStart();
            //getContext().stop(self());
        }else if(msg instanceof Tcp.Connected){
            final Tcp.Connected connected = (Tcp.Connected)msg;
            logger.info("link {}", msg);//+ " self="+getSelf()+" sender=" + getSender());
            manager.tell(connected, self());

            final ActorRef linkRef = getContext().actorOf(Props.create(AkkaTcpLinkActor.class, linkMgrRef));
            linkMgrRef.tell(Boolean.TRUE, linkRef);
            getSender().tell(TcpMessage.register(linkRef), self());
        }else if(msg instanceof InetSocketAddress){
            address = (InetSocketAddress)msg;
            logger.info("bind({})", address);
            manager.tell(TcpMessage.bind(self(), address, 100), self());
        }else{
            logger.info("receive other {}", msg.getClass().getSimpleName());
        }
    }

}
