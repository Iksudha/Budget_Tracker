package com.expensify.budgettracker

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = arrayOf(BudgetModel::class), version = 1)
abstract class BudgetDatabse: RoomDatabase() {

    abstract fun budgetdao(): BudgetDao

}