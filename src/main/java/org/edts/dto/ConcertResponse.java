package org.edts.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConcertResponse {
    private Long ticketId;
    private int remainingTickets;
    private String name;
    private Timestamp time;
    private String location;
}
