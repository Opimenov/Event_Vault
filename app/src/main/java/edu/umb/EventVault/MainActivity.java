package edu.umb.EventVault;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;

import static edu.umb.EventVault.R.id.map;
import static edu.umb.EventVault.R.id.rough_spinner;

/**
 * AUTHORS: OLGA ANDREEVA
 *          OLEKSANDR PIMENOV
 *          2016
 */


public class MainActivity extends FragmentActivity implements
        OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private EditText mEditText;   //gets user input
    private ImageButton mSearchButton;   //start the search
	private final static String DEBUG_TAG="OP";
    private int rough_spinner_position = 0;
    private int fine_spinner_position = 0;
    private int godelNum = 0;
    private String user_input; //input from the user
    private Spinner roughSpinner; //
    private Spinner fineSpinner;
    public GoogleMap mMap;
    private boolean mapIsOk = false;
    private ConnectivityManager connMgr;
    private NetworkInfo netInfo;
    private ArrayList<DummyEvent>  moc_query_results = new ArrayList<>(); //testing data
    private ArrayList<DummyEvent>  query_results = new ArrayList<>(); //real data
    private HashMap<String,Marker> markers = new HashMap<>(); //to stores all markers created



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

        //create some dummy data for testing
        create_testing_data();


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
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }


    public void startSearch(View v){ // responds to user search clicks

        //get the input string from the user
        user_input = mEditText.getText().toString();

        //construct DB query based on user input and current options
        String query = createQuery(user_input);


        //check the network and establish connection
        if (connectionAndNetworkIsOk()) {
            // get the data from the database here
            //gonna need an AsyncTask for this
//<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
            //if map is ready put markers on the map
            Log.i(DEBUG_TAG, "map OK " + mapIsOk );
            if( mapIsOk ) {
                displayResultsOnMap();
            }
        }


    }

    /**
     * puts markers on the map using query_results
     * add every marker to the HashMap
     */
    private boolean displayResultsOnMap() {
        for (DummyEvent de : moc_query_results) {  //<<<<<<<<<<<<<<<<<<replace with query_results
            Log.i(DEBUG_TAG, "adding marker for "+de.toString());
            Marker temp = mMap.addMarker( new MarkerOptions()
                            .position(new LatLng(de.getLat(),de.getLon())));
            markers.put(de.toString(),temp);
        }
        CameraUpdate center = CameraUpdateFactory
                .newLatLng(new LatLng(
                        moc_query_results.get(50).getLat(),
                        moc_query_results.get(50).getLon()));
        //do some set up here
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(12);
        mMap.moveCamera(center);
        mMap.animateCamera(zoom);
        return true;
    }

    /**
     * creates connectivity manager
     * checks the network info
     * returns true if connection is successful
     */
    private boolean connectionAndNetworkIsOk() {
        boolean return_flag = false;
        connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        //get network info from the connection manager
        netInfo = connMgr.getActiveNetworkInfo();
        //proceed only when connection is active

        if (netInfo != null) {
            Log.i(DEBUG_TAG, "connection and network info "+(netInfo != null) +
                    " "+(netInfo.isConnected()));
        }
        if (netInfo != null && netInfo.isConnected()) {
            return_flag = true;
        } else {
            Toast.makeText(getApplicationContext(), "no network connection",
                    Toast.LENGTH_SHORT).show();
        }
        return return_flag;
    }

    /**
     * creates necessary query
     * takes string from the user
     *  using rough_spinner_position and fine_spinner_position
     * determines what user is searching for
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
    private String createQuery(String input) {
       String query = "";
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
        return query;
    }

    /**
     * since we have two parameters we could use Godel numbers to get just one
     */
    private int godel(int x, int y) {
        return ((int) Math.pow( 2.0, (double) x))*((2*y) +1) - 1;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.mMap=map;
        mapIsOk = true;
    }

    /**
     * display stats info about the marker
     * @param marker
     * @return boolean if there is no info associated with the marker
     */
    @Override
    public boolean onMarkerClick(Marker marker) {

        return false;
    }

    /**
     * creates 100 events with different locations and names
     * every tenth event activity type changes
     * so there are only 10 different activities
     */
    private void create_testing_data() {
        String activity = "activity";
        for(double i = 0; i < 100; i++) {
            String name = "event_name";

            double lat = 40.741895 + i/100;
            double lon = -73.989308 + i/100;
            if ( i%10 == 0) {
                activity = "activity" + i;
            }
            name = name + i;
            moc_query_results.add(new DummyEvent(activity, lat, lon, name));
            Log.i(DEBUG_TAG, activity+" "+ lat+" "+lon+" "+name);
        }
    }

    public void show_popup(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.main, popupMenu.getMenu());
        popupMenu.show();
    }

}
