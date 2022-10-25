package com.example.expensetracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "budgets")
class BudgetModel(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val title:String,
    val amount:Double,
    val month: String): Serializable {
}