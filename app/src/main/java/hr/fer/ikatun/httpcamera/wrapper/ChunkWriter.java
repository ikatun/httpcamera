package hr.fer.ikatun.httpcamera.wrapper;

/**
 * Created by ikatun on 12.4.2015..
 */
public interface ChunkWriter {
    public void writeChunk(byte[] data, int offset, int len);
    public void onReadingStopped(Exception e);
}