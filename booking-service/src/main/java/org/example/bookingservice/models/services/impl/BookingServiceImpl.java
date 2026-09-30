package org.example.bookingservice.models.services.impl;

import lombok.RequiredArgsConstructor;
import org.example.bookingservice.exceptions.DuplicateMovieException;
import org.example.bookingservice.exceptions.MovieNotFoundException;
import org.example.bookingservice.models.constants.BookingStatus;
import org.example.bookingservice.models.dto.requests.CreateBookingDetailRequest;
import org.example.bookingservice.models.dto.requests.CreateBookingRequest;
import org.example.bookingservice.models.dto.responses.BookingDetailResponse;
import org.example.bookingservice.models.dto.responses.BookingResponse;
import org.example.bookingservice.models.dto.responses.MovieResponse;
import org.example.bookingservice.models.entities.Booking;
import org.example.bookingservice.models.entities.BookingDetail;
import org.example.bookingservice.models.repositories.BookingDetailRepository;
import org.example.bookingservice.models.repositories.BookingRepository;
import org.example.bookingservice.models.services.BookingService;
import org.jspecify.annotations.NonNull;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.print.Book;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

        private final BookingRepository bookingRepository;
        private final BookingDetailRepository bookingDetailRepository;
        private final MovieGatewayService movieGatewayService;
        private final KafkaProducerService kafkaProducerService;
        private static final String TOPIC = "booking-created";


        @Override
        @Transactional
        public BookingResponse createBooking(CreateBookingRequest request) {

                Set<Long> movieIds = new HashSet<>();
                for( CreateBookingDetailRequest items: request.items()){
                        if(!movieIds.add(items.movieId())){
                                throw new DuplicateMovieException(items.movieId());
                        }
                }

                Booking booking = new Booking();
                booking.setCustomerName(request.customerName());
                booking.setCustomerEmail(request.customerEmail());
                booking.setStatus(BookingStatus.SUCCESS);
                booking.setTotal(0.0);

                booking = bookingRepository.save(booking);

                double total = 0.0;

                List<BookingDetail> details = new ArrayList<>();
                List<BookingDetailResponse> responses = new ArrayList<>();

                for(CreateBookingDetailRequest item: request.items()){
                        MovieResponse movieResponse = movieGatewayService.getMovieById(item.movieId());

                        BookingDetail detail = new BookingDetail();
                        detail.setBooking(booking);
                        detail.setMovieId(movieResponse.id());
                        detail.setQuantity(item.quantity());
                        details.add(detail);

                        double itemTotal = movieResponse.ticketPrice() * item.quantity();

                        total += itemTotal;


                }
                details = bookingDetailRepository.saveAll(details);

                for (int i = 0; i < details.size(); i++){
                        BookingDetailResponse old = responses.get(i);

                        responses.set(i, new BookingDetailResponse(
                                details.get(i).getId(),
                                old.movieId(),
                                old.movieTitle(),
                                old.quantity(),
                                old.unitPrice(),
                                old.subtotal()
                        ));
                }

                booking.setTotal(total);
                bookingRepository.save(booking);

                kafkaProducerService.sendMessage(request.customerEmail());

                return new BookingResponse(
                        booking.getId(),
                        booking.getCustomerName(),
                        booking.getCustomerEmail(),
                        booking.getTotal(),
                        booking.getStatus(),
                        responses
                );
        }
}
