package com.example.golan.whazap;

import android.app.Activity;
import android.content.Intent;
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

/**
 * Created by golan on 08/06/2017. regular expression
 */

public class RegistrationActivity extends Activity {
    EditText emailTxt, nameTxt, passTxt, passTxtConfirm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        emailTxt = (EditText) findViewById(R.id.emailText);
        nameTxt = (EditText) findViewById(R.id.nameText);
        passTxt = (EditText) findViewById(R.id.passwordText);
        passTxtConfirm = (EditText) findViewById(R.id.passwordTextConfirmation);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        Backendless.initApp(this, LogInActivity.appID, LogInActivity.key); // where to get the argument values for this call
    }

    public void register(View v) {
        String passTxtStr = passTxt.getText().toString();
        if (passTxtStr.equals(passTxtConfirm.getText().toString())) {
            BackendlessUser user = new BackendlessUser();
            String nameTextStr = nameTxt.getText().toString();
            String emailTxtStr = emailTxt.getText().toString();
            user.setProperty("name", nameTextStr);
            user.setProperty("email", emailTxtStr);
            user.setPassword(passTxtStr);
            Backendless.UserService.register(user, new AsyncCallback<BackendlessUser>() {
                public void handleResponse(BackendlessUser registeredUser) {
                    Toast.makeText(RegistrationActivity.this, "successfully registered ", Toast.LENGTH_LONG).show();
                    Intent i = new Intent(RegistrationActivity.this, LogInActivity.class);
                    startActivity(i);
                }

                public void handleFault(BackendlessFault fault) {
                    Toast.makeText(RegistrationActivity.this, "not registered ", Toast.LENGTH_LONG).show();
                    // an error has occurred, the error code can be retrieved with fault.getCode()
                }
            });
        }else{
            Toast.makeText(RegistrationActivity.this,"password confirmation failed", Toast.LENGTH_SHORT).show();
        }
    }
}
