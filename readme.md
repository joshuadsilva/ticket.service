# Ticket Service

## Assumptions
- No physical storage, REST API or front end GUI has been implemented
- All seat assignments/reservations are held in memory, but should be pushed into a  cache to allow services to scale out
- Distributed mutex for concurrency using a cache has not been implemented, `TODO:` markers indicate where it should be implemented
- Seat map defaults to 9 rows with 33 seats per row and a 10s hold time out unless instantiated using constructor below:
```java
public TicketServiceImpl(int numberOfRows, int seatsPerRow, int holdTimeout) {
	this.service = new SeatingService(numberOfRows, seatsPerRow, holdTimeout);
}
```

The order of ticket block assignments is as follows:
- All seats in a single row as close to the stage/front as possible

## Instructions to build
To build the solution use the command `mvn compile`

## Instructions to execute tests
To run all tests use the command `mvn test`.

### Logged output
If the `debug` flag in `com.dsilvaj.walmart.ticket.service.SeatingServce` is on additional information will be logged to stdout

#### 1. Log format for seat holds
seat holds will be logged as show below, (R0:4: row 0, seat 4)
```
findSeats:3 email:johndoe@foo.com
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
              MAP            row availableSeatBlocks
1 1 1 1 . . . 6 6 0 0 0 0 . . R0 4-6 13-14
5 5 5 5 5 5 5 5 5 5 5 5 . . . R1 12-14
. . . . . . . . . . . . . . . R2 0-14
. . . . . . . . . . . . . . . R3 0-14
. . . . . . . . . . . . . . . R4 0-14
```
