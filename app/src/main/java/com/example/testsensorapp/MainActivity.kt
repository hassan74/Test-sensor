package com.example.testsensorapp

import android.Manifest
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataSource
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataSourcesRequest
import kotlinx.coroutines.delay
import java.util.Date

class MainActivity : AppCompatActivity() {

    // private lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManager
    private var proximity: Sensor? = null
    private var gyro: Sensor? = null
    var xPreviousAccel = 0f
    var yPreviousAccel = 0f
    var zPreviousAccel = 0f
    var xAccel = 0f
    var yAccel = 0f
    var zAccel = 0f
    var date = Date()
    var xPreviousRot = 0f
    var yPreviousRot = 0f
    var zPreviousRot = 0f
    var xRot = 0f
    var yRot = 0f
    var zRot = 0f
    var  currentMomentSensorAccData = SensorData(0f ,0f,0f ,Date())
    var  currentMomentSensorGyroData = SensorData(0f ,0f,0f ,Date())
    var accelometerParams = ArrayList<SensorData>()
    var gyroParams = ArrayList<SensorData>()

    var prevdate = Date()


    private val mLightSensorListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            xAccel = event.values[0]
            yAccel = event.values[1]
            zAccel = event.values[2]
            currentMomentSensorAccData = SensorData(xAccel, yAccel, zAccel, Date())
            accelometerParams.add(currentMomentSensorAccData)
            var isTakbirSens1=false
            var isTakbirSens2=false
            for (item in accelometerParams.reversed()) {
                val diff: Long = Date().time - item.date.time
                val seconds = diff / 1000
                if (seconds <5) {
                    isTakbirSens1 =isTakbir(currentMomentSensorAccData, item)
                    if(isTakbirSens1)
                        break
                }
            }
            for (item in gyroParams.reversed()) {
                val diff: Long = Date().time - item.date.time
                val seconds = diff / 1000
                if (seconds <5) {
                    isTakbirSens2 =isTakbir2(currentMomentSensorGyroData, item)
                    if(isTakbirSens2)
                        break
                }
            }
            getValueFromOneSecond(accelometerParams)?.let { prevSecondSensorDate ->
               isTakbirSens1 =isTakbir(currentMomentSensorAccData, prevSecondSensorDate)
            }
           getValueFromOneSecond(gyroParams)?.let { prevSecondSensorDate ->
                isTakbirSens2 =isTakbir2(currentMomentSensorGyroData , prevSecondSensorDate)
            }
            if(isTakbirSens1&&isTakbirSens2){
                val mMediaPlayer = MediaPlayer.create(this@MainActivity, R.raw.done)
                mMediaPlayer.start()
            }
            if(accelometerParams.size==2000){
                accelometerParams= accelometerParams.subList(0 ,200) as ArrayList<SensorData>
            }
            if(gyroParams.size==2000){
                gyroParams= gyroParams.subList(0 ,200) as ArrayList<SensorData>
            }

        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        }
    }

    private fun isTakbir(currentSensorDate: SensorData, prevSecondSensorDate: SensorData) :Boolean{
        var deltaX = Math.abs(currentSensorDate.x - prevSecondSensorDate.x)
        var deltaY = Math.abs(currentSensorDate.y - prevSecondSensorDate.y)
        var deltaZ = Math.abs(currentSensorDate.z - prevSecondSensorDate.z)
        //Log.e("Delta" ,deltaX.toString() +" "+deltaY +" "+deltaZ)
            if(deltaX>6.5 && deltaX<=13 && deltaY>4 && deltaY<=7 && deltaZ>5 && deltaZ<=11){
            return true
        }
        return false
    }
    private fun isTakbir2(currentSensorDate: SensorData, prevSecondSensorDate: SensorData) :Boolean{
        var deltaX = Math.abs(currentSensorDate.x - prevSecondSensorDate.x)
        var deltaY = Math.abs(currentSensorDate.y - prevSecondSensorDate.y)
        var deltaZ = Math.abs(currentSensorDate.z - prevSecondSensorDate.z)
       // Log.e("Delta2" ,deltaX.toString() +" "+deltaY +" "+deltaZ)

        if(deltaX>0.7 && deltaX<=2.6 && deltaY>=0.1 && deltaY<=1.5){
            return true
        }
        return false
    }

    private fun getValueFromOneSecond(accelometerParams: java.util.ArrayList<SensorData>): SensorData? {
        for (item in accelometerParams.reversed()) {
            val diff: Long = Date().time - item.date.time
            val seconds = diff / 1000
            if (seconds >= 1) {
                return item
            }
        }
        return null
    }

    private val gyroscopeSensor: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            xRot = event.values[0]
            yRot = event.values[1]
            zRot = event.values[2]
            currentMomentSensorGyroData = SensorData(xRot, yRot, zRot, Date())
            gyroParams.add(currentMomentSensorGyroData)

        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // setContentView(binding.root)
        findViewById<ConstraintLayout>(R.id.pick_acc).setOnClickListener {
            date = Date()
            val diff: Long = date.time - prevdate.time
            val seconds = diff / 1000
            var deltaX = Math.abs(xPreviousAccel - xAccel)
            var deltaY = Math.abs(yPreviousAccel - yAccel)
            var deltaZ = Math.abs(zPreviousAccel - zAccel)

            Log.e("x", "" + xAccel + " , " + xPreviousAccel + " , " + deltaX + " , " + seconds)
            Log.e("y", "" + yAccel + " , " + yPreviousAccel + " , " + deltaY + " , " + seconds)
            Log.e("z", "" + zAccel + " , " + zPreviousAccel + " , " + deltaZ + " , " + seconds)
            isTakbir(currentMomentSensorAccData, SensorData(xPreviousAccel ,yPreviousAccel ,zPreviousAccel ,Date()))

            xPreviousAccel = xAccel
            yPreviousAccel = yAccel
            zPreviousAccel = zAccel

            var deltaXRot = Math.abs(xPreviousRot - xRot)
            var deltaYRot = Math.abs(yPreviousRot - yRot)
            var deltaZRot = Math.abs(zPreviousRot - zRot)

            isTakbir2(currentMomentSensorGyroData, SensorData(xPreviousRot ,yPreviousRot ,zPreviousRot , Date()))
            Log.e("xRot", "" + xRot + " , " + xPreviousRot + " , " + deltaXRot + " , " + seconds)
            Log.e("yRot", "" + yRot + " , " + yPreviousRot + " , " + deltaYRot + " , " + seconds)
            Log.e("zRot", "" + zRot + " , " + zPreviousRot + " , " + deltaZRot + " , " + seconds)

            xPreviousRot = xRot
            yPreviousRot = yRot
            zPreviousRot = zRot
            prevdate = date
        }
        if (checkSelfPermission(Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.BODY_SENSORS), 1);
        } else {
            Log.d(ContentValues.TAG, "ALREADY GRANTED");
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximity = sensorManager.getDefaultSensor(Sensor.TYPE_MOTION_DETECT)
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        val mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL)

        gyro?.also { proximity ->
            sensorManager.registerListener(
                gyroscopeSensor,
                gyro,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        for (sensor in sensors) {
            println(sensor.name)
        }
        val accSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mSensorManager.registerListener(
            mLightSensorListener, accSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }



}