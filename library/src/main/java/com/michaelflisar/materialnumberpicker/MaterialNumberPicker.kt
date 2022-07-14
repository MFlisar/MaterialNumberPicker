package com.michaelflisar.materialnumberpicker

import android.text.InputType

object MaterialNumberPicker {

    const val DEFAULT_OFFSET_ITEMS = 2

    // ordinal must match the array resource array values!
    enum class DataType(val inputType: kotlin.Int) {
        /* 0 */ Int(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED),
        /* 1 */ Float(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED or InputType.TYPE_NUMBER_FLAG_DECIMAL)

        ;

        companion object {
            fun fromObject(obj: Any): DataType {
                return when (obj) {
                    is kotlin.Int -> Int
                    is kotlin.Float -> Float
                    else -> throw RuntimeException("Type not handled!")
                }
            }
        }
    }

    // ordinal must match the array resource array values!
    enum class Style {
        /* 0 */ Input,
        /* 1 */ Scroll
    }
}