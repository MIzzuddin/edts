package org.edts.sevice;

import jakarta.persistence.OptimisticLockException;
import org.edts.dto.BookingRequest;
import org.edts.dto.ConcertRequest;
import org.edts.dto.ConcertResponse;
import org.edts.entity.Booking;
import org.edts.entity.Concert;
import org.edts.entity.Ticket;
import org.edts.repository.BookingRepository;
import org.edts.repository.ConcertRepository;
import org.edts.repository.TicketRepository;
import org.edts.service.ConcertServiceImpl;
import org.edts.utils.BookingRateLimiter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class ConcertServiceImplTest {

    @InjectMocks
    private ConcertServiceImpl concertService;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingRateLimiter rateLimiter;

    @Test
    void testBookTicketSuccess() {
        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setRemainingTickets(10);
        Concert concert = new Concert();
        concert.setId(100L);
        ticket.setConcert(concert);

        BookingRequest request = new BookingRequest();
        request.setTicketId(1L);
        request.setQuantity(2);
        request.setUserId(1L);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(rateLimiter.allowBooking(concert.getId(), 2)).thenReturn(true);
        when(ticketRepository.save(any())).thenReturn(ticket);
        when(bookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Booking booking = concertService.bookTickets(request);

        assertNotNull(booking);
        assertEquals(2, booking.getQuantity());
    }

    @Test
    void testBookRetryAndFail() {
        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setRemainingTickets(5);
        Concert concert = new Concert();
        concert.setId(300L);
        ticket.setConcert(concert);

        BookingRequest request = new BookingRequest();
        request.setTicketId(1L);
        request.setQuantity(1);
        request.setUserId(1L);

        when(rateLimiter.allowBooking(anyLong(), anyInt())).thenReturn(true);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any())).thenThrow(new OptimisticLockException());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> concertService.bookTickets(request));
        assertTrue(ex.getMessage().contains("Failed to book tickets"));
    }

    @Test
    void testBookRateLimiterFail() {
        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setRemainingTickets(5);
        Concert concert = new Concert();
        concert.setId(123L);
        ticket.setConcert(concert);

        BookingRequest request = new BookingRequest();
        request.setTicketId(1L);
        request.setQuantity(1);
        request.setUserId(1L);

        when(rateLimiter.allowBooking(anyLong(), anyInt())).thenReturn(false);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> concertService.bookTickets(request));
        assertTrue(ex.getMessage().contains("Booking limit exceeded"));
    }

    @Test
    void testBookNotEnoughTickets() {
        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setRemainingTickets(1);
        Concert concert = new Concert();
        concert.setId(200L);
        ticket.setConcert(concert);

        BookingRequest request = new BookingRequest();
        request.setTicketId(1L);
        request.setQuantity(5);
        request.setUserId(1L);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(rateLimiter.allowBooking(concert.getId(), 5)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> concertService.bookTickets(request));
        assertEquals("Not enough tickets available", ex.getMessage());
    }

    @Test
    void testGetConcerts() {
        Concert concert = new Concert();
        concert.setId(1L);
        concert.setName("Awesome Show");
        concert.setLocation("Jakarta");
        concert.setTime(new Timestamp(Instant.now().toEpochMilli()));

        Ticket ticket = new Ticket();
        ticket.setId(10L);
        ticket.setRemainingTickets(3);
        ticket.setConcert(concert);

        when(ticketRepository.searchTickets("Jakarta", "Awesome Show")).thenReturn(List.of(ticket));

        ConcertRequest request = new ConcertRequest();
        request.setLocation("Jakarta");
        request.setName("Awesome Show");

        List<ConcertResponse> responses = concertService.getConcerts(request);

        assertEquals(1, responses.size());
        ConcertResponse response = responses.getFirst();
        assertEquals("Awesome Show", response.getName());
        assertEquals("Jakarta", response.getLocation());
        assertEquals(3, response.getRemainingTickets());
        assertEquals(10L, response.getTicketId());
    }
}
