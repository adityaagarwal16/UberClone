package com.example.uberclone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.icu.text.TimeZoneFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    Switch switch1;
    boolean condition ;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                .applicationId("x23Nip5nUgYtEWfJJyU7SrbBwRSyAxlHRQMb9CDq")
                .clientKey("NFLVtOf82brygysRQdsNuzdHQPeJUhDliAUr7ChR")
                .server("https://parseapi.back4app.com/")
                .build()
        );

        intent = new Intent(getApplicationContext(),Rideractivity.class);

        if(ParseUser.getCurrentUser()!=null)
        {
            startActivity(intent);
        }

        switch1 = findViewById(R.id.switch1);
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                condition = isChecked;
            }
        });

    }


    public void start(View view)
    {
        if(ParseUser.getCurrentUser()==null)
        {
            ParseAnonymousUtils.logIn(new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if(e==null)
                    {
                        Log.i("log","logged in anonymously");
                    }
                    else  Log.i("log", Objects.requireNonNull(e.getMessage()));
                }
            });
        }

        if(condition)
        {
            Log.i("switch pressed","driver");
            ParseUser.getCurrentUser().put("kind","driver");
            Log.i("show",ParseUser.getCurrentUser().getString("kind"));

        }
        else {
            Log.i("switch pressed", "rider");
            ParseUser.getCurrentUser().put("kind", "rider");

            startActivity(intent);
        }

        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e==null)
                {
                    Log.i("saved","saved");
                }
            }
        });
    }
}
