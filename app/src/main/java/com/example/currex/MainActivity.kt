package com.example.currex

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

class MainActivity : AppCompatActivity() {
    private lateinit var amountInput: EditText
    private lateinit var fromCurrency: AutoCompleteTextView
    private lateinit var toCurrency: AutoCompleteTextView
    private lateinit var convertButton: Button
    private lateinit var resultText: TextView
    private lateinit var requestQueue: RequestQueue

    private val apikey = "d1d6ea42b49a47a098789dd64b4f69a4"
    private val apiurl = "https://api.currencyfreaks.com/latest?apikey=$apikey"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        amountInput = findViewById(R.id.amountInput)
        fromCurrency = findViewById(R.id.fromCurrency)
        toCurrency = findViewById(R.id.toCurrency)
        convertButton = findViewById(R.id.convertButton)
        resultText = findViewById(R.id.resultText)

        requestQueue = Volley.newRequestQueue(applicationContext)

        // Fetch currency codes and populate dropdowns
        fetchCurrencyCodes()

        convertButton.setOnClickListener {
            convertCurrency()
        }
    }

    private fun fetchCurrencyCodes() {
        val request = JsonObjectRequest(Request.Method.GET, apiurl, null,
            { response ->
                try {
                    if (!response.has("rates")) {
                        Toast.makeText(this, "Invalid API response", Toast.LENGTH_SHORT).show()
                        return@JsonObjectRequest
                    }

                    val rates = response.getJSONObject("rates")
                    val currencyList = ArrayList(rates.keys().asSequence().toList())

                    // Create separate adapters for each AutoCompleteTextView
                    val fromAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, currencyList)
                    val toAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, currencyList)

                    fromCurrency.setAdapter(fromAdapter)
                    toCurrency.setAdapter(toAdapter)

                    // Ensure dropdown appears when clicked
                    fromCurrency.setOnFocusChangeListener { _, hasFocus ->
                        if (hasFocus) fromCurrency.showDropDown()
                    }
                    fromCurrency.setOnClickListener { fromCurrency.showDropDown() }

                    toCurrency.setOnFocusChangeListener { _, hasFocus ->
                        if (hasFocus) toCurrency.showDropDown()
                    }
                    toCurrency.setOnClickListener { toCurrency.showDropDown() }

                    // Set selected item when clicked
                    fromCurrency.setOnItemClickListener { parent, _, position, _ ->
                        fromCurrency.setText(parent.getItemAtPosition(position).toString(), false)
                    }
                    toCurrency.setOnItemClickListener { parent, _, position, _ ->
                        toCurrency.setText(parent.getItemAtPosition(position).toString(), false)
                    }

                } catch (e: Exception) {
                    Toast.makeText(this, "Error fetching currencies", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "API request failed: ${error.message}", Toast.LENGTH_SHORT).show()
            })

        requestQueue.add(request)
    }

    @SuppressLint("SetTextI18n")
    private fun convertCurrency() {
        val amountStr = amountInput.text.toString().trim()
        if (amountStr.isEmpty()) {
            resultText.text = "Enter a valid amount"
            return
        }

        val from = fromCurrency.text.toString().uppercase()
        val to = toCurrency.text.toString().uppercase()
        if (from.isEmpty() || to.isEmpty()) {
            resultText.text = "Select currencies"
            return
        }

        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            resultText.text = "Enter a valid number"
            return
        }

        val request = JsonObjectRequest(Request.Method.GET, apiurl, null,
            { response ->
                try {
                    val rates = response.getJSONObject("rates")
                    if (!rates.has(from) || !rates.has(to)) {
                        resultText.text = "Invalid currency codes"
                        return@JsonObjectRequest
                    }

                    val fromRate = rates.getDouble(from)
                    val toRate = rates.getDouble(to)
                    val convertedAmount = (amount / fromRate) * toRate
                    resultText.text = "%.2f %s".format(convertedAmount, to)
                } catch (e: Exception) {
                    resultText.text = "Error in conversion"
                }
            },
            { error ->
                resultText.text = "API request failed: ${error.message}"
            })

        requestQueue.add(request)
    }
}
