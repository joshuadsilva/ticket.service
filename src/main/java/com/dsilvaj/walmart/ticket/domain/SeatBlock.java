package com.dsilvaj.walmart.ticket.domain;

import java.util.Comparator;

import org.apache.commons.lang3.Range;

public class SeatBlock implements Comparable<SeatBlock> {
	private int startSeatNumber;
	private int endSeatNumber;
	private int rowNumber;
	
	public SeatBlock(int startSeatNumber, int endSeatNumber, int rowNumber) {
		this.startSeatNumber = startSeatNumber;
		this.endSeatNumber = endSeatNumber;
		this.rowNumber = rowNumber;
	}
	
	public boolean willFit(int numberOfSeats) {
		return numberOfSeats <= getNumberOfSeats();
	}
	
	public int getRowNumber() {
		return rowNumber;
	}
	
	public int getStartSeatNumber() {
		return startSeatNumber;
	}

	public int getEndSeatNumber() {
		return endSeatNumber;
	}
	
	public void setEndSeatNumber(int endSeatNumber) {
		this.endSeatNumber = endSeatNumber;
	}

	public int getNumberOfSeats() {
		return endSeatNumber - startSeatNumber + 1;
	}
	
	public int intersectWith(SeatBlock other) {
		Range<Integer> r1 = Range.between(this.getStartSeatNumber(), this.getEndSeatNumber());
		Range<Integer> r2 = Range.between(other.getStartSeatNumber(), other.getEndSeatNumber());
		return Math.min(r1.getMaximum(), r2.getMaximum()) - Math.max(r1.getMinimum(), r2.getMinimum()) + 1;
	}
	
	public boolean overlapsWith(SeatBlock other) {
		Range<Integer> r1 = Range.between(this.getStartSeatNumber(), this.getEndSeatNumber());
		Range<Integer> r2 = Range.between(other.getStartSeatNumber(), other.getEndSeatNumber());
		return r1.isOverlappedBy(r2);
	}

	@Override
	public int compareTo(SeatBlock o) {
		return Integer.valueOf(this.getNumberOfSeats()).compareTo(Integer.valueOf(o.getNumberOfSeats()));
	}
	
	@Override
	public String toString() {
		return String.format("%s:%s-%s ", "R" + rowNumber, startSeatNumber, endSeatNumber);
	}
	
	public static class RowNumberComparator implements Comparator<SeatBlock> {
		@Override
		public int compare(SeatBlock o1, SeatBlock o2) {
			return Integer.valueOf(o1.rowNumber).compareTo(Integer.valueOf(o2.getRowNumber()));
		}
		
	}
}
