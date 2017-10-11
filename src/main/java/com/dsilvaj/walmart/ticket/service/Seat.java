package com.dsilvaj.walmart.ticket.service;

import java.util.Optional;

import org.joda.time.DateTime;

public class Seat extends AbstractExpirable implements Comparable<Seat>{
	public static final String STATUS_EMPTY = ".";
	public static final String STATUS_RESERVED = "X";
	public static final String STATUS_HELD = "H";
	
	private Integer seatNumber;
	private Integer rowNumber;
	private Integer distanceFromStage;
	private String reservationStatus;
	private Optional<Integer> seatHoldId = Optional.empty();
	public Seat() {}
	public Seat(Integer seatNumber, Integer rowNumber) {
		this.seatNumber = seatNumber;
		this.rowNumber = rowNumber;
		this.reservationStatus = STATUS_EMPTY;
	}
	
	public int compareTo(Seat o) {
		return this.seatNumber.compareTo(o.seatNumber);
	}
	
	public Integer getSeatHoldId() {
		return seatHoldId.orElse(0);
	}
	
	public String getReservationStatus() {
		return reservationStatus;
	}
	
	public Integer getDistanceFromStage() {
		return distanceFromStage;
	}
	
	public Integer getRowNumber() {
		return rowNumber;
	}
	
	public Integer getSeatNumber() {
		return seatNumber;
	}
	
	public void hold(DateTime heldAt, DateTime expiresAt) {
		this.heldAt = heldAt;
		this.expiresAt = expiresAt;
		this.reservationStatus = STATUS_HELD;
	}
	
	public void reserve(int seatHoldId) {
		this.seatHoldId = Optional.of(seatHoldId);
		this.reservationStatus = STATUS_RESERVED;
	}
	
	public boolean isReserved() {
		return reservationStatus == STATUS_RESERVED;
	}
	
	public String getNumber() {
		return "R" + rowNumber + "-" + seatNumber + ", ";
	}
}
