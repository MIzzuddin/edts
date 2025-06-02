# Database Design Document

## Overview

This document outlines the database structure for a concert ticket booking system.

---

## Tables and Fields

### 1. `concert`

| Field Name | Data Type | Constraints                  | Description                   |
| ---------- | --------- | ---------------------------- | ----------------------------- |
| id         | BIGINT    | PRIMARY KEY, AUTO\_INCREMENT | Unique concert ID             |
| name       | VARCHAR   | NOT NULL                     | Name of the concert           |
| time       | TIMESTAMP | NOT NULL                     | Scheduled time of the concert |
| location   | VARCHAR   | NOT NULL                     | Concert venue                 |

#### Relationships:

* `concert` is referenced by `ticket` and `booking` tables.
* One concert can have many tickets and bookings.

---

### 2. `ticket`

| Field Name         | Data Type | Constraints                         | Description                        |
| ------------------ | --------- | ----------------------------------- | ---------------------------------- |
| id                 | BIGINT    | PRIMARY KEY, AUTO\_INCREMENT        | Unique ticket ID                   |
| concert\_id        | BIGINT    | FOREIGN KEY → concert(id), NOT NULL | Concert this ticket belongs to     |
| start\_time        | TIMESTAMP | NOT NULL                            | When ticket becomes valid          |
| end\_time          | TIMESTAMP | NOT NULL                            | When ticket expires                |
| quantity           | INT       | NOT NULL                            | Total number of tickets issued     |
| remaining\_tickets | INT       | NOT NULL                            | Tickets left for sale              |
| version            | INT       | @Version (Optimistic Locking)       | Used for concurrent access control |

#### Relationships:

* Many tickets belong to one concert.
* Ticket is referenced by `booking`.

---

### 3. `booking`

| Field Name  | Data Type | Constraints                         | Description                             |
| ----------- | --------- | ----------------------------------- | --------------------------------------- |
| id          | BIGINT    | PRIMARY KEY, AUTO\_INCREMENT        | Unique booking ID                       |
| user\_id    | BIGINT    | NOT NULL                            | ID of the user who made the booking     |
| quantity    | INT       | NOT NULL                            | Number of tickets booked                |
| ticket\_id  | BIGINT    | FOREIGN KEY → ticket(id), NOT NULL  | Ticket booked                           |
| concert\_id | BIGINT    | FOREIGN KEY → concert(id), NOT NULL | Redundant reference for easier querying |

#### Relationships:

* Many bookings belong to one ticket.
* Booking also directly references concert (denormalized for convenience).

---

## Relationships Summary

| From Table | To Table  | Type        | Description                           |
| ---------- | --------- | ----------- | ------------------------------------- |
| `ticket`   | `concert` | Many-to-One | Many tickets belong to one concert    |
| `booking`  | `ticket`  | Many-to-One | A booking is made for one ticket type |
| `booking`  | `concert` | Many-to-One | A booking references the concert      |

---

## CREATE TABLE DDLs

### 1. `concert`

```sql
CREATE TABLE concert (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    time TIMESTAMP NOT NULL,
    location VARCHAR(255) NOT NULL
);
```

---

### 2. `ticket`

```sql
CREATE TABLE ticket (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    concert_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    quantity INT NOT NULL,
    remaining_tickets INT NOT NULL,
    version INT NOT NULL,
    CONSTRAINT fk_ticket_concert
        FOREIGN KEY (concert_id)
        REFERENCES concert(id));
```

---

### 3. `booking`

```sql
CREATE TABLE booking (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    ticket_id BIGINT NOT NULL,
    concert_id BIGINT NOT NULL,
    CONSTRAINT fk_booking_ticket
        FOREIGN KEY (ticket_id)
        REFERENCES ticket(id),
    CONSTRAINT fk_booking_concert
        FOREIGN KEY (concert_id)
        REFERENCES concert(id));
```

---

## Notes and Rationale

* **Redundant `concert_id` in Booking:** While the ticket already points to a concert, keeping `concert_id` in `booking` optimizes queries that filter by concert directly (e.g., bookings per concert).
* **Optimistic Locking (`@Version`) in Ticket:** Ensures safe concurrent updates, particularly for ticket reservation or sales.
* **No `User` entity here:** Assumes user information is stored in a separate service or database.

