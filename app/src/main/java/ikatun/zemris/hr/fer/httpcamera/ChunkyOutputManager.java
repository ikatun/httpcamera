package ikatun.zemris.hr.fer.httpcamera;

import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by ikatun on 15.1.2015..
 */
public class ChunkyOutputManager {
    private static File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM", "HttpCamera");
    private int chunkCount = 0;
    private Runnable onFirstChunkCompleted = null;

    private static File getOutputMediaFile(String name) {
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("HttpCamera", "failed to create directory");
                return null;
            }
        }

        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + name);

        return mediaFile;
    }

    public ChunkyOutputManager() {
        clearOutputDirectory();
    }

    public void clearOutputDirectory() {
        if (mediaStorageDir.exists()) {
            for (File file : mediaStorageDir.listFiles()) {
                file.delete();
            }
        }
    }

    public File getNextFile() {
        return getOutputMediaFile(Integer.toString(chunkCount + 1));
    }

    public void reportLastFileFinished() {
        chunkCount++;
        if (onFirstChunkCompleted != null && chunkCount == 1)
            onFirstChunkCompleted.run();
    }

    public File getLastFinishedFile() {
        return chunkCount == 0 ? null : getOutputMediaFile(Integer.toString(chunkCount));
    }

    public void setOnFirstChunkCompleted(Runnable runnable) {
        this.onFirstChunkCompleted = runnable;
    }

    public int getChunkCount() {
        return chunkCount;
    }

}
