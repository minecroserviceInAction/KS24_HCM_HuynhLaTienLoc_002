package org.example.movieservice.exceptions;

public class DuplicateMovieException extends RuntimeException {
    public DuplicateMovieException(String message) {
        super("Movie Id bi trung!");
    }
}
