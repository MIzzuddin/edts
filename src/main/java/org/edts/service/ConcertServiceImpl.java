package org.edts.service;

import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import org.edts.dto.BookingRequest;
import org.edts.dto.ConcertRequest;
import org.edts.dto.ConcertResponse;
import org.edts.entity.Booking;
import org.edts.entity.Concert;
import org.edts.entity.Ticket;
import org.edts.repository.BookingRepository;
import org.edts.repository.ConcertRepository;
import org.edts.repository.TicketRepository;
import org.edts.utils.BookingRateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConcertServiceImpl implements ConcertService {
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private ConcertRepository concertRepository;
    @Autowired
    private BookingRateLimiter rateLimiter;

    @Transactional
    public Booking bookTickets(BookingRequest request) {
        Ticket ticket = ticketRepository.findById(request.getTicketId()).orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (!rateLimiter.allowBooking(ticket.getConcert().getId(), request.getQuantity())) {
            throw new RuntimeException("Booking limit exceeded");
        }

        if (ticket.getRemainingTickets() < request.getQuantity()) {
            throw new RuntimeException("Not enough tickets available");
        }

        int attempt = 0;
        while (attempt < 3) {
            try {
                ticket.setRemainingTickets(ticket.getRemainingTickets() - request.getQuantity());
                ticketRepository.save(ticket);
                attempt = 3;
            } catch (OptimisticLockException e) {
                attempt++;
                ticket = ticketRepository.findById(request.getTicketId()).orElseThrow(() -> new RuntimeException("Ticket not found"));
                if (attempt == 3) {
                    throw new RuntimeException("Failed to book tickets after multiple attempts");
                }
            }
        }

        Booking booking = new Booking();
        booking.setUserId(request.getUserId());
        booking.setQuantity(request.getQuantity());
        booking.setTicket(ticket);
        booking.setConcert(ticket.getConcert());

        return bookingRepository.save(booking);
    }

    public List<ConcertResponse> getConcerts(ConcertRequest request) {
        List<Ticket> tickets = ticketRepository.searchTickets(request.getLocation(), request.getName());
        return tickets.stream().map(t->{
            Concert concert = t.getConcert();
            ConcertResponse response = new ConcertResponse();
            response.setName(concert.getName());
            response.setLocation(concert.getLocation());
            response.setTime(concert.getTime());
            response.setRemainingTickets(t.getRemainingTickets());
            response.setTicketId(t.getId());
            return response;
        }).distinct().toList();
    }

}
