package ikatun.zemris.hr.fer.httpcamera;

import android.graphics.Color;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class CameraActivity extends ActionBarActivity {
    private Camera mCamera;
    private CameraPreview mPreview;
    private MediaRecorder mMediaRecorder = null;
    private Button captureButton = null;
    private ChunkyOutputManager chunkyOutputManager = null;
    private SimpleTcpServer tcpServer = new SimpleTcpServer();
    private TextView statusTextView = null;

    // Add a listener to the Capture button

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);

        // Create an instance of Camera
        mCamera = Services.getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        captureButton = (Button) findViewById(R.id.button_capture);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview, 0);
        statusTextView = (TextView)findViewById(R.id.status_text);
    }

    private boolean prepareVideoRecorder(File outputFile) {

        mCamera = Services.getCameraInstance();
        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        //mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        //mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.WEBM);
        //mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.VORBIS);
        //mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.VP8);

        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

        // Step 4: Set output file
        mMediaRecorder.setOutputFile(outputFile.toString());

        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d("ikatun", "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d("ikatun", "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera() {
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    private boolean isRecording = false;
    public void toggleCapture(View view) {
        if (isRecording) {
            timer.cancel();
            timer = new Timer();
            stopRecording();
            tcpServer.stopListening();
            statusTextView.setText("Server stopped.");
        } else {
            chunkyOutputManager = new ChunkyOutputManager();
            startRecording(chunkyOutputManager.getNextFile());
            statusTextView.setText("Buffering...");
            chunkyOutputManager.setOnFirstChunkCompleted(new Runnable() {
                public void run() {
                    try {
                        final StringBuilder statusText = new StringBuilder();
                        for (String address : Services.getIPAddresses()) {
                            statusText.append("Listening on: http://" + address + ":12345\n");
                        }
                        statusText.setLength(statusText.length() - 1);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                statusTextView.setText(statusText.toString());
                            }
                        });
                        tcpServer.startListening(new SimpleTcpServer.ConnectedHandler() {
                            public void clientConnected(Socket clientSocket) {
                                try {
                                    InputStream is = clientSocket.getInputStream();
                                    OutputStream clientStream = clientSocket.getOutputStream();
                                    List<String> header = Services.readHttpRequest(is);
                                    String requestPath = Services.getPath(header);
                                    if (requestPath.equals("")) {
                                        Services.copy(Services.getHttpIndexResponse(), clientStream);
                                    } else {
                                            Services.copy(Services.getHttpResponsePrefix(chunkyOutputManager.getChunkCount()), clientStream);
                                            InputStream in = new FileInputStream(chunkyOutputManager.getLastFinishedFile());
                                            Services.copy(in, clientStream);
                                            in.close();
                                    }
                                    is.close();
                                    clientStream.close();
                                } catch (IOException e) {
                                    Log.d("ikatun", e.toString());
                                }
                            }
                        }, null);
                    } catch (IOException e) {
                        Log.d("ikatun", e.toString());
                    }
                }
            });
        }
    }

    private void startRecording(File outputFile) {
        // initialize video camera
        if (prepareVideoRecorder(outputFile)) {
            // Camera is available and unlocked, MediaRecorder is prepared,
            // now you can start recording
            try {
                mMediaRecorder.start();
                timedRestart();
            } catch (Exception e) {
                Log.d("ikatun", e.toString());
            }

            // inform the user that recording has started
            captureButton.setText("Stop");
            isRecording = true;
        } else {
            // prepare didn't work, release the camera
            releaseMediaRecorder();
            // inform user
        }
    }

    private void stopRecording() {
        // stop recording and release camera
        mMediaRecorder.stop();  // stop the recording
        releaseMediaRecorder(); // release the MediaRecorder object
        mCamera.lock();         // take camera access back from MediaRecorder

        // inform the user that recording has stopped
        captureButton.setText("Start");
        isRecording = false;
    }

    Timer timer = new Timer();

    private void timedRestart() {
        timer.schedule(new TimerTask() {
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        stopRecording();
                        chunkyOutputManager.reportLastFileFinished();
                        startRecording(chunkyOutputManager.getNextFile());
                    }
                });
            }
        }, 5000);
    }
}
