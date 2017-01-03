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
    private volatile short [] buffer;
    private AudioRecord audioRecord;
    private boolean isRecording;
    private volatile Note currentNote;
    private volatile Handler handler;

    public Tuner(){
        bufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
        readSize = bufferSize/2;
        buffer = new short[readSize];
        isRecording = false;
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, sampleRate, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        currentNote = new Note(Note.DEFAULT_FREQUENCY);
        handler = new Handler(Looper.getMainLooper());
    }

    public void start(){
        isRecording = true;
        if(audioRecord!=null){
            audioRecord.startRecording();
            findNote();
        }
    }

    public void stop(){
        isRecording = false;
        if(audioRecord!=null){
            audioRecord.stop();
        }
    }

    private void findNote(){
//        while (isRecording){
//            audioRecord.read(buffer, 0, readSize);
//            Detector d = new Detector();
//            Detector note = d.getPitch(buffer);
//        }
    }
}
