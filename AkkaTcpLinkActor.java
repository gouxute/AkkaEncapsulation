
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.io.Tcp;
import akka.io.TcpMessage;
import akka.util.ByteString;

/**
 * Created by maotz on 2015-03-18. *
 * TCP 链接Actor
 */
public class AkkaTcpLinkActor extends UntypedActor {
    private final static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AkkaTcpLinkActor.class);

    private final ActorRef linkMgr;
    private ActorRef tunnel = null;
    private AkkaTcpLinkObj linkObj = null;

    AkkaTcpLinkActor(ActorRef _linkMgr){
        linkMgr = _linkMgr;
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        if(msg instanceof Tcp.Received){// 上传原始数据
            if(null==tunnel) tunnel = sender();
            final ByteString raw = ((Tcp.Received)msg).data();
            logger.debug("data {}", raw.toString() );
            linkMgr.tell(raw, self());
        }else if (msg instanceof Tcp.ConnectionClosed){// 终端链接断开
            Tcp.ConnectionClosed closed = (Tcp.ConnectionClosed)msg;
            logger.info("brok {} self{} sender={}", closed, self(), sender() );
            linkMgr.tell(Boolean.FALSE, self());
            context().stop(self());
        }else if(msg instanceof ByteString){// 下发请求
            final ByteString cmd = (ByteString)msg;
            logger.debug("send {}", cmd);
            tunnel.tell(TcpMessage.write(cmd), self());
        }else if(msg instanceof AkkaTcpLinkObj){// 对象关联
            linkObj = (AkkaTcpLinkObj)msg;
        }else{
            logger.info("other {}", msg);
            unhandled(msg);
        }
    }
}
