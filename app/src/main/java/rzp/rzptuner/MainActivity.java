package rzp.rzptuner;

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

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getCanonicalName();

    private Button buttonStart;
    private TextView tvResult;
    private boolean running;
    private int sampleRate;
    private int bufferSize;
    private volatile int readSize;
    private volatile short [] buffer;
    private volatile Note currentNote;


    TunerTask task;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get references for UI elemtents
        buttonStart = (Button) findViewById(R.id.buttonStart);
        tvResult = (TextView) findViewById(R.id.tvResult);

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
                    buttonStart.setText("Start");
                    tvResult.setText("");
                    task.cancel(false);
                }

            }
        });

    }

    private class TunerTask extends AsyncTask<Void, String, Void> {
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
                Log.d(TAG,"Deviation: " + d.getDeviation());

                //PUBLISH RESULT
                if(d.getFrequency() > -1) {
                    publishProgress(Double.valueOf(d.getFrequency()).toString() + "   " + d.getNote() + "  " + d.getPosition() + " " + d.getDeviation());
                }
                //FAKE DELAY FOR TEST
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            audioRecord.stop();
            return null;
        }

        @Override
        protected void onProgressUpdate(String... params){
            //VISUALIZE RESULT
            tvResult.setText(params[0] + "");
        }

        @Override
        protected void onPostExecute(Void aVoid){
            tvResult.setText("Konec");
        }

    }



}
