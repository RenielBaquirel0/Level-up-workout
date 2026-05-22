package com.example.data.model

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object JsonUtils {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val exercisesListType = Types.newParameterizedType(List::class.java, Exercise::class.java)
    private val exercisesAdapter = moshi.adapter<List<Exercise>>(exercisesListType)

    fun exercisesToJson(exercises: List<Exercise>): String {
        return try {
            exercisesAdapter.toJson(exercises) ?: "[]"
        } catch (e: Exception) {
            "[]"
        }
    }

    fun jsonToExercises(json: String): List<Exercise> {
        return try {
            if (json.isBlank()) emptyList() else exercisesAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
