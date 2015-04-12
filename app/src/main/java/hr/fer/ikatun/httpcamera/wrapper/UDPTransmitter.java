package hr.fer.ikatun.httpcamera.wrapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by ikatun on 25.3.2015..
 */
public class UDPTransmitter {
    private DatagramSocket socket;

    private DatagramPacket sendPacket;
    byte[] recvBuffer;
    private DatagramPacket recvPacket;

    private void init(String theirAddress, int theirPort) throws SocketException, UnknownHostException {
        sendPacket = new DatagramPacket(new byte[0], 0, InetAddress.getByName(theirAddress), theirPort);
        recvBuffer = new byte[socket.getReceiveBufferSize()];
        recvPacket = new DatagramPacket(recvBuffer, 0, recvBuffer.length);
    }

    public UDPTransmitter(int myPort, String theirAddress, int theirPort) throws SocketException, UnknownHostException {
        socket = new DatagramSocket(myPort);
        init(theirAddress, theirPort);
    }

    public UDPTransmitter(String theirAddress, int theirPort) throws SocketException, UnknownHostException {
        socket = new DatagramSocket();
        init(theirAddress, theirPort);
    }

    public void send(byte[] data, int offset, int length) throws IOException {
        sendPacket.setData(data, offset, length);
        socket.send(sendPacket);
    }

    public void receiveTo(OutputStream outputStream) throws IOException {
        recvPacket.setData(recvBuffer, 0, recvBuffer.length);
        socket.receive(recvPacket);

        outputStream.write(recvPacket.getData(), recvPacket.getOffset(), recvPacket.getLength());
    }

    public void close() {
        socket.close();
        socket = null;
    }

    public int getReceivePort() { return socket.getPort(); }
    public int getReceiveBufferSize() { return recvBuffer.length; }
}
