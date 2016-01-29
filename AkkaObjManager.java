
import com.esotericsoftware.kryo.Kryo;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by maotz on 2015-03-19.
 *
 */
public class AkkaObjManager {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AkkaObjManager.class);

    private final List<Class> clsList = new LinkedList<Class>();

    public AkkaObjManager(){
    }

    public void registerClass(Class _cls){
        if(null!=_cls && !clsList.contains(_cls))
            clsList.add(_cls);
    }

    Class getClass(int _index){
        return clsList.get(_index);
    }

    int getIndex(Class _cls){
        return clsList.indexOf(_cls);
    }

    void registerKryo(Kryo _kryo){
        for(Class cls : clsList)
            _kryo.register(cls);
    }
}
