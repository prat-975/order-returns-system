package com.orderrreturns.exception;

public class AlreadyReviewedException extends RuntimeException {

    public AlreadyReviewedException(String message) {
        super(message);
    }
}
