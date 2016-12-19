package edu.umb.EventVault;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddPlace extends AppCompatActivity {

    public final static String DEBUG_TAG="edu.umb.cs443.MYMSG";
    private String stringUrl ="https://maps.google.com/maps/api/geocode/json?address=";
    private String ending = "&sensor=false";

    private static final String url = "jdbc:mysql://85.10.205.173:3306/umboston";
    private static final String user = "cs443";
    private static final String pass = "cs443-2016";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        EditText name = (EditText) findViewById(R.id.placeName);
        EditText adress = (EditText) findViewById(R.id.placeAdress);

        Button btn = (Button) findViewById(R.id.add_place_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                EditText adress = (EditText) findViewById(R.id.placeAdress);
                String inputadress = adress.getText().toString().replace(" ","");
                EditText city = (EditText) findViewById(R.id.placeCity);
                String inputcity = city.getText().toString().replace(" ","");
                EditText state = (EditText) findViewById(R.id.placeState);
                String inputstate = state.getText().toString().replace(" ","");
                String place = "";
                place = inputadress+","+inputcity+","+inputstate;

                ConnectivityManager connMgr = (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    Log.i(DEBUG_TAG, "Calling: " + stringUrl+place+ending);
                    new DownloadWebpageTask().execute(stringUrl+place+ending);
                } else {
                    Toast.makeText(getApplicationContext(), "No network connection available", Toast.LENGTH_SHORT);
                }
            }
        });
    }

    private class DownloadWebpageTask extends AsyncTask<String, Void, List<Address>> {
        @Override
        protected List<Address> doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return null;
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(List<Address> result) {
            if(result!=null){
                try {
                    EditText name = (EditText) findViewById(R.id.placeName);
                    String inputname = name.getText().toString();
                    Class.forName("com.mysql.jdbc.Driver");
                    Connection con = DriverManager.getConnection(url, user, pass);
                    Statement st = con.createStatement();
                    String insert = "INSERT INTO `umboston`.places (name,adress,lat,lon) VALUES ('"+inputname+"','"+ result.get(0).getAddressLine(0)
                                            +"',"+ result.get(0).getLatitude() +","+ result.get(0).getLongitude()+")";
                    Log.i(DEBUG_TAG, "Insert line: " + insert);
                    st.executeUpdate(insert);

                    con.close();
                    Intent intent = new Intent(AddPlace.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
            else{
                Log.i(DEBUG_TAG, "returned latlon is null");}
        }
    }

    private List<Address> downloadUrl(String myurl) throws IOException {
        InputStream is = null;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(20000 /* milliseconds */);
            conn.setConnectTimeout(30000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            String line;
            StringBuilder sb = new StringBuilder();
            int response = conn.getResponseCode();
            Log.i(DEBUG_TAG, "The response is: " + response);
            is = conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            JSONObject json = new JSONObject(sb.toString());
            JSONArray array  = (JSONArray) json.get("results");
            Double lon = array.getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lng");
            Double lat = array.getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
            String name = array.getJSONObject(0).getString("formatted_address");
            Address addr = new Address(Locale.getDefault());
            addr.setLatitude(lat);
            addr.setLongitude(lon);
            addr.setAddressLine(0, name != null ? name : "");
            List<Address> res = new ArrayList<Address>();
            res.add(addr);
            return res;
        }catch(Exception e) {
            Log.i(DEBUG_TAG, e.toString());
        }finally {
            if (is != null) {
                is.close();
            }
        }

        return null;
    }

}
