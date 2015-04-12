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
import java.io.IOException;
import java.io.InputStream;

import hr.fer.ikatun.httpcamera.CameraPreview;
import hr.fer.ikatun.httpcamera.Services;

/**
 * Created by ikatun on 11.4.2015..
 */
public class CameraReader implements ICameraReader {
    private CameraPreview mCameraPreview;
    private MediaRecorder mMediaRecorder;
    //private Camera mCamera;
    private boolean isReading = false;
    private String socketAddress = "camera_stream";

    public boolean isReading() {
        return isReading;
    }

    public CameraReader(Context context, FrameLayout frameLayout) {
        mCameraPreview = new CameraPreview(context, Services.getCameraInstance());
        frameLayout.addView(mCameraPreview, 0);
    }

    private Thread listenThread = null;

    @Override
    public synchronized void startReading(ChunkReadHandler handler, VideoFormat format) throws IOException {
        if (isReading) return;

        listenThread = new Thread(new Runnable() {
            public void run() {
                LocalServerSocket serverSocket = null;
                LocalSocket receiver = null;
                try {
                    UDPTransmitter udpTransmitter = new UDPTransmitter("192.168.0.17", 12345);
                    serverSocket = new LocalServerSocket(socketAddress);
                    receiver = serverSocket.accept();
                    InputStream inputStream = receiver.getInputStream();
                    int b = 0;
                    byte[] buffer = new byte[512];
                    while (true) {
                        int bytesRead = inputStream.read(buffer);
                        udpTransmitter.send(buffer, 0, bytesRead);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try { receiver.close(); } catch (Exception e) { e.printStackTrace(); }
                try { serverSocket.close(); } catch (Exception e) { e.printStackTrace(); }
            }
        });
        listenThread.start();

        mMediaRecorder = prepareVideoRecorder(format);
        isReading = true;
    }

    @Override
    public synchronized void stopReading() {
        if (!isReading) return;
        releaseMediaRecorder(mMediaRecorder);
        isReading = false;
    }

    LocalSocket sender = null;
    private MediaRecorder prepareVideoRecorder(VideoFormat format) throws IOException {

        sender = new LocalSocket();
        sender.connect(new LocalSocketAddress(socketAddress));

        Camera mCamera = Services.getCameraInstance();
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
        mMediaRecorder.setOutputFile(sender.getFileDescriptor());

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

    private void releaseMediaRecorder(MediaRecorder mediaRecorder){
        if (mediaRecorder != null) {
            try { mediaRecorder.stop(); } catch (Exception e) { }
            try { sender.close(); } catch (Exception e) { }
            mediaRecorder.reset();   // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            Services.getCameraInstance().lock();  // lock camera for later use
        }
    }
}
