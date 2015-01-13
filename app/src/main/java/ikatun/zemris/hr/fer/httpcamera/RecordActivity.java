package ikatun.zemris.hr.fer.httpcamera;

import android.hardware.Camera;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;


public class RecordActivity extends ActionBarActivity {
    //private SurfaceView surfaceView = null;
    //private Button recordButton = null;
    private PreviewableChunkyVideoRecorder previewRecorder = null;
    Button recordButton = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        previewRecorder = (PreviewableChunkyVideoRecorder) findViewById(R.id.preview_recorder);
        recordButton = (Button) findViewById(R.id.record);
        /*SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);

        SurfaceHolder holder = surfaceView.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {}
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {}

            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                surfaceHolder.removeCallback(this);
                final Camera camera = Camera.open();
                camera.unlock();
                final MediaRecorder mediaRecorder = new MediaRecorder();
                mediaRecorder.setCamera(camera);
                mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                //new FileInputStream(new FileDescriptor());
                //new FileDe

                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);


                //ServerSocket serverSocket = new ServerSocket(12345);
                //Socket clientSocket = serverSocket.accept();
                //((TextView)findViewById(R.id.textView)).setText(serverSocket.getInetAddress().getHostAddress() + ":12345");
                //ParcelFileDescriptor parcelFileDescriptor = ParcelFileDescriptor.fromSocket(clientSocket);
                //clientSocket.getOutputStream().write("heeeeej\n".getBytes());
                //mediaRecorder.setOutputFile(parcelFileDescriptor.getFileDescriptor());
                mediaRecorder.setOutputFile(getOutputVideoFile(1).getAbsolutePath());
                //Uri.fromParts(null, null, null).
                mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
                try {
                    mediaRecorder.prepare();
                } catch (IllegalStateException e) {
                    Log.d("ikatun", e.toString());
                } catch (IOException e) {
                    Log.d("ikatun", e.toString());
                }
                //new FileInputStream()
                try {
                    final Button recordButton = (Button) findViewById(R.id.record);
                    recordButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View view) {
                            recordButton.setText("Stop");
                            mediaRecorder.start();
                            recordButton.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View view) {
                                    mediaRecorder.stop();
                                    mediaRecorder.reset();
                                    mediaRecorder.release();
                                    camera.lock();
                                    camera.stopPreview();
                                }
                            });
                        }
                    });
                } catch (Exception e) {
                    Log.d("ikatun", e.toString());
                }

            }

        });

        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        */
    }

    private boolean isRecording = false;

    public void toggleRecording(View view) {
        if (isRecording) {
            previewRecorder.stop();
            recordButton.setText("Record");
        } else {
            previewRecorder.start();
            recordButton.setText("Stop recording...");
        }
        isRecording = !isRecording;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_record, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /** Create a File for saving an image or video */
    private static File getOutputVideoFile(int index){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM", "HttpCamera");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        return new File(mediaStorageDir.getPath() + File.separator + index);
    }
}
