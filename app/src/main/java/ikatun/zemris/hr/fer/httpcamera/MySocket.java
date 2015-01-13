package ikatun.zemris.hr.fer.httpcamera;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Created by ikatun on 13.1.2015..
 */
public class MySocket extends Socket {
    private InputStream inputStream;
    private OutputStream outputStream;

    public MySocket(InputStream inputStream, OutputStream outputStream) {
        this.inputStream = inputStream;
    }

    public void bind(SocketAddress addr){}
    public void close(){}
    public void connect(SocketAddress endpoint){}


}
