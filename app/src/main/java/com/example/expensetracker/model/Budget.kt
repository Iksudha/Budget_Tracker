package com.example.expensetracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey


data class Budget(
    val title:String,
    val amount:Double,
    val month: String,
    val spent:Double) {
}