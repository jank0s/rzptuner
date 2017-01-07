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

import static android.Manifest.permission;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.graphics.Color.GREEN;
import static android.graphics.Color.RED;
import static android.graphics.Color.YELLOW;
import static android.os.Build.VERSION;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES;
import static android.os.Build.VERSION_CODES.M;
import static android.util.Log.d;
import static android.view.View.INVISIBLE;
import static android.view.View.OnClickListener;
import static android.view.View.VISIBLE;
import static com.cardiomood.android.controls.gauge.SpeedometerGauge.LabelConverter;
import static java.lang.Math.round;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static rzp.rzptuner.R.id;
import static rzp.rzptuner.R.layout;
import static rzp.rzptuner.R.layout.activity_main;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getCanonicalName();

    private Button buttonStart;
    private Button buttonPlay;
    private Button buttonPlus;
    private Button buttonMinus;
    private Button buttonPrev;
    private Button buttonNext;
    private TextView tvResult;
    private TextView tvNoteResult;
    private TextView tvNoteResultPosition;
    private TextView tvFrequencyResult;
    private SpeedometerGauge gauge;
    private volatile boolean running;
    private volatile boolean playing;
    private int sampleRate;
    private int sampleCount;
    private double frequency;
    private int bufferSize;
    private volatile int readSize;
    private volatile short [] buffer;
    private volatile Note currentNote;
    private volatile double offset;
    private volatile Player player;


    TunerTask task;

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_main);

        //Get references for UI elemtents
        buttonStart = (Button) findViewById(id.buttonStart);
        buttonPlay = (Button) findViewById(id.buttonPlay);
        buttonPlus = (Button) findViewById(id.buttonPlus);
        buttonMinus = (Button) findViewById(id.buttonMinus);
        buttonPrev = (Button) findViewById(id.buttonPrev);
        buttonNext = (Button) findViewById(id.buttonNext);
        tvResult = (TextView) findViewById(id.tvResult);
        tvNoteResult = (TextView) findViewById(id.tvNoteResult);
        tvNoteResultPosition = (TextView) findViewById(id.tvNoteResultPosition);
        tvFrequencyResult = (TextView) findViewById(id.tvFrequencyResult);

        gauge = (SpeedometerGauge) findViewById(id.gauge);

        // Add label converter
        gauge.setLabelConverter(new LabelConverter() {
            @Override
            public String getLabelFor(double progress, double maxProgress) {
                return valueOf((int) round(progress));
            }
        });

        // configure value range and ticks
        gauge.setMaxSpeed(100);
        gauge.setMajorTickStep(30);
        gauge.setMinorTicks(2);

        // Configure value range colors
        gauge.addColoredRange(0, 25, RED);
        gauge.addColoredRange(25, 40, YELLOW);
        gauge.addColoredRange(40, 60, GREEN);
        gauge.addColoredRange(60, 75, YELLOW);
        gauge.addColoredRange(75, 100, RED);

        gauge.setSpeed(50.0);
        tvResult.setText("0 %");
        tvResult.setTextColor(GREEN);
        currentNote = new Note(440.0);
        tvFrequencyResult.setText(format("%.2f Hz", currentNote.getFrequency()));
        tvNoteResult.setText(currentNote.getNote());
        tvNoteResultPosition.setText("" + currentNote.getPosition());

        player = new Player(currentNote.getFrequency() + offset);

        //Set offset buttons on click listener
        buttonMinus.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                offset -= 1.0;
                tvFrequencyResult.setText(format("%.2f Hz", currentNote.getFrequency() + offset));
                tvNoteResultPosition.setText("" + currentNote.getPosition());
                if (playing) {
                    player.stop();
                    player.setFrequency(currentNote.getFrequency() + offset);
                    player.play();
                }
            }
        });
        buttonPlus.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                offset += 1.0;
                tvFrequencyResult.setText(format("%.2f Hz", currentNote.getFrequency() + offset));
                tvNoteResultPosition.setText("" + currentNote.getPosition());
                if (playing) {
                    player.stop();
                    player.setFrequency(currentNote.getFrequency() + offset);
                    player.play();
                }
            }
        });

        //Set note selection (Next, Previous) buttons on click listener
        buttonPrev.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                double newFreq = currentNote.getNoteBelowFrequency();
                if (newFreq > 0) {
                    currentNote = new Note(newFreq);
                    tvFrequencyResult.setText(format("%.2f Hz", currentNote.getFrequency() + offset));
                    tvNoteResultPosition.setText("" + currentNote.getPosition());
                    tvNoteResult.setText(currentNote.getNote());
                    if (playing) {
                        player.stop();
                        player.setFrequency(currentNote.getFrequency() + offset);
                        player.play();
                    }
                }
            }
        });
        buttonNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                double newFreq = currentNote.getNoteAboveFrequency();
                if (newFreq > 0) {
                    currentNote = new Note(newFreq);
                    tvFrequencyResult.setText(format("%.2f Hz", currentNote.getFrequency() + offset));
                    tvNoteResultPosition.setText("" + currentNote.getPosition());
                    tvNoteResult.setText(currentNote.getNote());
                    if (playing) {
                        player.stop();
                        player.setFrequency(currentNote.getFrequency() + offset);
                        player.play();
                    }
                }
            }
        });


        //Set on click listener for detect button
        buttonStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SDK_INT >= M) {
                    int hasAudioRecordPermission = checkSelfPermission(RECORD_AUDIO);
                    if (hasAudioRecordPermission != PERMISSION_GRANTED) {
                        requestPermissions(new String[]{RECORD_AUDIO},
                                REQUEST_CODE_ASK_PERMISSIONS);
                        return;
                    }
                }
                if (!running) {
                    running = true;
                    buttonPlay.setEnabled(false);
                    buttonPrev.setVisibility(INVISIBLE);
                    buttonNext.setVisibility(INVISIBLE);
                    buttonStart.setText("Stop");
                    task = new TunerTask();
                    task.execute();
                } else {
                    running = false;
                    task.cancel(false);
                    buttonStart.setText("Detect");
                    tvResult.setText("");
                    gauge.setSpeed(50.0);
                    tvResult.setText("0 %");
                    tvResult.setTextColor(GREEN);
                    //currentNote = new Note(440.0);
                    //tvFrequencyResult.setText(String.format("%.2f Hz", currentNote.getFrequency() + offset));
                    //tvNoteResultPosition.setText(currentNote.getPosition());
                    tvNoteResult.setText(currentNote.getNote());
                    buttonPlay.setEnabled(true);
                    buttonPrev.setVisibility(VISIBLE);
                    buttonNext.setVisibility(VISIBLE);
                }

            }
        });

        //Set on lick listener for play button
        buttonPlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!playing) {
                    buttonStart.setEnabled(false);
                    playing = true;
                    buttonPlay.setText("Stop");
                    player.setFrequency(currentNote.getFrequency() + offset);
                    player.play();
                } else {
                    playing = false;
                    buttonPlay.setText("Play");
                    player.stop();
                    gauge.setSpeed(50.0);
                    tvResult.setText("0 %");
                    tvResult.setTextColor(GREEN);
                    tvNoteResult.setText(currentNote.getNote());
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
        protected void onProgressUpdate(Detector... params) {
            //VISUALIZE RESULT
            Detector d = params[0];
            double speed = d.getDeviation() < -50 ? 0 : (d.getDeviation() > 50 ? 100 : d.getDeviation() + 50.0);
            gauge.setSpeed(speed);
            double deviation = d.getDeviation();
            tvResult.setText(format("%.2f %%", deviation));
            if (deviation == 0) {
                tvResult.setTextColor(GREEN);
            } else {
                tvResult.setTextColor(RED);
            }
            tvNoteResult.setText(d.getNote());
            currentNote = d.getNoteDetect();
            tvFrequencyResult.setText(format("%6.2f Hz", currentNote.getFrequency() + offset));
            tvNoteResultPosition.setText("" + currentNote.getPosition());
            d(TAG, "Published => Frequency: " + d.getFrequency() + " Note: " + d.getNote() + "  Position:" + d.getPosition() + " Deviation:" + d.getDeviation());
        }

        @Override
        protected void onPostExecute(Void aVoid){
            tvResult.setText("Konec");
        }

    }
}
