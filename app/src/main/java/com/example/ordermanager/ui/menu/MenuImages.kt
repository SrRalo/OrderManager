package com.example.ordermanager.ui.menu

import android.util.Log
import com.example.ordermanager.R

object MenuImages {
    val productImageMap: Map<String, Int> = mapOf(
        "Hamburguesa Clásica" to R.drawable.hamburguesa,
        "Papas Fritas Grandes" to R.drawable.papasfritas,
        "Pizza Pepperoni" to R.drawable.pizza,
        "Tacos al Pastor" to R.drawable.tacos,
        "Ensalada César" to R.drawable.ensalada,
        "Refresco de Cola" to R.drawable.refresco,
        "Agua Natural" to R.drawable.agua,
        "Flan Napolitano" to R.drawable.flan,
        "Quesadillas" to R.drawable.quesadilla,
        "Burrito Supreme" to R.drawable.burrito
    )

    fun getImageResId(productName: String): Int {
        val trimmed = productName.trim()
        // Case-insensitive lookup
        for ((key, value) in productImageMap) {
            if (key.equals(trimmed, true)) { // true = ignore case
                return value
            }
        }
        Log.w("MenuImages", "Sin imagen para '$productName' (trimmed: '$trimmed') — se usa placeholder")
        return R.drawable.img_placeholder
    }
}
