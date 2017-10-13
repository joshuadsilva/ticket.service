# Ticket Service
A simple ticket service that facilitates the discovery, temporary hold, and final reservation of seats within a high-demand performance venue. The service offers 3 functions via the `TicketService` interface:

- Find the number of seats available within the venue
Note: available seats are seats that are neither held nor reserved.

- Find and hold the best available seats on behalf of a customer
Note: each ticket hold should expire within a set number of seconds.

- Reserve and commit a specific group of held seats for a customer

## Requirements
The project requires Java 1.8 and Maven to be installed.

## Assumptions
- No physical storage, REST API or front end GUI has been implemented
- All seat assignments/reservations are held in memory for now, but should be pushed into a cache backed by a persistent store to allow services to scale out
- Seat map defaults to 9 rows with 33 seats per row and a 60s hold time out unless instantiated using the constructor below:
```java
public TicketServiceImpl(int numberOfRows, int seatsPerRow, int holdTimeout)
```

The order of ticket block assignments is as follows:
- All seats in a single row as close to the stage/front as possible
- If the entire row is open, seats will be held as close to the center for best viewing angle, else the seats will be held in the first available open block from the left edge when facing the stage
- seats in blocks that have the highest contiguity (overlap/grouping) factor and span multiple rows

## Instructions to build
To build the solution use the command `mvn compile`

## Instructions to execute tests
To run all tests use the command `mvn test`.

### Logged output
If the `debug` flag in `com.dsilvaj.ticket.service.SeatingServce` is on additional information will be logged to stdout

#### 1. Log format for seat holds
seat holds will be logged as show below, (R0:4: row 0, seat 4)
```
findSeats:3 email:johndoe@foo.com seatHoldId:7
seatHoldId:7 heldSeats:3 seats:R0:4, R0:5, R0:6,
```

#### 2. Log format for seat reservations
seat reservations will be logged as show below:
```
reserveSeats seatHoldId:7
seatHoldId:7 reservedSeats:3 seats:R0:4, R0:5, R0:6,
```
#### 3. Log format for seat maps
seat maps will be logged as shown below when holds or reservations are made where:

- 0 - held seats that have not yet expired
- N - seat held or reserved under seatHoldId N where N > 0
- "." - empty seat
- R(n) - Row number n
- N-M - available seat block starting at seat N and ending at seat M

```
                                                        Map  row availableSeatBlocks
----------------------------------------------------------- ---- -------------------
  1   1   1   1   1   .   .   .   3   3   3   3   3   .   .  R0  5-7 13-14
  0   0   0   0   0   0   0   .   .   .   .   .   .   .   .  R1  7-14
  .   .   .   .   .   .   .   .   .   .   .   .   .   .   .  R2  0-14
  .   .   .   .   .   .   .   .   .   .   .   .   .   .   .  R3  0-14
  .   .   .   .   .   .   .   .   .   .   .   .   .   .   .  R4  0-14
```
