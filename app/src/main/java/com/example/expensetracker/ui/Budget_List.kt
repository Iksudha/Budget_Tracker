package com.example.expensetracker.ui

import android.R
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.expensetracker.adapter.BudgetList
import com.example.expensetracker.db.BudgetDatabse
import com.example.expensetracker.model.Budget
import com.example.expensetracker.model.BudgetModel
import com.example.expensetracker.model.TransactionModel
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class Budget_List : AppCompatActivity() {
    private lateinit var transactions : List<TransactionModel>
    private lateinit var deletedBudget: Budget
    private lateinit var Budgets : List<Budget>
    private lateinit var oldBudget : List<Budget>
    private lateinit var Budget : List<BudgetModel>
    private lateinit var BudgetAdapter: BudgetList
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var db : BudgetDatabse
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.expensetracker.R.layout.activity_budget_list)



        Budgets = arrayListOf(
            Budget("Budget 1",35000.0,"September",30000.00),
            Budget("Budget 2",25000.0,"August",25000.00),
            Budget("Budget 3",40000.0,"July",37000.00),
            Budget("Budget 4",35000.0,"June",34000.00),
            Budget("Budget 5",28000.0,"June",25000.00),
            Budget("Budget 6",45000.0,"June",45000.00),
        )

        BudgetAdapter = BudgetList(Budgets)
        linearLayoutManager = LinearLayoutManager(this)

        var recyclerview = findViewById<RecyclerView>(com.example.expensetracker.R.id.budget_recyclerview)

        recyclerview.apply {
            adapter = BudgetAdapter
            layoutManager = linearLayoutManager

        }

        db = Room.databaseBuilder(this,
            BudgetDatabse::class.java,
            "budgets").build()
        //fetchAll()

        val itemTouchHelper = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteBudget(Budgets[viewHolder.adapterPosition])
            }

        }

        val swipeHelper = ItemTouchHelper(itemTouchHelper)
        swipeHelper.attachToRecyclerView(recyclerview)

        findViewById<MaterialCardView>(com.example.expensetracker.R.id.budget_card).setOnClickListener {
            val intent = Intent(this, EditBudget::class.java)
            intent.putExtra("Budget",Budget[0])
            startActivity(intent)
        }

        findViewById<FloatingActionButton>(com.example.expensetracker.R.id.btn_add_budget).setOnClickListener {
            val intent = Intent(this, Add_Budget::class.java)
            startActivity(intent)
        }
        findViewById<ImageButton>(com.example.expensetracker.R.id.backBtn).setOnClickListener {
            finish()

        }
    }

    private fun fetchAll(){
        GlobalScope.launch {

           //db.budgetdao().insertAll(BudgetModel(0,"Budget 1",35000.0,"September"))
            Budget = db.budgetdao().getlast()

            val budget_title = findViewById<View>(com.example.expensetracker.R.id.budget_title) as TextView
            //val amount = findViewById<View>(com.example.expensetracker.R.id.budget_balance) as TextView
            val balance = findViewById<View>(com.example.expensetracker.R.id.budget_balance) as TextView
            val spent = findViewById<View>(com.example.expensetracker.R.id.budget_spent) as TextView
            val month = findViewById<View>(com.example.expensetracker.R.id.budget_month) as TextView



            runOnUiThread {
                // Stuff that updates the UI
                budget_title.text = Budget[0].title
               //val spented = transactions.filter { it.transactionType == "Expense" }.map{it.amount}.sum()
                //val balanced= Budget[0].amount - spented

                spent.text = 0.00.toString()
                balance.text = Budget[0].amount.toString()
                month.text = Budget[0].month


            }



        }
    }

    private fun undoDelete(){
        GlobalScope.launch {

            Budgets = oldBudget

            runOnUiThread {
                BudgetAdapter.setData(Budgets)

            }
        }
    }

    private fun showSnackbar(){
        val view = findViewById<View>(com.example.expensetracker.R.id.coordinator)
        val snackbar = Snackbar.make(view, "Budget Deleted deleted!",Snackbar.LENGTH_LONG)
        snackbar.setAction("Undo"){
            undoDelete()
        }
            .setActionTextColor(ContextCompat.getColor(this, com.example.expensetracker.R.color.blue_500))
            .setTextColor(ContextCompat.getColor(this, com.example.expensetracker.R.color.white))
            .show()
    }

    private fun deleteBudget(budget: Budget){
        deletedBudget = budget
        oldBudget = Budgets

        GlobalScope.launch {

            print(Budgets.indexOf(budget))
            Budgets = Budgets.drop(Budgets.indexOf(budget) +1)
            runOnUiThread {
                BudgetAdapter.setData(Budgets)
                showSnackbar()
            }
        }
    }
    override fun onResume() {

        super.onResume()
        runOnUiThread {
            // Stuff that updates the UI
            fetchAll()
        }

    }
}