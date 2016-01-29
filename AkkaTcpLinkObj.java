
import akka.actor.ActorRef;
import com.etrans.lib.utils.MemBuffer;

/**
 * Created by maotz on 2015-03-18.
 *
 */
public class AkkaTcpLinkObj {

    /**
     * 链接对象 引用
     */
    final ActorRef linkObjRef;

    /**
     * 管理器对象 引用
     */
    private final ActorRef linkMgrRef;

    final MemBuffer buffer = new MemBuffer(512);

    AkkaTcpLinkObj(ActorRef _linkObjRef, ActorRef _linkMgrRef){
        linkObjRef = _linkObjRef;
        linkMgrRef = _linkMgrRef;
    }

    /**
     * 发送数据
     * @param _obj 待发对象
     */
    public void send(AkkaObj _obj){
        linkMgrRef.tell(_obj, linkObjRef);
    }

}
