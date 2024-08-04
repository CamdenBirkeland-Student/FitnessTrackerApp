package com.example.fitnesstrackerapp

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.content.SharedPreferences
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var stepsTextView: TextView
    private lateinit var caloriesTextView: TextView
    private lateinit var distanceTextView: TextView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var summaryButton: Button

    private lateinit var sensorManager: SensorManager
    private var stepCounter: Sensor? = null
    private var isTracking = false
    private var stepCount = 0

    private lateinit var sharedPreferences: SharedPreferences
    private val PREFS_NAME = "fitnessPrefs"
    private val STEPS_KEY = "steps"
    private val CALORIES_KEY = "calories"
    private val DISTANCE_KEY = "distance"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI components
        stepsTextView = findViewById(R.id.stepsTextView)
        caloriesTextView = findViewById(R.id.caloriesTextView)
        distanceTextView = findViewById(R.id.distanceTextView)
        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        summaryButton = findViewById(R.id.summaryButton)

        // Initialize SensorManager and Step Counter
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Set up button click listeners
        startButton.setOnClickListener {
            mockTracking() // Simulate step tracking
        }

        stopButton.setOnClickListener {
            stopTracking()
        }

        summaryButton.setOnClickListener {
            displaySummary()
        }
    }

    private fun mockTracking() {
        val mockSteps = 1000 // Set a fixed number of steps for testing
        stepCount = mockSteps
        stepsTextView.text = "$mockSteps steps"

        // Update distance and calories on the UI
        val distance = calculateDistance(mockSteps)
        distanceTextView.text = String.format("%.2f km", distance)

        val calories = calculateCalories(mockSteps)
        caloriesTextView.text = String.format("%.2f kcal", calories)

        // Save mock data to SharedPreferences
        saveData(mockSteps, distance, calories)
    }

    private fun startTracking() {
        if (!isTracking) {
            stepCount = 0
            sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_NORMAL)
            isTracking = true
            stepsTextView.text = getString(R.string.tracking_started)
        }
    }

    private fun stopTracking() {
        if (isTracking) {
            sensorManager.unregisterListener(this, stepCounter)
            isTracking = false
            stepsTextView.text = getString(R.string.tracking_stopped)

            // Save data to SharedPreferences
            saveData(stepCount, calculateDistance(stepCount), calculateCalories(stepCount))
        }
    }

    private fun saveData(steps: Int, distance: Double, calories: Double) {
        val editor = sharedPreferences.edit()
        editor.putInt(STEPS_KEY, steps)
        editor.putFloat(DISTANCE_KEY, distance.toFloat())
        editor.putFloat(CALORIES_KEY, calories.toFloat())
        editor.apply()
    }

    private fun calculateDistance(steps: Int): Double {
        return steps * 0.775 / 1000 // Convert meters to kilometers
    }

    private fun calculateCalories(steps: Int): Double {
        return steps * 0.04
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (isTracking) {
            stepCount++
            stepsTextView.text = "$stepCount steps"

            // Update distance and calories on the UI
            val distance = calculateDistance(stepCount)
            distanceTextView.text = String.format("%.2f km", distance)

            val calories = calculateCalories(stepCount)
            caloriesTextView.text = String.format("%.2f kcal", calories)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used in this example
    }

    private fun displaySummary() {
        val steps = sharedPreferences.getInt(STEPS_KEY, 0)
        val distance = sharedPreferences.getFloat(DISTANCE_KEY, 0f)
        val calories = sharedPreferences.getFloat(CALORIES_KEY, 0f)

        val summaryMessage = "Summary:\nSteps: $steps\nDistance: %.2f km\nCalories: %.2f kcal".format(distance, calories)
        showAlert(summaryMessage)
    }

    private fun showAlert(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Fitness Summary")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}