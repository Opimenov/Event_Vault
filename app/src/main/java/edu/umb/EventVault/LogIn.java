package edu.umb.EventVault;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Created by Olga on 18.12.2016.
 */

public class LogIn extends Activity {
    Button button_login, button_cancel, button_signUp;
    EditText ETuserName,ETuserPassword;

    //custom logging
    public final static String DEBUG_TAG="edu.umb.EV.MYMSG";

    //database connection
    private static final String url = "jdbc:mysql://85.10.205.173:3306/umboston";
    private static final String user = "cs443";
    private static final String pass = "cs443-2016";

    GlobalVariables gv = GlobalVariables.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        button_login = (Button)findViewById(R.id.log_in_button);
        ETuserName = (EditText)findViewById(R.id.userName);
        ETuserPassword = (EditText)findViewById(R.id.userPassword);

        button_cancel = (Button)findViewById(R.id.cancel_button);
        button_signUp = (Button)findViewById(R.id.sign_up_button);

        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uname = ETuserName.getText().toString();
                String upass = ETuserPassword.getText().toString();
                //check user name & password with information in database
                try {
                    Class.forName("com.mysql.jdbc.Driver");
                    Connection con = DriverManager.getConnection(url, user, pass);
                    Statement st = con.createStatement();
                    // find password with entered username
                    String get_user_password = "SELECT password FROM `umboston`.users WHERE name = '"+uname+"'";
                    ResultSet rs = st.executeQuery(get_user_password);
                    if (rs.equals(null)){
                        Toast.makeText(getApplicationContext(),
                                "There is no user " + uname,Toast.LENGTH_SHORT).show();
                    } else {
                        while (rs.next()) {
                            gv.setUserPassword(rs.getString(1));
                        }
                        if(upass.equals(gv.getUserPassword())) {
                            gv.setUserName(uname);
                            Intent intent = new Intent(LogIn.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }else{
                            gv.setUserPassword("");
                            Toast.makeText(getApplicationContext(), "Wrong Credentials",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                    con.close();
                }
                catch(Exception e) {
                    e.printStackTrace();
                }

            }
        });

        // Cancel button returns to main_logged_in activity
        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogIn.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        // SignUp button creates new activity to create user
        button_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("edu.umb.EventVault.AddUser");
                startActivity(intent);
            }
        });
    }
}
