package edu.umb.EventVault;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class AddUser extends AppCompatActivity {

    Button button_signUp;
    EditText ETuserName,ETuserPassword, ETconfirmPassword;

    //custom logging
    public final static String DEBUG_TAG="edu.umb.EV.MYMSG";

    //database connection
    private static final String url = "jdbc:mysql://85.10.205.173:3306/umboston";
    private static final String user = "cs443";
    private static final String pass = "cs443-2016";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        button_signUp = (Button)findViewById(R.id.sign_up_button);
        ETuserName = (EditText)findViewById(R.id.userName);
        ETuserPassword = (EditText)findViewById(R.id.userPassword);
        ETconfirmPassword = (EditText)findViewById(R.id.userConfirmPassword);

        button_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uname = ETuserName.getText().toString();
                String upass = ETuserPassword.getText().toString();
                String confpass = ETuserPassword.getText().toString();
                if (upass.equals(confpass)) {
                    try {
                        Class.forName("com.mysql.jdbc.Driver");
                        Connection con = DriverManager.getConnection(url, user, pass);
                        Statement st = con.createStatement();
                        // find password with entered username
                        String insert_user = "INSERT INTO `umboston`.users (name, password) VALUES ('"+uname+"','"+upass+"')";
                        Log.i(DEBUG_TAG, "Insert line: " + insert_user);
                        st.executeUpdate(insert_user);
                        con.close();
                        Intent intent = new Intent(AddUser.this, LogIn.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Passwords are not equal",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
