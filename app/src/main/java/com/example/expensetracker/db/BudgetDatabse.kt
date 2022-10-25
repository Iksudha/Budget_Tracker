package com.example.expensetracker.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.expensetracker.model.Budget
import com.example.expensetracker.model.BudgetModel


@Database(entities = arrayOf(BudgetModel::class), version = 1)
abstract class BudgetDatabse: RoomDatabase() {

    abstract fun budgetdao(): BudgetDao

}