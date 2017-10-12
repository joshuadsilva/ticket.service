package com.dsilvaj.walmart.ticket.domain;

import java.util.ArrayList;
import java.util.List;

public class SeatRow {

	private int rowNumber;
	private List<Seat> seats;
	
	public SeatRow(int rowNumber) {
		this.rowNumber = rowNumber;
		seats = new ArrayList<>();
	}
	
	public SeatRow(int numberOfSeats, int rowNumber) {
		this(rowNumber);
		for (int s=0; s<numberOfSeats; s++) {
			seats.add(new Seat(s, this.rowNumber));
		}
	}
	
	public int getRowNumber() {
		return rowNumber;
	}
	
	public long getRemainingCapacity() {
		return seats.stream().filter(s -> !s.isReserved() && !s.hasNotExpired()).count();
	}
	
	public List<SeatBlock> getAvailableSeatBlocks() {
		List<SeatBlock> blocks = new ArrayList<>();
		SeatBlock block = null;
		for (int s=0; s<seats.size(); s++) {
			Seat seat = seats.get(s);
			if (!seat.isReserved() && !seat.hasNotExpired()) {
				if (block == null) {
					block = new SeatBlock(s, s, rowNumber);
				}
				block.setEndSeatNumber(s);
			} 
			if (block != null && (seat.isReserved() || seat.hasNotExpired() || s == seats.size() - 1)) {
				blocks.add(block);
				block = null;
			}
		}
		return blocks;
	}
	
	public int getCapacity() {
		return seats.size();
	}
	
	public List<Seat> getSeats() {
		return seats;
	}
}
