package ikatun.zemris.hr.fer.httpcamera;

import android.content.Context;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.File;
import java.io.IOException;

/**
 * Created by ikatun on 13.1.2015..
 */
public class PreviewableChunkyVideoRecorder extends SurfaceView {
    private int chunkCount = 0;
    private boolean isSurfaceCreated = false;

    private MediaRecorder mediaRecorder = null;
    private Camera camera = null;
    private SurfaceHolder surfaceHolder = null;

    public boolean isStartAllowed = false;
    public boolean isStopAllowed = false;
    public boolean isPreviewStopped = false;

    private static Camera staticCamera = null;


    public PreviewableChunkyVideoRecorder(final Context context, AttributeSet a) {
        super(context, a);
        deleteOldVideoFiles();

        getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolderLocal) {
                if (isSurfaceCreated) return;
                isSurfaceCreated = true;

                if (staticCamera == null) {
                    staticCamera = Camera.open();
                }

                camera = staticCamera;
                camera.unlock();
                if (android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    camera.enableShutterSound(false);
                } else {
                    AudioManager audio= (AudioManager)context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                    audio.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                }
                mediaRecorder = new MediaRecorder();
                surfaceHolder = surfaceHolderLocal;
                prepare();
                isStartAllowed = true;
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int w, int h) {
                //Camera.Parameters parameters = camera.getParameters();
                //parameters.setPreviewSize(w, h);
                //requestLayout();
                //camera.setParameters(parameters);

                // Important: Call startPreview() to start updating the preview surface.
                // Preview must be started before you can take a picture.
                //invalidate();
                //start();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                //stop();
            }
        });

        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private void prepare() {
        mediaRecorder.setCamera(camera);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
        mediaRecorder.setOutputFile(getOutputVideoFile(chunkCount + 1).getAbsolutePath());
        mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());

        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d("ikatun", e.toString());
        } catch (IOException e) {
            Log.d("ikatun", e.toString());
        }
    }

    public File getChunkPath() {
        return chunkCount == 0 ? null : getOutputVideoFile(chunkCount);
    }

    public void start() {
        if (isStartAllowed) {
            mediaRecorder.start();
            isStopAllowed = true;
            isStartAllowed = false;
        }
    }

    public void stop() {
        if (isStopAllowed) {
            mediaRecorder.stop();
            isStopAllowed = false;
            isStartAllowed = true;
            prepare();
            chunkCount++;
        }
    }

    public int getChunksCount() {
        return chunkCount;
    }

    private static File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM", "HttpCamera");
    private static File getOutputVideoFile(int index) {
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        return new File(mediaStorageDir.getPath() + File.separator + index);
    }
    private static void deleteOldVideoFiles() {
        if (mediaStorageDir.exists()) {
            for (File f : mediaStorageDir.listFiles()) {
                f.delete();
            }
        }
    }
}
