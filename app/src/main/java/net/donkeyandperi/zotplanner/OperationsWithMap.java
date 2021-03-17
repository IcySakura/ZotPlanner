package net.donkeyandperi.zotplanner;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class OperationsWithMap {

    private final static String TAG = "OperationsWithMap";
    public final static String GOOGLE_MAP_API_KEY = "AIzaSyBRa-KNzjNFYnyM7tR6yPjuBE-fW8KNk5I";

    // This GeoCoder API sucks!!!!!!!!!!!!!!!!!!!
    public static LatLng getLocationFromAddress(Context context, String strAddress){
        // Return null on error

        strAddress = strAddress.replace("&", "and");

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            address = coder.getFromLocationName(strAddress,5);
            if (address==null) {
                return null;
            }
//            Log.d(TAG, "getLocationFromAddress: the result is ready(" + address.size() + ")... for strAddress: " + strAddress);
            Address location=address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(),location.getLongitude() );

        } catch (IOException e) {
            e.printStackTrace();
        }

        return p1;
    }

    public static class SendRequestForGeoLocation extends Thread {
        private Handler handler;
        private boolean runningFlag = false;
        private String locationName;
        private String cityName;

        public SendRequestForGeoLocation(Handler handler, String locationName, String cityName) {
            this.handler = handler;
            locationName = locationName.replace("&", "and");
            this.locationName = locationName;
            this.cityName = cityName;
        }

        @Override
        public void run() {
            super.run();
            runningFlag = true;
            Message message = new Message();
            Bundle data = new Bundle();
            String urlStr = "https://maps.google.com/maps/api/geocode/json?address=" +
                    locationName +
                    " " +
                    cityName +
                    "&key=" +
                    GOOGLE_MAP_API_KEY;
            String response = getLatLongByURL(urlStr);
            try {
                JSONObject jsonObject = new JSONObject(response);

                double lat = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lat");

                double lng = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lng");

//                Log.d("latitude", "" + lat);
//                Log.d("longitude", "" + lng);
                data.putDouble("latitude", lat);
                data.putDouble("longitude", lng);
                data.putBoolean("is_success", true);
            } catch (JSONException e) {
                Log.d(TAG, "SendRequestForCourseLocation: Something going wrong here...");
                e.printStackTrace();
                data.putBoolean("is_success", false);
            }
            message.setData(data);
//            Log.i(TAG, "SendRequestForCourseLocation: Going to send message back to the handler.");
            handler.sendMessage(message);
            runningFlag = false;
        }

        public boolean getRunningFlag() {
            return runningFlag;
        }
    }

    private static String getLatLongByURL(String requestURL) {
        URL url;
        StringBuilder response = new StringBuilder();
        try {
            url = new URL(requestURL);

//            Log.d(TAG, "getLatLongByURL: the final url is: " + url);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            } else {
                response = new StringBuilder();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response.toString();
    }
}
