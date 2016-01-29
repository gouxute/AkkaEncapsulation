
import java.util.ArrayList;
import java.util.List;

/**
 * Created by maotz on 2015-03-18.
 */
public class AkkaListeners<T> implements IAkkaObjListener<T> {
    private final List<IAkkaObjListener<T>> list = new ArrayList<IAkkaObjListener<T>>();

    public void register(IAkkaObjListener<T> _listener){
        if(null!=_listener && !list.contains(_listener))
            list.add(_listener);
    }

    public void onLink(T _sender){
        for(IAkkaObjListener<T> listener:list)
            listener.onLink(_sender);
    }
    public void onBrok(T _sender){
        for(IAkkaObjListener<T> listener:list)
            listener.onBrok(_sender);
    }
    public void onData(T _sender, AkkaObj _obj){
        for(IAkkaObjListener<T> listener:list)
            listener.onData(_sender, _obj);}
}
