package edu.umb.EventVault;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Created by Olga on 18.12.2016.
 */

public class AddActivity extends AppCompatActivity {

    public final static String DEBUG_TAG="edu.umb.EV.MYMSG";
    private String stringUrl ="https://maps.google.com/maps/api/geocode/json?address=";
    private String ending = "&sensor=false";

    private static final String url = "jdbc:mysql://85.10.205.173:3306/umboston";
    private static final String user = "cs443";
    private static final String pass = "cs443-2016";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_activity);

        EditText name = (EditText) findViewById(R.id.activityName);

        Button btn = (Button) findViewById(R.id.add_activity_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try {
                    EditText name = (EditText) findViewById(R.id.activityName);
                    String inputname = name.getText().toString();
                    Class.forName("com.mysql.jdbc.Driver");
                    Connection con = DriverManager.getConnection(url, user, pass);
                    Statement st = con.createStatement();
                    String insert = "INSERT INTO `umboston`.activities (name) VALUES ('"+inputname+"')";
                    Log.i(DEBUG_TAG, "Insert line: " + insert);
                    st.executeUpdate(insert);

                    con.close();
                    Intent intent = new Intent(AddActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                catch(Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }
}
