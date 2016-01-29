
/**
 * Created by maotz on 2015-03-18.
 * 事件与数据 回调接口
 */
public interface IAkkaObjListener<T>{
    public void onLink(T _sender);
    public void onBrok(T _sender);
    public void onData(T _sender, AkkaObj _data);
}
