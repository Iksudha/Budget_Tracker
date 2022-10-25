package com.example.expensetracker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.R
import com.example.expensetracker.model.Budget

class BudgetList(private var budgets: List<Budget>) :
    RecyclerView.Adapter<BudgetList.BudgetHolder>() {

    class BudgetHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title : TextView = view.findViewById(R.id.budget_title_list)
        val amount : TextView = view.findViewById(R.id.budget_amount_list)
        val month : TextView = view.findViewById(R.id.budget_month_list)
        val spent : TextView = view.findViewById(R.id.budget_spent_list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.budget_card, parent, false)
        return BudgetHolder(view)
    }

    override fun onBindViewHolder(holder: BudgetHolder, position: Int) {
        val budget = budgets[position]

        holder.title.text = budget.title
        holder.month.text = budget.month
        holder.spent.text = budget.spent.toString()
        holder.amount.text = budget.amount.toString()


    }

    override fun getItemCount(): Int {
        return budgets.size
    }

    fun setData(transactions: List<Budget>){
        this.budgets = transactions
        notifyDataSetChanged()
    }
}

