package com.dsilvaj.walmart.ticket.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SeatingServiceTest {
	private static final String EMAIL_JOHN = "johndoe@foo.com";

	@Test(expected=RuntimeException.class)
	public void testNotEnoughSeatsAvailable() {
		SeatingService service = new SeatingService(9, 30, 4);
		service.findAndHoldSeats(1123, EMAIL_JOHN);
	}

	@Test(expected = RuntimeException.class)
	public void testReserveWithInvalidSeatHoldId() {
		SeatingService service = new SeatingService(9, 30, 4);
		service.reserveSeats(123, EMAIL_JOHN);
	}
	
	@Test
	public void testGetTotalNumberOfSeats() {
		SeatingService service = new SeatingService(9, 33, 4);
		int totalSeats = 297;
		assertEquals("Total Number of seats", totalSeats, service.getTotalNumberOfSeats());
		assertEquals("Total Number of held or reserved seats", 0, service.getNumberOfHeldOrReservedSeats());
		assertEquals("Total Number of available seats: no reservations or holds", totalSeats, service.getNumberOfAvailableSeats());
	}
	
	@Test
	public void testHoldReducesAvailableSeats() {
		SeatingService service = new SeatingService(10, 20, 5);
		SeatHold hold = service.findAndHoldSeats(10, EMAIL_JOHN);
		assertEquals("190 seats should be available", 190, service.getNumberOfAvailableSeats());
		assertEquals("10 seats should be held", 10, hold.getHeldSeats().size());
	}
	
	@Test
	public void testHoldFiveAllAvailable() throws Exception {
		SeatingService service = new SeatingService(5, 15, 4);
		SeatHold hold = service.findAndHoldSeats(4, EMAIL_JOHN);
		service.reserveSeats(hold.getSeatHoldId(), EMAIL_JOHN);
		hold = service.findAndHoldSeats(8, EMAIL_JOHN);
		while (hold.hasNotExpired()) {
			
		}
		service.printMap();
		service.findAndHoldSeats(3, EMAIL_JOHN);
		service.findAndHoldSeats(16, EMAIL_JOHN);
		hold = service.findAndHoldSeats(12, EMAIL_JOHN);
		service.reserveSeats(hold.getSeatHoldId(), EMAIL_JOHN);
		hold = service.findAndHoldSeats(2, EMAIL_JOHN);
		service.reserveSeats(hold.getSeatHoldId(), EMAIL_JOHN);
		
		while (hold.hasNotExpired()) {
			
		}
		service.printMap();
		hold = service.findAndHoldSeats(3, EMAIL_JOHN);
		service.reserveSeats(hold.getSeatHoldId(), EMAIL_JOHN);
		service.printReservations();
	}
	
}
