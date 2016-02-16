package com.example.boriswainberg.simpleweather;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * A placeholder fragment containing a simple view.
 */
public class WeatherActivityFragment extends Fragment {
    Typeface weatherFont;

    TextView cityField;
    TextView updatedField;
    TextView detailsField;
    TextView currentTemperatureField;
    TextView weatherIcon;
    CityPreference preferencies;
    Handler handler;
    ProgressBar progressBar;
    JSONObject json;
    Button updateButton;

    public WeatherActivityFragment() {
        handler = new Handler();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String TAG = "Typefaces";



        try {
            weatherFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/weather.ttf");

        } catch (Exception e) {
            Log.e(TAG, "unable to load font");
        }

        preferencies = new CityPreference(getActivity());
        updateWeatherData(preferencies.getCity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_weather, container, false);
        cityField = (TextView)rootView.findViewById(R.id.city_field);
        updatedField = (TextView)rootView.findViewById(R.id.updated_field);
        detailsField = (TextView)rootView.findViewById(R.id.details_field);
        currentTemperatureField = (TextView)rootView.findViewById(R.id.current_temperature_field);
        weatherIcon = (TextView)rootView.findViewById(R.id.weather_icon);
        updateButton = (Button)rootView.findViewById(R.id.update_button);
        updateButton.setEnabled(true);
        Log.v("onCreateView: ", "button_enabled");
        weatherIcon.setTypeface(weatherFont);

        return rootView;
    }

    public void forceUpdate() {
        updateWeatherData(preferencies.getCity());

        Log.v("Current city in pref: ",preferencies.getCity());
    }

    public void updateWeatherData(final String city) {
        (new MyTask()).execute(city);
        new Thread() {
            public void run() {
                if (json == null) {
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getActivity(),
                                    getActivity().getString(R.string.place_not_found),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        public void run() {
                            renderWeather();
                        }
                    });
                }
            }
        }.start();
    }

    private void renderWeather(){
        String TAG = "renderWeather";
        try {
            cityField.setText(json.getString("name").toUpperCase(Locale.US) +
                    ", " +
                    json.getJSONObject("sys").getString("country"));

            Log.v(TAG,"city: "+json.getString("name"));

            JSONObject details = json.getJSONArray("weather").getJSONObject(0);
            JSONObject main = json.getJSONObject("main");
            detailsField.setText(
                    details.getString("description").toUpperCase(Locale.US) +
                            "\n" + "Humidity: " + main.getString("humidity") + "%" +
                            "\n" + "Pressure: " + main.getString("pressure") + " hPa");

            currentTemperatureField.setText(
                    String.format("%.2f", main.getDouble("temp"))+ " ℃");

            DateFormat df = DateFormat.getDateTimeInstance();
            String updatedOn = df.format(new Date(json.getLong("dt")*1000));
            updatedField.setText("Last update: " + updatedOn);
            Log.v(TAG, "last update: " + updatedOn);

        }catch(Exception e){
            Log.e("SimpleWeather", "One or more fields not found in the JSON data");
        }
    }
    public void changeCity(String city){
        updateWeatherData(city);
    }

    class MyTask extends AsyncTask<String, Integer, String> {

        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            final Activity activity = getActivity();
            Log.v("UPDATER: ", "update started");
            progressDialog = new ProgressDialog(getActivity());
            progressDialog .setMessage("Loading...");
            progressDialog .setCancelable(false);
            progressDialog .show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            Log.v("UPDATER: ", "update done");
            renderWeather();
            progressDialog.dismiss();
        }

        @Override
        protected String doInBackground(String... params) {
            json = RemoteFetch.getJSON(getActivity(),params[0]);
            return "Done";
        }
    }
}
