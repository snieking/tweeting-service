package org.thecuriousdev.tweetingservice.reddit.domain;

import java.util.List;
import lombok.Data;

@Data
public class RedditTopLevelData {

  private int dest;
  private List<ChildrenData> children;

}
