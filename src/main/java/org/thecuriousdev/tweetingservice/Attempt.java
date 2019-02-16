package org.thecuriousdev.tweetingservice;

import java.util.Optional;

public class Attempt<T> {

  private T result;
  private Exception exception;

  private Attempt(T result, Exception e) {
    this.result = result;
    this.exception = e;
  }

  public static <T> Attempt<T> of(final RuntimeExceptionWrappable<T> supplier) {
    try {
      return new Attempt(supplier.execute(), null);
    } catch (Exception e) {
      return new Attempt(null, e);
    }
  }

  public Optional<T> getResult() {
    return Optional.ofNullable(result);
  }

  public Optional<Exception> getException() {
    return Optional.ofNullable(exception);
  }

  @FunctionalInterface
  interface RuntimeExceptionWrappable<T> {
    T execute() throws Exception;
  }

}
