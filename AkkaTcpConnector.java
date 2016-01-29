
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.io.Tcp;
import akka.io.TcpMessage;
import akka.util.ByteString;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;

/**
 * Created by maotz on 2015-03-19.
 * 连接器 对象
 */
class AkkaTcpConnector extends UntypedActor{
    private final static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AkkaTcpConnector.class);

    private final ActorRef tcpManager;
    private final AkkaListeners<AkkaTcpClient> listeners;
    private final AkkaObjManager objManager;

    private final ByteArrayOutputStream os = new ByteArrayOutputStream();
    private final Output output = new Output(os);
    private final Input input = new Input();
    private final Kryo kryoUp = new Kryo();
    private final Kryo kryoDn = new Kryo();

    AkkaTcpConnector(ActorRef _tcpMgr, AkkaObjManager _objMgr, AkkaListeners<AkkaTcpClient> _listeners){
        logger.info("new AkkaTcpConnector");
        tcpManager = _tcpMgr;
        objManager = _objMgr;
        listeners  = _listeners;

        objManager.registerKryo(kryoDn);
        objManager.registerKryo(kryoUp);
    }

    @Override
    public void onReceive(Object _msg) throws Exception{
        if (_msg instanceof Tcp.Received) {// 收到数据
            ByteString raw = ((Tcp.Received) _msg).data();
            logger.info("data msg:{} sender:{}", raw, sender());
        }else if(_msg instanceof AkkaObj){// 请求发送 : LEN[4] + TYP[2] + BODY[size]
            AkkaObj obj = (AkkaObj)_msg;
            output.clear();
            kryoDn.writeObject(output, obj);
            int size = output.position();
            byte[] bytes = new byte[size+4+2];
            bytes[0] = (byte)( (size+4) >>24);
            bytes[1] = (byte)( (size+4) >>16);
            bytes[2] = (byte)( (size+4) >>8);
            bytes[3] = (byte)( (size+4) );
            int index = objManager.getIndex(_msg.getClass());
            bytes[4] = (byte)(index >> 8);
            bytes[5] = (byte)(index);
            System.arraycopy(output.getBuffer(), 0, bytes, 2, output.position());
            ByteString raw = ByteString.fromArray(bytes);
            self().tell(TcpMessage.write(raw), self());
        }else if(_msg instanceof InetSocketAddress){
            InetSocketAddress remote = (InetSocketAddress)_msg;
            logger.info("make to {}", remote);
            tcpManager.tell(TcpMessage.connect(remote), self());
        }else if(_msg instanceof Tcp.CommandFailed){
            logger.error("failed msg:{} from:{}", _msg, sender());
            //context().stop(self());
        }else if(_msg instanceof Tcp.Connected){
            logger.info("link msg:{} from:{}", _msg, sender());

        }else if (_msg instanceof Tcp.ConnectionClosed) {
            logger.info("brok msg:{} sender:{}", _msg, sender());
        }else{
            logger.info("unknow msg {} {} from {}", _msg, _msg.getClass(), sender());
            unhandled(_msg);
        }
    }
}
