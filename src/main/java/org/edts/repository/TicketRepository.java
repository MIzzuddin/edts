package org.edts.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.edts.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("SELECT t FROM Ticket t " +
            "JOIN Concert c ON t.concert.id = c.id " +
            "WHERE (:location IS NULL OR c.location LIKE %:location%) " +
            "AND (:name IS NULL OR c.name LIKE %:name%) " +
            "AND t.startTime <= CURRENT_TIMESTAMP " +
            "AND t.endTime >= CURRENT_TIMESTAMP " +
            "AND t.remainingTickets > 0")
    List<Ticket> searchTickets(@Param("location") String location,
                               @Param("name") String name);


}

