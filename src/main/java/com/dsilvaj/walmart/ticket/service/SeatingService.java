package com.dsilvaj.walmart.ticket.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.junit.rules.Stopwatch;

public class SeatingService {
	private final boolean debug = true;
	private final String MSG_NOT_ENOUGH_SEATS = "Not enough seats available";
	private final String MSG_INVALID_HOLD_ID = "No hold found, you need to start over";
	private final String MSG_HOLD_HAS_EXPIRED = "Your time has expired, you need to start over.";
	private final String MSG_BLOCK_ALREADY_RESERVED = "This block of seats has already been reserved.";
	private final String ROWLABEL = "R%s ";
	private final String SPACE = " ";
	private final String NEWLINE = "\n";
	private final String HOLD_LOG_FORMAT = "seatHoldId:%s heldSeats:%s seats:%s";
	private final String RESERVE_LOG_FORMAT = "seatHoldId:%s reservedSeats:%s seats:%s";
	private final String RESERVATION_FORMAT = "email:%s seatHoldId:%s seats(%s):%s confirmationCode:%s";
	private int cnt = 0;
	private List<SeatRow> rows;
	private List<SeatHold> holds = new ArrayList<>();
	private int holdTimeout = 0;
	private int seatsPerRow;

	public SeatingService() {
		this(9, 33, 10);
	}

	public SeatingService(int numberOfRows, int seatsPerRow, int holdTimeout) {
		this.rows = new ArrayList<>();
		this.holdTimeout = holdTimeout;
		this.seatsPerRow = seatsPerRow;
		for (int r = 0; r < numberOfRows; r++) {
			rows.add(new SeatRow(seatsPerRow, r));
		}
	}

	public List<SeatRow> getRows() {
		return rows;
	}

	public int getHoldTimeout() {
		return holdTimeout;
	}
	
	public void printReservations() {
		StringBuilder sb = new StringBuilder();
		holds.stream().filter(h -> h.getReservation() != null)
				.forEach(h -> sb
						.append(String.format(RESERVATION_FORMAT, h.getCustomerEmail() ,h.getSeatHoldId(),
								h.getHeldSeats().size(), SeatingService.getSeatNumbers(h.getHeldSeats()), h.getReservation().getReservationCode()))
						.append(NEWLINE));
		if (debug) System.out.println(sb.toString());
	}

	public void printMap() {
		StringBuilder sb = new StringBuilder();
		for (SeatRow r : rows) {
			for (Seat seat : r.getSeats()) {
				sb.append(seat.isReserved() || seat.hasNotExpired() ? seat.getSeatHoldId() : Seat.STATUS_EMPTY)
						.append(SPACE);
			}
			List<String> blocks = r.getAvailableSeatBlocks().stream().map(b -> "{" + b.getStartSeatNumber() + "," + b.getEndSeatNumber() + "}").collect(Collectors.toList());
			sb.append(String.format(ROWLABEL, r.getRowNumber())).append(String.join(SPACE, blocks)).append(NEWLINE);
		}
		if (debug) System.out.println(sb.toString());
	}

	public int getNumberOfAvailableSeats() {
		return getTotalNumberOfSeats() - getNumberOfHeldOrReservedSeats();
	}

	public int getNumberOfHeldOrReservedSeats() {
		return holds.stream().filter(hold -> hold.isReserved() || hold.hasNotExpired())
				.mapToInt(hold -> hold.getNumberOfHeldOrReservedSeats()).sum();
	}

	public int getTotalNumberOfSeats() {
		return rows.size() * seatsPerRow;
	}

	public void addHold(SeatHold hold) {
		holds.add(hold);
	}

	public Optional<SeatHold> findHoldById(int seatHoldId) {
		return holds.stream().filter(h -> seatHoldId == h.getSeatHoldId()).findFirst();
	}

	public String reserveSeats(int seatHoldId, String customerEmail) {
		if (debug) System.out.println("reserveSeats seatHoldId:" + seatHoldId);
		Optional<SeatHold> hold = findHoldById(seatHoldId);
		if (hold.isPresent()) {
			SeatHold seatHold = hold.get();
			if (seatHold.isReserved()) {
				if (debug) System.err.println(MSG_BLOCK_ALREADY_RESERVED);
				throw new RuntimeException(MSG_BLOCK_ALREADY_RESERVED);
			}
			if (seatHold.hasNotExpired()) {
				//TODO: charge customer
				String reservationCode = seatHold.reserve();
				if (debug) System.out.println(String.format(RESERVE_LOG_FORMAT, seatHold.getSeatHoldId(), seatHold.getHeldSeats().size(), getSeatNumbers(seatHold.getHeldSeats())));
				printMap();
				return reservationCode;
			} else {
				if (debug) System.err.println(MSG_HOLD_HAS_EXPIRED);
				throw new RuntimeException(MSG_HOLD_HAS_EXPIRED);
			}
		}
		if (debug) System.err.println(MSG_INVALID_HOLD_ID);
		throw new RuntimeException(MSG_INVALID_HOLD_ID);
	}

	public static String getSeatNumbers(Set<Seat> heldSeats) {
		StringBuilder sb = new StringBuilder();
		heldSeats.stream().sorted().forEach(seat -> sb.append(seat.getNumber()));
		return sb.toString();
	}

	public SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
		if (debug) System.out.println("findSeats:" + numSeats + " email:" + customerEmail);
		if (numSeats > getNumberOfAvailableSeats()) {
			if (debug) System.err.println(MSG_NOT_ENOUGH_SEATS);
			throw new RuntimeException(MSG_NOT_ENOUGH_SEATS);
		}
		Set<Seat> heldSeats = findSeats(numSeats);
		SeatHold hold = new SeatHold(++cnt, customerEmail, DateTime.now(), DateTime.now().plusSeconds(holdTimeout),
				heldSeats);
		addHold(hold);
		if (debug) System.out.println(String.format(HOLD_LOG_FORMAT, hold.getSeatHoldId(), hold.getNumberOfHeldOrReservedSeats(), getSeatNumbers(hold.getHeldSeats())));
		printMap();
		return hold;
	}
	
	private Set<Seat> findSeats(int numSeats) {
		return solve(numSeats, new LinkedHashSet<>());
	}

	protected Set<Seat> solve(int numberOfSeatsToHold, Set<Seat> heldSeats) {
		if (numberOfSeatsToHold <= 0) {
			return heldSeats;
		}

		Optional<Set<Seat>> seats = tryToAssignSeatToARow(numberOfSeatsToHold);
		if (seats.isPresent()) {
			heldSeats.addAll(seats.get());
		} 
		return heldSeats;
	}
	
	private Optional<Set<Seat>> tryToAssignSeatToARow(int numberOfSeats) {
		for (SeatRow row : rows) {
			if (row.getRemainingCapacity() < numberOfSeats) {
				continue;
			}
			List<SeatBlock> blocks = row.getAvailableSeatBlocks().stream().filter(b -> b.willFit(numberOfSeats)).collect(Collectors.toList());

			if (blocks.isEmpty()) {
				continue;
			}
			SeatBlock bestblock = blocks.get(0);
			Set<Seat> seats = new HashSet<>();
			int start = bestblock.getStartSeatNumber();
			int stop = start + numberOfSeats - 1;
			for (int s=start; s<=stop; s++) {
				Seat seat = row.getSeats().get(s);
				seats.add(seat);
			}
			return Optional.of(seats);
		}
		return Optional.empty();
	}
 }
