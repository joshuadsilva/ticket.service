package com.dsilvaj.walmart.ticket.service;

import static org.junit.Assert.*;

import org.junit.Test;

public class SeatBlockTest {

	@Test
	public void testWillNotFitInSeatBlock() {
		SeatBlock block = new SeatBlock(4, 10, 1);
		assertFalse("9 seats cannot be held in a block of 7 seats", block.willFit(9));
	}
	
	@Test
	public void testWillFitInSeatBlock() {
		SeatBlock block = new SeatBlock(4, 10, 1);
		assertTrue("3 seats can be held in a block of 7 seats", block.willFit(3));
	}

}