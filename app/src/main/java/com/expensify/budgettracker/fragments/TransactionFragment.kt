package com.expensify.budgettracker.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.expensify.budgettracker.*
import com.expensify.budgettracker.R
import java.util.*
import kotlin.collections.ArrayList

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class TransactionFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var transactionRecyclerView: RecyclerView
    private lateinit var tvNoData: TextView
    private lateinit var noDataImage: ImageView
    private lateinit var tvNoDataTitle: TextView
    private lateinit var tvVisibilityNoData: TextView
    private lateinit var shimmerLoading: ShimmerFrameLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var transactionList: ArrayList<TransactionModel>
    private lateinit var exportButton: ImageButton
    private lateinit var dbRef: DatabaseReference
    private val user = Firebase.auth.currentUser
    private lateinit var typeOption: Spinner
    private lateinit var timeSpanOption: Spinner
    private var selectedType: String = "All Type"//default is all type
    private var selectedTimeSpan: String = "All Time" //default is all time
    var dateStart: Long = 0
    var dateEnd: Long = 0
    var amountExpense: Double = 0.0
    var amountIncome: Double = 0.0
    var allTimeExpense: Double = 0.0
    var allTimeIncome: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transaction, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeItems()

        showUserName()

        exportButtonClicked()
        getRangeDate(Calendar.DAY_OF_MONTH)


        visibilityOptions() //visibility option spinner

        //--Recycler View transaction items--
        transactionRecyclerView = view.findViewById(R.id.rvTransaction)
        transactionRecyclerView.layoutManager = LinearLayoutManager(this.activity)
        transactionRecyclerView.setHasFixedSize(true)

        transactionList = arrayListOf<TransactionModel>()

        getTransactionData()

        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh)
        swipeRefreshLayout.setOnRefreshListener { //call getTransaction() back to refresh the recyclerview
            getTransactionData()
            swipeRefreshLayout.isRefreshing = false
        }
        //----
    }

    private fun exportButtonClicked() {
        exportButton.setOnClickListener {
            val intent = Intent(this@TransactionFragment.activity, ExportData::class.java)
            startActivity(intent)
        }
    }

    private fun initializeItems() {
        tvNoData = requireView().findViewById(R.id.tvNoData)
        noDataImage = requireView().findViewById(R.id.noDataImage)
        tvNoDataTitle = requireView().findViewById(R.id.tvNoDataTitle)
        tvVisibilityNoData = requireView().findViewById(R.id.visibilityNoData)
        shimmerLoading = requireView().findViewById(R.id.shimmerFrameLayout)
        exportButton = requireView().findViewById(R.id.exportButton)
    }

    private fun showUserName() {
        user?.reload()
        val tvUserName: TextView = requireView().findViewById(R.id.userNameTV)
        val email = user!!.email
        val userName = user.displayName


        val name = if (userName == null || userName == ""){
            val splitValue = email?.split("@")
            splitValue?.get(0).toString()
        }else{
            userName
        }

        tvUserName.text = "Hi, ${name}!"
    }

    private fun visibilityOptions (){
        typeOption = requireView().findViewById(R.id.typeSpinner) as Spinner
        val typeList = arrayOf("All Type", "Expense", "Income")
        //typeOption.adapter = ArrayAdapter<String>(this.requireActivity(),android.R.layout.simple_list_item_1,options)
        val typeSpinnerAdapter = ArrayAdapter<String>(this.requireActivity(),R.layout.selected_spinner,typeList)
        typeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        typeOption.adapter = typeSpinnerAdapter

        timeSpanOption = requireView().findViewById(R.id.timeSpanSpinner) as Spinner
        val timeSpanList = arrayOf("All Time", "This Month", "This Week", "Today")
        val timeSpanAdapter = ArrayAdapter<String>(this.requireActivity(),R.layout.selected_spinner, timeSpanList)
        timeSpanAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        timeSpanOption.adapter = timeSpanAdapter

        typeOption.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                when(typeList[p2]){
                    "All Type" -> selectedType = "All Type"
                    "Expense" -> selectedType = "Expense"
                    "Income" -> selectedType = "Income"
                }
                getTransactionData()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        timeSpanOption.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                when(timeSpanList[p2]){
                    "All Time" -> selectedTimeSpan = "All Time"
                    "This Month" -> {
                        selectedTimeSpan = "This Month"
                        getRangeDate(Calendar.DAY_OF_MONTH)
                    }
                    "This Week" -> {
                        selectedTimeSpan = "This Week"
                        getRangeDate(Calendar.DAY_OF_WEEK)
                    }
                    "Today" -> {
                        selectedTimeSpan = "Today"
                        dateStart = System.currentTimeMillis()
                        dateEnd = System.currentTimeMillis()
                    }
                }
                getTransactionData()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

    private fun getRangeDate(rangeType: Int) {
        val currentDate = Date()
        val cal: Calendar = Calendar.getInstance(TimeZone.getDefault())
        cal.time = currentDate

        val startDay = cal.getActualMinimum(rangeType) //get the first date of the month
        cal.set(rangeType, startDay)
        val startDate = cal.time
        dateStart = startDate.time //convert to millis

        val endDay = cal.getActualMaximum(rangeType) //get the last date of the month
        cal.set(rangeType, endDay)
        val endDate = cal.time
        dateEnd= endDate.time //convert to millis

        fetchAmount(dateStart, dateEnd)

    }

    private fun fetchAmount(dateStart: Long, dateEnd: Long) { //show and calculate transaction recap
        var amountExpenseTemp = 0.0
        var amountIncomeTemp = 0.0

        val transactionList: ArrayList<TransactionModel> = arrayListOf<TransactionModel>()
        val uid = user?.uid //get user id from database
        if (uid != null) {
            dbRef = FirebaseDatabase.getInstance().getReference(uid)
        }
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
                        transactionList[i].date!! > dateStart-86400000 && //minus by 1 day
                        transactionList[i].date!! <= dateEnd){
                        amountExpenseTemp += transactionList[i].amount!!
                    }else if (transactionList[i].type == 2 &&
                        transactionList[i].date!! > dateStart-86400000 &&
                        transactionList[i].date!! <= dateEnd){
                        amountIncomeTemp += transactionList[i].amount!!
                    }
                }
                amountExpense= amountExpenseTemp
                amountIncome = amountIncomeTemp

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
                allTimeIncome = amountIncomeTemp
                showAllTimeRecap()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }
    private fun showAllTimeRecap() {
        //---show recap after calculation---
        val tvNetAmount: TextView = requireView().findViewById(R.id.netAmount)
        val tvAmountExpense: TextView = requireView().findViewById(R.id.expenseAmount)
        val tvAmountIncome: TextView = requireView().findViewById(R.id.incomeAmount)

        tvNetAmount.text = "${allTimeIncome-allTimeExpense}"
        tvAmountExpense.text = "$allTimeExpense"
        tvAmountIncome.text = "$allTimeIncome"
    }

    private fun getTransactionData() {
        shimmerLoading.startShimmerAnimation()
        shimmerLoading.visibility = View.VISIBLE
        tvVisibilityNoData.visibility = View.GONE
        transactionRecyclerView.visibility = View.GONE //hide the recycler view
        tvNoData.visibility = View.GONE
        noDataImage.visibility = View.GONE
        tvNoDataTitle.visibility = View.GONE

        val uid = user?.uid //get user id from database
        if (uid != null) {
            dbRef = FirebaseDatabase.getInstance().getReference(uid)
        }
        val query: Query = dbRef.orderByChild("invertedDate") //sorting date descending
        query.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                transactionList.clear()
                if (snapshot.exists()){
                    when (selectedType) {
                        "All Type" -> { //all option selected
                            for (transactionSnap in snapshot.children){
                                val transactionData = transactionSnap.getValue(TransactionModel::class.java) //reference data class
                                if (selectedTimeSpan == "All Time"){
                                    transactionList.add(transactionData!!)
                                }else{
                                    if (transactionData!!.date!! > dateStart-86400000 &&
                                        transactionData.date!!<= dateEnd){
                                        transactionList.add(transactionData)
                                    }
                                }
                            }
                        }
                        "Expense" -> { //expense option selected
                            for (transactionSnap in snapshot.children){
                                val transactionData = transactionSnap.getValue(TransactionModel::class.java) //reference data class
                                if (transactionData!!.type == 1){ //expense type
                                    if (selectedTimeSpan == "All Time"){
                                        transactionList.add(transactionData)
                                    }else{
                                        if (transactionData.date!! > dateStart-86400000 &&
                                            transactionData.date!! <= dateEnd){
                                            transactionList.add(transactionData)
                                        }
                                    }
                                }
                            }
                        }
                        "Income" -> {
                            for (transactionSnap in snapshot.children){
                                val transactionData = transactionSnap.getValue(TransactionModel::class.java) //reference data class
                                if (transactionData!!.type == 2){ //income type
                                    if (selectedTimeSpan == "All Time"){
                                        transactionList.add(transactionData)
                                    }else{
                                        if (transactionData.date!! > dateStart-86400000 &&
                                            transactionData.date!! <= dateEnd){
                                            transactionList.add(transactionData)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (transactionList.isEmpty()){ //if there is no data being displayed
                        noDataImage.visibility = View.VISIBLE
                        tvNoDataTitle.visibility = View.VISIBLE
                        tvVisibilityNoData.visibility = View.VISIBLE
                        tvVisibilityNoData.text = "There is no $selectedType data $selectedTimeSpan"
                    }else{
                        val mAdapter = TransactionAdapter(transactionList)
                        transactionRecyclerView.adapter = mAdapter

                        mAdapter.setOnItemClickListener(object: TransactionAdapter.onItemClickListener{ //item click listener and pass extra data
                            override fun onItemClick(position: Int) {
                                val intent = Intent(this@TransactionFragment.activity, TransactionDetails::class.java)

                                //put extras
                                intent.putExtra("transactionID", transactionList[position].transactionID)
                                intent.putExtra("type", transactionList[position].type)
                                intent.putExtra("title", transactionList[position].title)
                                intent.putExtra("category", transactionList[position].category)
                                intent.putExtra("amount", transactionList[position].amount)
                                intent.putExtra("date", transactionList[position].date)
                                intent.putExtra("note", transactionList[position].note)
                                startActivity(intent)
                            }
                        })
                        transactionRecyclerView.visibility = View.VISIBLE
                    }
                    shimmerLoading.stopShimmerAnimation()
                    shimmerLoading.visibility = View.GONE
                }else{ //if there is no data in database
                    shimmerLoading.stopShimmerAnimation()
                    shimmerLoading.visibility = View.GONE

                    noDataImage.visibility = View.VISIBLE
                    tvNoDataTitle.visibility = View.VISIBLE
                    tvNoData.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                print("Listener was cancelled")
            }

        })
    }

    override fun onResume() {
        super.onResume()

        getTransactionData()
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TransactionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

