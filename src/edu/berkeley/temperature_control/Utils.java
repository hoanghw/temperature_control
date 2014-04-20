package edu.berkeley.temperature_control;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
    static final String CURRENT_TEMPERATURE = "current_tc";
    static final String CURRENT_HUMIDITY = "current_h";
    static final String INSIDE_TEMPERATURE = "HP.0013a20040a8c307.tc";
    static final String INSIDE_HUMIDITY = "HP.0013a20040a8c307.h";
    static final String HEATER = "switch";
    static final String URL_GET_HEATER = "http://api.xively.com/v2/feeds/730000241/datastreams/switch";
    static final String URL_CURRENT_T = "http://api.xively.com/v2/feeds/730000241/datastreams/current_tc";
    static final String URL_CURRENT_H = "http://api.xively.com/v2/feeds/730000241/datastreams/current_h";
    static final String URL_INSIDE_T = "http://api.xively.com/v2/feeds/730000241/datastreams/HP.0013a20040a8c307.tc";
    static final String URL_INSIDE_H = "http://api.xively.com/v2/feeds/730000241/datastreams/HP.0013a20040a8c307.h";
    static final String URL_SET_HEATER = "https://api.xively.com/v2/feeds/730000241.json";
    static final String NO_RESULT = "None";
    static final int CHANNEL_TEMPERATURE_1 = 1;
    static final int CHANNEL_HUMIDITY_1 = 2;
    static final int CHANNEL_TEMPERATURE_2 = 3;
    static final int CHANNEL_HUMIDITY_2 = 4;
    static final int CHANNEL_HEATER = 5;
    static final String API_KEY = "5Eoigt8lr646STNFtBHRvpAD7zGVDVIgkotoEpxiIICCut7K";
    static final String TIMESTAMP_FORMAT ="yyyy-MM-dd'T'HH:mm:ss'Z'";
    static final String jsonString = "{\"version\":\"1.0.0\"," +
                                    "\"datastreams\" : [{\"id\" : \"example\","+
                                    "\"datapoints\":[{\"at\":\"2013-04-22T00:35:43Z\",\"value\":\"1\"}],"+
                                    "\"current_value\" : \"0\"}]}";

    static public JSONObject getJson(String id, String timestamp, String value){

        try{
            JSONObject result = new JSONObject();

            JSONObject dataPoint = new JSONObject();
            dataPoint.put("at", timestamp);
            dataPoint.put("value", value);

            JSONArray dataPoints = new JSONArray();
            dataPoints.put(dataPoint);

            JSONObject dataStream = new JSONObject();
            dataStream.put("id", id);
            dataStream.put("datapoints", dataPoints);
            dataStream.put("current_value", value);

            JSONArray dataStreams = new JSONArray();
            dataStreams.put(dataStream);

            result.put("version", "1.0.0");
            result.put("datastreams", dataStreams);

            return result;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    static public double cToF(double c){
        double raw = c*9/5+32;
        return ((int) raw*10)/10;
    }

    static public String appendC(String s){
        return s+" \u2103";
    }

    static public String appendF(String s){
        return s+" \u2109";
    }

    static public String appendP(String s){
        return s+" %";
    }

    static public String parseTimeStamp(String timestamp){
        SimpleDateFormat formatter = new SimpleDateFormat(TIMESTAMP_FORMAT);
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
