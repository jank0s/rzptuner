package rzp.rzptuner;

import android.media.AudioFormat;
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
    private AudioTrack track;
    private volatile List<Double> sound;

    public Player(){
        frequency = 440.0;
        sampleRate = 44100;
        bufferSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        sampleCount = bufferSize;
    }

    public byte[] getBufferTone(double frequency){
        sound = getTone(frequency, sampleRate);
        byte[] buffer = new byte[2 * sound.size()];
        int i = 0;
        for(double d : sound){
            short val = (short) (d * 32767);
            buffer[i++] = (byte) (val & 0x00ff);
            buffer[i++] = (byte) ((val & 0xff00) >>> 8);
        }
        return buffer;
    }

    private List<Double> getTone(double frequency, int sampleRate){
        List<Double> tone = new ArrayList<>();
        for (int i = 0; i < sampleCount; i++) {
            tone.add(Math.sin(2 * Math.PI * i / (sampleRate / frequency)));
        }
        return tone;
    }
}
