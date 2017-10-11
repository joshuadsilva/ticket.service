package com.dsilvaj.walmart.ticket.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.joda.time.DateTime;

public class SeatingService {
	private static final String ROWLABEL = "R%s ";
	private static final String SPACE = " ";
	private static final String NEWLINE = "\n";
	private final String RESERVATION_FORMAT = "seatHoldId:%s confirmationCode:%s seats(%s):%s";
	private int cnt = 0;
	private List<SeatRow> rows;
	private List<SeatHold> holds = new ArrayList<>();
	private int holdTimeout = 0;
	private int seatsPerRow;

	public SeatingService() {
		this(9, 33, 10);
	}

	public SeatingService(int holdTimeout, int seatsPerRow) {
		this.rows = new ArrayList<>();
		this.holdTimeout = holdTimeout;
		this.seatsPerRow = seatsPerRow;
	}

	public SeatingService(int numberOfRows, int seatsPerRow, int holdTimeout) {
		this(holdTimeout, seatsPerRow);
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
						.append(String.format(RESERVATION_FORMAT, h.getSeatHoldId(),
								h.getReservation().getReservationCode(), h.getHeldSeats().size(), SeatingService.getSeatNumbers(h.getHeldSeats())))
						.append(NEWLINE));
		System.out.println(sb.toString());
	}

	public void printMap() {
		StringBuilder sb = new StringBuilder();
		for (SeatRow r : rows) {
			for (Seat seat : r.getSeats()) {
				sb.append(seat.isReserved() || seat.hasNotExpired() ? seat.getSeatHoldId() : Seat.STATUS_EMPTY)
						.append(SPACE);
			}
			List<String> blocks = r.getAvailableSeatBlocks().stream().map(b -> "{" + b.getStartSeatNumber() + "," + b.getEndSeatNumber() + "} ").collect(Collectors.toList());
			sb.append(String.format(ROWLABEL, r.getRowNumber())).append(String.join(",", blocks)).append(NEWLINE);
		}
		System.out.println(sb.toString());
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
		Optional<SeatHold> hold = findHoldById(seatHoldId);
		if (hold.isPresent()) {
			SeatHold seatHold = hold.get();
			if (seatHold.isReserved()) {
				throw new RuntimeException("This block of seats has already been reserved.");
			}
			if (seatHold.hasNotExpired()) {
				//TODO: charge customer
				String reservationCode = seatHold.reserve();
				printMap();
				return reservationCode;
			} else {
				throw new RuntimeException("Your time has expired, you need to start over.");
			}
		}
		throw new RuntimeException("No hold found, you need to start over");
	}

	public static String getSeatNumbers(Set<Seat> heldSeats) {
		StringBuilder sb = new StringBuilder();
		heldSeats.stream().forEach(seat -> sb.append(seat.getNumber()));
		return sb.toString();
	}

	public SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
		if (numSeats > getNumberOfAvailableSeats()) {
			throw new RuntimeException("Not enough seats available");
		}
		Set<Seat> heldSeats = findSeats(numSeats);
		System.out.println("heldSeats: " + heldSeats.size() + " "  + getSeatNumbers(heldSeats));
		SeatHold hold = new SeatHold(++cnt, customerEmail, DateTime.now(), DateTime.now().plusSeconds(holdTimeout),
				heldSeats);
		addHold(hold);
		
		printMap();
		return hold;
	}
	
	private Set<Seat> findSeats(int numSeats) {
		System.out.println("findSeats:" + numSeats);
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
