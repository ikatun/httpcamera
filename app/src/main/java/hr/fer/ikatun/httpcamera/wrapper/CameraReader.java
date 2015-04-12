package hr.fer.ikatun.httpcamera.wrapper;

import android.content.Context;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Environment;
import android.util.Log;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;

import hr.fer.ikatun.httpcamera.CameraPreview;
import hr.fer.ikatun.httpcamera.Services;

/**
 * Created by ikatun on 11.4.2015..
 */
public class CameraReader {
    private CameraPreview mCameraPreview;
    private MediaRecorder mMediaRecorder;
    private boolean isReading = false;
    private LocalSocketReader localSocketReader = null;

    public boolean isReading() {
        return isReading;
    }

    public CameraReader(Context context, FrameLayout frameLayout) {
        mCameraPreview = new CameraPreview(context, Services.getCameraInstance());
        frameLayout.addView(mCameraPreview, 0);
    }


    public synchronized void startReading(ChunkWriter chunkWriter, VideoFormat format) throws IOException {
        if (isReading) return;

        localSocketReader = new LocalSocketReader(100 * 1024, chunkWriter);
        mMediaRecorder = prepareVideoRecorder(mCameraPreview, format, localSocketReader.getWriteableFileDescriptor());

        isReading = true;
    }


    public synchronized void stopReading() {
        if (!isReading) return;
        releaseMediaRecorder(mMediaRecorder);
        localSocketReader.stopReading();
        isReading = false;
    }

    private static MediaRecorder prepareVideoRecorder(CameraPreview mCameraPreview, VideoFormat format, FileDescriptor writeableFileDescriptor) throws IOException {

        Camera mCamera = Services.getCameraInstance();
        MediaRecorder mMediaRecorder;
        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        //mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        mMediaRecorder.setOutputFormat(format.getOutputFormat());
        mMediaRecorder.setAudioEncoder(format.getAudioEncoder());
        mMediaRecorder.setVideoEncoder(format.getVideoEncoder());


        // Step 4: Set output file
        //mMediaRecorder.setOutputFile(getOutputMediaFile("test").toString());
        //mMediaRecorder.setOutputFile(ParcelFileDescriptor.fromDatagramSocket(datagramSocket).getFileDescriptor());
        mMediaRecorder.setOutputFile(writeableFileDescriptor);

        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mCameraPreview.getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (IllegalStateException e) {
            Log.d("ikatun", "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder(mMediaRecorder);
            throw(e);
        } catch (IOException e) {
            Log.d("ikatun", "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder(mMediaRecorder);
            throw(e);
        }

        return mMediaRecorder;
    }

    private static File getOutputMediaFile(String name) {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM", "HttpCamera");
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("HttpCamera", "failed to create directory");
                return null;
            }
        }

        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + name);

        return mediaFile;
    }

    private static void releaseMediaRecorder(MediaRecorder mediaRecorder){
        if (mediaRecorder != null) {
            try { mediaRecorder.stop(); } catch (Exception e) { }
            mediaRecorder.reset();   // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            Services.getCameraInstance().lock();  // lock camera for later use
        }
    }
}
