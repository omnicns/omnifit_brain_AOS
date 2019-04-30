package omnifit.sdk.brain.sample

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import com.tedpark.tedpermission.rx2.TedRx2Permission
import omnifit.sdk.brain.ConnectionState
import omnifit.sdk.brain.HeadsetProperties
import omnifit.sdk.brain.MeasurementState
import omnifit.sdk.brain.OmnifitBrain
import org.joda.time.Instant.EPOCH
import org.joda.time.format.DateTimeFormat
import java.util.concurrent.TimeUnit

class SampleActivity : AppCompatActivity(), View.OnClickListener {
    private var progressMessageContainer: ScrollView? = null
    private var progressMessageView: EditText? = null
    private var findToggleButton: ToggleButton? = null
    private var serialNoView: TextView? = null
    private var connectToggleButton: ToggleButton? = null
    private var batteryLevelToggleButton: ToggleButton? = null
    private var electrodeStatusToggleButton: ToggleButton? = null
    private var measureToggleButton: ToggleButton? = null

    private var progressMessageBuilder = StringBuilder()
    private var headsetProperties: HeadsetProperties? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
            WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
        )
        setContentView(R.layout.activity_sample)

        progressMessageContainer = findViewById(R.id.sv_progress_message_container)
        progressMessageView = findViewById(R.id.tv_progress_message)
        findToggleButton = findViewById(R.id.tbtn_find)
        serialNoView = findViewById(R.id.tv_serial_no)
        connectToggleButton = findViewById(R.id.tbtn_connect)
        connectToggleButton!!.isEnabled = false
        electrodeStatusToggleButton = findViewById(R.id.tbtn_electrode_state)
        batteryLevelToggleButton = findViewById(R.id.tbtn_battery_level)
        measureToggleButton = findViewById(R.id.tbtn_measure)


        // permission check
        requirePermission()
        toggleButtonEnabled(false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_clear -> {
                updateProgressMessage("")
                return true
            }
            else              -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tbtn_find            -> if ((v as ToggleButton).isChecked) find() else stopFind()
            R.id.tbtn_connect         -> if ((v as ToggleButton).isChecked) {
                v.isEnabled = false
                connect()
            }
            else disconnect()
            R.id.tbtn_electrode_state -> if ((v as ToggleButton).isChecked) subscribeElectrodeStateChange() else unsubscribeElectrodeStateChange()
            R.id.tbtn_battery_level   -> if ((v as ToggleButton).isChecked) subscribeBatteryLevelChange() else unsubscribeBatteryLevelChange()
            R.id.tbtn_measure         -> if ((v as ToggleButton).isChecked) measure()
            else stopMeasure()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        OmnifitBrain.unsubscribeConnectionStateChange()
    }

    private fun find() {
        OmnifitBrain.find(
            {
                updateProgressMessage("> FIND-START")
                this.headsetProperties = null
                connectToggleButton!!.isEnabled = false
            },
            { headset ->
                updateProgressMessage("> FOUND : " + headset?.name)
                this.headsetProperties = headset
                serialNoView!!.text = headsetProperties!!.serialNo
                connectToggleButton!!.isEnabled = true
            },
            {
                updateProgressMessage("> FIND-FINISH")
                findToggleButton?.isChecked = false
                headsetProperties?.let {
                    serialNoView!!.text = it.serialNo
                    connectToggleButton!!.isEnabled = true
                }
            },
            { throwable ->
                updateProgressMessage("> FIND-ERROR : " + throwable.javaClass.simpleName)

            }
        )
    }

    private fun stopFind() {
        OmnifitBrain.stopFind(true)
    }


    private fun connect() {
        OmnifitBrain.connect(applicationContext, headsetProperties!!)
    }

    private fun disconnect() {
        OmnifitBrain.disconnect(applicationContext)
    }

    private fun subscribeElectrodeStateChange() {
        OmnifitBrain.subscribeElectrodeStateChange { state ->
            updateProgressMessage("> $state")
            Unit
        }
    }

    private fun unsubscribeElectrodeStateChange() {
        OmnifitBrain.unsubscribeElectrodeStateChange()
    }

    private fun subscribeBatteryLevelChange() {
        OmnifitBrain.subscribeBatteryLevelChange { level ->
            updateProgressMessage("> BATTERY-LEVEL : $level%")
        }
    }

    private fun unsubscribeBatteryLevelChange() {
        OmnifitBrain.unsubscribeBatteryLevelChange()
    }

    private fun measure() {

        OmnifitBrain.measure(
            onState = { state ->
                when (state) {
                    is MeasurementState.Start     -> updateProgressMessage("> [${displayElapsedTime(state.elapsedTime)}] MEASURE-START")
                    is MeasurementState.Measuring -> updateProgressMessage("> [${displayElapsedTime(state.elapsedTime)}] MEASURE-START")
                    is MeasurementState.Stop      -> updateProgressMessage("> [${displayElapsedTime(state.elapsedTime)}]  MEASURE-STOP")
                }
            }
        )
    }

    private fun stopMeasure() {
        OmnifitBrain.stopMeasure()
    }

    @SuppressLint("CheckResult")
    private fun requirePermission() {
        TedRx2Permission.with(this@SampleActivity)
            .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
            .request()
            .subscribe({ r ->
                if (r.isGranted) {
                    OmnifitBrain.subscribeConnectionStateChange { state ->
                        updateProgressMessage("> $state")
                        if (state === ConnectionState.CONNECTED) {
                            findToggleButton!!.isEnabled = false
                            connectToggleButton!!.isEnabled = true
                            toggleButtonEnabled(true)
                        }
                        else if (state === ConnectionState.DISCONNECTED) {
                            findToggleButton!!.isEnabled = true
                            toggleButtonEnabled(false)
                        }
                        Unit
                    }
                }
            }, { throwable -> })
    }

    private fun updateProgressMessage(msg: String) {
        if (msg.isEmpty()) {
            progressMessageBuilder = StringBuilder()
        }
        else
            progressMessageBuilder.append(msg).append("\n")
        progressMessageView!!.setText(progressMessageBuilder.toString())
        progressMessageView!!.setSelection(progressMessageView!!.length())
        Handler().postDelayed({ progressMessageContainer!!.fullScroll(View.FOCUS_DOWN) }, 50L)
    }

    private fun displayElapsedTime(elapsedTime: Int): String {
        return EPOCH.withMillis(TimeUnit.SECONDS.toMillis(elapsedTime.toLong())).toString(DateTimeFormat.forPattern("mm:ss"))
    }

    private fun toggleButtonEnabled(isEnabled: Boolean) {
        electrodeStatusToggleButton!!.isEnabled = isEnabled
        batteryLevelToggleButton!!.isEnabled = isEnabled
        measureToggleButton!!.isEnabled = isEnabled
    }
}