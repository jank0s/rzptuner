package rzp.rzptuner;

import static rzp.rzptuner.FFT.fft;

/**
 * Created by Lovro on 2. 01. 2017.
 */

public class Detector {
    private String note;
    private double frequency;
    private double deviation;
    private int position;

    public void Detector(){
        note = "";
        position = 0;
        deviation = 0.0;
        frequency = 0.0;
    }

    public void getPitch(short[] buffer, int sampleRate){

        //step 1 FFT
        Complex[] input = new Complex[buffer.length];
        for(int i = 0; i < buffer.length; i++){
            input[i] = new Complex(buffer[i], 0.0);
        }

        fft(input);
        double maxPeak = 0;
        int index  = 0;
        int counter = 0;
        for(Complex c : input){
            double peak = Math.sqrt(Math.pow(c.re, 2) + Math.pow(c.im,2));
            if(peak > maxPeak){
                maxPeak = peak;
                index = counter;
            }
            counter++;
        }

        //step2 convert fft to frequency
        double freq = (index * sampleRate)/input.length;

        //step 3 find note
        Note noteDetect = new Note(freq);
        note = noteDetect.getNote();
        frequency = noteDetect.getFrequency();
        deviation = noteDetect.getDifference();
        position = noteDetect.getPosition();
    }

    public double getDeviation() {
        return deviation;
    }

    public void setDeviation(double deviation) {
        this.deviation = deviation;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public double getFrequency() {
        return frequency;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
