package rzp.rzptuner;

import static rzp.rzptuner.FFT.fft;

/**
 * Created by Lovro on 2. 01. 2017.
 */

public class Detector {
    private String noteName;
    private double freq;
    private double deviation;

    public void Detector(){
        noteName = "";
        deviation = 0.0;
        freq = 0.0;
    }

    public Detector getPitch(short[] buffer){

        //step 1 FFT
        Complex[] input = new Complex[buffer.length];
        for(int i = 0; i < buffer.length; i++){
            input[i] = new Complex(buffer[i], 0.0);
        }

        fft(input);
        double sum = 0;
        for(Complex c : input){
            sum += Math.sqrt(Math.pow(c.re, 2) + Math.pow(c.im,2));
        }

        double freq = sum/input.length;

        //step 2 find correct note
        Note note = new Note(freq);
        noteName = note.getNote();
        freq = note.getFrequency();
        deviation = note.getDifference();

        return this;
    }

    public String getNoteName() {
        return noteName;
    }

    public void setNoteName(String noteName) {
        this.noteName = noteName;
    }

    public double getDeviation() {
        return deviation;
    }

    public void setDeviation(double deviation) {
        this.deviation = deviation;
    }

    public double getFreq() {
        return freq;
    }

    public void setFreq(double freq) {
        this.freq = freq;
    }
}
