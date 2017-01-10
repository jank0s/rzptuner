package rzp.rzptuner;

public class Note{
    private String note;
    private double frequency;
    private double actualFrequency;
    private int position;
    private double difference;
    private double noteBelowFrequency;
    private double noteAboveFrequency;
    private int index;

    public static final double DEFAULT_FREQUENCY = 440.000;
    public static final double UNKNOWN_FREQUENCY = -1;
    public static final int UNKNOWN_POSITION = -1;
    public static final String UNKNOWN_NOTE = "";

    public static final double[] PIANO_NOTE_FREQUENCIES = new double[]{27.5000, 29.1352, 30.8677,
            32.7032, 34.6478, 36.7081, 38.8909, 41.2034, 43.6535, 46.2493, 48.9994, 51.9131, 55.0000,
            58.2705, 61.7354, 65.4064, 69.2957, 73.4162, 77.7817, 82.4069, 87.3071, 92.4986, 97.9989,
            103.826, 110.000, 116.541, 123.471, 130.813, 138.591, 146.832, 155.563, 164.814, 174.614,
            184.997, 195.998, 207.652, 220.000, 233.082, 246.942, 261.626, 277.183, 293.665, 311.127,
            329.628, 349.228, 369.994, 391.995, 415.305, 440.000, 466.164, 493.883, 523.251, 554.365,
            587.330, 622.254, 659.255, 698.456, 739.989, 783.991, 830.609, 880.000, 932.328, 987.767,
            1046.50, 1108.73, 1174.66, 1244.51, 1318.51, 1396.91, 1479.98, 1567.98, 1661.22, 1760.00,
            1864.66, 1975.53, 2093.00, 2217.46, 2349.32, 2489.02, 2637.02, 2793.83, 2959.96, 3135.96,
            3322.44, 3520.00, 3729.31, 3951.07, 4186.01};

    public static final String[] NOTES = new String[]{"A", "A\u266F", "B", "C", "C\u266F", "D",
            "D\u266F", "E", "F", "F\u266F", "G", "G\u266F"};

    public Note(double frequency){
        this.actualFrequency = frequency;
        init(frequency);
    }

    private void init(double frequency){
        if(frequency < 27.5000 || frequency > 4186.01){
            this.frequency = UNKNOWN_FREQUENCY;
            this.position = UNKNOWN_POSITION;
            this.note = UNKNOWN_NOTE;
            this.noteAboveFrequency = 29.1352;
            this.noteBelowFrequency = UNKNOWN_FREQUENCY;
            this.index = -1;
        }else{
            this.index = searchForNote(this.actualFrequency, PIANO_NOTE_FREQUENCIES,
                    0, PIANO_NOTE_FREQUENCIES.length);
            this.frequency = PIANO_NOTE_FREQUENCIES[index];
            if(index - 1 >= 0) {
                this.noteBelowFrequency = PIANO_NOTE_FREQUENCIES[index - 1];
            }
            if(index + 1 < PIANO_NOTE_FREQUENCIES.length) {
                this.noteAboveFrequency = PIANO_NOTE_FREQUENCIES[index + 1];
            }
            this.note = getNoteFromIndex(index);
            this.position = getPositionFromIndex(index);
        }
        this.difference = this.actualFrequency - this.frequency;
    }


    private int searchForNote(double frequency, double[] array, int minIndex, int maxIndex){
        if(minIndex + 1 >= maxIndex){
            return minIndex;
        }

        int pivot = (minIndex + (maxIndex - 1)) / 2;
        double midValue = array[pivot];
        double belowValue = ( pivot-1 > 0 )? array[pivot-1] : array[pivot];
        double aboveValue = ( pivot+1 < array.length )? array[pivot+1] : array[pivot];
        double fromValue = midValue - (midValue - belowValue) / 2;
        double toValue = midValue + (aboveValue - midValue) / 2;

        if(frequency >= fromValue && frequency < toValue){
            return pivot;
        }else if(frequency < fromValue){
            return searchForNote(frequency, array, minIndex, pivot);
        }else{
            return searchForNote(frequency, array, pivot + 1, maxIndex);
        }
    }

    private int getPositionFromIndex(int index){
        if(index < 3){
            return 0;
        }else if(index < 15){
            return 1;
        }else if(index < 27){
            return 2;
        }else if(index < 39){
            return 3;
        }else if(index < 51){
            return 4;
        }else if(index < 63){
            return 5;
        }else if(index < 75){
            return 6;
        }else if(index < 87){
            return 7;
        }else{
            return 8;
        }
    }

    private String getNoteFromIndex(int index){
        if(index < 12) {
            return NOTES[index];
        }else{
            return NOTES[index % 12];
        }
    }

    public String getNote() {
        return note;
    }

    public double getFrequency() {
        return frequency;
    }

    public double getActualFrequency() {
        return actualFrequency;
    }

    public int getPosition() {
        return position;
    }

    public double getDifference() {
        return difference;
    }

    public double getNoteBelowFrequency() {
        return noteBelowFrequency;
    }

    public double getNoteAboveFrequency() {
        return noteAboveFrequency;
    }
}
