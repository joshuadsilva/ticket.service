package com.dsilvaj.walmart.ticket.domain;

import java.util.UUID;

import org.joda.time.DateTime;

public class Reservation {
	private DateTime reservedAt;
	private String reservationCode;
	
	public Reservation() {
		this.reservedAt = DateTime.now();
		this.reservationCode = UUID.randomUUID().toString();
	}
	
	public String getReservationCode() {
		return reservationCode;
	}
}
