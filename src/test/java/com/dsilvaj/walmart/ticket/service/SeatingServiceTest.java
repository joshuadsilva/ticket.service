package com.dsilvaj.walmart.ticket.service;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import com.dsilvaj.walmart.ticket.domain.MultiRowBlock;
import com.dsilvaj.walmart.ticket.domain.SeatBlock;
import com.dsilvaj.walmart.ticket.domain.SeatRow;

public class SeatingServiceTest {

	private static final int ROWSPAN2 = 2;
	private static final int ROWSPAN3 = 3;

	@Test
	public void testGetTotalNumberOfSeats() {
		System.out.println("--> testGetTotalNumberOfSeats");
		SeatingService service = new SeatingService(9, 33, 4);
		int totalSeats = 297;
		assertEquals("Total Number of seats", totalSeats, service.getTotalNumberOfSeats());
		assertEquals("Total Number of held or reserved seats", 0, service.getNumberOfHeldOrReservedSeats());
		assertEquals("Total Number of available seats: no reservations or holds", totalSeats,
				service.numSeatsAvailable());
	}
	
	@Test
	public void testGetMultiRowBlockWithAvailableBlockInNextTwoRows() {
		System.out.println("--> testGetMultiRowBlockWithAvailableBlockInNextTwoRows");
		SeatingService service = new SeatingService(5, 15, 10);
		setSeatsReserved(service);
		
		Optional<MultiRowBlock> multiBlock1 = service.getMultiRowBlock(new SeatBlock(1, 4, 1), Optional.empty(), ROWSPAN3);
		assertTrue("Multiblock starting row1-seats1-4 spanning 3 rows found", multiBlock1.isPresent());
		assertEquals("Multiblock has capacity 15", 15, multiBlock1.get().getNumberOfSeats());
		assertEquals("Multiblock has groupFactor 6", 6, multiBlock1.get().getGroupFactor());
		
		Optional<MultiRowBlock> multiBlock2 = service.getMultiRowBlock(new SeatBlock(9, 13, 1), Optional.empty(), ROWSPAN3);
		assertTrue("Multiblock starting row1-seats9-13 spanning 3 rows found", multiBlock2.isPresent());
		assertEquals("Multiblock has capacity 13", 13, multiBlock2.get().getNumberOfSeats());
		assertEquals("Multiblock has groupFactor 7", 7, multiBlock2.get().getGroupFactor());
		
		assertTrue("Multiblock2 with groupFactor=7 is better than Multiblock1 with groupFactor=6", multiBlock2.get().isBetterThan(multiBlock1.get()));
	
		Optional<MultiRowBlock> multiBlock3 = service.getMultiRowBlock(new SeatBlock(5, 9, 3), Optional.empty(), ROWSPAN3);
		assertTrue("Multiblock starting row3-seats5-9 spanning 2 rows found", multiBlock3.isPresent());
		assertEquals("Multiblock has capacity 10", 10, multiBlock3.get().getNumberOfSeats());
		assertEquals("Multiblock has groupFactor 5", 5, multiBlock3.get().getGroupFactor());
		
		assertFalse("Multiblock3 with groupFactor=5 is not better than Multiblock2 with groupFactor=7", multiBlock3.get().isBetterThan(multiBlock2.get()));
	
	}
	
	@Test
	public void testGetMultiRowBlockWithAvailableBlockInNextRow() {
		System.out.println("--> testGetMultiRowBlockWithAvailableBlockInNextRow");
		SeatingService service = new SeatingService(5, 15, 10);
		setSeatsReserved(service);
		
		Optional<MultiRowBlock> multiBlock1 = service.getMultiRowBlock(new SeatBlock(1, 4, 1), Optional.empty(), ROWSPAN2);
		assertTrue("Multiblock starting row1-seats1-4 spanning 2 rows found", multiBlock1.isPresent());
		assertEquals("Multiblock has capacity 10", 10, multiBlock1.get().getNumberOfSeats());
		assertEquals("Multiblock has groupFactor 3", 3, multiBlock1.get().getGroupFactor());
	
		Optional<MultiRowBlock> multiBlock2 = service.getMultiRowBlock(new SeatBlock(9, 13, 1), Optional.empty(), ROWSPAN2);
		assertTrue("Multiblock starting row1-seats9-13 spanning 2 rows found", multiBlock2.isPresent());
		assertEquals("Multiblock has capacity 10", 10, multiBlock2.get().getNumberOfSeats());
		assertEquals("Multiblock has groupFactor 4", 4, multiBlock2.get().getGroupFactor());
		
		assertTrue("Multiblock2 with groupFactor=4 is better than Multiblock1 with groupFactor=3", multiBlock2.get().isBetterThan(multiBlock1.get()));
	
		Optional<MultiRowBlock> multiBlock3 = service.getMultiRowBlock(new SeatBlock(5, 9, 3), Optional.empty(), ROWSPAN2);
		assertTrue("Multiblock starting row3-seats5-9 spanning 2 rows found", multiBlock3.isPresent());
		assertEquals("Multiblock has capacity 10", 10, multiBlock3.get().getNumberOfSeats());
		assertEquals("Multiblock has groupFactor 5", 5, multiBlock3.get().getGroupFactor());
		
		assertTrue("Multiblock3 with groupFactor=5 is better than Multiblock2 with groupFactor=4", multiBlock3.get().isBetterThan(multiBlock2.get()));
	}
	
