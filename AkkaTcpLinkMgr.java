
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.util.ByteString;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by maotz on 2015-03-18.
 *
 */
class AkkaTcpLinkMgr extends UntypedActor{
    private final static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AkkaTcpLinkMgr.class);

    private final Map<ActorRef, AkkaTcpLinkObj> links = new ConcurrentHashMap<ActorRef, AkkaTcpLinkObj>();
    private final IAkkaObjListener<AkkaTcpLinkObj> listener;
    private final AkkaObjManager objMgr;

    private final ByteArrayOutputStream os = new ByteArrayOutputStream();
    private final Output output = new Output(os);
    private final Input input = new Input();
    private final Kryo kryoUp = new Kryo();
    private final Kryo kryoDn = new Kryo();

    AkkaTcpLinkMgr(IAkkaObjListener<AkkaTcpLinkObj> _listener, AkkaObjManager _objMgr){
        listener = _listener;
        objMgr = _objMgr;

        objMgr.registerKryo(kryoDn);
        objMgr.registerKryo(kryoUp);
    }

    /**
     * 接收消息
     * @param _msg 消息
     * @throws Exception
     */
    @Override
    public void onReceive(Object _msg) throws Exception{
        if(_msg instanceof ByteString){// 上行数据
            ByteString raw = (ByteString)_msg;
            AkkaTcpLinkObj linkObj = links.get(sender());
            linkObj.buffer.pack();
            linkObj.buffer.write(raw.toArray());
            input.setBuffer(linkObj.buffer.getData(), 0, linkObj.buffer.getSize());
            AkkaObj obj;
            while(input.canReadInt()) {
                int index = input.readInt();
                Class cls = objMgr.getClass(index);
                obj = (AkkaObj)kryoUp.readObject(input, cls);
                listener.onData(linkObj, obj);
            }
            linkObj.buffer.read(input.position());
        }else if(_msg instanceof AkkaObj) {// 下行数据
            AkkaObj obj = (AkkaObj)_msg;
            output.clear();
            output.writeInt(objMgr.getIndex(_msg.getClass()));
            kryoDn.writeObject(output, obj);
            byte[] bytes = output.toBytes();
            ByteString raw = ByteString.fromArray(bytes);
            sender().tell(raw, self());
        }else if (_msg instanceof Boolean){// 链接建立，链接断开
            Boolean bool = (Boolean)_msg;
            ActorRef linkRef = sender();
            AkkaTcpLinkObj obj;
            if(bool){
                //logger.info("link {}", linkRef);
                obj = new AkkaTcpLinkObj(linkRef, self());
                linkRef.tell(obj, self());
                links.put(linkRef, obj);
                listener.onLink(obj);
            }else{
                //logger.info("brok {}", linkRef);
                obj = links.get(linkRef);
                listener.onBrok(obj);
                links.remove(linkRef);
                linkRef.tell(akka.actor.PoisonPill.getInstance(), self());
            }
        }else {
            logger.info("unknow message {}", _msg);
            unhandled(_msg);
        }
    }
}
