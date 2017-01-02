package rzp.rzptuner;

/**
 * Created by Lovro on 2. 01. 2017.
 */

public class Detector {
    private String noteName;
    private double deviation;

    public void Detector(){
        noteName = "";
        deviation = 0.0;
    }

    public void getPitch(float[] buffer){

        //step 1 FFT


        //step 2 find correct note
        Note note = new Note(440.0);
        noteName = note.getNote();
        deviation = note.getDifference();
    }

    private void FFT(){

    }

}
