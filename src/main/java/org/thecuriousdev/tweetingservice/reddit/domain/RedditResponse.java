package org.thecuriousdev.tweetingservice.reddit.domain;

import lombok.Data;

@Data
public class RedditResponse {

  private String kind;
  private RedditTopLevelData data;

}
