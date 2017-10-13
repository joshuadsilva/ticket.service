package com.dsilvaj.walmart.ticket.service;

import java.util.concurrent.locks.ReentrantLock;

import com.dsilvaj.walmart.ticket.domain.SeatHold;

public class TicketServiceImpl implements TicketService {
	private ReentrantLock lock = new ReentrantLock();
	private SeatingService service = SeatingService.getInstance();
	
	public TicketServiceImpl() {

	}

	public TicketServiceImpl(int numberOfRows, int seatsPerRow, int holdTimeout) {
		service.init(numberOfRows, seatsPerRow, holdTimeout);
	}

	public int numSeatsAvailable() {
		return service.numSeatsAvailable();
	}

	public SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
		lock.lock();
		SeatHold hold = null;
		try {
			hold = service.findAndHoldSeats(numSeats, customerEmail);
		} catch (Exception e) {
			System.err.println(e);
			throw(e);
		} finally {
			lock.unlock();
		}
		return hold;
	}

	public String reserveSeats(int seatHoldId, String customerEmail) {
		return service.reserveSeats(seatHoldId, customerEmail);
	}
}
