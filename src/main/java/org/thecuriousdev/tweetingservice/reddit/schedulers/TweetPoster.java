package org.thecuriousdev.tweetingservice.reddit.schedulers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

@Slf4j
@Service
@EnableScheduling
public class TweetPoster {

  private final RedditPostFetcher redditPostFetcher;

  @Value("${twitter.aww.oauth.consumerKey}")
  private String awwConsumerKey;

  @Value("${twitter.aww.oauth.consumerSecret}")
  private String awwConsumerSecret;

  @Value("${twitter.aww.oauth.accessToken}")
  private String awwAccessToken;

  @Value("${twitter.aww.oauth.accessTokenSecret}")
  private String awwAccesessTokenSecret;

  private Twitter twitter;

  @Autowired
  public TweetPoster(RedditPostFetcher redditPostFetcher) {
    this.redditPostFetcher = redditPostFetcher;
  }

  public void setup() {
    ConfigurationBuilder cb = new ConfigurationBuilder();
    cb.setDebugEnabled(true).setOAuthConsumerKey(awwConsumerKey)
        .setOAuthConsumerSecret(awwConsumerSecret)
        .setOAuthAccessToken(awwAccessToken)
        .setOAuthAccessTokenSecret(awwAccesessTokenSecret);
    TwitterFactory tf = new TwitterFactory(cb.build());
    twitter = tf.getInstance();
  }

  @Scheduled(fixedDelay = 1000 * 60 * 60)
  public void postDailyAww() {
    redditPostFetcher.getAwwTweet().ifPresent(tweet -> {
      StatusUpdate statusUpdate = new StatusUpdate(tweet);
      try {
        twitter.tweets().updateStatus(statusUpdate);
      } catch (TwitterException e) {
        log.warn("Failed to update status for r/aww: {}", statusUpdate);
      }
    });
  }

}
