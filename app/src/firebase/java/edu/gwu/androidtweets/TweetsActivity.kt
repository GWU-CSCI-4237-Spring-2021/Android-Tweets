package edu.gwu.androidtweets

import android.location.Address
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.jetbrains.anko.doAsync
import kotlin.random.Random

class TweetsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tweetContent: EditText
    private lateinit var addTweet: FloatingActionButton

    private lateinit var firebaseDatabase: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tweet)

        firebaseDatabase = FirebaseDatabase.getInstance()

        // Retrieve data from the Intent that launched this screen
        val intent = getIntent()
        val address: Address = intent.getParcelableExtra<Address>(MapsActivity.BUNDLE_KEY_ADDRESS)!!
        val city = address.locality ?: "Unknown"

        // Kotlin-shorthand for setTitle(...)
        // getString(...) reads from strings.xml and allows you to substitute in any formatting arguments
        title = getString(R.string.tweets_title, city)

        recyclerView = findViewById(R.id.recyclerView)
        tweetContent = findViewById(R.id.tweet_content)
        addTweet = findViewById(R.id.add_tweet)

        getTweetsFromFirebase(address)
    }

    private fun getTweetsFromFirebase(address: Address) {
        val email: String = FirebaseAuth.getInstance().currentUser!!.email!!
        val state: String = address.adminArea ?: "Unknown"

        val reference: DatabaseReference = firebaseDatabase.getReference("tweets/$state")

        val shouldProcessButton = featureTogglingManager.shouldButtonBeEnabled()
        if (shouldProcessButton) {
            addTweet.setOnClickListener {
                val content = tweetContent.text.toString()

                if (content.isNotEmpty()) {
                    val tweet = Tweet(
                        username = email,
                        handle = email,
                        content = content,
                        iconUrl = ""
                    )

                    val pushReference = reference.push()
                    pushReference.setValue(tweet)
                }
            }
        }

        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.e("TweetsActivity", "Failed to connect to Firebase!", error.toException())
                Toast.makeText(
                    this@TweetsActivity,
                    "Failed to retrieve Tweets from DB!",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val tweets = mutableListOf<Tweet>()
                snapshot.children.forEach { child: DataSnapshot ->
                    val tweet = child.getValue(Tweet::class.java)
                    if (tweet != null) {
                        tweets.add(tweet)
                    }
                }

                val adapter = TweetsAdapter(tweets)
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(this@TweetsActivity) // Sets scrolling direction to vertical
            }
        })
    }
}