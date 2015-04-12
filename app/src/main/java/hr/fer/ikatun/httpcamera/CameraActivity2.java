package hr.fer.ikatun.httpcamera;

import android.app.Activity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;

import android.widget.FrameLayout;

import java.io.IOException;

import hr.fer.ikatun.httpcamera.wrapper.CameraReader;
import hr.fer.ikatun.httpcamera.wrapper.ChunkWriter;
import hr.fer.ikatun.httpcamera.wrapper.VideoFormat;


public class CameraActivity2 extends Activity {
    CameraReader cameraReader;

    // Add a listener to the Capture button

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);

        cameraReader = new CameraReader(this, (FrameLayout) findViewById(R.id.camera_preview));
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    public void toggleCapture(View view) {
        if (cameraReader.isReading()) {
            cameraReader.stopReading();
        } else {
            try {
                cameraReader.startReading(new ChunkWriter() {
                    @Override
                    public void writeChunk(byte[] data, int offset, int len) {
                        Log.d("ikatun", "Written " + len + " bytes from camera");
                    }

                    @Override
                    public void onReadingStopped(Exception e) {
                        Log.d("ikatun", "Camera finished");
                    }
                }, VideoFormat.MPEG4_AAC_H264);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
