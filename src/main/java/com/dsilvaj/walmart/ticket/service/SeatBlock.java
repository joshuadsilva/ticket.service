package com.dsilvaj.walmart.ticket.service;

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

	@Override
	public int compareTo(SeatBlock o) {
		return Integer.valueOf(this.getNumberOfSeats()).compareTo(Integer.valueOf(o.getNumberOfSeats()));
	}
	
	@Override
	public String toString() {
		return String.format("{%s,%s} ", startSeatNumber, endSeatNumber);
	}
}
