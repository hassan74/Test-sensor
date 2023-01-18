package com.example.testsensorapp

import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.concurrent.futures.await
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.HealthServices
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.data.*
import androidx.health.services.client.data.DataType.Companion.HEART_RATE_BPM
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.request.SensorRequest
import com.google.android.gms.tasks.OnSuccessListener
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class MainActivity2 : AppCompatActivity() {
    private var supportsHeartRate: Boolean = false
    private var supportsExerecise: Boolean = false
    private val MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION = 101
    lateinit var fitnessOptions: FitnessOptions
    lateinit var runningCapabilities:ExerciseCapabilities
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        val healthClient = HealthServices.getClient(this /*context*/)

        val measureClient = healthClient.measureClient
        val exerciseClient = healthClient.exerciseClient

        lifecycleScope.launch {
            val capabilities = measureClient.getCapabilitiesAsync().await()
            supportsHeartRate = DataType.HEART_RATE_BPM in capabilities.supportedDataTypesMeasure
        }
        lifecycleScope.launch {
            val capabilities = exerciseClient.getCapabilitiesAsync().await()
            supportsExerecise = ExerciseType.RUNNING in capabilities.supportedExerciseTypes
        }


        // Whether we can make a one-time goal for aggregate steps.
       /* val stepGoals = runningCapabilities.supportedGoals[DataType.STEPS_TOTAL]
        supportsStepGoals =
            (stepGoals != null && ComparisonType.GREATER_THAN_OR_EQUAL in stepGoals)
*/
        // Whether auto-pause is supported
       // val supportsAutoPause = runningCapabilities.supportsAutoPauseAndResume


        if (!supportsHeartRate) {
            Toast.makeText(this, "This device not support heart rate", Toast.LENGTH_SHORT).show()
        } else {
            // Register the callback.
            measureClient.registerMeasureCallback(DataType.HEART_RATE_BPM, heartRateCallback)
        }
        if (!supportsExerecise) {
            Toast.makeText(this, "This device not support running", Toast.LENGTH_SHORT).show()
        } else {
            // Register the callback.
            exerciseClient.setUpdateCallback(exercisecallback)
        }
    }

    val heartRateCallback = object : MeasureCallback {
        override fun onAvailabilityChanged(
            dataType: DeltaDataType<*, *>,
            availability: Availability
        ) {
            if (availability is DataTypeAvailability) {

                // Handle availability change.
                Toast.makeText(this@MainActivity2, "This device not support running", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onDataReceived(data: DataPointContainer) {
            // Inspect data points.
        }
    }

    val exercisecallback = object : ExerciseUpdateCallback {
        override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
            val exerciseStateInfo = update.exerciseStateInfo
            val activeDuration = update.activeDurationCheckpoint
            val latestMetrics = update.latestMetrics
            val latestGoals = update.latestAchievedGoals
            Toast.makeText(this@MainActivity2, exerciseStateInfo.toString(), Toast.LENGTH_SHORT).show()

        }

        override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) {
            // For ExerciseTypes that support laps, this is called when a lap is marked.
        }

        override fun onRegistered() {
            Toast.makeText(this@MainActivity2, "exercise onRegistered", Toast.LENGTH_SHORT).show()
        }

        override fun onRegistrationFailed(throwable: Throwable) {
            Toast.makeText(this@MainActivity2, "onRegistrationFailed", Toast.LENGTH_SHORT).show()

        }

        override fun onAvailabilityChanged(
            dataType: DataType<*, *>,
            availability: Availability
        ) {
            // Called when the availability of a particular DataType changes.
            when {
                availability is LocationAvailability ->
                    Toast.makeText(this@MainActivity2, "LocationAvailability", Toast.LENGTH_SHORT).show()

                availability is DataTypeAvailability ->
                    Toast.makeText(this@MainActivity2, "DataTypeAvailability", Toast.LENGTH_SHORT).show()

            }
        }
    }

/*    fun checkGoogleFitPremission() {
        fitnessOptions =
            FitnessOptions.builder()
                .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_HEART_POINTS, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_SLEEP_SEGMENT, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_WORKOUT_EXERCISE, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_LOCATION_SAMPLE, FitnessOptions.ACCESS_READ)
                .build()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION
            )
        } else {
        }
        getDataUsingSensor(DataType.TYPE_HEART_POINTS)
        getDataUsingSensor(DataType.TYPE_STEP_COUNT_DELTA)
        getDataUsingSensor(DataType.TYPE_SLEEP_SEGMENT)
        getDataUsingSensor(DataType.TYPE_WORKOUT_EXERCISE)
        getDataUsingSensor(DataType.TYPE_ACTIVITY_SEGMENT)
        getDataUsingSensor(DataType.TYPE_DISTANCE_DELTA)
        getDataUsingSensor(DataType.TYPE_LOCATION_SAMPLE)

        if (!GoogleSignIn.hasPermissions(
                getGoogleAccount(),
                fitnessOptions
            )
        ) {
            val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 100
            GoogleSignIn.requestPermissions(
                this, // your activity
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                GoogleSignIn.getLastSignedInAccount(this),
                fitnessOptions
            )
        } else {
            startDataReading()
            getDataUsingSensor(DataType.TYPE_HEART_POINTS)
            getDataUsingSensor(DataType.TYPE_STEP_COUNT_DELTA)
            getDataUsingSensor(DataType.TYPE_SLEEP_SEGMENT)
            getDataUsingSensor(DataType.TYPE_WORKOUT_EXERCISE)
            getDataUsingSensor(DataType.TYPE_ACTIVITY_SEGMENT)
            getDataUsingSensor(DataType.TYPE_DISTANCE_DELTA)
            getDataUsingSensor(DataType.TYPE_LOCATION_SAMPLE)

        }


    }

    private fun getGoogleAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getAccountForExtension(this, fitnessOptions)
    }

    private fun startDataReading() {
        getGoogleAccount()?.let {
            Fitness.getHistoryClient(this, it)
                .readDailyTotal(DataType.TYPE_WORKOUT_EXERCISE)
                .addOnSuccessListener {
                    Log.i("datasource", "Data source found: ${it.dataSource}")
                    Log.i("datasource", "Data Source type: ${it.dataType.name}")
                    Toast.makeText(this, it.dataType.toString(), Toast.LENGTH_SHORT).show()
                }
                .addOnSuccessListener(this)
            Fitness.getHistoryClient(this, it)
                .readDailyTotal(DataType.TYPE_ACTIVITY_SEGMENT)
                .addOnSuccessListener {
                    Log.i("datasource_s", "Data source found: ${it.dataSource}")
                    Log.i("datasource_s", "Data Source type: ${it.dataType.name}")
                    Toast.makeText(this, it.dataType.toString(), Toast.LENGTH_SHORT).show()
                }
                .addOnSuccessListener(this)

            Fitness.getHistoryClient(this, it)
                .readDailyTotal(DataType.AGGREGATE_CALORIES_EXPENDED)
                .addOnSuccessListener(this) {
                    Log.i("datasource_c", "Data source found: ${it.dataSource}")
                    Log.i("datasource_c", "Data Source type: ${it.dataType.name}")
                    Toast.makeText(this, it.dataType.toString(), Toast.LENGTH_SHORT).show()
                }
                .addOnSuccessListener(this)
        }
    }

    fun getDataUsingSensor(dataType: DataType) {
        Fitness.getSensorsClient(this, getGoogleAccount()!!)
            .add(SensorRequest.Builder()
                .setDataType(dataType)
                .setSamplingRate(1, TimeUnit.SECONDS)
                .build(),
                {
                    var value = it.getValue(Field.FIELD_ACTIVITY).toString()
                    Log.e("sens-activity", value)
                    Toast.makeText(this, value, Toast.LENGTH_SHORT).show()

                }

            )

    }



    override fun onSuccess(p0: DataSet?) {
            Log.i("datasource_c", "Data source found: ${p0?.dataSource}")
            Log.i("datasource_c", "Data Source type: ${p0?.dataType?.name}")
            Toast.makeText(this, p0?.dataType.toString(), Toast.LENGTH_SHORT).show()

    }*/
}