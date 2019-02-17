package org.thecuriousdev.tweetingservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Slf4j
public class AttemptTest {

  @Test
  public void givenSupplierThatThrowsCheckedException() {
    final Attempt<String> attempt = Attempt.of(() -> doSomething(-1));

    assertTrue(attempt.getException().isPresent());
    assertFalse(attempt.getResult().isPresent());
  }

  @Test
  public void givenSupplierThatExecutesSuccessfully() {
    final Attempt<String> attempt = Attempt.of(() -> doSomething(0));

    assertFalse(attempt.getException().isPresent());
    assertTrue(attempt.getResult().isPresent());
  }

  @Test
  public void givenStreamExpectAllItemsProcessed() {
    int expectedFailures = 5;
    int expectedSuccess = 10;
    final List<Attempt<String>> attempts = IntStream.range(0 - expectedFailures, expectedSuccess)
        .mapToObj(i -> Attempt.of(() -> doSomething(i)))
        .collect(Collectors.toList());

    final Map<Boolean, List<Attempt<String>>> results = attempts.stream()
        .collect(Collectors.groupingBy(attempt -> attempt.getResult().isPresent()));

    assertEquals(expectedSuccess + expectedFailures, attempts.size());
    assertEquals(expectedSuccess, results.get(Boolean.TRUE).size());
    assertEquals(expectedFailures, results.get(Boolean.FALSE).size());
  }

  public String doSomething(int i) throws Exception {
    if (i < 0) {
      throw new Exception();
    } else {
      return "TEST: " + i;
    }
  }

}
