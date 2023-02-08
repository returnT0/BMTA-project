package com.example.bmta.room

import androidx.room.TypeConverter
import com.google.gson.Gson
import java.util.*

class DateConverter {
    @TypeConverter
    fun dateToJson(date : Date) : String = Gson().toJson(date)
    @TypeConverter
    fun jsonToDate(json : String) : Date = Gson().fromJson(json, Date::class.java)
}