package org.edts.service;

import org.edts.dto.BookingRequest;
import org.edts.dto.ConcertRequest;
import org.edts.dto.ConcertResponse;
import org.edts.entity.Booking;
import org.edts.entity.Concert;

import java.util.List;

public interface ConcertService {

     Booking bookTickets(BookingRequest request);
     List<ConcertResponse> getConcerts(ConcertRequest request);

}
