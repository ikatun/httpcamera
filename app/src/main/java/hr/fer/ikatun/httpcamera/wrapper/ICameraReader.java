package hr.fer.ikatun.httpcamera.wrapper;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ikatun on 11.4.2015..
 */
public interface ICameraReader {
    public void startReading(ChunkReadHandler handler, VideoFormat format) throws IOException;
    public void stopReading();

    interface ChunkReadHandler {
        public void readChunk(byte[] data, int offset, int len);
    }
}
