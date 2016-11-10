package edu.umb.EventVault;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

import static edu.umb.EventVault.R.id.map;
import static edu.umb.EventVault.R.id.rough_spinner;

/**
 * AUTHORS: OLGA ANDREEVA
 *          OLEKSANDR PIMENOV
 *          2016
 */


public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    private EditText mEditText;   //gets user input
    private ImageButton mSearchButton;   //start the search
	private final static String DEBUG_TAG="OP";
    private int rough_spinner_position = 0;
    private int fine_spinner_position = 0;
    private int godelNum = 0;
    private String user_input; //input from the user
    private Spinner roughSpinner; //
    private Spinner fineSpinner;
    private GoogleMap mMap;
    private Toolbar mToolbar;
    private AppCompatDelegate delegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /** wire the map fragment so we can use it in the code */
        MapFragment mFragment=((MapFragment) getFragmentManager().findFragmentById(map));
        mFragment.getMapAsync(this); // this is interesting
        mEditText = (EditText) findViewById(R.id.editText); // wire the EditText for later use
        mSearchButton = (ImageButton) findViewById(R.id.search_button); //wire search button
        roughSpinner = (Spinner) findViewById(rough_spinner);
        //create an array using the string array and default spinner layout
        ArrayAdapter<CharSequence> rough_adapter = ArrayAdapter
                .createFromResource(this,R.array.rough_spinner_options,
                        android.R.layout.simple_spinner_item);
        //specify what layout to use when the list of choices appears
        rough_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //apply the adapter to the spinner
        roughSpinner.setAdapter(rough_adapter);
        roughSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                rough_spinner_position = pos;
                godelNum = godel(rough_spinner_position, fine_spinner_position);
                Log.i(DEBUG_TAG, "current Gorel number ::" + godelNum);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        //create an array using the string array and default spinner layout
        fineSpinner = (Spinner) findViewById(R.id.fine_spinner);
        ArrayAdapter<CharSequence> fine_adapter = ArrayAdapter
                .createFromResource(this,R.array.fine_spinner_options,
                        android.R.layout.simple_spinner_item);
        //specify what layout to use when the list of choices appears
        fine_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //apply the adapter to the spinner
        fineSpinner.setAdapter(fine_adapter);
        fineSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                fine_spinner_position = i;
                godelNum = godel(rough_spinner_position, fine_spinner_position);
                Log.i(DEBUG_TAG, "current Gorel number ::" + godelNum);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });


    }

    //might add in the future
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.display_message, menu);
        return true;
    }
    //part of the menu feature
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void startSearch(View v){ // responds to user search clicks
        String query = "";
        //get the input string from the user
        user_input = mEditText.getText().toString();
        /** at this point we need to figure out what are we looking for */
        /** using rough_spinner_position and fine_spinner_position
         * determine what user is searching for
         * there are 9 possible options
         0  0 godel # :: 0
         0  1 godel # :: 2
         0  2 godel # :: 4
         1  0 godel # :: 1
         1  1 godel # :: 5
         1  2 godel # :: 9
         2  0 godel # :: 3
         2  1 godel # :: 11
         2  2 godel # :: 19


         */
        switch (godelNum) {
            case 0:
            //Event by activity
                query = "";
                break;
            case 2:
            //Event by name
                query = "";
                break;
            case 4:
            //Event by location
                query = "";
                break;
            case 1:
            //People by activity
                query = "";
                break;
            case 5:
            //People by name
                query = "";
                break;
            case 9:
            //People by location
                query = "";
                break;
            case 3:
            //Places by activity
                query = "";
                break;
            case 11:
            //Places by name
                query = "";
                break;
            case 19:
            //Places by location
                query = "";
                break;
            default:
                Log.i(DEBUG_TAG, "godel numbers aren't working ::");
        }

        //create connection manager object
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        //get network info from the connection manager
        NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
        //proceed only when connection is active
        if (netInfo != null && netInfo.isConnected()) {
            //query the database

        } else {
            Toast.makeText(getApplicationContext(), "no network connection", Toast.LENGTH_SHORT);
        }
    }


    
/**    private class DownloadInfoTask extends AsyncTask<String,Void, returnType> {
        private InputStream mIS = null;
        private JSONObject jsOb = null;


         * 1.get an input stream
         * 2.parse JSON to get temperature, location coordinates, and image name
         * 3.download image
         * 4.construct info class and return it

        @Override
        protected IM_TEMP_LOC doInBackground(String... urls) {
            Double lat = 0.0,lng = 0.0;
            int temp = 0;
            String im_name;
            Bitmap image = null;
            try {
                mIS = getIS(urls[0]); //get an input stream
                jsOb = convIStoJSONObject(mIS); //convert an input stream into JSONObject Object
            } catch (Exception e) {
                Log.i(DEBUG_TAG, "inside DownloadInfoTask doInBackground ::"+e.toString());
            }
            Log.i(DEBUG_TAG, "did we get JSONOBject::"+jsOb);
            if (jsOb != null) {
                //parse json object and populate IM_TEMP_LOC
                try {
                    lat = jsOb.getJSONObject("coord").getDouble("lat");
                    lng = jsOb.getJSONObject("coord").getDouble("lon");
                    // "key":[ {"another_key" : "value", "some_key" : "some_value" } ]
                    // square brackets means an array of JSON Objects, in this case there single obj
                    im_name = jsOb.getJSONArray("weather").getJSONObject(0).getString("icon");
                    //the unit is in Kelvin C = K - 273.15
                    //do the floor, otherwise too many fractional digits
                    temp = (int) Math.floor(jsOb.getJSONObject("main").getDouble("temp") - 273.15);
                    image = downloadImage(im_name); //download the image using the name
                } catch (JSONException e) {
                    Log.i(DEBUG_TAG, "parsing json::"+e.toString());
                } catch (IOException e) {
                    Log.i(DEBUG_TAG, "downloading image::"+e.toString());
                }

            }
            //create a wrapper class for all the info that we need
            return new IM_TEMP_LOC(image,lat,lng,temp);
        }

        //after doInBackground finishes update UI elements
        @Override
        protected void onPostExecute(int r) {
            if (result != null) {
                //show the info on the map
                //update camera location
                CameraUpdate center = CameraUpdateFactory
                        .newLatLng(new LatLng(result.getLat(),result.getLng()));
                //do some set up here
                CameraUpdate zoom = CameraUpdateFactory.zoomTo(12);
                mMap.moveCamera(center);
                mMap.animateCamera(zoom);
            }
        }
    }
*/

    /**
     * since we have two parameters we could use Godel numbers to get just one
     */
    private int godel(int x, int y) {
        return ((int) Math.pow( 2.0, (double) x))*((2*y) +1) - 1;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.mMap=map;
    }
}
