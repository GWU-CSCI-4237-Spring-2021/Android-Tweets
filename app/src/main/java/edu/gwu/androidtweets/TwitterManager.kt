package edu.gwu.androidtweets

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject

class TwitterManager {

    // The OkHttpClient will facilitate the complexities of networking
    // with Twitter's servers
    val okHttpClient: OkHttpClient

    // An init block allows us to do extra logic during class initialization
    init {
        val builder = OkHttpClient.Builder()

        // Set our networking client up to log all requests & responses
        // to console
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        builder.addInterceptor(logging)

        okHttpClient = builder.build()
    }

    fun retrieveTweets(lat: Double, lon: Double): List<Tweet> {
        val query = "Android"
        val radius = "30mi"

        // Constructs a request to look for "Android" Tweets around the provided lat / lng
        // Note: The "Authorization" header is specific to Twitter. It is part of "OAuth" and will be covered in Lecture 7.
        val request = Request.Builder()
            .get()
            .url("https://api.twitter.com/1.1/search/tweets.json?q=$query&geocode=$lat,$lon,$radius")
            .header("Authorization", "Bearer AAAAAAAAAAAAAAAAAAAAAJ6N8QAAAAAABppHnTpssd0Hrsdpsi6vYN%2BTfks%3DFY1iVemJdKF5HWRZhQnHRbGpwXJevg3sYyvYC3R53sHCfOJvFk")
            .build()

        // "Execute" the request (.execute will block the current thread until the server replies with a response)
        val response: Response = okHttpClient.newCall(request).execute()

        // Create an empty, mutable list to hold up the Tweets we will parse from the JSON
        val tweets = mutableListOf<Tweet>()

        // Get the JSON body String from the response (if it exists)
        val responseBody: String? = response.body?.string()

        // If the response was successful (e.g. status code was a 200) AND the server sent us back
        // some JSON (which will contain the Tweets), then we can go ahead and parse the JSON body.
        if (response.isSuccessful && !responseBody.isNullOrBlank()) {
            // Set up for parsing the JSON response from the root element
            val json = JSONObject(responseBody)

            // The list of Tweets will be within the statuses array, per Twitter's docs
            val statuses = json.getJSONArray("statuses")

            // Loop thru the statuses array and parse each individual list, adding it to our `tweets`
            // list which we will return at the end.
            for (i in 0 until statuses.length()) {
                val curr = statuses.getJSONObject(i)
                val text = curr.getString("text")
                val user = curr.getJSONObject("user")
                val name = user.getString("name")
                val handle = user.getString("screen_name")
                val profilePictureUrl = user.getString("profile_image_url")

                val tweet = Tweet(
                    username = name,
                    handle = handle,
                    iconUrl = profilePictureUrl,
                    content = text
                )

                tweets.add(tweet)
            }
        } else {
            // Response failed (maybe the server is down)
            // We could throw an Exception here for the Activity, or update the function to return an error-type,
            // but for now, it'll just return an empty list
        }

        return tweets
    }

}