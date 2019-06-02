package com.example.exam1;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class StopPhoneService extends Service {
    private CountDownTimer countDownTimer;
    private boolean timerRunning;
    private long timeLeftMillisecons = 1000 * 60 * 60 * 24; // 24 h
    private Intent broadcastIntent;
    private Intent i;

    public StopPhoneService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTimer();
        Log.d("TAG", "onDestroy: service stopped");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        startTimer(intent.getLongExtra("timerValue",timeLeftMillisecons));
        i = new Intent(MainActivity.UPDATE_TICK);
        Log.d("TAG", "onStart: service started");
    }

    private void fire(long timeLeftMillisecons) {
        i.putExtra("tick", timeLeftMillisecons);
        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(i);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        broadcastIntent = new Intent();
        broadcastIntent.setAction("TicksBroadcast");
    }

    private void startTimer(final long settimeLeftMillisecons) {
        countDownTimer = new CountDownTimer(settimeLeftMillisecons, 1000) {
            @Override
            public void onTick(long l) {
                timeLeftMillisecons = l;
                fire(timeLeftMillisecons);
            }

            @Override
            public void onFinish() {

            }
        }.start();
        timerRunning = true;
    }


    private void stopTimer() {
        countDownTimer.cancel();
        timerRunning = false;
    }



}

