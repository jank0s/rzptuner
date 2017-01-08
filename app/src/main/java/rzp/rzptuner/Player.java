package rzp.rzptuner;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jan on 06/01/2017.
 */

public class Player {
    private double frequency;
    private int sampleRate;
    private volatile int bufferSize;
    private volatile int sampleCount;
    private AudioTrack audioTrack1;
    private byte [] buffer;
    private volatile List<Double> sound;

    public Player(double freq){
        frequency = freq;
        sampleRate = 44100;
        this.init();
        bufferSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        audioTrack1 = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STATIC);
    }

    public void init(){
        sampleCount = (int) Math.round(sampleRate / frequency);;
        sound = getTone(frequency, sampleRate);
        buffer = new byte[2 * sampleCount];
        int i = 0;
        for(double d : sound){
            short val = (short) (d * 32767);
            buffer[i++] = (byte) (val & 0x00ff);
            buffer[i++] = (byte) ((val & 0xff00) >>> 8);
        }
    }

    public void play(){
        audioTrack1.write(buffer, 0, buffer.length);
        audioTrack1.reloadStaticData();
        audioTrack1.setLoopPoints(0, buffer.length / 2 , -1);
        audioTrack1.play();
    }

    public void stop(){
        audioTrack1.stop();
    }

    private List<Double> getTone(double frequency, int sampleRate){
        List<Double> tone = new ArrayList<>();
        for (int i = 0; i < sampleCount; i++) {
            tone.add(Math.sin(2 * Math.PI * i / (sampleRate / frequency)));
        }
        return tone;
    }

    public void setFrequency(double frequency){
        this.frequency = frequency;
        init();
    }
}
