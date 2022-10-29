package com.expensify.budgettracker

data class Budget(
    val title:String,
    val amount:Double,
    val month: String,
    val spent:Double) {
}