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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CurrentTemperatureActivity extends Activity {
    Activity mActivity;
    private ProgressDialog dialog;
    private Button mBtnGetTemperature;
    private TextView mTxtCurrentTemperature;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.current_temperature);
        mActivity = CurrentTemperatureActivity.this;

        dialog = new ProgressDialog(mActivity);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Connecting to server...");

        mTxtCurrentTemperature = (TextView) findViewById(R.id.txtCurrentTemperature);
        mBtnGetTemperature = (Button) findViewById(R.id.btnGetTemperature);
        mBtnGetTemperature.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                new fetchData().execute();
            }
        });
    }

    private class fetchData extends AsyncTask<Void, Void, String>{

        @Override
        protected void onPreExecute() {
            dialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result;
            int currentTemperature = 0;
            String timestamp = null;   //dow mon dd hh:mm:ss zzz yyyy
            SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
            URLConnection connection;
            try {
                connection = new URL("http://api.xively.com/v2/feeds/730000241/datastreams/current_tc").openConnection();
                connection.setRequestProperty("X-ApiKey", "5Eoigt8lr646STNFtBHRvpAD7zGVDVIgkotoEpxiIICCut7K");
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

                currentTemperature = json.getInt("current_value");
                timestamp = json.getString("at");
                timestamp = parseTimeStamp(timestamp);

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (timestamp == null){
                result = mSharedPreferences.getString(Utils.CURRENT_TEMPERATURE,"NONE");
                result += " @ ";
                result += mSharedPreferences.getString(Utils.CURRENT_TIMEPSTAMP,"NONE");
            }else{
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString(Utils.CURRENT_TEMPERATURE, Integer.toString(currentTemperature));
                editor.putString(Utils.CURRENT_TIMEPSTAMP, timestamp);
                editor.commit();
                result = Integer.toString(currentTemperature) + " @ " + timestamp;
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            dialog.dismiss();
            mTxtCurrentTemperature.setText(result);
        }

        private String parseTimeStamp(String timestamp){
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            formatter.setLenient(true);

            timestamp = timestamp.replaceAll("([0-9]*)(.)([0-9]*)(Z)", "$1$4");

            Date date = null;

            try {
                date = formatter.parse(timestamp);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return (date == null)? null:date.toString();
        }
    }

}
