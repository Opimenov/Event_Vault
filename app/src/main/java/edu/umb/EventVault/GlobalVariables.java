package edu.umb.EventVault;

import android.app.Application;

/**
 * Created by Olga on 18.12.2016.
 */

public class GlobalVariables{

    private static GlobalVariables instance;

    private String userName = null;
    private String userPassword = null;

    // Restrict the constructor from being instantiated
    private GlobalVariables(){}

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

    public static synchronized GlobalVariables getInstance(){
        if(instance==null){
            instance=new GlobalVariables();
        }
        return instance;
    }

}
