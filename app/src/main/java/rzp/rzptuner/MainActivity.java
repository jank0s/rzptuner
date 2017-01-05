package rzp.rzptuner;

import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
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
    private TextView tvResult;
    private TextView tvNoteResult;
    private TextView tvFrequencyResult;
    private boolean running;
    private int sampleRate;
    private int bufferSize;
    private volatile int readSize;
    private volatile short [] buffer;
    private volatile Note currentNote;
    private SpeedometerGauge gauge;


    TunerTask task;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get references for UI elemtents
        buttonStart = (Button) findViewById(R.id.buttonStart);
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

        //Set on lick listener for start button
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!running){
                    running = true;
                    buttonStart.setText("Stop");
                    task = new TunerTask();
                    task.execute();
                }else{
                    running = false;
                    task.cancel(false);
                    buttonStart.setText("Start");
                    tvResult.setText("");
                    gauge.setSpeed(50.0);
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
            sampleRate = 11025;
            bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
            readSize = bufferSize;
            buffer = new short[readSize];   // length = 1024
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, sampleRate, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
            currentNote = new Note(Note.DEFAULT_FREQUENCY);
        }

        @Override
        protected Void doInBackground(Void... params) {
            audioRecord.startRecording();
            while(!isCancelled()){
                //PROCESS AUDIO
                audioRecord.read(buffer, 0, readSize);
                Detector d = new Detector();
                d.getPitch(buffer, sampleRate);

                i++;
                Log.d(TAG, "Frequency: "+d.getFrequency() + " Note: " + d.getNote() + "  Position:" + d.getPosition() + " Deviation:" + d.getDeviation());

                //PUBLISH RESULT
                if(d.getFrequency() > -1) {
                    publishProgress(d);
                }

                //sleep interval
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
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
            tvResult.setText(String.format("%.2f", deviation));
            if(deviation == 0){
                tvResult.setTextColor(Color.GREEN);
            }else{
                tvResult.setTextColor(Color.RED);
            }
            tvNoteResult.setText(d.getNote());
            tvFrequencyResult.setText(String.format("%6.2f Hz", d.getFrequency()));
            Log.d(TAG, "Published => Frequency: "+d.getFrequency() + " Note: " + d.getNote() + "  Position:" + d.getPosition() + " Deviation:" + d.getDeviation());
        }

        @Override
        protected void onPostExecute(Void aVoid){
            tvResult.setText("Konec");
        }

    }



}
