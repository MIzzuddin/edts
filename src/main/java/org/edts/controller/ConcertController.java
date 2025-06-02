package org.edts.controller;

import jakarta.annotation.Nullable;
import org.edts.dto.BookingRequest;
import org.edts.dto.ConcertRequest;
import org.edts.dto.ConcertResponse;
import org.edts.entity.Booking;
import org.edts.service.ConcertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/concerts")
public class ConcertController {
    @Autowired
    private ConcertService concertService;

    @GetMapping
    public List<ConcertResponse> getConcerts(@ModelAttribute @Nullable ConcertRequest request) {
        return concertService.getConcerts(request);
    }

    @PostMapping("/book")
    public Booking booking(@RequestBody BookingRequest request) {
        return concertService.bookTickets(request);
    }
}

