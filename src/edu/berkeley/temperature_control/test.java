package edu.berkeley.temperature_control;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class test{
    public static void main(String[] args){
        double currentTemperature;
        String timestamp;   //dow mon dd hh:mm:ss zzz yyyy
        URLConnection connection = null;
        try {
            connection = new URL("http://api.xively.com/v2/feeds/730000241/datastreams/current_h").openConnection();
            connection.setRequestProperty("X-ApiKey", "5Eoigt8lr646STNFtBHRvpAD7zGVDVIgkotoEpxiIICCut7K");
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder completeStream =  new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null){
                completeStream.append(inputLine);
            }
            in.close();
            System.out.println("Recieved from Xively:");
            System.out.println(completeStream.toString());
            JSONObject json = new JSONObject(completeStream.toString());

            currentTemperature = json.getDouble("current_value");
            timestamp = json.getString("at");
            timestamp = parseTimeStamp(timestamp);

        } catch (Exception e) {
            e.printStackTrace();
        }



        String timestamp1 = new SimpleDateFormat(Utils.TIMESTAMP_FORMAT).format(new Date());
        HttpURLConnection connection1;
        try {
            connection1 = (HttpURLConnection) new URL(Utils.URL_SET_HEATER).openConnection();
            connection1.setRequestProperty("X-ApiKey", Utils.API_KEY);
            connection1.setRequestMethod("PUT");
            connection1.setDoOutput(true);

            JSONObject json = Utils.getJson(Utils.HEATER, timestamp1, "0");

            System.out.println(json.toString());
            OutputStreamWriter out = new OutputStreamWriter(
                    connection1.getOutputStream());
            out.write(json.toString());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    static public String parseTimeStamp(String timestamp){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        formatter.setLenient(true);

        timestamp = timestamp.replaceAll("([0-9]*)(.)([0-9]*)(Z)", "$1$4");

        Date date = null;

        try {
            date = formatter.parse(timestamp);
        } catch (ParseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return (date == null)? "Cannot parse":date.toString();
    }


}

