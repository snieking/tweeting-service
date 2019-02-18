package org.thecuriousdev.tweetingservice.reddit.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PostData {

  private String subreddit;
  private String title;
  private String url;
  private String permalink;
  private PostMedia media;
  private int score;
  @JsonProperty("is_video") private boolean isVideo;

}
