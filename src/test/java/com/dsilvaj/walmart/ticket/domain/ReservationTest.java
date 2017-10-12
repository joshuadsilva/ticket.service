package com.dsilvaj.walmart.ticket.domain;

import static org.junit.Assert.*;

import org.junit.Test;

import com.dsilvaj.walmart.ticket.domain.Reservation;

public class ReservationTest {

	@Test
	public void testGetReservationCode() {
		System.out.println("--> testGetReservationCode");
		Reservation r = new Reservation();
		assertNotNull("Reservation Code is not null", r.getReservationCode());
		System.out.println(r.getReservationCode());
	}

}
