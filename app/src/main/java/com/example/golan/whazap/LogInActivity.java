package com.example.golan.whazap;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;


public class LogInActivity extends Activity {
    EditText nameID, passID;
    String name, pass, userToken;
    public static final String appID = "11A3DA39-0D00-B0B1-FFAA-EE3913AECC00", key = "66F1A88C-EC31-5BAC-FF1B-F428E1A3FB00", GCMSenderID = "194724522599";
    SharedPreferences data;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Backendless.initApp( this,appID, key ); // where to get the argument values for this call
        data=getSharedPreferences("data",0);
        nameID = (EditText) findViewById(R.id.nameID);
        passID = (EditText) findViewById(R.id.passID);
        userToken=data.getString("userToken",null);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        Backendless.UserService.findById(userToken, new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser currentUser) {
                Backendless.UserService.setCurrentUser( currentUser );
                Intent i = new Intent(LogInActivity.this,ChatsListActivity.class);
                startActivity(i);
            }
            @Override
            public void handleFault(BackendlessFault backendlessFault) {
                Toast.makeText(LogInActivity.this,"loginActivity, 45", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void login(View v) {
        name = nameID.getText().toString();
        pass = passID.getText().toString();
        Backendless.UserService.login(name, pass, new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser backendlessUser) {
                userToken = Backendless.UserService.loggedInUser();
                data.edit().putString("userToken",userToken).apply();
                Toast.makeText(LogInActivity.this,"successfully login ", Toast.LENGTH_LONG).show();
                Intent i = new Intent(LogInActivity.this,ChatsListActivity.class);
                startActivity(i);
            }

            @Override
            public void handleFault(BackendlessFault e) {
                e.getCode();
                Toast.makeText(LogInActivity.this,"the user name or the password isn`t correct", Toast.LENGTH_LONG).show();

            }
        }, true);
    }
    public void moveToRegistration(View v){
        Intent i = new Intent(this, RegistrationActivity.class);
        startActivity(i);
    }
}
