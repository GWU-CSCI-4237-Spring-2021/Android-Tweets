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
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.jetbrains.anko.doAsync
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

object UserInfo

object FetchFriendsListExample {

    val firebaseDb = FirebaseDatabase.getInstance()

    val userInfo = mutableListOf<UserInfo>()

    fun fetchUserInfoList(userIds: List<String>, onCompleteListener: (List<UserInfo>) -> Unit) {
        val reference = "users"
        val userIdsQueue: Queue<String> = LinkedList(userIds)

        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                // ...
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                // Parse out user data from snapshot
                userInfo.add(...)

                // If there are more users we need to fetch, continue getting user info
                val nextId = userIdsQueue.poll()
                if (nextId != null) {
                    firebaseDb.getReference("users/$nextId").addListenerForSingleValueEvent(this)
                } else {
                    // Collection is finished, invoke the original caller with the complete list
                    onCompleteListener(userInfo)
                }
            }
        }

        // addListenerForSingleValueEvent is the same as addValueEventListener, except it "unsubscribes"
        // from updates after the first data event
        // See: https://firebase.google.com/docs/database/android/read-and-write#read_data_once
        val firstId = userIdsQueue.poll()
        firebaseDb.getReference("users/$firstId").addListenerForSingleValueEvent(valueEventListener)


        // .get() gives you a "pending data Task" which you can block and wait on
        // See: https://firebase.google.com/docs/database/android/read-and-write#read_data_once
        val pendingData: Task<DataSnapshot> = firebaseDb.getReference("users/...").get()

        // Blocks the thread until you have a result
        // Now you need to handle all of the threading and error handling (try-catch) similar to what we've done with API calls
        // Also see: https://developers.google.com/android/guides/tasks#chaining
        val result: DataSnapshot = Tasks.await(pendingData)

    }

}

class TweetsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tweetContent: EditText
    private lateinit var addTweet: FloatingActionButton

    private lateinit var firebaseDatabase: FirebaseDatabase

    private val currentTweets: MutableList<Tweet> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tweet)

        firebaseDatabase = FirebaseDatabase.getInstance()

        // Retrieve data from the Intent that launched this screen
        val intent = getIntent()
        val address: Address = intent.getParcelableExtra<Address>("address")!!
        val city = address.locality ?: "Unknown"

        // Kotlin-shorthand for setTitle(...)
        // getString(...) reads from strings.xml and allows you to substitute in any formatting arguments
        title = getString(R.string.tweets_title, city)

        recyclerView = findViewById(R.id.recyclerView)
        tweetContent = findViewById(R.id.tweet_content)
        addTweet = findViewById(R.id.add_tweet)

        if (savedInstanceState != null) {
            // Activity has just been rotated and our state has been saved in the bundle
            currentTweets.addAll(savedInstanceState.getSerializable("tweets") as List<Tweet>)
            val adapter = TweetsAdapter(currentTweets)
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(this@TweetsActivity) // Sets scrolling direction to vertical
        } else {
            // First time Activity launch, retrieve data from Twitter / Firebase
            getTweetsFromTwitter(address)
            //getTweetsFromFirebase(address)
        }
    }

    private fun getTweetsFromFirebase(address: Address) {
        val email: String = FirebaseAuth.getInstance().currentUser!!.email!!
        val state: String = address.adminArea ?: "Unknown"

        val reference: DatabaseReference = firebaseDatabase.getReference("tweets/$state")

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

    private fun getTweetsFromTwitter(address: Address) {
        val twitterManager = TwitterManager()
        val apiKey = getString(R.string.twitter_api_key)
        val apiSecret = getString(R.string.twitter_api_secret)

        tweetContent.visibility = View.GONE
        addTweet.visibility = View.GONE

        doAsync {
            try {
                val oAuthToken = twitterManager.retrieveOAuthToken(apiKey, apiSecret)
                val tweets = twitterManager.retrieveTweets(oAuthToken, address.latitude, address.longitude)

                currentTweets.clear()
                currentTweets.addAll(tweets)

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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val serializableList = ArrayList(currentTweets)
        outState.putSerializable("tweets", serializableList)
    }
}