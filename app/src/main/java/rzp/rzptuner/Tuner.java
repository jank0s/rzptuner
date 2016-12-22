package rzp.rzptuner;

/**
 * Created by Lovro on 20. 12. 2016.
 */

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;


public class Tuner {

    private int sampleRate;
    private int bufferSize;
    private volatile int readSize;
    private volatile float [] intermediaryBuffer;
    private volatile float [] buffer;
    private AudioRecord audioRecord;
    private boolean isRecording;
    private volatile Note currentNote;
    private volatile Handler handler;

    public Tuner(){
        bufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
        readSize = bufferSize/2;
        buffer = new float[readSize];
        intermediaryBuffer = new float[readSize];
        isRecording = false;
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, sampleRate, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        currentNote = new Note(Note.DEFAULT_FREQUENCY);
        handler = new Handler(Looper.getMainLooper());
    }

    public void start(){
        isRecording = true;
        if(audioRecord!=null){
            audioRecord.startRecording();
        }
    }

    public void stop(){
        isRecording = false;
        if(audioRecord!=null){
            audioRecord.stop();
        }
    }
}
