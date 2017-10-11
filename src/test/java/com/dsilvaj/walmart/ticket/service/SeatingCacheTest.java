package com.dsilvaj.walmart.ticket.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SeatingCacheTest {

	@Test
	public void testGetTotalNumberOfSeats() {
		Stadium s = new Stadium(9, 33, 4);
		int totalSeats = 297;
		assertEquals("Total Number of seats", totalSeats, s.getTotalNumberOfSeats());
		assertEquals("Total Number of held or reserved seats", 0, s.getNumberOfHeldOrReservedSeats());
		assertEquals("Total Number of available seats: no reservations or holds", totalSeats, s.getNumberOfAvailableSeats());
	}
	
	@Test
	public void testHoldFiveAllAvailable() throws Exception {
		String email = "foo@bar.com";
		Stadium service = new Stadium(5, 15, 4);
		SeatHold hold = service.findAndHoldSeats(4, email);
		service.reserveSeats(hold.getSeatHoldId(), email);
		hold = service.findAndHoldSeats(8, email);
		while (hold.hasNotExpired()) {
			
		}
		service.printMap();
		service.findAndHoldSeats(3, email);
		service.findAndHoldSeats(16, email);
		hold = service.findAndHoldSeats(12, email);
		service.reserveSeats(hold.getSeatHoldId(), email);
		hold = service.findAndHoldSeats(2, email);
		service.reserveSeats(hold.getSeatHoldId(), email);
		
		while (hold.hasNotExpired()) {
			
		}
		service.printMap();
		hold = service.findAndHoldSeats(3, email);
		service.reserveSeats(hold.getSeatHoldId(), email);
		
	}
	
	@Test(expected=RuntimeException.class)
	public void testNotEnoughSeatsAvailable() {
		Stadium cache = new Stadium(9, 30, 4);
		cache.findAndHoldSeats(1123, "foo@bar.com");
	}

	@Test(expected = RuntimeException.class)
	public void testReserveWithInvalidSeatHoldId() {
		Stadium cache = new Stadium(9, 30, 4);
		cache.reserveSeats(123, "foo@bar.com");
	}
}
