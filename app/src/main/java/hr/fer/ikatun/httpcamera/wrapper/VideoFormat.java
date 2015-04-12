package hr.fer.ikatun.httpcamera.wrapper;

import android.media.MediaRecorder;

/**
 * Created by ikatun on 11.4.2015..
 */
public enum VideoFormat {

    MPEG4_AAC_H264 {
        public int getOutputFormat() { return MediaRecorder.OutputFormat.MPEG_4; }
        public int getAudioEncoder() { return MediaRecorder.AudioEncoder.AAC; }
        public int getVideoEncoder() { return MediaRecorder.VideoEncoder.H264; }
    },
    WEBM_VORBIS_VP8 {
        public int getOutputFormat() { return MediaRecorder.OutputFormat.WEBM; }
        public int getAudioEncoder() { return MediaRecorder.AudioEncoder.VORBIS; }
        public int getVideoEncoder() { return MediaRecorder.VideoEncoder.VP8; }
    };

    public abstract int getOutputFormat();
    public abstract int getAudioEncoder();
    public abstract int getVideoEncoder();
}
