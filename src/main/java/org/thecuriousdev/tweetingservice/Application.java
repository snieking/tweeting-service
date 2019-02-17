package org.thecuriousdev.tweetingservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.function.client.WebClient;
import org.thecuriousdev.tweetingservice.reddit.domain.ChildrenData;
import org.thecuriousdev.tweetingservice.reddit.domain.RedditResponse;
import org.thecuriousdev.tweetingservice.reddit.domain.RedditTopLevelData;
import reactor.core.publisher.Flux;

@Slf4j
@SpringBootApplication
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

}

