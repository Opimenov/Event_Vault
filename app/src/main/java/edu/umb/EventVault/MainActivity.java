package edu.umb.EventVault;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
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
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
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
        OnMapReadyCallback {
    //  GoogleMap.OnMarkerClickListener add this in case we want custom marker click event\
    private EditText mEditText;   //gets user input
    private ImageButton mSearchButton;   //start the search
	private final static String DEBUG_TAG="OP";
    private int rough_spinner_position = 0;
    private int fine_spinner_position = 0;
    private int radius = 10; //display events only this far away from the user
    private int godelNum = -1;
    private String user_input; //input from the user
    private Spinner roughSpinner; //
    private Spinner fineSpinner;
    private Spinner milesSpinner;
    public GoogleMap mMap;
    private boolean mapIsOk = false;
    private ConnectivityManager connMgr;
    private NetworkInfo netInfo;
    private ArrayList<DummyEvent>  moc_query_results = new ArrayList<>(); //testing data
    private ArrayList<DummyEvent>  query_results = new ArrayList<>(); //real data
    private HashMap<Marker, String> markers = new HashMap<>(); //to stores all markers created
    /**  database parameters */
    private static final String url = "jdbc:mysql://85.10.205.173:3306/umboston";
    private static final String user = "cs443";
    private static final String pass = "cs443-2016";

    /** singleton logIN info class */
    private GlobalVariables mGlobalVars = GlobalVariables.getInstance();

    /** marker clustering */
    private Bitmap greenmarkerImage, redmarkerImage;
    private float oldZoom = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /** custom markers for displaying on the map */
        greenmarkerImage = BitmapFactory.decodeResource(this.getResources(), R.drawable.green_marker);
        redmarkerImage = BitmapFactory.decodeResource(this.getResources(), R.drawable.big_red_marker);


        setContentView(R.layout.activity_main);
        /** compatibility issue workaround */
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        /** wire the map fragment so we can use it in the code */
        final MapFragment mFragment=((MapFragment) getFragmentManager().findFragmentById(map));
        mFragment.getMapAsync(this); // this is interesting

        mEditText = (EditText) findViewById(R.id.editText); // wire the EditText for later use
        mSearchButton = (ImageButton) findViewById(R.id.search_button); //wire search button
        roughSpinner = (Spinner) findViewById(rough_spinner);

        /** create an array using the string array and default spinner layout */
        ArrayAdapter<CharSequence> rough_adapter = ArrayAdapter
                .createFromResource(this,R.array.rough_spinner_options,
                        android.R.layout.simple_spinner_item);

        /** specify what layout to use when the list of choices appears */
        rough_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        /** apply the adapter to the spinner */
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

            //marker clustering
            //mMap.OnCameraChangeListener(this);

        });

        /** radius of the search spinner */
        milesSpinner = (Spinner) findViewById(R.id.miles_spinner);

        /** create an array using the string array and default spinner layout */
        ArrayAdapter<CharSequence> miles_adapter = ArrayAdapter
                .createFromResource(this,R.array.miles_spinner_options,
                        android.R.layout.simple_spinner_item);

        /** specify what layout to use when the list of choices appears */
        miles_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        milesSpinner.setAdapter(miles_adapter);
        milesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            /** update the radius search parameter when user picks one */
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                switch (pos) {
                    case 0: {
                        radius = 10; break;
                    }
                    case 1: {
                        radius = 25; break;
                    }
                    case 2: {
                        radius = 50; break;
                    }
                    case 3: {
                        radius = 100; break;
                    }
                    default:
                        Log.i(DEBUG_TAG, "miles spinner switch failure");
                }
                Log.i(DEBUG_TAG, "current radius ::" + radius);
            }

            /** user didn't change the current radius -> so, do nothing */
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });


        /** create an array using the string array and default spinner layout */
        fineSpinner = (Spinner) findViewById(R.id.fine_spinner);
        ArrayAdapter<CharSequence> fine_adapter = ArrayAdapter
                .createFromResource(this,R.array.fine_spinner_options,
                        android.R.layout.simple_spinner_item);

        /** specify what layout to use when the list of choices appears */
        fine_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        /** apply the adapter to the spinner */
        fineSpinner.setAdapter(fine_adapter);
        fineSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            /** calculate unique number based on two searching options that currently selected */

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                fine_spinner_position = i;
                godelNum = godel(rough_spinner_position, fine_spinner_position);
                Log.i(DEBUG_TAG, "current Gorel number ::" + godelNum);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        /** create some dummy data for testing without connection */
        //create_testing_data(); //uncomment for testing without inet connection
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

    /** responds to user search button clicks */
    public void startSearch(View v){

        /* get the input string from the user */
        user_input = mEditText.getText().toString();

        /** construct DB query based on user input and current options */
        String query = createQuery(user_input);

        /** get the date from the DB */
        if (query != "" && query != " " && query != null)
            query_dataBase(query);

        /** check the network and establish connection */
        if (connectionAndNetworkIsOk()) {

            /**if map is ready put markers on the map */
            Log.i(DEBUG_TAG, "map OK " + mapIsOk );
            if( mapIsOk ) {
                displayResultsOnMap();
            }
        }
    }

    /**
     * puts markers on the map using query_results
     * adds every marker to the HashMap to keep track of what
     * marker needs to be visible
     */
    private boolean displayResultsOnMap() {
        mMap.clear(); // to clear map from previous markers
        if (!query_results.isEmpty()) {
            for (DummyEvent de : query_results) {  //use moc_query_results for testing without DB
                Log.i(DEBUG_TAG, "adding marker for " + de.toString());

                Marker temp = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(de.getLat(), de.getLon()))
                        .title(de.getName())
                        .icon(BitmapDescriptorFactory.fromBitmap(redmarkerImage))//marker clustering
                        .snippet(de.getActivity()));
                markers.put(temp, de.toString());

            }
            CameraUpdate center = CameraUpdateFactory
                    .newLatLng(new LatLng(
                            //center on the first marker
                            //
                            //should be centered on user's location
                            //
                            query_results.get(0).getLat(),
                            query_results.get(0).getLon()));
            //do some set up here
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(12);
            mMap.moveCamera(center);
            mMap.animateCamera(zoom);

            /** at this point we need to delete all search results */
            query_results.clear();
            return true;
        }
        else {
            Toast.makeText(getBaseContext(), "search resulted in nothing :/",
                    Toast.LENGTH_LONG).show();
        }
            return false;
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

     /////////////
          WE ALSO NEED TO TAKE SEARCH RADIUS INTO CONSIDERATION
                                                    //////////////
     */
    private String createQuery(String input) {
       String query;
        String test;
        switch (godelNum) {
            case 0:
                //Event by activity
                test = input.replace(" ","");
                if (test.equals("")){
                    query = "SELECT distinct a.name, p.lat, p.lon, p.name FROM " +
                            "(`umboston`.events e left JOIN `umboston`.places p ON " +
                            "e.pid=p.id) left Join `umboston`.activities a on e.aid=a.id;";
                } else {
                    query = "SELECT distinct a.name, p.lat, p.lon, p.name FROM " +
                            "(`umboston`.events e left JOIN `umboston`.places p ON " +
                            "e.pid=p.id) left Join `umboston`.activities a on e.aid=a.id" +
                            " WHERE a.name LIKE '%"+input+"%';";
                }
                break;
            case 2:
                //Event by name --no event name only activity name
                query = "";
                Toast.makeText(getBaseContext(), "current search option is not yet supported",
                        Toast.LENGTH_SHORT).show();
                break;
            case 4:
                //Event by location
                test = input.replace(" ","");
                if (test.equals("")){
                    query = "SELECT distinct a.name, p.lat, p.lon, p.name FROM " +
                            "(`umboston`.events e left JOIN `umboston`.places p ON " +
                            "e.pid=p.id) left Join `umboston`.activities a on e.aid=a.id;";
                } else {
                    query = "SELECT distinct a.name, p.lat, p.lon, p.name FROM " +
                            "(`umboston`.events e left JOIN `umboston`.places p ON " +
                            "e.pid=p.id) left Join `umboston`.activities a on e.aid=a.id" +
                            " WHERE p.adress LIKE '"+input+"';";
                }
                break;
            case 1:
                //People by activity -- can't show on map
                query = "";
                Toast.makeText(getBaseContext(), "current search option is not yet supported",
                        Toast.LENGTH_SHORT).show();
                break;
            case 5:
                //People by name -- which places user attends
                test = input.replace(" ","");
                // if there's no user name in the input, then return all places
                if (test.equals("")){
                    query = "SELECT distinct 0, p.lat, p.lon, p.name " +
                            "FROM  `umboston`.places p";
                } else {
                    query = "SELECT distinct 0, p.lat, p.lon, p.name " +
                            "FROM  `umboston`.places p " +
                            "WHERE p.id IN " +
                            "(SELECT pid FROM users_places " +
                            " WHERE uid = (SELECT id FROM users WHERE name = '"+input+"'))";
                }
                break;
            case 9:
                //People by location -- can't display people on map
                query = "";
                Toast.makeText(getBaseContext(), "current search option is not yet supported",
                        Toast.LENGTH_SHORT).show();
                break;
            case 3:
                //Places by activity create new table places_activities
                test = input.replace(" ","");
                if (test.equals("")){
                    query = "SELECT distinct a.name, p.lat, p.lon, p.name FROM " +
                            "(`umboston`.events e left JOIN `umboston`.places p ON " +
                            "e.pid=p.id) left Join `umboston`.activities a on e.aid=a.id;";
                } else {
                    query = "";
                    Toast.makeText(getBaseContext(), "current search option is not yet supported",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case 11:
                //Places by name
                test = input.replace(" ","");
                if (test.equals("")){
                    query = "SELECT distinct 0, p.lat, p.lon, p.name \n" +
                            "FROM `umboston`.places p;";
                } else {
                    query = "SELECT distinct 0, p.lat, p.lon, p.name \n" +
                            "FROM `umboston`.places p" +
                            " WHERE p.name LIKE  '%"+input+"%';";
                }
                break;
            case 19:
                //Places by location
                test = input.replace(" ","");
                if (test.equals("")){
                    query = "SELECT distinct 0, p.lat, p.lon, p.name " +
                            " FROM `umboston`.places p;";
                } else {
                    query = "SELECT distinct 0, p.lat, p.lon, p.name " +
                            " FROM `umboston`.places p" +
                            " WHERE p.adress LIKE '"+input+"';";
                }
                break;
            default:
                Log.i(DEBUG_TAG, "something went wrong in create query");
                query = null;
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
     * connect to the database
     * get the result set
     * create testing events from the resulting set
     */

    private void query_dataBase(String query) {
        Log.i(DEBUG_TAG, "querying database");
        //testing the Minimum viable product
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection(url, user, pass);
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                query_results.add(new DummyEvent(rs.getString(1), rs.getDouble(2),
                        rs.getDouble(3), rs.getString(4)));
            }
            con.close();
        } catch (Exception e) {
            Log.i(DEBUG_TAG, "DB query failed");
            e.printStackTrace();
        }
    }

    /**
     * FOR TESTING WITHOUT DB CONNECTION
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

    /**
     * shows the simple menu. Uses 2 different layouts depending on if user is logged in or not
     * @param view
     */
    public void show_popup(final View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        MenuInflater inflater = popupMenu.getMenuInflater();

        if (mGlobalVars.isLoggedIn())
            inflater.inflate(R.menu.main_logged_in, popupMenu.getMenu());
        else
            inflater.inflate(R.menu.main_logged_off, popupMenu.getMenu());

        /**
         * Defining menu item click listener for the popup menu
         * */
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.add_place:
                        startAddPlace(view);
                        return true;
                    case R.id.add_event:
                        startAddEvent(view);
                        return true;
                    case R.id.add_activity:
                        startAddActivity(view);
                        return true;
                    case R.id.action_login:
                        startLogIn(view);
                        return true;
                }
                Log.i(DEBUG_TAG, "You selected the action : " +
                        item.getGroupId());
                return true;
            }
        });
        popupMenu.show();
    }

    // start new activity to log in
    public void startLogIn(View v) {
        Intent intent = new Intent("edu.umb.EventVault.LogIn");
        startActivity(intent);
    }

    // start new activity to add new place
    public void startAddPlace(View v) {
        Intent intent = new Intent("edu.umb.EventVault.AddPlace");
        startActivity(intent);
    }

    // start new activity to add new event
    public void startAddEvent(View v) {
        Intent intent = new Intent("edu.umb.EventVault.AddEvent");
        startActivity(intent);
    }

    // start new activity to add new activity
    public void startAddActivity(View v) {
        Intent intent = new Intent("edu.umb.EventVault.AddActivity");
        startActivity(intent);
    }

    // marker clustering

    private void checkMarkers(GoogleMap map) {
        Projection projection = map.getProjection();
        LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
        HashMap<Marker, Point> points = new HashMap<Marker, Point>();
        for (Marker marker : markers.keySet()) {
            if (bounds.contains(marker.getPosition())) {
                points.put(marker, projection.toScreenLocation(marker.getPosition()));
                marker.setVisible(false);
            }
        }
        CheckMarkersTask checkMarkersTask = new CheckMarkersTask();
        checkMarkersTask.execute(points);
    }



    private class CheckMarkersTask extends AsyncTask<HashMap<Marker, Point>, Void, HashMap<Point,
            ArrayList<Marker>>> {


        private double findDistance(float x1, float y1, float x2, float y2) {
            return Math.sqrt(((x1 - x2) * (x1 - x2)) + ((y1 - y2) * (y1 - y2)));
        }

        @Override
        protected HashMap<Point, ArrayList<Marker>>
                    doInBackground(HashMap<Marker, Point>... params) {

            HashMap<Point, ArrayList<Marker>> clusters = new HashMap<Point, ArrayList<Marker>>();
            HashMap<Marker, Point> points = params[0];
            boolean wasClustered;
            for (Marker marker : points.keySet()) {
                Point point = points.get(marker);
                wasClustered = false;
                for (Point existingPoint : clusters.keySet()) {
                    if (findDistance(point.x, point.y, existingPoint.x, existingPoint.y) < 25) {
                        wasClustered = true;
                        clusters.get(existingPoint).add(marker);
                        break;
                    }
                }
                if (!wasClustered) {
                    ArrayList<Marker> markersForPoint = new ArrayList<Marker>();
                    markersForPoint.add(marker);
                    clusters.put(point, markersForPoint);
                }
            }
            return clusters;
        }

        @Override
        protected void onPostExecute(HashMap<Point, ArrayList<Marker>> clusters) {
            for (Point point : clusters.keySet()) {
                ArrayList<Marker> markersForPoint = clusters.get(point);
                Marker mainMarker = markersForPoint.get(0);
                mainMarker.setTitle(Integer.toString(markersForPoint.size()));
                mainMarker.setVisible(true);
            }
        }

    }


}
