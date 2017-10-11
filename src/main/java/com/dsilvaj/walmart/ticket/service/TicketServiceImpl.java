package com.dsilvaj.walmart.ticket.service;

public class TicketServiceImpl implements TicketService {

	private Stadium service = new Stadium();

	public int numSeatsAvailable() {
		return service.getNumberOfAvailableSeats();
	}

	public AbstractExpirable findAndHoldSeats(int numSeats, String customerEmail) {
		return service.findAndHoldSeats(numSeats, customerEmail);
	}

	public String reserveSeats(int seatHoldId, String customerEmail) {
		return service.reserveSeats(seatHoldId, customerEmail);
	}

}
