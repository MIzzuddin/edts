# Concert Booking API

This API allows users to view available concerts and book tickets. It provides filtering options and basic ticket booking functionality.

Base URL: `/concerts`

---

## GET `/concerts`

### Description

Returns a list of available concerts. You can optionally filter the list by location and/or name.

### Query Parameters (optional)

| Parameter | Type   | Description                     |
| --------- | ------ |---------------------------------|
| location  | String | Filter concerts by location     |
| name      | String | Filter concerts by concert name |

If no query parameters are provided, all concerts will be returned.

### Example Request

```
GET /concerts?location={location name}&name={concert name}
```

### Example Response

```json
[
  {
    "ticketId": 101,
    "remainingTickets": 150,
    "name": "Summer Fest",
    "time": "2025-07-15T18:00:00Z",
    "location": "Jakarta"
  },
  {
    "ticketId": 102,
    "remainingTickets": 50,
    "name": "Jazz Night",
    "time": "2025-07-20T20:00:00Z",
    "location": "Jakarta"
  }
]
```

### Use Case

A user wants to see concerts in Jakarta that have "Fest" in the title. They use this endpoint to filter the available concerts before making a booking. TicketIDs and remaining tickets are included in the response to help users decide which concert to book.

---

## POST `/concerts/book`

### Description

Books one or more tickets for a specific concert on behalf of a user.

### Example Request Body

```json
{
  "userId": 1, // {user id}
  "ticketId": 101, // {concert ticket id}
  "quantity": 2 // {number of tickets to book}
}
```

| Field    | Type | Description                                                             |
| -------- | ---- |-------------------------------------------------------------------------|
| userId   | Long | The ID of the user making the booking                                   |
| ticketId | Long | The ID of the concert ticket to be booked, provided on list concert api |
| quantity | int  | Number of tickets to book                                               |

### Example Response

```json
{
  "id": 5001,
  "userId": 1,
  "ticketId": 101,
  "quantity": 2
}
```

### Use Case

After selecting a concert, a user books 2 tickets. This endpoint processes and confirms the booking, returning relevant booking details including the booking ID.

---

