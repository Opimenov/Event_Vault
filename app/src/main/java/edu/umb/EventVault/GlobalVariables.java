package edu.umb.EventVault;

import android.app.Application;

/**
 * Created by Olga on 18.12.2016.
 */

public class GlobalVariables extends Application{

    private String userName;
    private String userPassword;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

}
