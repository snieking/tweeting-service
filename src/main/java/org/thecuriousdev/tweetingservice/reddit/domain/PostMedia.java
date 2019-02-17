package org.thecuriousdev.tweetingservice.reddit.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PostMedia {

  @JsonProperty("reddit_video")
  private RedditVideo redditVideo;


}
