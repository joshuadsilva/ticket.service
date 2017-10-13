package com.dsilvaj.ticket.domain;

import static org.junit.Assert.*;

import org.junit.Test;

import com.dsilvaj.ticket.domain.SeatBlock;

public class SeatBlockTest {

	@Test
	public void testWillNotFitInSeatBlock() {
		System.out.println("--> testWillNotFitInSeatBlock");
		SeatBlock block = new SeatBlock(4, 10, 1);
		assertFalse("9 seats cannot be held in a block of 7 seats", block.willFit(9));
	}
	
	@Test
	public void testWillFitInSeatBlock() {
		System.out.println("--> testWillFitInSeatBlock");
		SeatBlock block = new SeatBlock(4, 10, 1);
		assertTrue("3 seats can be held in a block of 7 seats", block.willFit(3));
	}

}
