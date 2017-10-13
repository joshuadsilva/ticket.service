package com.dsilvaj.walmart.ticket.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.OptionalInt;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;

import com.dsilvaj.walmart.ticket.domain.SeatHold;

public class TicketServiceTest {
	private static final int MAX_SEATS_RESERVABLE = 15;
	private static final String EMAIL_JOHN = "johndoe@foo.com";
	private static final String EMAIL_JANE = "janedoe@foo.com";
	private static final String EMAIL_BARB = "barbdoe@foo.com";
	private static final String EMAIL_CARL = "carldoe@foo.com";
	private static final String EMAIL_JACK = "jackdoe@foo.com";
	private TicketService service;

	@Test(expected = RuntimeException.class)
	public void testNotEnoughSeatsAvailable() {
		System.out.println("--> testNotEnoughSeatsAvailable");
		service = new TicketServiceImpl(9, 30, 4);
		service.findAndHoldSeats(1123, EMAIL_JOHN);
	}

	@Test(expected = RuntimeException.class)
	public void testReserveWithInvalidSeatHoldId() {
		System.out.println("--> testReserveWithInvalidSeatHoldId");
		service = new TicketServiceImpl(9, 30, 4);
		service.reserveSeats(123, EMAIL_JOHN);
	}

	@Test
	public void testHoldReducesAvailableSeats() {
		System.out.println("--> testHoldReducesAvailableSeats");
		service = new TicketServiceImpl(10, 20, 2);
		assertEquals("200 seats should be available", 200, service.numSeatsAvailable());
		SeatHold hold = service.findAndHoldSeats(10, EMAIL_JOHN);
		assertEquals("190 seats should be available", 190, service.numSeatsAvailable());
		assertEquals("10 seats should be held", 10, hold.getHeldSeats().size());
		while (hold.hasNotExpired()) {

		}
		assertEquals("200 seats should be available when hold expires", 200, service.numSeatsAvailable());
	}

	@Test
	public void testHoldAndReservation() throws Exception {
		System.out.println("--> testHoldAndReservation");
		service = new TicketServiceImpl(5, 15, 2);

		// Hold and reserve 4 for john
		SeatHold hold = holdAndReserve(4, EMAIL_JOHN);
		assertEquals("4 seats are held for John", 4, hold.getHeldSeats().size());
		assertEquals("Johns email is on the hold", EMAIL_JOHN, hold.getCustomerEmail());
		assertEquals("Seats held in row 0", OptionalInt.of(0), hold.getHeldSeats().stream().mapToInt(s -> s.getRowNumber()).findFirst());
		assertTrue("Seats held are 0, 1, 2, 3", hold.getHeldSeats().stream().map(s -> s.getSeatNumber()).collect(Collectors.toSet()).containsAll(Arrays.asList(0, 1, 2, 3)));
		assertNotNull("Reserved 4 seats for John", hold.getReservation().getReservationCode());

		// Hold 8 for Jane, wait for them to expire
		hold = hold(8, EMAIL_JANE);
		assertEquals("8 seats are held for Jane", 8, hold.getHeldSeats().size());
		assertEquals("Janes email is on the hold", EMAIL_JANE, hold.getCustomerEmail());
		assertEquals("Seats held in row 0", OptionalInt.of(0), hold.getHeldSeats().stream().mapToInt(s -> s.getRowNumber()).findFirst());
		assertTrue("Seats held are 4, 5, 6, 7, 8, 9, 10, 11", hold.getHeldSeats().stream().map(s -> s.getSeatNumber()).collect(Collectors.toSet()).containsAll(Arrays.asList(4, 5, 6, 7, 8, 9, 10, 11)));
		waitUntilExpiration(hold);

		hold = hold(3, EMAIL_BARB);
		assertEquals("3 seats are held for Jane", 3, hold.getHeldSeats().size());
		assertEquals("Barbs email is on the hold", EMAIL_BARB, hold.getCustomerEmail());
		assertEquals("Seats held in row 0", OptionalInt.of(0), hold.getHeldSeats().stream().mapToInt(s -> s.getRowNumber()).findFirst());
		assertTrue("Seats held are 4, 5, 6", hold.getHeldSeats().stream().map(s -> s.getSeatNumber()).collect(Collectors.toSet()).containsAll(Arrays.asList(4, 5, 6)));
		
		holdAndReserve(16, EMAIL_JOHN);
		holdAndReserve(12, EMAIL_JOHN);
		
		hold = holdAndReserve(2, EMAIL_CARL);
		assertEquals("2 seats are held for Carl", 2, hold.getHeldSeats().size());
		assertEquals("Carls email is on the hold", EMAIL_CARL, hold.getCustomerEmail());
		assertEquals("Seats held in row 0", OptionalInt.of(0), hold.getHeldSeats().stream().mapToInt(s -> s.getRowNumber()).findFirst());
		assertTrue("Seats held are 7, 8", hold.getHeldSeats().stream().map(s -> s.getSeatNumber()).collect(Collectors.toSet()).containsAll(Arrays.asList(7, 8)));
		
		while (hold.hasNotExpired()) {

		}
		hold = holdAndReserve(3, EMAIL_JACK);
		assertEquals("3 seats are held for Jack", 3, hold.getHeldSeats().size());
		assertEquals("Jacks email is on the hold", EMAIL_JACK, hold.getCustomerEmail());
		assertEquals("Seats held in row 0", OptionalInt.of(0), hold.getHeldSeats().stream().mapToInt(s -> s.getRowNumber()).findFirst());
		assertTrue("Seats held are 4, 5, 6", hold.getHeldSeats().stream().map(s -> s.getSeatNumber()).collect(Collectors.toSet()).containsAll(Arrays.asList(4, 5, 6)));
		
	}

