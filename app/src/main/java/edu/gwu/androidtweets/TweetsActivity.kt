package edu.gwu.androidtweets

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.jetbrains.anko.doAsync

class TweetsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tweet)

        // Retrieve data from the Intent that launched this screen
        val intent = getIntent()
        val location: String = intent.getStringExtra("LOCATION")!!

        // Kotlin-shorthand for setTitle(...)
        title = "Tweets near $location"

        recyclerView = findViewById(R.id.recyclerView)

        val twitterManager = TwitterManager()

        doAsync {
            try {
                val tweets = twitterManager.retrieveTweets(37.7697583, -122.42079689999998)

                runOnUiThread {
                    val adapter = TweetsAdapter(tweets)
                    recyclerView.adapter = adapter
                    recyclerView.layoutManager = LinearLayoutManager(this@TweetsActivity) // Sets scrolling direction to vertical
                }
            } catch(exception: Exception) {
                Log.e("TweetsActivity", "Twitter API failed!", exception)
                runOnUiThread {
                    Toast.makeText(this@TweetsActivity, "Failed to retrieve Tweets!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}