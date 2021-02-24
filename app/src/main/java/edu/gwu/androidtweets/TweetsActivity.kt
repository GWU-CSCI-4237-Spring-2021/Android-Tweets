package edu.gwu.androidtweets

import android.location.Address
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
        val address: Address = intent.getParcelableExtra<Address>("address")!!
        val city = address.locality ?: "Unknown"

        // Kotlin-shorthand for setTitle(...)
        // getString(...) reads from strings.xml and allows you to substitute in any formatting arguments
        title = getString(R.string.tweets_title, city)

        recyclerView = findViewById(R.id.recyclerView)

        val twitterManager = TwitterManager()
        val apiKey = getString(R.string.twitter_api_key)
        val apiSecret = getString(R.string.twitter_api_secret)

        doAsync {
            try {
                val oAuthToken = twitterManager.retrieveOAuthToken(apiKey, apiSecret)
                val tweets = twitterManager.retrieveTweets(oAuthToken, address.latitude, address.longitude)

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