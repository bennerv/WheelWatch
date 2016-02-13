package com.openxc.openxcstarter;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.view.MenuItem;
import android.widget.TextView;

import com.openxc.measurements.SteeringWheelAngle;
import com.openxc.measurements.VehicleSpeed;
import com.openxcplatform.openxcstarter.R;
import com.openxc.VehicleManager;
import com.openxc.measurements.Measurement;

import java.security.Key;
import java.text.DecimalFormat;
import java.util.Map;

public class StarterActivity extends Activity {
    private static final String TAG = "StarterActivity";

    private VehicleManager mVehicleManager;
    private TextView mVehicleSpeedView;
    private TextView mSteeringWheelView;

    private TelephonyManager telephonyManager;
    private PhoneStateListener phoneStateListener;

    private DecimalFormat df;

    // Variables to determine dangerous driving
    private static double maxCondition = 15 * 50;

    // Images for bad and good
    ImageView good_image;
    ImageView bad_image;

    private double speed;
    private double steeringAngle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starter);
        // grab a reference to the engine speed text object in the UI, so we can
        // manipulate its value later from Java code
        mVehicleSpeedView = (TextView) findViewById(R.id.vehicle_speed);
        mSteeringWheelView = (TextView) findViewById(R.id.steering);

        speed = 0.0;
        steeringAngle = 0.0;

        ActionBar actionBar = getActionBar();

        if (getActionBar() != null) {
            actionBar.setTitle(R.string.wheel_title);
        }

        good_image = (ImageView)findViewById(R.id.good_image);
        bad_image = (ImageView)findViewById(R.id.warning_image);
    }

    @Override
    public void onPause() {
        super.onPause();
        // When the activity goes into the background or exits, we want to make
        // sure to unbind from the service to avoid leaking memory
        if(mVehicleManager != null) {
            Log.i(TAG, "Unbinding from Vehicle Manager");
            // Remember to remove your listeners, in typical Android
            // fashion.
            mVehicleManager.removeListener(VehicleSpeed.class,
                    mSpeedListener);
            unbindService(mConnection);
            mVehicleManager = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // When the activity starts up or returns from the background,
        // re-connect to the VehicleManager so we can receive updates.
        if(mVehicleManager == null) {
            Intent intent = new Intent(this, VehicleManager.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    /* This is an OpenXC measurement listener object - the type is recognized
     * by the VehicleManager as something that can receive measurement updates.
     * Later in the file, we'll ask the VehicleManager to call the receive()
     * function here whenever a new EngineSpeed value arrives.
     */
     VehicleSpeed.Listener mSpeedListener = new VehicleSpeed.Listener() {
        @Override
        public void receive(Measurement measurement) {
            // When we receive a new EngineSpeed value from the car, we want to
            // update the UI to display the new value. First we cast the generic
            // Measurement back to the type we know it to be, an EngineSpeed.
            final VehicleSpeed speed_measurement = (VehicleSpeed) measurement;
            // In order to modify the UI, we have to make sure the code is
            // running on the "UI thread" - Google around for this, it's an
            // important concept in Android.
            StarterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Finally, we've got a new value and we're running on the
                    // UI thread - we set the text of the EngineSpeed view to
                    // the latest value
                    speed = speed_measurement.getValue().doubleValue();

                    String text = String.format(getString(R.string.speed_message), speed_measurement.getValue().doubleValue());
                    Log.i(TAG, text);
                    mVehicleSpeedView.setText(text);

                    if (checkConditions(speed, steeringAngle)) {
                        // show red
                        good_image.setVisibility(View.GONE);
                        bad_image.setVisibility(View.VISIBLE);
                    } else {
                        bad_image.setVisibility(View.GONE);
                        good_image.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    };

    SteeringWheelAngle.Listener mAngleListener = new SteeringWheelAngle.Listener() {
        @Override
        public void receive(Measurement measurement) {

            final SteeringWheelAngle steering_measurement = (SteeringWheelAngle) measurement;

            StarterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    steeringAngle = steering_measurement.getValue().doubleValue();

                    String text = String.format(getString(R.string.angle_message), steering_measurement.getValue().doubleValue());
                    Log.i(TAG, text);

                    mSteeringWheelView.setText(text);


                    if (checkConditions(speed, steeringAngle)) {
                        // show red
                        good_image.setVisibility(View.GONE);
                        bad_image.setVisibility(View.VISIBLE);
                    } else {
                        bad_image.setVisibility(View.GONE);
                        good_image.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the VehicleManager service is
        // established, i.e. bound.
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            Log.i(TAG, "Bound to VehicleManager");
            // When the VehicleManager starts up, we store a reference to it
            // here in "mVehicleManager" so we can call functions on it
            // elsewhere in our code.
            mVehicleManager = ((VehicleManager.VehicleBinder) service)
                    .getService();

            // We want to receive updates whenever the EngineSpeed changes. We
            // have an EngineSpeed.Listener (see above, mSpeedListener) and here
            // we request that the VehicleManager call its receive() method
            // whenever the EngineSpeed changes
            mVehicleManager.addListener(VehicleSpeed.class, mSpeedListener);
            mVehicleManager.addListener(SteeringWheelAngle.class, mAngleListener);
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "VehicleManager Service  disconnected unexpectedly");
            mVehicleManager = null;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.starter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if(id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if(id == R.id.phone_call) {
            makeCall();
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean checkConditions (double speed, double steeringAngle) {
        if (speed * Math.abs(steeringAngle) > maxCondition) {
            return true;
        }
        else {
            return false;
        }
    }

    public void makeCall() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String phone = sharedPreferences.getString("number", "5555555555");

        //Don't allow users to put in an emergency number for testing purposes
        if(phone.trim().equals("911")) {
            phone = "5555555555";
        }

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneStateListener = new MyPhoneStateListener(this);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        //make the phone call
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
        startActivity(intent);



        Log.d(TAG, "Calling the following number " + phone);

    }

    @Override
    public void onDestroy() {
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);


    }
}
