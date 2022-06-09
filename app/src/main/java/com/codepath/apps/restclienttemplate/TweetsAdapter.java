package com.codepath.apps.restclienttemplate;

import static com.facebook.stetho.inspector.network.ResponseHandlingInputStream.TAG;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import okhttp3.Headers;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {
    public static final String TAG = "sabiha";
    Context context;
    List<Tweet> tweets;

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public TweetsAdapter(Context context, List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tweet tweet = tweets.get(position);
        holder.bind(tweet);
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivProfileImage;
        TextView tvBody;
        TextView tvScreenName;
        ImageView ivTweetImage;
        TextView tvRelativeTime;
        ImageButton ibHeart;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            ivTweetImage = itemView.findViewById(R.id.ivTweetImage);
            tvRelativeTime = itemView.findViewById(R.id.tvRelativeTime);
            ibHeart = itemView.findViewById(R.id.ibHeart);
        }

        public void bind(Tweet tweet) {
            tvBody.setText(tweet.body);
            tvScreenName.setText(tweet.user.screenName);
            tvRelativeTime.setText(getRelativeTimeAgo(tweet.createdAt));
            TwitterClient client = TwitterApp.getRestClient(context);

            Glide.with(context).load(tweet.user.profileImageUrl).into(ivProfileImage);
            if (tweet.tweetImageUrl != null) {
                ivTweetImage.setVisibility(View.VISIBLE);
                Glide.with(context).load(tweet.tweetImageUrl).into(ivTweetImage);
            } else {
                ivTweetImage.setVisibility(View.GONE);
            }

            ibHeart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    client.likeTweet(tweet.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            ibHeart.setImageResource(R.drawable.ic_vector_heart);
                            Log.i(TAG, "onSuccess to like tweet");
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            client.unlikeTweet(tweet.id, new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Headers headers, JSON json) {
                                    ibHeart.setImageResource(R.drawable.ic_vector_heart_stroke);
                                    Log.i(TAG, "onSuccess to unlike tweet");
                                }

                                @Override
                                public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                    Log.i(TAG, "onFailure to unlike tweet");
                                }
                            });
                            Log.e(TAG, "onFailure to like tweet", throwable);
                        }
                    });
                }
            });
        }

    }

    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Tweet> list) {
        tweets.addAll(list);
        notifyDataSetChanged();
    }

    public String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        try {
            long time = sf.parse(rawJsonDate).getTime();
            long now = System.currentTimeMillis();

            final long diff = now - time;
            if (diff < MINUTE_MILLIS) {
                return "just now";
            } else if (diff < 2 * MINUTE_MILLIS) {
                return "a minute ago";
            } else if (diff < 50 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + " m";
            } else if (diff < 90 * MINUTE_MILLIS) {
                return "an hour ago";
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + " h";
            } else if (diff < 48 * HOUR_MILLIS) {
                return "yesterday";
            } else {
                return diff / DAY_MILLIS + " d";
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return "";
    }


}
