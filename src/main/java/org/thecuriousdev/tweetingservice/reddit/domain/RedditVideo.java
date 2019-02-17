package org.thecuriousdev.tweetingservice.reddit.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RedditVideo {

  @JsonProperty("fallback_url")
  private String fallbackUrl;

}
