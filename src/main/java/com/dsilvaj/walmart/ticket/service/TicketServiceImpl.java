package com.dsilvaj.walmart.ticket.service;

public class TicketServiceImpl implements TicketService {

	private SeatingService service = new SeatingService();

	public int numSeatsAvailable() {
		return service.getNumberOfAvailableSeats();
	}

	public AbstractExpirable findAndHoldSeats(int numSeats, String customerEmail) {
		// TODO: get distributed mutex
		SeatHold hold = service.findAndHoldSeats(numSeats, customerEmail);
		// TODO: release distributed mutex
		return hold;
	}

	public String reserveSeats(int seatHoldId, String customerEmail) {
		return service.reserveSeats(seatHoldId, customerEmail);
	}

}
