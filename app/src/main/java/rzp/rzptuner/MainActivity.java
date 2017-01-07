package rzp.rzptuner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.cardiomood.android.controls.gauge.SpeedometerGauge;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getCanonicalName();

    private Button buttonStart;
    private Button buttonPlay;
    private Button buttonPlus;
    private Button buttonMinus;
    private TextView tvResult;
    private TextView tvNoteResult;
    private TextView tvFrequencyResult;
    private SpeedometerGauge gauge;
    private boolean running;
    private boolean playing;
    private int sampleRate;
    private int sampleCount;
    private double frequency;
    private int bufferSize;
    private volatile int readSize;
    private volatile short [] buffer;
    private volatile Note currentNote;
    private volatile double currentFrequency;
    private volatile double offset;
    private Player player;


    TunerTask task;

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get references for UI elemtents
        buttonStart = (Button) findViewById(R.id.buttonStart);
        buttonPlay = (Button) findViewById(R.id.buttonPlay);
        buttonPlus = (Button) findViewById(R.id.buttonPlus);
        buttonMinus = (Button) findViewById(R.id.buttonMinus);
        tvResult = (TextView) findViewById(R.id.tvResult);
        tvNoteResult = (TextView) findViewById(R.id.tvNoteResult);
        tvFrequencyResult = (TextView) findViewById(R.id.tvFrequencyResult);

        gauge = (SpeedometerGauge) findViewById(R.id.gauge);

        // Add label converter
        gauge.setLabelConverter(new SpeedometerGauge.LabelConverter() {
            @Override
            public String getLabelFor(double progress, double maxProgress) {
                return String.valueOf((int) Math.round(progress));
            }
        });

        // configure value range and ticks
        gauge.setMaxSpeed(100);
        gauge.setMajorTickStep(30);
        gauge.setMinorTicks(2);

        // Configure value range colors
        gauge.addColoredRange(0, 25, Color.RED);
        gauge.addColoredRange(25, 40, Color.YELLOW);
        gauge.addColoredRange(40, 60, Color.GREEN);
        gauge.addColoredRange(60, 75, Color.YELLOW);
        gauge.addColoredRange(75, 100, Color.RED);

        gauge.setSpeed(50.0);
        tvResult.setText("0 %");
        tvResult.setTextColor(Color.GREEN);
        currentFrequency = 440.0;
        tvFrequencyResult.setText(String.format("%.2f Hz", currentFrequency));
        tvNoteResult.setText("A");

        //Set offset buttons on click listener
        buttonMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                offset -= 1.0;
                tvFrequencyResult.setText(String.format("%.2f Hz", currentFrequency + offset));
            }
        });
        buttonPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                offset += 1.0;
                tvFrequencyResult.setText(String.format("%.2f Hz", currentFrequency + offset));
            }
        });


        //Set on click listener for detect button
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    int hasAudioRecordPermission = checkSelfPermission(Manifest.permission.RECORD_AUDIO);
                    if (hasAudioRecordPermission != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},
                                REQUEST_CODE_ASK_PERMISSIONS);
                        return;
                    }
                }
                if(!running){
                    running = true;
                    buttonPlay.setEnabled(false);
                    buttonStart.setText("Stop");
                    task = new TunerTask();
                    task.execute();
                }else{
                    running = false;
                    task.cancel(false);
                    buttonStart.setText("Detect");
                    tvResult.setText("");
                    gauge.setSpeed(50.0);
                    tvResult.setText("0 %");
                    tvResult.setTextColor(Color.GREEN);
                    currentFrequency = 440.0;
                    tvFrequencyResult.setText(String.format("%.2f Hz", currentFrequency + offset));
                    tvNoteResult.setText("A");
                    buttonPlay.setEnabled(true);
                }

            }
        });

        //Set on lick listener for play button
        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!playing){
                    buttonStart.setEnabled(false);
                    playing = true;
                    buttonPlay.setText("Stop");
                    player = new Player(currentFrequency + offset);
                    player.play();
                }else{
                    playing = false;
                    buttonPlay.setText("Play");
                    player.stop();
                    gauge.setSpeed(50.0);
                    tvResult.setText("0 %");
                    tvResult.setTextColor(Color.GREEN);
                    currentFrequency = 440.0;
                    tvFrequencyResult.setText(String.format("%.2f Hz", currentFrequency + offset));
                    tvNoteResult.setText("A");
                    buttonStart.setEnabled(true);
                }

            }
        });

    }

    private class TunerTask extends AsyncTask<Void, Detector, Void> {
        int i;
        private AudioRecord audioRecord;

        public TunerTask(){
            super();
            i = 0;
            sampleRate = 11025;  // options [11025, 22050, 44100]
            bufferSize = 4096;  // size = 2^x
            readSize = bufferSize;
            buffer = new short[readSize];
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, sampleRate, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
            currentNote = new Note(Note.DEFAULT_FREQUENCY);
        }

        @Override
        protected Void doInBackground(Void... params) {
            audioRecord.startRecording();
            while(!isCancelled()){
                //PROCESS AUDIO
                audioRecord.read(buffer, 0, readSize);
                Detector d = new Detector(offset);
                d.getPitch(buffer, sampleRate);

                i++;
                Log.d(TAG, "Frequency: "+d.getFrequency() + " Note: " + d.getNote() + "  Position:" + d.getPosition() + " Deviation:" + d.getDeviation());

                //PUBLISH RESULT
                if(d.getFrequency() > -1) {
                    publishProgress(d);
                }
            }
            audioRecord.stop();
            return null;
        }

        @Override
        protected void onProgressUpdate(Detector... params){
            //VISUALIZE RESULT
            Detector d = params[0];
            double speed = d.getDeviation() < -50 ? 0 : (d.getDeviation() > 50? 100 : d.getDeviation() + 50.0);
            gauge.setSpeed(speed);
            double deviation = d.getDeviation();
            tvResult.setText(String.format("%.2f %%", deviation));
            if(deviation == 0){
                tvResult.setTextColor(Color.GREEN);
            }else{
                tvResult.setTextColor(Color.RED);
            }
            tvNoteResult.setText(d.getNote());
            currentFrequency = d.getFrequency();
            tvFrequencyResult.setText(String.format("%6.2f Hz", currentFrequency + offset));
            Log.d(TAG, "Published => Frequency: "+d.getFrequency() + " Note: " + d.getNote() + "  Position:" + d.getPosition() + " Deviation:" + d.getDeviation());
        }

        @Override
        protected void onPostExecute(Void aVoid){
            tvResult.setText("Konec");
        }

    }
}
