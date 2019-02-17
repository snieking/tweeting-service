package org.thecuriousdev.tweetingservice.reddit.scheduler;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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
public class TweetScheduler {

  private final WebClient webClient;
  private final Cache<SubReddit, PostData> redditCache;

  public TweetScheduler() {
    webClient = WebClient.builder()
        .baseUrl("https://www.reddit.com/r")
        .build();
    redditCache = CacheBuilder.newBuilder()
        .expireAfterWrite(24, TimeUnit.HOURS)
        .build();
  }

  @Scheduled(fixedDelay = 600000)
  public void checkAwwSubReddit() {
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
      }
    } catch (ExecutionException e) {
      log.warn("Error caching reddit response for subReddit: [{}] and post: [{}]", subReddit, postData, e);
    }
  }

  enum SubReddit {
    AWW
  }

}