	@Test
	public void testGetMultiRowBlockWithNoAvailableBlockInNextRow() {
		System.out.println("--> testGetMultiRowBlockWithNoAvailableBlockInNextRow");
		SeatingService service = new SeatingService(5, 15, 10);
		setSeatsReserved(service);
		
		Optional<MultiRowBlock> multiBlock1 = service.getMultiRowBlock(new SeatBlock(7, 8, 0), Optional.empty(), ROWSPAN2);
		assertFalse("MultiBlock1 starting row0-seats7-8 has no contiguous block in next row", multiBlock1.isPresent());
	
		Optional<MultiRowBlock> multiBlock2 = service.getMultiRowBlock(new SeatBlock(12, 14, 3), Optional.empty(), ROWSPAN2);
		assertFalse("MultiBlock2 starting row3-seats12-14 has no contiguous block in next row", multiBlock2.isPresent());
	
		Optional<MultiRowBlock> multiBlock3 = service.getMultiRowBlock(new SeatBlock(0, 1, 4), Optional.empty(), ROWSPAN2);
		assertFalse("MultiBlock3 starting row4-seats0-1 has no contiguous block in next row", multiBlock3.isPresent());
	}
	
	@Test
	public void testFindBestMultiRowBlockAcross2RowsWithCapacityFor10() {
		System.out.println("--> testFindBestMultiRowBlockAcross2RowsWithCapacityFor10");
		SeatingService service = new SeatingService(5, 15, 10);
		setSeatsReserved(service);
		
		Optional<MultiRowBlock> bestMultiBlock = service.findBestMultiRowBlock(10, 2);
		assertTrue("Multiblock starting row3-seats5-9 spanning 2 rows found", bestMultiBlock.isPresent());
		assertEquals("Multiblock has capacity 10", 10, bestMultiBlock.get().getNumberOfSeats());
		assertEquals("Multiblock has groupFactor 5", 5, bestMultiBlock.get().getGroupFactor());
	}
	
	@Test
	public void testFindBestMultiRowBlockAcross3RowsWithCapacityFor13() {
		System.out.println("--> testFindBestMultiRowBlockAcross3RowsWithCapacityFor13");
		SeatingService service = new SeatingService(5, 15, 10);
		setSeatsReserved(service);
		
		Optional<MultiRowBlock> bestMultiBlock = service.findBestMultiRowBlock(13, 3);
		assertTrue("Multiblock starting row2-seats2-7 spanning 3 rows found", bestMultiBlock.isPresent());
		assertEquals("Multiblock has capacity 16", 16, bestMultiBlock.get().getNumberOfSeats());
		assertEquals("Multiblock has groupFactor 8", 8, bestMultiBlock.get().getGroupFactor());
	}
	
	private void setSeatsReserved(SeatingService service) {
		List<String> seats = Arrays.asList("R0:0-6", "R0:9-14", "R1:0", "R1:5-8", "R1:14",
				"R2:0-1", "R2:8-9", "R3:0-4", "R3:10-11", "R4:2-4", "R4:10-14");
		
		List<SeatRow> rows = service.getRows();
		for (String seatNumber : seats) {
			String[] tokens = seatNumber.replace("R", "").split(":");
			if (tokens.length != 2) {
				continue;
			}
			Integer rowNbr = Integer.valueOf(tokens[0]);
			Integer startSeatNbr, endSeatNbr;
			if (tokens[1].contains("-")) {
				String[] seatTokens = tokens[1].split("-");
				startSeatNbr = Integer.valueOf(seatTokens[0]);
				endSeatNbr = Integer.valueOf(seatTokens[1]);
			} else {
				startSeatNbr = endSeatNbr = Integer.valueOf(tokens[1]);
			}
			for (int s = startSeatNbr; s<=endSeatNbr; s++) {
				rows.get(rowNbr).getSeats().get(s).reserve(0);
			}
			
		}
		service.printMap();
	}
}
