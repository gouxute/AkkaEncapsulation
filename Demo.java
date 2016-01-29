
/**
 * Created by maotz on 2015-03-19.
 */
public class Demo extends AkkaObj{
    public String name;
    public int value;

    public Demo(String _name, int _value){
        name = _name;
        value = _value;
    }

    @Override
    public String toString(){
        return String.format("name:%s, value:%d", name, value);
    }
}
