package org.example.bookingservice.exceptions;

public class DuplicateMovieException extends RuntimeException {
    public DuplicateMovieException(Long movieId) {
        super("Movie bi trung");
    }
}
