package omnifit.sdk.brain.sample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;
import androidx.appcompat.app.AppCompatActivity;
import com.tedpark.tedpermission.rx2.TedRx2Permission;
import kotlin.Unit;
import omnifit.sdk.brain.ConnectionState;
import omnifit.sdk.brain.HeadsetProperties;
import omnifit.sdk.brain.MeasurementState;
import omnifit.sdk.brain.OmnifitBrain;
import org.joda.time.format.DateTimeFormat;

import java.util.concurrent.TimeUnit;

import static org.joda.time.Instant.EPOCH;

public class SampleActivity extends AppCompatActivity implements View.OnClickListener {

    private ScrollView progressMessageContainer;
    private EditText progressMessageView;
    private ToggleButton findToggleButton;
    private TextView serialNoView;
    private ToggleButton connectToggleButton;
    private ToggleButton batteryLevelToggleButton;
    private ToggleButton electrodeStatusToggleButton;
    private ToggleButton measureToggleButton;

    private StringBuilder progressMessageBuilder = new StringBuilder();
    private HeadsetProperties headsetProperties;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
        );
        setContentView(R.layout.activity_sample);

        progressMessageContainer = findViewById(R.id.sv_progress_message_container);
        progressMessageView = findViewById(R.id.tv_progress_message);
        findToggleButton = findViewById(R.id.tbtn_find);
        serialNoView = findViewById(R.id.tv_serial_no);
        connectToggleButton = findViewById(R.id.tbtn_connect);
        connectToggleButton.setEnabled(false);
        electrodeStatusToggleButton = findViewById(R.id.tbtn_electrode_state);
        batteryLevelToggleButton = findViewById(R.id.tbtn_battery_level);
        measureToggleButton = findViewById(R.id.tbtn_measure);


        // permission check
        requirePermission();
        toggleButtonEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear: {
                updateProgressMessage("");
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tbtn_find:
                if (((ToggleButton) v).isChecked()) find();
                else stopFind();
                break;
            case R.id.tbtn_connect:
                if (((ToggleButton) v).isChecked()) {
                    ((ToggleButton) v).setEnabled(false);
                    connect();
                }
                else {
                    disconnect();
                }
                break;
            case R.id.tbtn_electrode_state:
                if (((ToggleButton) v).isChecked()) subscribeElectrodeStateChange();
                else unsubscribeElectrodeStateChange();
                break;
            case R.id.tbtn_battery_level:
                if (((ToggleButton) v).isChecked()) subscribeBatteryLevelChange();
                else unsubscribeBatteryLevelChange();
                break;
            case R.id.tbtn_measure:
                if (((ToggleButton) v).isChecked()) measure();
                else stopMeasure();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OmnifitBrain.unsubscribeConnectionStateChange();
    }

    private void find() {
        OmnifitBrain.find(
                () -> {

                    updateProgressMessage("> FIND-START");
                    this.headsetProperties = null;
                    connectToggleButton.setEnabled(false);
                    return Unit.INSTANCE;
                },
                headset -> {

                    updateProgressMessage("> FOUND : " + headset.getName());
                    this.headsetProperties = headset;
                    serialNoView.setText(headsetProperties.getSerialNo());
                    connectToggleButton.setEnabled(true);

                    return Unit.INSTANCE;
                },
                () -> {

                    updateProgressMessage("> FIND-FINISH");
                    findToggleButton.setChecked(false);
                    if (headsetProperties != null) {
                        serialNoView.setText(headsetProperties.getSerialNo());
                        connectToggleButton.setEnabled(true);
                    }
                    return Unit.INSTANCE;
                },
                throwable -> {

                    updateProgressMessage("> FIND-ERROR : " + throwable.getClass().getSimpleName());


                    return Unit.INSTANCE;
                }
        );
    }

    private void stopFind() {
        OmnifitBrain.stopFind(true);
    }


    private void connect() {
        OmnifitBrain.connect(getApplicationContext(), headsetProperties);
    }

    private void disconnect() {
        OmnifitBrain.disconnect(getApplicationContext());
    }

    private void subscribeElectrodeStateChange() {
        OmnifitBrain.subscribeElectrodeStateChange(state -> {
            updateProgressMessage("> " + state);
            return Unit.INSTANCE;
        });
    }

    private void unsubscribeElectrodeStateChange() {
        OmnifitBrain.unsubscribeElectrodeStateChange();
    }

    private void subscribeBatteryLevelChange() {
        OmnifitBrain.subscribeBatteryLevelChange(level -> {
            updateProgressMessage("> BATTERY-LEVEL : " + level + "%");
            return Unit.INSTANCE;
        });
    }

    private void unsubscribeBatteryLevelChange() {
        OmnifitBrain.unsubscribeBatteryLevelChange();
    }

    private void measure() {
        OmnifitBrain.measure(
                state -> {
                    if (state instanceof MeasurementState.Start) {
                        MeasurementState.Start s = (MeasurementState.Start) state;
                        updateProgressMessage("> [" + displayElapsedTime(s.getElapsedTime()) + "] MEASURE-START");
                    } else if (state instanceof MeasurementState.Measuring) {
                        MeasurementState.Measuring s = (MeasurementState.Measuring) state;
                        updateProgressMessage("> [" + displayElapsedTime(s.getElapsedTime()) + "] MEASURING");
                    } else if (state instanceof MeasurementState.Stop) {
                        MeasurementState.Stop s = (MeasurementState.Stop) state;
                        updateProgressMessage("> [" + displayElapsedTime(s.getElapsedTime()) + "] MEASURE-STOP");
                    }
                    return Unit.INSTANCE;
                }
        );
    }

    private void stopMeasure() {
        OmnifitBrain.stopMeasure();
    }

    @SuppressLint("CheckResult")
    private void requirePermission() {
        TedRx2Permission.with(SampleActivity.this)
                .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
                .request()
                .subscribe(r -> {
                    if (r.isGranted()) {
                        OmnifitBrain.subscribeConnectionStateChange(
                                state -> {
                                    updateProgressMessage("> " + state);
                                    if (state == ConnectionState.CONNECTED) {
                                        findToggleButton.setEnabled(false);
                                        connectToggleButton.setEnabled(true);
                                        toggleButtonEnabled(true);
                                    } else if (state == ConnectionState.DISCONNECTED) {
                                        findToggleButton.setEnabled(true);
                                        toggleButtonEnabled(false);
                                    }
                                    return Unit.INSTANCE;
                                }
                        );
                    }
                }, throwable -> {
                });
    }

    private void updateProgressMessage(String msg) {
        if (msg.isEmpty()) {
            progressMessageBuilder = new StringBuilder();
        } else progressMessageBuilder.append(msg).append("\n");
        progressMessageView.setText(progressMessageBuilder.toString());
        progressMessageView.setSelection(progressMessageView.length());
        new Handler().postDelayed(() -> progressMessageContainer.fullScroll(View.FOCUS_DOWN), 50L);
    }

    private String displayElapsedTime(int elapsedTime) {
        return EPOCH.withMillis(TimeUnit.SECONDS.toMillis((long) elapsedTime)).toString(DateTimeFormat.forPattern("mm:ss"));
    }

    private void toggleButtonEnabled(boolean isEnabled) {
        electrodeStatusToggleButton.setEnabled(isEnabled);
        batteryLevelToggleButton.setEnabled(isEnabled);
        measureToggleButton.setEnabled(isEnabled);
    }
}
