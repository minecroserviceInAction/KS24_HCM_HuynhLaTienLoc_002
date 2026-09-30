package org.example.bookingservice.models.services.impl;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.example.bookingservice.clients.MovieClient;
import org.example.bookingservice.exceptions.MovieNotFoundException;
import org.example.bookingservice.exceptions.MovieServiceException;
import org.example.bookingservice.models.dto.responses.MovieResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MovieGatewayService {

    private final MovieClient movieClient;


    @CircuitBreaker(name = "movieService", fallbackMethod = "getMovieByFallback")
    public MovieResponse getMovieById(Long movieId) {
        try{
            return movieClient.getMovieById(movieId);
        }catch (FeignException.NotFound e){
            throw new MovieServiceException("Service not shutdown");
        }catch (FeignException e){
            throw new MovieNotFoundException(movieId);
        }
    }

    public MovieResponse getMovieByFallback(Long movieId,Throwable a){
        if(a instanceof MovieNotFoundException){
            throw (MovieNotFoundException) a;
        }
        throw new MovieServiceException("Movie service unavaiable");
    }

}
