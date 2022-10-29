package com.expensify.budgettracker

import android.R
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class Budget_List : AppCompatActivity() {

    private var auth: FirebaseAuth = Firebase.auth
    private var user = Firebase.auth.currentUser
    private val uid = user?.uid //get user id from database
    private var dbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference(uid!!)
    private lateinit var deletedBudget: Budget
    private lateinit var Budgets : List<Budget>
    private lateinit var oldBudget : List<Budget>
    private lateinit var Budget : List<BudgetModel>
    private lateinit var BudgetAdapter: BudgetList
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var db : BudgetDatabse
    var amountExpenseTemp = 0.0
    var amountIncomeTemp = 0.0
    var allTimeExpense: Double = 0.0
    val transactionList: ArrayList<TransactionModel> = arrayListOf<TransactionModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.expensify.budgettracker.R.layout.activity_budget_list)



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

        var recyclerview = findViewById<RecyclerView>(com.expensify.budgettracker.R.id.budget_recyclerview)

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

        findViewById<MaterialCardView>(com.expensify.budgettracker.R.id.budget_card).setOnClickListener {
            val intent = Intent(this, EditBudget::class.java)
            intent.putExtra("Budget",Budget[0])
            startActivity(intent)
        }

        findViewById<FloatingActionButton>(com.expensify.budgettracker.R.id.btn_add_budget).setOnClickListener {
            val intent = Intent(this, Add_Budget::class.java)
            startActivity(intent)
        }
        findViewById<ImageButton>(com.expensify.budgettracker.R.id.backBtn).setOnClickListener {
            val spent = findViewById<View>(com.expensify.budgettracker.R.id.budget_spent) as TextView
            spent.text = allTimeExpense.toString()

            Log.d("TAG", spent.toString())
            finish()
        }
        findViewById<ImageButton>(com.expensify.budgettracker.R.id.refresh).setOnClickListener {
            var pvalue: Double = 0.00
            GlobalScope.launch {

                //db.budgetdao().insertAll(BudgetModel(0,"Budget 1",35000.0,"September"))
                Budget = db.budgetdao().getlast()

                val budget_title = findViewById<View>(com.expensify.budgettracker.R.id.budget_title) as TextView
                //val amount = findViewById<View>(com.example.expensetracker.R.id.budget_balance) as TextView
                val balance = findViewById<View>(com.expensify.budgettracker.R.id.budget_balance) as TextView
                val spent = findViewById<View>(com.expensify.budgettracker.R.id.budget_spent) as TextView
                val month = findViewById<View>(com.expensify.budgettracker.R.id.budget_month) as TextView
                val progress = findViewById<ProgressBar>(com.expensify.budgettracker.R.id.progressBar4)
                val processnumber = findViewById<View>(com.expensify.budgettracker.R.id.progress_number) as TextView





                runOnUiThread {
                    // Stuff that updates the UI
                    budget_title.text = Budget[0].title
                    //val spented = transactions.filter { it.transactionType == "Expense" }.map{it.amount}.sum()
                    //val balanced= Budget[0].amount - spented
                    dbRef.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            transactionList.clear()
                            if (snapshot.exists()) {
                                for (transactionSnap in snapshot.children) {
                                    val transactionData =
                                        transactionSnap.getValue(TransactionModel::class.java) //reference data class
                                    transactionList.add(transactionData!!)
                                }
                            }
                            //separate expanse amount and income amount, and show it based on the range date :
                            for ((i) in transactionList.withIndex()){
                                if (transactionList[i].type == 1 &&
                                    transactionList[i].date!! > 0-86400000 && //minus by 1 day
                                    transactionList[i].date!! <= 0){
                                    amountExpenseTemp += transactionList[i].amount!!
                                }else if (transactionList[i].type == 2 &&
                                    transactionList[i].date!! > 0-86400000 &&
                                    transactionList[i].date!! <= 0){
                                    amountIncomeTemp += transactionList[i].amount!!
                                }
                            }


                            var amountExpenseTemp = 0.0 //reset
                            var amountIncomeTemp = 0.0

                            //take all amount expense and income :
                            for ((i) in transactionList.withIndex()){
                                if (transactionList[i].type == 1 ){
                                    amountExpenseTemp += transactionList[i].amount!!
                                }else if (transactionList[i].type == 2){
                                    amountIncomeTemp += transactionList[i].amount!!
                                }
                            }

                            allTimeExpense = amountExpenseTemp

                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })



                    spent.text = allTimeExpense.toString()

                    balance.text = (Budget[0].amount - allTimeExpense).toString()
                    month.text = Budget[0].month

                    pvalue = (allTimeExpense*100)/Budget[0].amount

                    Log.d("TAG", (pvalue.toInt().toString()))
                    progress.progress = pvalue.toInt()
                    if (pvalue.toInt() >= 100){
                        pvalue= 100.00
                    }
                    processnumber.text = pvalue.toInt().toString()+"%"

                    if(pvalue >= 100){
                        val builder = AlertDialog.Builder(this@Budget_List)
                        //set title for alert dialog
                        builder.setTitle("Alert")
                        //set message for alert dialog
                        builder.setMessage("You have spend more than your allocated amount")
                        builder.setIcon(android.R.drawable.ic_dialog_alert)

                        //performing positive action
                        builder.setPositiveButton("OK"){dialogInterface, which ->
                            Toast.makeText(applicationContext,"clicked ok",Toast.LENGTH_LONG).show()
                        }
                        //performing cancel action

                        val alertDialog: AlertDialog = builder.create()
                        // Set other dialog properties
                        alertDialog.setCancelable(false)
                        alertDialog.show()

                    }



                }

            }
        }
    }



    private fun fetchAll(){
        GlobalScope.launch {

            //db.budgetdao().insertAll(BudgetModel(0,"Budget 1",35000.0,"September"))
            Budget = db.budgetdao().getlast()

            val budget_title = findViewById<View>(com.expensify.budgettracker.R.id.budget_title) as TextView
            //val amount = findViewById<View>(com.example.expensetracker.R.id.budget_balance) as TextView
            val balance = findViewById<View>(com.expensify.budgettracker.R.id.budget_balance) as TextView
            val spent = findViewById<View>(com.expensify.budgettracker.R.id.budget_spent) as TextView
            val month = findViewById<View>(com.expensify.budgettracker.R.id.budget_month) as TextView







            runOnUiThread {
                // Stuff that updates the UI
                budget_title.text = Budget[0].title
                //val spented = transactions.filter { it.transactionType == "Expense" }.map{it.amount}.sum()
                //val balanced= Budget[0].amount - spented
                dbRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        transactionList.clear()
                        if (snapshot.exists()) {
                            for (transactionSnap in snapshot.children) {
                                val transactionData =
                                    transactionSnap.getValue(TransactionModel::class.java) //reference data class
                                transactionList.add(transactionData!!)
                            }
                        }
                        //separate expanse amount and income amount, and show it based on the range date :
                        for ((i) in transactionList.withIndex()){
                            if (transactionList[i].type == 1 &&
                                transactionList[i].date!! > 0-86400000 && //minus by 1 day
                                transactionList[i].date!! <= 0){
                                amountExpenseTemp += transactionList[i].amount!!
                            }else if (transactionList[i].type == 2 &&
                                transactionList[i].date!! > 0-86400000 &&
                                transactionList[i].date!! <= 0){
                                amountIncomeTemp += transactionList[i].amount!!
                            }
                        }


                        var amountExpenseTemp = 0.0 //reset
                        var amountIncomeTemp = 0.0

                        //take all amount expense and income :
                        for ((i) in transactionList.withIndex()){
                            if (transactionList[i].type == 1 ){
                                amountExpenseTemp += transactionList[i].amount!!
                            }else if (transactionList[i].type == 2){
                                amountIncomeTemp += transactionList[i].amount!!
                            }
                        }

                        allTimeExpense = amountExpenseTemp

                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })



                spent.text = allTimeExpense.toString()

                balance.text = (Budget[0].amount - allTimeExpense).toString()
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
        val view = findViewById<View>(com.expensify.budgettracker.R.id.coordinator)
        val snackbar = Snackbar.make(view, "Budget Deleted deleted!",Snackbar.LENGTH_LONG)
        snackbar.setAction("Undo"){
            undoDelete()
        }
            .setActionTextColor(ContextCompat.getColor(this, com.expensify.budgettracker.R.color.blue_500))
            .setTextColor(ContextCompat.getColor(this, com.expensify.budgettracker.R.color.white))
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