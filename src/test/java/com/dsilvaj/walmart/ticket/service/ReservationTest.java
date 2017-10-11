package com.dsilvaj.walmart.ticket.service;

import static org.junit.Assert.*;

import org.junit.Test;

public class ReservationTest {

	@Test
	public void testGetReservationCode() {
		Reservation r = new Reservation();
		assertNotNull("Reservation Code is not null", r.getReservationCode());
		System.out.println(r.getReservationCode());
	}

}
