package com.dsilvaj.walmart.ticket.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.Ignore;
import org.junit.Test;

public class SeatingServiceTest {
	private static final String EMAIL_JOHN = "johndoe@foo.com";
	private static final String EMAIL_JANE = "janedoe@foo.com";

	@Test(expected=RuntimeException.class)
	public void testNotEnoughSeatsAvailable() {
		System.out.println("testNotEnoughSeatsAvailable");
		SeatingService service = new SeatingService(9, 30, 4);
		service.findAndHoldSeats(1123, EMAIL_JOHN);
	}

	@Test(expected = RuntimeException.class)
	public void testReserveWithInvalidSeatHoldId() {
		System.out.println("testReserveWithInvalidSeatHoldId");
		SeatingService service = new SeatingService(9, 30, 4);
		service.reserveSeats(123, EMAIL_JOHN);
	}
	
	@Test
	public void testGetTotalNumberOfSeats() {
		System.out.println("testGetTotalNumberOfSeats");
		SeatingService service = new SeatingService(9, 33, 4);
		int totalSeats = 297;
		assertEquals("Total Number of seats", totalSeats, service.getTotalNumberOfSeats());
		assertEquals("Total Number of held or reserved seats", 0, service.getNumberOfHeldOrReservedSeats());
		assertEquals("Total Number of available seats: no reservations or holds", totalSeats, service.getNumberOfAvailableSeats());
	}
	
	@Test
	public void testHoldReducesAvailableSeats() {
		System.out.println("testHoldReducesAvailableSeats");
		SeatingService service = new SeatingService(10, 20, 2);
		assertEquals("200 seats should be available", 200, service.getNumberOfAvailableSeats());
		SeatHold hold = service.findAndHoldSeats(10, EMAIL_JOHN);
		assertEquals("190 seats should be available", 190, service.getNumberOfAvailableSeats());
		assertEquals("10 seats should be held", 10, hold.getHeldSeats().size());
		while (hold.hasNotExpired()) {
			
		}
		assertEquals("200 seats should be available when hold expires", 200, service.getNumberOfAvailableSeats());
	}
	
	@Test
	public void testHoldAndReservation() throws Exception {
		System.out.println("testHoldAndReservation");
		SeatingService service = new SeatingService(5, 15, 2);
		
		// Hold and reserve 4 for john
		SeatHold hold = service.findAndHoldSeats(4, EMAIL_JOHN);
		assertEquals("4 seats are held for John", 4, hold.getHeldSeats().size());
		assertEquals("Johns email is on the hold", EMAIL_JOHN, hold.getCustomerEmail());
		assertTrue("Seats held in row 0", hold.getHeldSeats().stream().map(s -> s.getRowNumber()).collect(Collectors.toSet()).contains(0));
		assertTrue("Seats held are 0, 1, 2, 3", hold.getHeldSeats().stream().map(s -> s.getSeatNumber()).collect(Collectors.toSet()).containsAll(Arrays.asList(0, 1, 2, 3)));
		String reservationCode = service.reserveSeats(hold.getSeatHoldId(), EMAIL_JOHN);
		assertNotNull("Reserved 4 seats for John", reservationCode);
		
		// Hold 8 for Jane, wait for them to expire
		hold = service.findAndHoldSeats(8, EMAIL_JANE);
		assertEquals("8 seats are held for Jane", 8, hold.getHeldSeats().size());
		assertEquals("Janes email is on the hold", EMAIL_JANE, hold.getCustomerEmail());
		assertTrue("Seats held in row 0", hold.getHeldSeats().stream().map(s -> s.getRowNumber()).collect(Collectors.toSet()).contains(0));
		assertTrue("Seats held are 4, 5, 6, 7, 8, 9, 10, 11", hold.getHeldSeats().stream().map(s -> s.getSeatNumber()).collect(Collectors.toSet()).containsAll(Arrays.asList(4, 5, 6, 7, 8, 9, 10, 11)));
		while (hold.hasNotExpired()) {
			
		}
		//service.printMap();
		
		
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
	
	@Ignore
	@Test
	public void bigTest() {
		SeatingService service = new SeatingService(5, 15, 2);
		service.findAndHoldSeats(3, EMAIL_JOHN);
		service.findAndHoldSeats(4, EMAIL_JOHN);
		service.findAndHoldSeats(5, EMAIL_JOHN);
		service.findAndHoldSeats(6, EMAIL_JOHN);
		service.findAndHoldSeats(7, EMAIL_JOHN);
		service.findAndHoldSeats(8, EMAIL_JOHN);
		service.findAndHoldSeats(9, EMAIL_JOHN);
		service.findAndHoldSeats(10, EMAIL_JOHN);
		service.printMap();
		System.out.println(service.getNumberOfAvailableSeats());
	}
	
}
