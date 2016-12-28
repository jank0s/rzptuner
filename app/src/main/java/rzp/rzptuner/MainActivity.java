package rzp.rzptuner;

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
                    task.cancel(true);
                }

            }
        });

    }

    private class TunerTask extends AsyncTask<Void, Double, Void> {
        int i;

        public TunerTask(){
            super();
            i = 0;
        }

        @Override
        protected Void doInBackground(Void... params) {
            while(!isCancelled()){
                //PROCESS AUDIO
                i++;

                //PUBLISH RESULT
                publishProgress(Double.valueOf(i));

                //FAKE DELAY FOR TEST
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Double... params){
            //VISUALIZE RESULT
            tvResult.setText(params[0] + "");
        }

        @Override
        protected void onPostExecute(Void aVoid){
            tvResult.setText("Konec");
        }

    }



}
