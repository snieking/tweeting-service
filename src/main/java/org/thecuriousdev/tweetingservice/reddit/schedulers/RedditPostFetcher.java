package org.thecuriousdev.tweetingservice.reddit.schedulers;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.thecuriousdev.tweetingservice.reddit.domain.ChildrenData;
import org.thecuriousdev.tweetingservice.reddit.domain.PostData;
import org.thecuriousdev.tweetingservice.reddit.domain.RedditResponse;
import org.thecuriousdev.tweetingservice.reddit.domain.RedditTopLevelData;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@EnableScheduling
public class RedditPostFetcher {

  public static final int ONE_SECOND = 1000;
  public static final int ONE_MINUTE = 60;
  public static final int ONE_HOUR = 60;
  private final WebClient webClient;
  private final Cache<SubReddit, PostData> redditCache;

  public RedditPostFetcher() {
    webClient = WebClient.builder()
        .baseUrl("https://www.reddit.com/r")
        .build();
    redditCache = CacheBuilder.newBuilder()
        .expireAfterWrite(24, TimeUnit.HOURS)
        .build();
  }

  @Scheduled(fixedDelay = ONE_SECOND * ONE_MINUTE * ONE_HOUR)
  public void checkAwwSubReddit() {
    log.info("Scanning for new r/aww tweets");
    webClient.get()
        .uri("/aww/top/.json?count=1")
        .exchange()
        .doOnError(error -> log.warn("Error fetching response for aww subReddit", error))
        .flatMap(clientResponse -> clientResponse.bodyToMono(RedditResponse.class))
        .map(RedditResponse::getData)
        .map(RedditTopLevelData::getChildren)
        .flatMapMany(Flux::fromIterable)
        .map(ChildrenData::getData)
        .subscribe(postData -> cacheRedditResponse(postData, SubReddit.AWW));
  }

  public Optional<String> getAwwTweet() {
    return Optional.ofNullable(redditCache.getIfPresent(SubReddit.AWW))
        .map(this::buildTweet);
  }

  private synchronized void cacheRedditResponse(PostData postData, SubReddit subReddit) {
    try {
      final PostData oldPost = redditCache.get(subReddit, () -> postData);

      if (!oldPost.equals(postData) && postData.getScore() > oldPost.getScore()) {
        redditCache.put(subReddit, postData);
        log.info("New highest scored post for subReddit {} with {} points: {}", subReddit, postData.getScore(), postData);
      } else if (oldPost.equals(postData)) {
        oldPost.setScore(postData.getScore());
        log.info("Updated previous highest scored post for subReddit {} with {} to {} points",
            subReddit, oldPost.getScore(), postData.getScore());
      } else {
        log.info("New post with score [{}] wasn't higher than the prior top with score [{}]",
            postData.getScore(), oldPost.getScore());
      }
    } catch (ExecutionException e) {
      log.warn("Error caching reddit response for subReddit: [{}] and post: [{}]", subReddit, postData, e);
    }
  }

  private String buildTweet(PostData postData) {
    return new StringBuilder()
        .append(postData.getTitle())
        .append(" - ")
        .append(postData.getUrl())
        .append("\n\n")
        .append(postData.getPermalink())
        .toString();
  }

  enum SubReddit {
    AWW
  }

}
