package com.dsilvaj.ticket.domain;

import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTime;

public class SeatHold extends AbstractExpirable {
	private int seatHoldId;
	private String customerEmail;
	private Set<Seat> heldSeats = new HashSet<>();
	private Reservation reservation;

	public SeatHold(int seatHoldId, String customerEmail, DateTime heldAt, DateTime expiresAt, Set<Seat> heldSeats) {
		this.seatHoldId = seatHoldId;
		this.customerEmail = customerEmail;
		this.heldAt = heldAt;
		this.expiresAt = expiresAt;
		this.heldSeats = heldSeats;
		for (Seat seat : heldSeats) {
			seat.hold(heldAt, expiresAt);
		}
	}
	
	public String getCustomerEmail() {
		return customerEmail;
	}
	
	public Set<Seat> getHeldSeats() {
		return heldSeats;
	}

	public int getSeatHoldId() {
		return seatHoldId;
	}

	public Reservation getReservation() {
		return reservation;
	}

	public boolean isReserved() {
		return reservation != null;
	}

	public int getNumberOfHeldOrReservedSeats() {
		return heldSeats.size();
	}

	public String reserve() {
		reservation = new Reservation();
		for (Seat seat : heldSeats) {
			seat.reserve(seatHoldId);
		}
		return reservation.getReservationCode();
	}
}
