package com.dsilvaj.walmart.ticket.service;

import com.dsilvaj.walmart.ticket.domain.SeatHold;

public class TicketServiceImpl implements TicketService {

	private SeatingService service = new SeatingService();

	public TicketServiceImpl() {

	}

	public TicketServiceImpl(int numberOfRows, int seatsPerRow, int holdTimeout) {
		this.service = new SeatingService(numberOfRows, seatsPerRow, holdTimeout);
	}

	public int numSeatsAvailable() {
		return service.numSeatsAvailable();
	}

	public SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
		// TODO: get distributed mutex
		SeatHold hold = service.findAndHoldSeats(numSeats, customerEmail);
		// TODO: release distributed mutex
		return hold;
	}

	public String reserveSeats(int seatHoldId, String customerEmail) {
		return service.reserveSeats(seatHoldId, customerEmail);
	}
}
