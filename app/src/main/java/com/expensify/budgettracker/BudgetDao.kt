package com.expensify.budgettracker

import androidx.room.*

@Dao
interface BudgetDao {
    @Query("SELECT * from budgets")
    fun getAll(): List<BudgetModel>

    @Query("SELECT  * FROM budgets ORDER BY id DESC LIMIT 1")
    fun getlast(): List<BudgetModel>

    @Insert
    fun insertAll(vararg budget: BudgetModel)

    @Delete
    fun delete(budget: BudgetModel)

    @Update
    fun update(vararg budget: BudgetModel)
}