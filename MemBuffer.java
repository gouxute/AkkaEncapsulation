
import java.io.*;

public class MemBuffer {
	static private final int C_SIZE = 1024 * 8;
	static private final int C_INCS = 1024;

	private int head;
	private int tail;
	private int capacity;
	private int increment;
	private byte[] data;

	public MemBuffer(int _capacity){
		if(0==_capacity)
			_capacity = C_SIZE;
		capacity = _capacity;
		increment = C_INCS;
		head = tail = 0;
		data = new byte[capacity];
	}

	public MemBuffer(){
		this(C_SIZE);
	}

	private void ensureCapacity(int _new_size){
		if(_new_size > capacity){
			int new_capacity;
			if(_new_size % increment ==0)
				new_capacity = _new_size;
			else
				new_capacity = increment * ( (_new_size / increment) +1);
			byte[] copy = new byte[new_capacity];
			System.arraycopy(data, 0, copy, 0, capacity);
			data = copy;
			capacity = new_capacity;
		}

	}

    public boolean isEmpty(){
        return tail == head;
    }

	public int getSize(){
		return tail - head;
	}

    public byte[] getData(){
        return data;
    }

    public int getHead(){
        return head;
    }

    public int getTail(){
        return tail;
    }

	public void clear(){
		head = tail = 0;
	}

	public void pack(){
		int unread_size = tail - head;
		if(unread_size >0)
			System.arraycopy(data, head, data, 0, unread_size);
		head = 0;
		tail = unread_size;
	}

	public int indexOfEOL(){
		for (int i = head; i < tail; i ++) {
			if (data[i] == '\n') {
				return i - head;
			} else if ( (data[i] == '\r') && (i < tail - 1) && (data[i + 1] == '\n') ) {
				return i - head;  // \r\n
			}
		}
		return -1;  // Not found.
	}

	public int indexOf(char _c){
		for (int i = head; i < tail; i ++) {
			if (data[i] == _c) {
				return i - head;
			}
		}
		return -1;  // Not found.
	}

    public int indexOf(byte _v){
        return indexOf(_v, 0);
    }

    public int indexOf(byte _v, int _offset){
        for (int i = head + _offset; i < tail; i ++) {
            if (data[i] == _v) {
                return i - head;
            }
        }
        return -1;  // Not found.
    }

    public byte byteAt(int _index){ return data[head+_index]; }

	public char charAt(int _index){
		return (char)data[ head + _index ];
	}

	public void write(byte[] _bytes){
		write(_bytes, 0, _bytes.length);
	}

	public void write(byte[] _bytes,int _offset, int _len){
		ensureCapacity(getSize() + _len);
		int free_on_tail = capacity - tail;
		if( free_on_tail < _len)
			pack();
		System.arraycopy(_bytes, _offset, data, tail, _len);
		tail += _len;
	}

	public void read(int _len){
		read(_len, null);
	}

	public int read(int _len, MemBuffer _out){
		_len = Math.min(tail-head, _len);
		if(null!=_out)
			_out.write(data, head, _len);
		head += _len;
		return _len;
	}

	public String readStr(int _len){
		_len = Math.min(_len, tail-head);
		String res = new String(data, head, _len);
		head += _len;
		return res;
	}

	@Override
	public String toString(){
		String res = null;
		try{
			res = new String(data, head, tail-head);
		}catch (Exception e){
			e.printStackTrace();
		}
		return res;
	}

	public String toString(String charset) throws Exception{
		return new String(data, head, tail-head, charset);
	}

    public String toHex(){
        return Coder.byteArrayToHex(data, head, tail - head);
    }

	public void load(File _file) throws Exception{
		byte[] buf = new byte[8192];
		FileInputStream fis = new FileInputStream(_file);
		try {
			BufferedInputStream bis = new BufferedInputStream(fis);
			try {
				while (bis.available() > 0) {
					int bytes = bis.read(buf, 0, 8192);
					if (bytes > 0)
						this.write(buf, 0, bytes);
				}
			}finally {
				bis.close();
			}
		}finally {
			fis.close();
		}
	}

    public void save(String _file) throws Exception{
        FileOutputStream fos = new FileOutputStream(_file, true);
        fos.write(data, head, tail-head);
        fos.flush();
        fos.close();
    }
}
