package com.expensify.budgettracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.room.Room
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class EditBudget : AppCompatActivity() {

    private lateinit var budget : BudgetModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_budget)

        val languages = resources.getStringArray(R.array.months)
        // create an array adapter and pass the required parameter
        // in our case pass the context, drop down layout , and array.
        val arrayAdapter = ArrayAdapter(this, R.layout.dropdown_item, languages)
        // get reference to the autocomplete text view
        val autocompleteTV = findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView)

        // set adapter to the autocomplete tv to the arrayAdapter


        budget = intent.getSerializableExtra("Budget") as BudgetModel

        findViewById<EditText>(R.id.budget_name_Input).setText(budget.title)
        findViewById<EditText>(R.id.budget_amount_Input).setText(budget.amount.toString())
        findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView).setText(budget.month.toString())

        autocompleteTV.setAdapter(arrayAdapter)

        findViewById<EditText>(R.id.budget_name_Input).addTextChangedListener {
            if(it!!.count() > 0)
                findViewById<TextInputLayout>(R.id.add_budget_name).error = null
        }

        findViewById<EditText>(R.id.budget_amount_Input).addTextChangedListener {
            if(it!!.count() > 0)

                findViewById<TextInputLayout>(R.id.add_budget_amount).error = null
        }

        var update = findViewById<Button>(R.id.updateBtn)

        update.setOnClickListener {
            val title = findViewById<EditText>(R.id.budget_name_Input).text.toString()
            val amount = findViewById<EditText>(R.id.budget_amount_Input).text.toString().toDoubleOrNull()
            val month = autocompleteTV.text.toString()
            val spent = 0.00.toString().toDoubleOrNull()
            print(month + spent )

//
            if(title.isEmpty())
                findViewById<TextInputLayout>(R.id.add_budget_name).error = "Please enter a title"

            else if(amount == null)
                findViewById<TextInputLayout>(R.id.add_budget_amount).error = "Please enter amount"

            else {
                val budget  = BudgetModel(budget.id, title, amount, month)
                update(budget)
            }

        }

        findViewById<ImageButton>(R.id.closeBtn).setOnClickListener {
            finish()
        }

    }
    private fun update(budget: BudgetModel){
        val db = Room.databaseBuilder(this,
            BudgetDatabse::class.java,
            "budgets").build()

        GlobalScope.launch {
            db.budgetdao().update(budget)
            finish()
        }
    }
}