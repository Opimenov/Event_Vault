package edu.umb.EventVault;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import static android.view.View.VISIBLE;

public class AddEvent extends AppCompatActivity {

    private DatePicker datePicker;
    private Calendar calendar;
    private TextView dateView;
    private TextView time;
    private String format = "";

    private int year, month, day, hour, min;

    // place list
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    ScrollView scrl;

    //custom logging
    public final static String DEBUG_TAG="edu.umb.EV.MYMSG";

    //database connection
    private static final String url = "jdbc:mysql://85.10.205.173:3306/umboston";
    private static final String user = "cs443";
    private static final String pass = "cs443-2016";

    // activity dialog
    ListView activityList = null;
    TextView selectedActivity;

    //places dialog
    ListView placeList = null;
    TextView selectedPlace;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prepareListData();
        setContentView(R.layout.activity_add_event);

        dateView = (TextView) findViewById(R.id.textEventDate);
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        showDate(year, month+1, day);

        time = (TextView) findViewById(R.id.textEventTime);

        hour = calendar.get(Calendar.HOUR_OF_DAY);
        min = calendar.get(Calendar.MINUTE);
        showTime(hour, min);

        //create activity dialog
        selectedActivity = (TextView) findViewById(R.id.textActivity);
        activityList = new ListView(this);
        ArrayList<String> activity_items = new ArrayList<String>();
        String selectAllActivities = "SELECT name FROM `umboston`.activities";
        query_dataBase(selectAllActivities, activity_items);
        ArrayAdapter<String> activityDialogAdapter = new ArrayAdapter<String>(this, R.layout.list_item, R.id.lblListItem, activity_items);
        activityList.setAdapter(activityDialogAdapter);
        activityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ViewGroup vg = (ViewGroup) view;
                TextView txt = (TextView) vg.findViewById(R.id.lblListItem);
                selectedActivity.setText(txt.getText().toString());
            }
        });

        //create places dialog

        selectedPlace = (TextView) findViewById(R.id.textPlace);
        placeList = new ListView(this);
        String selectAllPlaces = "SELECT name FROM `umboston`.places";
        ArrayList<String> place_items = new ArrayList<String>();
        query_dataBase(selectAllPlaces, place_items);
        ArrayAdapter<String> placeDialogAdapter = new ArrayAdapter<String>(this, R.layout.list_item, R.id.lblListItem, place_items);
        placeList.setAdapter(placeDialogAdapter);
        placeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ViewGroup vg = (ViewGroup) view;
                TextView txt = (TextView) vg.findViewById(R.id.lblListItem);
                selectedPlace.setText(txt.getText().toString());
            }
        });



        Button btn = (Button) findViewById(R.id.add_event_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try {
                  //  EditText name = (EditText) findViewById(R.id.eventName);
                  //  String inputname = name.getText().toString();
                    TextView date = (TextView) findViewById(R.id.textEventDate);
                    String inputdate = date.getText().toString();
                    TextView time = (TextView) findViewById(R.id.textEventTime);
                    String inputtime = time.getText().toString();
                    TextView activity = (TextView) findViewById(R.id.textActivity);
                    String inputactivity = activity.getText().toString();
                    TextView place = (TextView) findViewById(R.id.textPlace);
                    String inputplace = place.getText().toString();
                    Class.forName("com.mysql.jdbc.Driver");
                    Connection con = DriverManager.getConnection(url, user, pass);
                    Statement st = con.createStatement();
                    String get_activity_id = "SELECT id FROM `umboston`.activities WHERE name = '"+inputactivity+"'";
                    String get_place_id = "SELECT id FROM `umboston`.places WHERE name= '"+inputplace+"'";
                    int aid = 0;
                    int pid = 0;
                    ResultSet rs = st.executeQuery(get_activity_id);
                    while(rs.next()) {
                        aid = rs.getInt(1);
                    }
                    rs = st.executeQuery(get_place_id);
                    while(rs.next()) {
                        pid = rs.getInt(1);
                    }
                    String insert = "INSERT INTO `umboston`.events (dt,pid,aid) VALUES ('"+
                                                    inputdate + " " + inputtime
                                                    +"',"+ pid +","+ aid+")";
                    Log.i(DEBUG_TAG, "Insert line: " + insert);
                    st.executeUpdate(insert);
                    con.close();
                    Intent intent = new Intent(AddEvent.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // get results from database

    private void query_dataBase(String query, ArrayList<String> query_results) {
        Log.i(DEBUG_TAG, "querying database");
        //testing the Minimum viable product
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection(url, user, pass);
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(query);
            while(rs.next()) {
                query_results.add(rs.getString(1));
            }
            con.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    //activity show dialog
    public void showActivitiesDialog(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(AddEvent.this);
        builder.setCancelable(true);
        builder.setPositiveButton("Ok",new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setView(activityList);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //places show dialog

    public void showPlacesDialog(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(AddEvent.this);
        builder.setCancelable(true);
        builder.setPositiveButton("Ok",new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setView(placeList);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //adjust listview height
    private void setListViewHeight(ExpandableListView listView,
                                   int group) {
        ExpandableListAdapter listAdapter = (ExpandableListAdapter) listView.getExpandableListAdapter();
        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(),
                View.MeasureSpec.EXACTLY);
        for (int i = 0; i < listAdapter.getGroupCount(); i++) {
            View groupItem = listAdapter.getGroupView(i, false, null, listView);
            groupItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);

            totalHeight += groupItem.getMeasuredHeight();

            if (((listView.isGroupExpanded(i)) && (i != group))
                    || ((!listView.isGroupExpanded(i)) && (i == group))) {
                for (int j = 0; j < listAdapter.getChildrenCount(i); j++) {
                    View listItem = listAdapter.getChildView(i, j, false, null,
                            listView);
                    listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);

                    totalHeight += listItem.getMeasuredHeight();

                }
            }
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        int height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getGroupCount()));
        if (height < 10)
            height = 200;
        params.height = height;
        listView.setLayoutParams(params);
        listView.requestLayout();

    }

    /*
  * Preparing the list data
  */
    private void prepareListData() {
        try {
            listDataHeader = new ArrayList<String>();
            listDataChild = new HashMap<String, List<String>>();

            // Adding child data
            listDataHeader.add("Places");

            // Adding child data
            List<String> places = new ArrayList<String>();
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection(url, user, pass);
            Statement st = con.createStatement();
            String query = "select distinct name FROM `umboston`.places order by id";
            ResultSet rs = st.executeQuery(query);
            while(rs.next()) {
                places.add(rs.getString(1));
            }
            con.close();
            listDataChild.put(listDataHeader.get(0), places); // Header, Child data
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    public void setDate(View view) {
        showDialog(999);
    }


    public void setTime(View view) {
        showDialog(888);
    }

    public void showTime(int hour, int min) {
        String shour = String.valueOf(hour);
        String smin = String.valueOf(min);
        if (hour == 0){
            shour = "00";
        }
        if (min == 0){
            smin = "00";
        }

        time.setText(new StringBuilder().append(shour).append(":").append(smin)
                .append(":").append("00"));
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        // TODO Auto-generated method stub
        if (id == 999) {
            return new DatePickerDialog(this,
                    myDateListener, year, month, day);
        }else if (id == 888) {
            return new TimePickerDialog(this,
                    myTimeListener, hour, min, true);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener myDateListener = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0,
                                      int arg1, int arg2, int arg3) {
                    // TODO Auto-generated method stub
                    // arg1 = year
                    // arg2 = month
                    // arg3 = day
                    showDate(arg1, arg2+1, arg3);
                }
            };

    private TimePickerDialog.OnTimeSetListener  myTimeListener = new
            TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                    // TODO Auto-generated method stub
                    // arg1 = hour
                    // arg2 = min
                    showTime(selectedHour, selectedMinute);
                }
            };

    private void showDate(int year, int month, int day) {
        dateView.setText(new StringBuilder().append(year).append("-")
                .append(month).append("-").append(day));
    }
}
