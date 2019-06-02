package com.example.exam1;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("Registered")
public class MainActivity extends AppCompatActivity {
    public static final String UPDATE_TICK = "updateTick";
    public static final String STOP_PHONE_SERVICE_STATE = "stopPhoneServiceState";
    public static final String START_PHONE_SERVICE = "start";
    public static final String STOP_PHONE_SERVICE = "stop";
    public static final String TICK_BROADCAST_VALUE_KEY = "tick";
    private TextView tvCountDown;
    private TextView tvHours;
    private TextView tvMinutes;
    private TextView tvSeconds;
    private TextView tvMessage;
    private TextView tvTimer;
    private BroadcastReceiver mReceiver;
    public static final String AUTH = "exam1.auth_settings";
    private long timeLeftMillisecons = 1000 * 60 * 60 * 24; // 24 h
    public Intent StopPhoneServiceIntent;
    private Button restartTimerBtn;
    private Button startStopServiceBtn;
    private Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViewBindings(savedInstanceState);
        StopPhoneServiceIntent = new Intent(this, StopPhoneService.class);
        setAdminOnBoot();
        listeners();
        startBroadcastService();
        startPhoneStopService(timeLeftMillisecons);
    }

    /*
     * bind fileds to view and start instantiate service intent object*/

    private void initViewBindings(Bundle savedInstanceState) {

        setContentView(R.layout.activity_main);
        tvCountDown = findViewById(R.id.tvCountDown);
        tvHours = findViewById(R.id.tvHours);
        tvMinutes = findViewById(R.id.tvMinutes);
        tvSeconds = findViewById(R.id.tvSeconds);
        tvMessage = findViewById(R.id.tvMessage);
        tvTimer =  findViewById(R.id.tvTimer);
        loginBtn = findViewById(R.id.loginBtn);
        restartTimerBtn = findViewById(R.id.restartTimerBtn);
        startStopServiceBtn = findViewById(R.id.startStopServiceBtn);

        restartTimerBtn.setVisibility(View.GONE);
        startStopServiceBtn.setVisibility(View.GONE);

        tvHours.setVisibility(View.GONE);
        tvMinutes.setVisibility(View.GONE);
        tvSeconds.setVisibility(View.GONE);
    }

    private void startBroadcastService() {

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (tvMessage != null) {
                    updateTimer(intent.getLongExtra(TICK_BROADCAST_VALUE_KEY, 0));
                }
            }
        };
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mReceiver, new IntentFilter(UPDATE_TICK));
    }


    @Override
    protected void onDestroy() {

        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        stopPhoneStopService();
    }

    private void listeners() {
        restartTimerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetTimer();
            }
        });

        startStopServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togglePhoneService();
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                toogleAuth();
            }
        });

    }


    private void toogleAuth() {

        hideKeyboard(this);
        loginBtn.setText(R.string.connexion);
        switch (getPref("authState")) {
            case "login":
                logoutHandler();
                break;
            case "logout":
                loginHandler();
                break;
            default:
                logoutHandler();
        }
    }

    /*
     * Auth the root user*/

    private void loginHandler() {

        EditText etUsername = findViewById(R.id.username);
        EditText etPassword = findViewById(R.id.password);
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();


        String nameToMatch = getPref("etUsername");
        String passwordToMatch = getPref("etPassword");
        if ((nameToMatch.equals(username) && passwordToMatch.equals(password))) {
            putPref("authState", "login");
            hideKeyboard(this);

            etUsername.setText("");
            etPassword.setText("");
            tvHours.setVisibility(View.VISIBLE);
            tvMinutes.setVisibility(View.VISIBLE);
            tvSeconds.setVisibility(View.VISIBLE);
            restartTimerBtn.setVisibility(View.VISIBLE);
            startStopServiceBtn.setVisibility(View.VISIBLE);
            tvMessage.setText(R.string.valideAuth);
            loginBtn.setText(R.string.déconnexion);
        } else {
            tvMessage.setText(R.string.fail_auth);
        }
    }

    public static void hideKeyboard(Activity activity) {

        View view = activity.findViewById(android.R.id.content);
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void logoutHandler() {

        putPref("authState", "logout");
        tvMessage.setText("");

        tvHours.setVisibility(View.GONE);
        tvMinutes.setVisibility(View.GONE);
        tvSeconds.setVisibility(View.GONE);
        restartTimerBtn.setVisibility(View.GONE);
        startStopServiceBtn.setVisibility(View.GONE);
    }

    /*
     * get stored preference*/

    private String getPref(String key) {

        assert key != null;
        SharedPreferences pref = getSharedPreferences(AUTH, MODE_PRIVATE);
        return pref.getString(key, "");
    }

    /*
     *set the admin user on app boot */

    private void setAdminOnBoot() {

        putPref("etUsername", "root");
        putPref("etPassword", "root");
    }


    /*
     * Some clean up on the user imput to avoid crashinf the app*/

    private String valueValidation(TextView value) {

        String string = value.getText().toString().trim();
        return string.equals("") ? "0" : string;
    }

    /*
     * reset the timer with root prefs*/

    private void resetTimer() {
        hideKeyboard(this);
        int setHoursValue = Integer.parseInt(valueValidation(tvHours));
        int setMinutesValue = Integer.parseInt(valueValidation(tvMinutes));
        int setSecondsValue = Integer.parseInt(valueValidation(tvSeconds));
        long newTimeLeftMillisecons = 1;
        newTimeLeftMillisecons += setHoursValue == 0 ? 1 : setHoursValue * 3600;
        newTimeLeftMillisecons += setMinutesValue == 0 ? 1 : setMinutesValue * 60;
        newTimeLeftMillisecons += setSecondsValue == 0 ? 1 : setSecondsValue;
        newTimeLeftMillisecons *= 1000;
        stopPhoneStopService();
        startPhoneStopService(newTimeLeftMillisecons);
    }

    /*
     * Stop the phonestop service & save the current state*/

    private void stopPhoneStopService() {

        putPref(STOP_PHONE_SERVICE_STATE, STOP_PHONE_SERVICE);
        try {
            stopService(StopPhoneServiceIntent);
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    /*
     * Start the phonestop service & save the current state*/

    private void startPhoneStopService(final long settimeLeftMillisecons) {

        putPref(STOP_PHONE_SERVICE_STATE, START_PHONE_SERVICE);

        try {
            StopPhoneServiceIntent.putExtra("timerValue", settimeLeftMillisecons);
            startService(StopPhoneServiceIntent);
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void putPref(String key, String value) {

        SharedPreferences.Editor editor = getSharedPreferences(AUTH, MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.apply();
    }

    private void togglePhoneService() {
        hideKeyboard(this);
        if (getPref(STOP_PHONE_SERVICE_STATE).equals(STOP_PHONE_SERVICE)) {
            startPhoneStopService(timeLeftMillisecons);
            startStopServiceBtn.setText(R.string.stoper_le_service);
        } else {
            startStopServiceBtn.setText(R.string.redémarrer_le_service);
            stopPhoneStopService();
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateTimer(long timeLeftMillisecons) {
        if (timeLeftMillisecons < 2000) {
            Toast.makeText(this, "Votre temps de travail sur cet appareil est écoulé, le système s'éteindra automatiquement", Toast.LENGTH_LONG).show();
            tvTimer.setText(R.string.byeMessage);
        }

        int hours = (int) (timeLeftMillisecons / 1000) / (60 * 60);
        int minutes = (int) ((timeLeftMillisecons / 1000) % (60 * 60)) / 60;
        int seconds = (int) ((timeLeftMillisecons / 1000) % (60 * 60)) % 60;
        String timeLeftText;
        timeLeftText = "";
        if (hours < 10) timeLeftText += "0";
        timeLeftText += "" + hours;
        timeLeftText += ":";
        if (minutes < 10) timeLeftText += "0";
        timeLeftText += "" + minutes;
        timeLeftText += ":";
        if (seconds < 10) timeLeftText += "0";
        timeLeftText += seconds;

        tvCountDown.setText(timeLeftText);

    }
}

