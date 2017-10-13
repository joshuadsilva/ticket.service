package com.dsilvaj.ticket.service;

import java.util.concurrent.locks.ReentrantLock;

import com.dsilvaj.ticket.domain.SeatHold;

public class TicketServiceImpl implements TicketService {
	private ReentrantLock lock = new ReentrantLock();
	private SeatingService service = SeatingService.getInstance();
	
	public TicketServiceImpl() {

	}

	public TicketServiceImpl(int numberOfRows, int seatsPerRow, int holdTimeout) {
		service.init(numberOfRows, seatsPerRow, holdTimeout);
	}

	/**
	 * @see {@link TicketService#numSeatsAvailable()}
	 */
	@Override
	public int numSeatsAvailable() {
		return service.numSeatsAvailable();
	}

	/**
	 * @see {@link TicketService#findAndHoldSeats(int, String)}
	 */
	@Override
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

	/**
	 * @see {@link TicketService#reserveSeats(int, String)}
	 */
	@Override
	public String reserveSeats(int seatHoldId, String customerEmail) {
		return service.reserveSeats(seatHoldId, customerEmail);
	}
}
