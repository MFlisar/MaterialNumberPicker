package com.michaelflisar.materialnumberpicker.demo.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.michaelflisar.materialnumberpicker.AbstractMaterialNumberPicker
import com.michaelflisar.materialnumberpicker.MaterialNumberPicker
import com.michaelflisar.materialnumberpicker.demo.databinding.ActivityDemoBinding
import com.michaelflisar.materialnumberpicker.picker.FloatPicker
import com.michaelflisar.materialnumberpicker.picker.IntPicker
import com.michaelflisar.materialnumberpicker.setup.INumberPickerSetup
import com.michaelflisar.materialnumberpicker.setup.NumberPickerSetupMinMax
import kotlin.collections.ArrayList
import kotlin.math.ceil
import kotlin.math.floor

class DemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDemoBinding

    private var toast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityDemoBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbar)

        val pickersFloat = listOf(binding.mnp2, binding.mnp5, binding.mnp6, binding.mnp12, binding.mnp14)
        val pickersInt =
            listOf(binding.mnp1, binding.mnp3, binding.mnp4, binding.mnp11, binding.mnp13)

        // Float Pickers
        pickersFloat.forEach {
            it.onValueChangedListener =
                { picker: FloatPicker, value: Float, fromUser: Boolean ->
                    showToast(picker, "New float value: $value (user = $fromUser)")
                }
            it.onInvalidValueSelected =
                { picker: FloatPicker, invalidInput: String?, invalidValue: Float?, fromButton: Boolean ->
                    // most likely, if fromButton == true, you don't want to handle this, but fir the demo we show a message in every case
                    showToast(picker, "Invalid float value: $invalidInput | $invalidValue (button = $fromButton)")
                }
        }

        // Int Pickers
        pickersInt.forEach {
            it.onValueChangedListener =
                { picker: IntPicker, value: Int, fromUser: Boolean ->
                    showToast(picker, "New int value: $value (user = $fromUser)")
                }
            it.onInvalidValueSelected =
                { picker: IntPicker, invalidInput: String?, invalidValue: Int?, fromButton: Boolean ->
                    // most likely, if fromButton == true, you don't want to handle this, but fir the demo we show a message in every case
                    showToast(picker, "Invalid int value: $invalidInput | $invalidValue (button = $fromButton)")
                }
        }

        // reading values
        val currentValue = binding.mnp1.value
        val currentSetup = binding.mnp1.setup
        // by default (from XML) the setup is always a min/max setup => if you change the setup programmatically, be more cautios here than me!
        val currentMinMaxSetup = currentSetup as NumberPickerSetupMinMax<Int>
        val currentMin = currentMinMaxSetup.min
        val currentMax = currentMinMaxSetup.max
        val currentStepSize = currentMinMaxSetup.stepSize
        val currentStepSizeLarge = currentMinMaxSetup.stepSizeSecondary

        // updating values
        // mnp4 is of type integer => you can provide any Number but they will be converted to whatever the picker supports!
        binding.mnp4.setup = NumberPickerSetupMinMax(
            100,
            100,
            1000,
            10,
            50, // select same value as stepSize to disable secondary buttons
            { "N=" + it.toString() + "s" }, // custom formatter, in this case it adds prefix and suffix
            { it.toIntOrNull() },
            2
        )

        if (savedInstanceState == null) {
            binding.mnp4.setValue(600) // this triggers the onValueChangedListener!
        }

        // example custom setup
        binding.mnp11.setup = NumberPickerSetupMinMax(
            5,
            0,
            100,
            1,
            2,
            { it.toString() + "x" },
            { it.replace("x", "").toIntOrNull() },
            2
        )
        if (savedInstanceState == null) {
            binding.mnp11.setValue(17)
        }

        // example with custom setup provider including special list of values and suffix
        binding.mnp12.setup = WeightNumberPickerSetup(50f)

        if (savedInstanceState == null) {
            binding.mnp12.setValue(
                70f,
                false /* we want to immediately set the new value and avoid the initial scrolling animation here */
            )
        }

        val stepsToTryFloat = listOf(0.5f, 1f, 2.5f, 5f)
        val stepsToTryInt = stepsToTryFloat.filter { ceil(it) == floor(it) }.map { it.toInt() }

        val updatePickers = { factor: Int ->
            pickersInt.forEach {
                for (adjustment in stepsToTryInt) {
                    if (it.setValue(it.value + adjustment * factor)) {
                        logInfo(it, "Accepted value: ${it.value}")
                        break
                    }
                }
            }
            pickersFloat.forEach {
                for (adjustment in stepsToTryFloat) {
                    if (it.setValue(it.value + adjustment * factor)) {
                        logInfo(it, "Accepted value: ${it.value}")
                        break
                    }
                }
            }
        }
        val setPickers = { value: Int ->
            pickersInt.forEach {
                it.setValue(value)
            }
            pickersFloat.forEach {
                it.setValue(value.toFloat())
            }
        }

        binding.btIncrease.setOnClickListener {
            updatePickers(1)
        }
        binding.btDecrease.setOnClickListener {
            updatePickers(-1)
        }
        binding.bt10.setOnClickListener {
            setPickers(10)
        }
        binding.bt25.setOnClickListener {
            setPickers(25)
        }
        binding.bt50.setOnClickListener {
            setPickers(50)
        }
    }

    private fun logInfo(picker: AbstractMaterialNumberPicker<*, *>, info: String) {
        val name = resources.getResourceName(picker.id).substringAfterLast(":id/")
        Log.d("LOG INFO", "[$name] $info")
    }

    private fun showToast(picker: AbstractMaterialNumberPicker<*, *>, info: String) {
        val name = resources.getResourceName(picker.id).substringAfterLast(":id/")
        val fullInfo = "[$name] $info"
        Log.d("SHOW TOAST", fullInfo)
        toast?.cancel()
        toast = Toast.makeText(this, fullInfo, Toast.LENGTH_SHORT)
        toast?.show()
    }

    /*
     * Provides weights like following:
     * 0..10kg:     1kg steps
     * 10..20kg:    1kg + 2.5kg steps
     * 20..50kg:    2.5kg steps
     * 50..1000kg   5kg steps
     */
    class WeightNumberPickerSetup(
        override val defaultValue: Float
    ) : INumberPickerSetup<Float>/*,
        INumberPickerSetup.ButtonProvider<Float>,
        INumberPickerSetup.SecondaryButtonProvider<Float> */ {

        override val type = MaterialNumberPicker.DataType.Float
        override val scrollerVisibleOffsetItems = 2

        override val formatter = { value: Float ->
            if (value.toInt().toFloat() == value) "${value.toInt()}kg" else "${value}kg"
        }

        override val parser = { value: String -> value.replace("kg", "").toFloatOrNull() }

        /*
        override fun calcPrimaryButtonResult(
            currentValue: Float,
            button: INumberPickerSetup.Button
        ): Float? {
        }

        override fun calcSecondaryButtonResult(
            currentValue: Float,
            button: INumberPickerSetup.Button
        ): Float? {
        }*/

        override fun isValueAllowed(style: MaterialNumberPicker.Style, value: Float?): Boolean {
            if (value == null || value < 0f || value > 1000f)
                return false
            // 0..10kg => all values are allowed, if the are natural numbers
            if (value <= 10f)
                return ceil(value) == floor(value)
            // 10..20 => all values are allowed, if the are natural numbers or divedable by 2.5
            else if (value <= 20f)
                return ceil(value) == floor(value) || (value % 2.5) == 0.0
            // 20..50 => all values are allowed, if they are divedable by 2.5
            else if (value <= 20f)
                return (value % 2.5) == 0.0
            // 50..1000 => all values are allowed, if they are divedable by 5
            else
                return (value % 5.0) == 0.0
        }

        override val longestValue = 5555.5f

        override val allValidValuesSorted by lazy {
            val values = ArrayList<Float>()

            // 0..10
            var value = 0f
            while (value < 10f) {
                values.add(value++)
            }

            // 10..20
            value = 10f
            while (value < 20) {
                if (value == 13f)
                    values.add(12.5f)
                else if (value == 18f)
                    values.add(17.5f)
                values.add(value++)
            }

            // 20..50
            value = 20f
            while (value < 50f) {
                values.add(value)
                value += 2.5f
            }

            // 50..1000
            value = 50f
            while (value <= 1000f) {
                values.add(value)
                value += 5f
            }

            values.sorted()
        }
    }
}