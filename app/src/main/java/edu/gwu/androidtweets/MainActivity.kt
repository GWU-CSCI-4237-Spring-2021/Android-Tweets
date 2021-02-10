package edu.gwu.androidtweets

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar


class MainActivity : AppCompatActivity() {

    // For lateinit var, see: https://docs.google.com/presentation/d/1hDnryyUH7aKIM7QsImi485a6Q0wwBwwfuiiIDM8A7ok/edit#slide=id.g615c45607e_0_156
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var login: Button
    private lateinit var progressBar: ProgressBar

    // onCreate is called the first time the Activity is to be shown to the user, so it a good spot
    // to put initialization logic.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Equivalent of a System.out.println (Android has different logging levels to organize logs -- .d is for DEBUG)
        // First parameter = the "tag" allows you to find related logging statements easier (e.g. all logs in the MainActivity)
        // Second parameter = the actual thing you want to log
        Log.d("MainActivity", "onCreate() has been called!")

        // Tells Android which layout file should be used for this screen.
        setContentView(R.layout.activity_main)

        // The IDs we are using here should match what was set in the "id" field for our views
        // in our XML layout (which was specified by setContentView).
        // Android will "search" the UI for the elements with the matching IDs to bind to our variables.
        username = findViewById(R.id.username)
        password = findViewById(R.id.password)
        login = findViewById(R.id.login)
        progressBar = findViewById(R.id.progressBar)

        // Kotlin shorthand for login.setEnabled(false).
        // If the getter / setter is unambiguous, Kotlin lets you use the property-style syntax
        login.isEnabled = false

        // Using a lambda to implement a View.OnClickListener interface. We can do this because
        // an OnClickListener is an interface that only requires *one* function.
        login.setOnClickListener { v: View ->
            // An Intent is used to start a new Activity
            // 1st param == a "Context" which is a reference point into the Android system. All Activities are Contexts by inheritance.
            // 2nd param == the Class-type of the Activity you want to navigate to.
            // An Intent can also be used like a Map (key-value pairs) to pass data between Activities.
            val intent = Intent(this, MapsActivity::class.java)
            // intent.putExtra("LOCATION", "Washington, D.C.")
            startActivity(intent)
        }

        // Using the same TextWatcher instance for both EditTexts so the same block of code runs on each character.
        username.addTextChangedListener(textWatcher)
        password.addTextChangedListener(textWatcher)
    }

    // Another example of explicitly implementing an interface (TextWatcher). We cannot use
    // a lambda in this case since there are multiple functions we need to implement.
    //
    // We're defining an "anonymous class" here using the `object` keyword (basically creating
    // a new, dedicated object to implement a TextWatcher for this variable assignment).
    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // Kotlin shorthand for username.getText().toString()
            // .toString() is needed because getText() returns an Editable (basically a char array).
            val inputtedUsername: String = username.text.toString()
            val inputtedPassword: String = password.text.toString()
            val enableButton: Boolean = inputtedUsername.isNotEmpty() && inputtedPassword.isNotEmpty()

            // Kotlin shorthand for login.setEnabled(enableButton)
            login.isEnabled = enableButton
        }
    }
}