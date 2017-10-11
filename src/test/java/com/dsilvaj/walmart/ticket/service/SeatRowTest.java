package com.dsilvaj.walmart.ticket.service;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Test;

public class SeatRowTest {

	@Test
	public void testGetAvailableSeatBlockNoneHeldOrReserved() {
		SeatRow row = new SeatRow(15, 1);
		List<SeatBlock> blocks = row.getAvailableSeatBlocks();
		assertEquals("1 block of available seats when none are taken", 1, blocks.size());
		assertEquals("All seats in the row are available in the block", 15, blocks.get(0).getNumberOfSeats());
		assertEquals("block starts at seat 0", 0, blocks.get(0).getStartSeatNumber());
		assertEquals("block ends at seat 14", 14, blocks.get(0).getEndSeatNumber());
		assertEquals("remaining capacity in row is 15", 15, row.getRemainingCapacity());
	}
	
	@Test
	public void testGetAvailableSeatBlockLeftTaken() {
		SeatRow row = new SeatRow(15, 1);
		setSeatsReserved(row.getSeats(), Arrays.asList(0,1,2));
		List<SeatBlock> blocks = row.getAvailableSeatBlocks();
		assertEquals("1 block of available seats when left edge is taken", 1, blocks.size());
		assertEquals("12 seats in the row are available in the block", 12, blocks.get(0).getNumberOfSeats());
		assertEquals("block starts at seat 3", 3, blocks.get(0).getStartSeatNumber());
		assertEquals("block ends at seat 14", 14, blocks.get(0).getEndSeatNumber());
		assertEquals("remaining capacity in row is 12", 12, row.getRemainingCapacity());
	}
	
	@Test
	public void testGetAvailableSeatBlockRightTaken() {
		SeatRow row = new SeatRow(15, 1);
		setSeatsReserved(row.getSeats(), Arrays.asList(13,14));
		List<SeatBlock> blocks = row.getAvailableSeatBlocks();
		assertEquals("1 block of available seats when right edge is taken", 1, blocks.size());
		assertEquals("13 seats in the row are available in the block", 13, blocks.get(0).getNumberOfSeats());
		assertEquals("block starts at seat 0", 0, blocks.get(0).getStartSeatNumber());
		assertEquals("block ends at seat 12", 12, blocks.get(0).getEndSeatNumber());
		assertEquals("remaining capacity in row is 13", 13, row.getRemainingCapacity());
	}
	
	@Test
	public void testGetAvailableSeatBlockMiddleTaken() {
		SeatRow row = new SeatRow(15, 1);
		setSeatsReserved(row.getSeats(), Arrays.asList(3,4,5,6));
		List<SeatBlock> blocks = row.getAvailableSeatBlocks();
		assertEquals("2 blocks of available seats when middle is taken", 2, blocks.size());
		assertEquals("3 seats in the row are available in block1", 3, blocks.get(0).getNumberOfSeats());
		assertEquals("block1 starts at seat 0", 0, blocks.get(0).getStartSeatNumber());
		assertEquals("block1 ends at seat 2", 2, blocks.get(0).getEndSeatNumber());
		assertEquals("8 seats in the row are available in block2", 8, blocks.get(1).getNumberOfSeats());
		assertEquals("block2 starts at seat 7", 7, blocks.get(1).getStartSeatNumber());
		assertEquals("block2 ends at seat 14", 14, blocks.get(1).getEndSeatNumber());	
		assertEquals("remaining capacity in row is 11", 11, row.getRemainingCapacity());
	}
	
	@Test
	public void testGetAvailableSeatBlockTwoMiddleBlocksTaken() {
		SeatRow row = new SeatRow(15, 1);
		setSeatsReserved(row.getSeats(), Arrays.asList(3,4,5,6,9,10));
		List<SeatBlock> blocks = row.getAvailableSeatBlocks();
		assertEquals("3 blocks of available seats when 2 middle blocks are taken", 3, blocks.size());
		assertEquals("3 seats in the row are available in block1", 3, blocks.get(0).getNumberOfSeats());
		assertEquals("block1 starts at seat 0", 0, blocks.get(0).getStartSeatNumber());
		assertEquals("block1 ends at seat 2", 2, blocks.get(0).getEndSeatNumber());
		
		assertEquals("2 seats in the row are available in block2", 2, blocks.get(1).getNumberOfSeats());
		assertEquals("block2 starts at seat 7", 7, blocks.get(1).getStartSeatNumber());
		assertEquals("block2 ends at seat 8", 8, blocks.get(1).getEndSeatNumber());	
		
		assertEquals("4 seats in the row are available in block3", 4, blocks.get(2).getNumberOfSeats());
		assertEquals("block3 starts at seat 11", 11, blocks.get(2).getStartSeatNumber());
		assertEquals("block3 ends at seat 14", 14, blocks.get(2).getEndSeatNumber());	

		assertEquals("remaining capacity in row is 9", 9, row.getRemainingCapacity());
	}

	private void setSeatsReserved(List<Seat> seats, List<Integer> seatNumbers) {
		for (Integer n : seatNumbers) {
			Seat s = seats.get(n);
			s.hold(DateTime.now(), DateTime.now().plusSeconds(10));
			s.reserve(123);
		}
	}
}
