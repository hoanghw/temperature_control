package edu.berkeley.temperature_control;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class CurrentTemperatureActivity extends Activity {
    Activity mActivity;
    private ProgressDialog dialog;
    private TextView mTxtTemperature1C;
    private TextView mTxtTemperature1F;
    private TextView mTxtTemperature2C;
    private TextView mTxtTemperature2F;
    private TextView mTxtHumidity1;
    private TextView mTxtHumidity2;
    Button mBtnRefresh;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivity = CurrentTemperatureActivity.this;

        dialog = new ProgressDialog(mActivity);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Connecting to server...");

        mTxtTemperature1C = (TextView) findViewById(R.id.temperature_1_c);
        mTxtTemperature1F = (TextView) findViewById(R.id.temperature_1_f);
        mTxtTemperature2C = (TextView) findViewById(R.id.temperature_2_c);
        mTxtTemperature2F = (TextView) findViewById(R.id.temperature_2_f);
        mTxtHumidity1 = (TextView) findViewById(R.id.humidity_1);
        mTxtHumidity2 = (TextView) findViewById(R.id.humidity_2);

        mBtnRefresh = (Button) findViewById(R.id.btn_refresh);
        mBtnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new FetchData(Utils.CHANNEL_TEMPERATURE_1, false, new TaskFinished() {
                    @Override
                    public void onTaskFinished(String result) {
                        if (result == null){
                            //SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
                            //result = mSharedPreferences.getString(Utils.CURRENT_TEMPERATURE, null);
                            mTxtTemperature1C.setText(Utils.NO_RESULT);
                            mTxtTemperature1F.setText(Utils.NO_RESULT);
                        }else{
                            mTxtTemperature1C.setText(Utils.appendC(result));
                            mTxtTemperature1F.setText(Utils.appendF(Double.toString(Utils.cToF(Double.parseDouble(result)))));
                        }
                    }
                }).execute();

                new FetchData(Utils.CHANNEL_TEMPERATURE_2, false, new TaskFinished() {
                    @Override
                    public void onTaskFinished(String result) {
                        if (result == null){
                            mTxtTemperature2C.setText(Utils.NO_RESULT);
                            mTxtTemperature2F.setText(Utils.NO_RESULT);
                        }else{
                            mTxtTemperature2C.setText(Utils.appendC(result));
                            mTxtTemperature2F.setText(Utils.appendF(Double.toString(Utils.cToF(Double.parseDouble(result)))));
                        }
                    }
                }).execute();

                new FetchData(Utils.CHANNEL_HUMIDITY_1, false, new TaskFinished() {
                    @Override
                    public void onTaskFinished(String result) {
                        if (result == null){
                            mTxtHumidity1.setText(Utils.NO_RESULT);
                        }else{
                            mTxtHumidity1.setText(Utils.appendP(result));
                        }
                    }
                }).execute();

                new FetchData(Utils.CHANNEL_HUMIDITY_2, false, new TaskFinished() {
                    @Override
                    public void onTaskFinished(String result) {
                        if (result == null){
                            mTxtHumidity2.setText(Utils.NO_RESULT);
                        }else{
                            mTxtHumidity2.setText(Utils.appendP(result));
                        }
                    }
                }).execute();
            }
        });

        mBtnRefresh.callOnClick();
    }

    interface TaskFinished{
        void onTaskFinished(String result);
    }

    private class FetchData extends AsyncTask<Void, Void, String>{
        private boolean showLoading = false;
        private String urlString = null;
        private String node = null;
        private TaskFinished callBack;

        public FetchData(int mode, boolean showLoading, TaskFinished callBack){
            super();
            this.showLoading = showLoading;
            this.callBack = callBack;

            switch(mode){
                case Utils.CHANNEL_HUMIDITY_1:
                    urlString = Utils.URL_CURRENT_H;
                    node = Utils.CURRENT_HUMIDITY;
                    break;
                case Utils.CHANNEL_HUMIDITY_2:
                    urlString = Utils.URL_INSIDE_H;
                    node = Utils.INSIDE_HUMIDITY;
                    break;
                case Utils.CHANNEL_TEMPERATURE_1:
                    urlString = Utils.URL_CURRENT_T;
                    node = Utils.CURRENT_TEMPERATURE;
                    break;
                case Utils.CHANNEL_TEMPERATURE_2:
                    urlString = Utils.URL_INSIDE_T;
                    node = Utils.INSIDE_TEMPERATURE;
                    break;
                default:

            }
        }

        @Override
        protected void onPreExecute() {
            if (showLoading){
                dialog.show();
            }
        }

        @Override
        protected String doInBackground(Void... parameter) {

            if (urlString == null) return null;

            String result;
            double currentTemperature = 0;
            String timestamp = null;   //dow mon dd hh:mm:ss zzz yyyy
            SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
            URLConnection connection;
            try {
                connection = new URL(urlString).openConnection();
                connection.setRequestProperty("X-ApiKey", Utils.API_KEY);
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder completeStream =  new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null){
                    completeStream.append(inputLine);
                }
                in.close();
                //System.out.println("Recieved from Xively:");
                //System.out.println(completeStream.toString());
                JSONObject json = new JSONObject(completeStream.toString());

                currentTemperature = json.getDouble("current_value");
                timestamp = json.getString("at");
                timestamp = Utils.parseTimeStamp(timestamp);

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (timestamp == null){
                result = null;
                //result = mSharedPreferences.getString(node,null);
                //timestamp = mSharedPreferences.getString(node + "_at","NONE");
            }else{
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString(node, Double.toString(currentTemperature));
                editor.putString(node + "_at", timestamp);
                editor.commit();
                result = Double.toString(currentTemperature);
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (showLoading){
                dialog.dismiss();
            }
            callBack.onTaskFinished(result);
        }
    }

}
