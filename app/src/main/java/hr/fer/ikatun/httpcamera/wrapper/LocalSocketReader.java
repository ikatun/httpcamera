package hr.fer.ikatun.httpcamera.wrapper;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ikatun on 12.4.2015..
 */
public class LocalSocketReader {
    private final String serverSocketUniqueName = Integer.toString(hashCode());

    private LocalServerSocket localServerSocket = null;
    private boolean isReading = false;
    private ChunkWriter chunkWriter = null;
    private int bufferSize;
    private volatile boolean endRequested = false;

    private LocalSocket sender = null;

    public LocalSocketReader(int bufferSize, ChunkWriter chunkWriter) throws IOException {
        this.bufferSize = bufferSize;
        localServerSocket = new LocalServerSocket(serverSocketUniqueName);

        sender = new LocalSocket();
        sender.connect(new LocalSocketAddress(serverSocketUniqueName));

        this.chunkWriter = chunkWriter;
        copyThread.start();
    }

    private final Thread copyThread = new Thread(new Runnable() {
        public void run() {
            isReading = true;
            try (LocalSocket receiverSocket = localServerSocket.accept()) {
                InputStream receiverStream = receiverSocket.getInputStream();
                byte[] buffer = new byte[bufferSize];
                while (true) {
                    Log.d("ikatun", "read from FileDescriptor started");
                    int bytes = receiverStream.read(buffer);
                    Log.d("ikatun", "read from FileDescriptor finished");
                    if (endRequested) {
                        Log.d("ikatun", "End requested, breaking from copy loop");
                        break;
                    }
                    chunkWriter.writeChunk(buffer, 0, bytes);
                }
            } catch (Exception e) {}
        }
    });

    public void stopReading() {
        if (!isReading) return;
        isReading = false;
        endRequested = true;
        try { sender.getOutputStream().write(0); } catch (Exception e) {}

        while (true) {
            try {
                Log.d("ikatun", "Trying to join the copy thread...");
                copyThread.join();
                Log.d("ikatun", "Thread joined.");
                break;
            } catch (InterruptedException e) {}
        }

        try { localServerSocket.close(); } catch (Exception e) {}
        try { sender.close(); } catch (Exception e) {}

        chunkWriter.onReadingStopped(null);
    }

    public FileDescriptor getWriteableFileDescriptor() {
        return sender.getFileDescriptor();
    }

    public boolean isReading() { return isReading; }


}
