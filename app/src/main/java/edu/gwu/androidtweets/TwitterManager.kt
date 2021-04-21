package edu.gwu.androidtweets

import android.util.Base64
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.net.URLEncoder

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

    /**
     * Twitter requires us to encode our API Key and API Secret in a special way for the request.
     *      API key = like a client identifier for our app
     *      API secret = the "secret password" that goes along with our API key (similar to username + password login)
     *
     * Step 1 for application-only OAuth from:
     * https://developer.twitter.com/en/docs/basics/authentication/oauth-2-0/application-only
     */
    fun encodeSecrets(
        apiKey: String,
        apiSecret: String
    ): String {
        // Encoding for a URL -- converts things like spaces into %20
        val encodedKey = URLEncoder.encode(apiKey, "UTF-8")
        val encodedSecret = URLEncoder.encode(apiSecret, "UTF-8")

        // Concatenate both values together with a colon in-between
        val combinedKeySecret = "$encodedKey:$encodedSecret"

        // Base-64 encode the combined string - server expects to have the credentials
        // in the agreed-upon format (generally used for transmitting binary data)
        // https://en.wikipedia.org/wiki/Base64
        // https://www.youtube.com/watch?v=8qkxeZmKmOY
        val base64Combined = Base64.encodeToString(
            combinedKeySecret.toByteArray(),
            Base64.NO_WRAP
        )

        return base64Combined
    }

    /**
     * All of Twitter's APIs are also protected by OAuth, so we need to get a token before calling
     * any other API.
     */
    fun retrieveOAuthToken(
        apiKey: String,
        apiSecret: String
    ): String {
        // Twitter requires us to encode our API Key and API Secret in a special way for the request headers.
        val encodedSecrets = encodeSecrets(apiKey, apiSecret)

        // Step 2 for application-only OAuth from:
        // https://developer.twitter.com/en/docs/authentication/oauth-2-0/application-only
        //
        // OAuth is defined to be a POST call, which has a specific body / payload to let the server
        // know we are doing "application-only" OAuth (e.g. we will only access public information)
        val requestBody = "grant_type=client_credentials"
            .toRequestBody(
                contentType = "application/x-www-form-urlencoded".toMediaType()
            )

        // The encoded secrets become a header on the request
        val request = Request.Builder()
            .url("https://api.twitter.com/oauth2/token")
            .header("Authorization", "Basic $encodedSecrets")
            .post(requestBody)
            .build()

        // "Execute" the request (.execute will block the current thread until the server replies with a response)
        val response: Response = okHttpClient.newCall(request).execute()

        // Get the JSON body from the response (if it exists)
        val responseString: String? = response.body?.string()

        // If the response was successful (e.g. status code was a 200) AND the server sent us back
        // some JSON (which will contain the OAuth token), then we can go ahead and parse the JSON body.
        if (response.isSuccessful && !responseString.isNullOrBlank()) {
            val json = JSONObject(responseString)

            // Pull out the OAuth token
            return json.getString("access_token")
        } else {
            // Response failed (maybe the server is down)
            // We could throw an Exception here for the Activity, or update the function to return an error-type,
            // but for now, it'll just return an empty string
        }

        return ""
    }

    fun retrieveTweets(oAuthToken: String, lat: Double, lon: Double): List<Tweet> {
        val query = "Android"
        val radius = "30mi"

        // Constructs a request to look for "Android" Tweets around the provided lat / lng
        // Note: The "Authorization" header is specific to Twitter. It is part of "OAuth" and will be covered in Lecture 7.
        val request = Request.Builder()
            .get()
            .url("https://api.twitter.com/1.1/search/tweets.json?q=$query&geocode=$lat,$lon,$radius")
            .header("Authorization", "Bearer $oAuthToken")
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
            val parsedTweets: List<Tweet> = SearchTweetsJsonParser().parseJson(responseBody)
            tweets.addAll(parsedTweets)
        } else {
            // Response failed (maybe the server is down)
            // We could throw an Exception here for the Activity, or update the function to return an error-type,
            // but for now, it'll just return an empty list
        }

        return tweets
    }

}