	@Test
	public void bigTest() {
		System.out.println("--> bigTest");
		service = new TicketServiceImpl(5, 15, 2);
		assertEquals("75 seats in venue", 75, service.numSeatsAvailable());
		holdAndReserve(5, EMAIL_JOHN);
		SeatHold hold3 = hold(3, EMAIL_JOHN);
		holdAndReserve(5, EMAIL_JOHN);
		waitUntilExpiration(hold3);
		holdAndReserve(7, EMAIL_JOHN);
		holdAndReserve(8, EMAIL_JOHN);
		hold(6, EMAIL_JOHN);
		holdAndReserve(3, EMAIL_JOHN);
		holdAndReserve(10, EMAIL_JOHN);
		holdAndReserve(11, EMAIL_JOHN);
		holdAndReserve(12, EMAIL_JOHN);
	}
	
	@Test
	public void concurrencyTest() {
		System.out.println("--> concurrencyTest");
		Random r = new Random();
		service = new TicketServiceImpl(20, 10, 2);
		ExecutorService executorService = Executors.newFixedThreadPool(2);
		
		IntStream.range(0, 30).forEach(i -> {
			Callable<SeatHold> callableReserveTask = () -> {
				int nbr = r.nextInt(MAX_SEATS_RESERVABLE) + 1;
				return holdAndReserve(nbr, "foo@bar.com");
			};
			
			Future<SeatHold> holdFuture = executorService.submit(callableReserveTask);
		});
		executorService.shutdown();
		try {
		    if (!executorService.awaitTermination(15, TimeUnit.SECONDS)) {
		        executorService.shutdownNow();
		    } 
		} catch (InterruptedException e) {
		    executorService.shutdownNow();
		} 
		
	}

	private SeatHold hold(int numberOfSeats, String email) {
		return service.findAndHoldSeats(numberOfSeats, email);
	}

	private SeatHold holdAndWaitForExpiration(int numberOfSeats, String email) {
		SeatHold hold = hold(numberOfSeats, email);
		waitUntilExpiration(hold);
		return hold;
	}

	private void waitUntilExpiration(SeatHold hold) {
		while (hold.hasNotExpired()) {

		}
	}

	private SeatHold holdAndReserve(int numberOfSeats, String email) {
		SeatHold hold = service.findAndHoldSeats(numberOfSeats, email);
		if (hold.getNumberOfHeldOrReservedSeats() > 0) {
			service.reserveSeats(hold.getSeatHoldId(), email);
		}
		return hold;
	}

}
