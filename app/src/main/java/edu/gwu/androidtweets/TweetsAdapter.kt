package edu.gwu.androidtweets

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class TweetsAdapter(val tweets: List<Tweet>) : RecyclerView.Adapter<TweetsAdapter.ViewHolder>() {

    // How many rows (total) do you want the adapter to render?
    override fun getItemCount(): Int {
        return tweets.size
    }

    // The RecyclerView needs a new row - we need to tell it what XML file to use and create a ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // The RecyclerView needs a "fresh" / new row, so we need to:
        // 1. Read in the XML file for the row type
        // 2. Use the new row to build a ViewHolder to return

        // Step 1
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val itemView: View = layoutInflater.inflate(R.layout.row_tweet, parent, false)

        // Step 2
        return ViewHolder(itemView)
    }

    // The RecyclerView is ready to display a new (or recycled) row on the screen
    // for position indicated -- override the UI elements in the ViewHolder with the correct data
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentTweet = tweets[position]

        holder.username.text = currentTweet.username
        holder.handle.text = currentTweet.handle
        holder.content.text = currentTweet.content

        if (!currentTweet.iconUrl.isNullOrBlank()) {
            // Uncomment to see debug indicators (whether the icon was downloaded or cached)
//            Picasso
//                .get()
//                .setIndicatorsEnabled(true)

            Picasso
                .get()
                .load(currentTweet.iconUrl)
                .into(holder.icon)
        }
    }

    // A ViewHolder represents the Views that comprise a single row in our list (e.g.
    // our row to display a Tweet contains three TextViews and one ImageView).
    //
    // The "itemView" passed into the constructor comes from onCreateViewHolder because our LayoutInflater
    // ultimately returns a reference to the root View in the row's inflated layout. From there, we can
    // call findViewById to search from that root View downwards to find the Views we card about.
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val username: TextView = itemView.findViewById(R.id.username)
        val handle: TextView = itemView.findViewById(R.id.handle)
        val content: TextView = itemView.findViewById(R.id.tweet_content)
        val icon: ImageView = itemView.findViewById(R.id.icon)
    }
}