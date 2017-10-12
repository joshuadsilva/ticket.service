package com.dsilvaj.walmart.ticket.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import com.dsilvaj.walmart.ticket.domain.MultiRowBlock;
import com.dsilvaj.walmart.ticket.domain.Seat;
import com.dsilvaj.walmart.ticket.domain.SeatBlock;
import com.dsilvaj.walmart.ticket.domain.SeatHold;
import com.dsilvaj.walmart.ticket.domain.SeatRow;

public class SeatingService {
	private final boolean debug = true;
	private final String MAP_HEADER = "            Map              row availableSeatBlocks";
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

	/**
	 * Construct a service for a venue of given dimensions and hold timeout
	 * 
	 * @param numberOfRows - the number of rows in the venue
	 * @param seatsPerRow - the number of seats in a row
	 * @param holdTimeout - the time in seconds to hold a seat before it expires and is returned to the pool of available seats
	 */
	public SeatingService(int numberOfRows, int seatsPerRow, int holdTimeout) {
		this.rows = new ArrayList<>();
		this.holdTimeout = holdTimeout;
		this.seatsPerRow = seatsPerRow;
		for (int r = 0; r < numberOfRows; r++) {
			rows.add(new SeatRow(seatsPerRow, r));
		}
	}

	protected List<SeatRow> getRows() {
		return rows;
	}

	public int getHoldTimeout() {
		return holdTimeout;
	}
	
	/**
	 * Write all the reservations to stdout 
	 */
	private void printReservations() {
		StringBuilder sb = new StringBuilder();
		holds.stream().filter(h -> h.getReservation() != null)
				.forEach(h -> sb
						.append(String.format(RESERVATION_FORMAT, h.getCustomerEmail() ,h.getSeatHoldId(),
								h.getHeldSeats().size(), SeatingService.getSeatNumbers(h.getHeldSeats()), h.getReservation().getReservationCode()))
						.append(NEWLINE));
		System.out.println(sb.toString());
	}

	/**
	 * Write a seat map to stdout, showing held and reserved seats and open blocks of seats in each row.
	 * 
	 * e.g.,
	 * <pre>
	 *             Map              row availableSeatBlocks
	 * ----------------------------------------------------
	 * 1 1 1 1 0 0 0 6 6 . . . . . . R0 9-14
	 * 4 4 4 4 4 4 4 4 4 4 4 4 4 4 4 R1 
	 * 4 5 5 5 5 5 5 5 5 5 5 5 5 . . R2 13-14
	 * . . . . . . . . . . . . . . . R3 0-14
	 * . . . . . . . . . . . . . . . R4 0-14
	 * 
	 * where 
	 *  0 - held seats that have not yet expired
     *  N - seat held or reserved under seatHoldId N where N > 0
     *  "." - empty seat
     *  R(n) - Row number n (0 < n < total number of rows in venue)
     *  N-M - open seat block starting at seat N and ending at seat M
	 * </pre>
	 */
	protected void printMap() {
		StringBuilder sb = new StringBuilder();
		sb.append(MAP_HEADER).append(NEWLINE);
		sb.append(StringUtils.repeat("-", MAP_HEADER.length())).append(NEWLINE);
		for (SeatRow r : rows) {
			for (Seat seat : r.getSeats()) {
				sb.append(seat.isReserved() || seat.hasNotExpired() ? seat.getSeatHoldId() : Seat.STATUS_EMPTY)
						.append(SPACE);
			}
			List<String> blocks = r.getAvailableSeatBlocks().stream().map(b -> b.getStartSeatNumber() + "-" + b.getEndSeatNumber()).collect(Collectors.toList());
			sb.append(String.format(ROWLABEL, r.getRowNumber())).append(String.join(SPACE, blocks)).append(NEWLINE);
		}
		System.out.println(sb.toString());
	}

	/**
	 * Compute the number of tickets available (not held or reserved)
	 * @return the number of tickets available in the venue
	 */
	public int numSeatsAvailable() {
		return getTotalNumberOfSeats() - getNumberOfHeldOrReservedSeats();
	}

	/**
	 * Compute the number of held (but not expired) and reserved seats in the venue
	 * @return the number of tickets held and reserved in the venue
	 */
	protected int getNumberOfHeldOrReservedSeats() {
		return holds.stream().filter(hold -> hold.isReserved() || hold.hasNotExpired())
				.mapToInt(hold -> hold.getNumberOfHeldOrReservedSeats()).sum();
	}

	/**
	 * Computes the total number of seats
	 * @return the total numebr of seats
	 */
	public int getTotalNumberOfSeats() {
		return rows.size() * seatsPerRow;
	}

	private void addHold(SeatHold hold) {
		holds.add(hold);
	}

	private Optional<SeatHold> findHoldById(int seatHoldId) {
		return holds.stream().filter(h -> seatHoldId == h.getSeatHoldId()).findFirst();
	}

	/**
	 * Reserve the seats associated with a seatHoldId for a specific customer.
	 * @param seatHoldId - the seat hold identifier
	 * @param customerEmail - the email of the customer
	 * @return a reservation confirmation code
	 */
	public String reserveSeats(int seatHoldId, String customerEmail) {
		if (debug) System.out.println("reserveSeats seatHoldId:" + seatHoldId + " customerEmail:" + customerEmail);
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
				if (debug) printMap();
				if (debug) printReservations();
				return reservationCode;
			} else {
				if (debug) System.err.println(MSG_HOLD_HAS_EXPIRED);
				throw new RuntimeException(MSG_HOLD_HAS_EXPIRED);
			}
		}
		if (debug) System.err.println(MSG_INVALID_HOLD_ID);
		throw new RuntimeException(MSG_INVALID_HOLD_ID);
	}

	/**
	 * Return a formatted string listing of seat numbers 
	 * 
	 * e.g., R2:1, R2:2, R2:3, R2:4
	 * 
	 * @param heldSeats - a collection of {@link Seat} objects
	 * @return a string of formatted seat numbers
	 */
	public static String getSeatNumbers(Set<Seat> heldSeats) {
		StringBuilder sb = new StringBuilder();
		heldSeats.stream().sorted().forEach(seat -> sb.append(seat.getNumber()));
		return sb.toString();
	}

	/**
	 * Find the best seats available. Highest preference is given to finding
	 * seats in a single row, followed by seats in blocks that have the highest 
	 * contiguity (overlap/grouping) factorthat span multiple rows.
	 *  
	 * @param numSeats - the number of seats to hold
	 * @param customerEmail - the email of the customer
	 * @return a {@link SeatHold} object identifying specific seats and related information
	 */
	public SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
		if (debug) System.out.println("findAndHoldSeats:" + numSeats + " email:" + customerEmail);
		if (numSeats > numSeatsAvailable()) {
			if (debug) System.err.println(MSG_NOT_ENOUGH_SEATS);
			throw new RuntimeException(MSG_NOT_ENOUGH_SEATS);
		}
		Set<Seat> bestSeats = findBestSeats(numSeats, new LinkedHashSet<>());
		SeatHold hold = new SeatHold(++cnt, customerEmail, DateTime.now(), DateTime.now().plusSeconds(holdTimeout),
				bestSeats);
		addHold(hold);
		if (debug) System.out.println(String.format(HOLD_LOG_FORMAT, hold.getSeatHoldId(), hold.getNumberOfHeldOrReservedSeats(), getSeatNumbers(hold.getHeldSeats())));
		if (debug) printMap();
		return hold;
	}
	
	/**
	 * 
	 * @param numberOfSeatsToHold
	 * @param heldSeats
	 * @return
	 */
	private Set<Seat> findBestSeats(int numberOfSeatsToHold, Set<Seat> heldSeats) {
		if (numberOfSeatsToHold <= 0) {
			return heldSeats;
		}

		Optional<Set<Seat>> seats = Optional.empty();
		
		// if seats requested can fit in a single row
		if (numberOfSeatsToHold <= seatsPerRow) {
			seats = tryToAssignSeatToASingleRow(numberOfSeatsToHold);
		}
		
		// seats cannot fit in a single row, try to scan multiple rows
		int numberOfRowsToSpan = 1;
		while (!seats.isPresent() && numberOfRowsToSpan < rows.size()) {
			seats = tryToAssignSeatsToMultipleContiguousRows(numberOfSeatsToHold, ++numberOfRowsToSpan);
		}
		
		// if we found seats return them
		if (seats.isPresent()) {
			heldSeats.addAll(seats.get());
		} 
		return heldSeats;
	}
	
	/**
	 * Try to assign the requested number of seats to a single row.
	 * 
	 * @param numberOfSeats - the number of seats that need to be held
	 * @return an {@link Optional} object that contains the seats that are the best match in a single row
	 */
	private Optional<Set<Seat>> tryToAssignSeatToASingleRow(int numberOfSeats) {
		for (SeatRow row : rows) {
			if (row.getRemainingCapacity() < numberOfSeats) {
				continue;
			}
			List<SeatBlock> blocks = row.getAvailableSeatBlocks().stream()
					.filter(b -> b.willFit(numberOfSeats))
					.sorted()
					.collect(Collectors.toList());

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
	
	/**
	 * Try to assign the requested number of seats to multiple contiguous rows.
	 * 
	 * @param numberOfSeats - the number of seats that need to be held
	 * @return an {@link Optional} object that contains the seats that are the best match
	 */
	private Optional<Set<Seat>> tryToAssignSeatsToMultipleContiguousRows(int numberOfSeats, int numberOfRowsToSpan) {
		if (debug) System.out.println("tryToAssignSeatsToMultipleContiguousRows seats:" + numberOfSeats + " rowSpan:" + numberOfRowsToSpan);
		Optional<MultiRowBlock> bestMultiRowBlock = findBestMultiRowBlock(numberOfSeats, numberOfRowsToSpan);
		if (bestMultiRowBlock.isPresent()) {
			return chooseSeatsInMultiBlock(numberOfSeats, bestMultiRowBlock.get());
		}
		System.err.println("No multiblock found. span:" + numberOfRowsToSpan);
		return Optional.empty();
	}
	
	/**
	 * Select the requested number of seats across multiple rows
	 * 
	 * @param numberOfSeats - the number of seats to be held
	 * @param multiBlock - the best block of seats spanning more than one row determined using the highest contiguity/grouping factor
	 * @return an {@link Optional} object that contains the seats that are the best match
	 */
	private Optional<Set<Seat>> chooseSeatsInMultiBlock(int numberOfSeats, MultiRowBlock multiBlock) {
		if (debug) System.out.println("holdSeatsInMultiBlock " + multiBlock.toString());
		List<SeatBlock> ordered = multiBlock.getBlocks().stream().sorted(new SeatBlock.RowNumberComparator()).collect(Collectors.toList());
		Set<Seat> seats = new HashSet<>();
		int idx = 0;
		while (numberOfSeats > 0) {
			SeatBlock block = ordered.get(idx++);
			int nbrSeatsToHoldInBlock = Math.min(numberOfSeats, block.getNumberOfSeats());
			for (int s=block.getStartSeatNumber(); s<block.getStartSeatNumber() + nbrSeatsToHoldInBlock; s++) {
				seats.add(rows.get(block.getRowNumber()).getSeats().get(s));
				numberOfSeats -= 1;
			}
		}
		return Optional.of(seats);
	}

	/**
	 * Find the best block of seats spanning more than one row determined using the highest contiguity/grouping factor.
	 * @param numberOfSeats - the number of seats requested
	 * @param numberOfRowsToSpan - the number of rows that should be used to find the best seats
	 * @return an {@link Optional} object that contains the best block of seats spanning more than one row determined using the highest contiguity/grouping factor
	 */
	protected Optional<MultiRowBlock> findBestMultiRowBlock(int numberOfSeats, int numberOfRowsToSpan) {
		List<SeatRow> availableRows = rows.stream().filter(r -> r.getRemainingCapacity() > 0).collect(Collectors.toList());
		Optional<MultiRowBlock> bestMultiRowBlock = Optional.empty();
		for (SeatRow row : availableRows) {
			for (SeatBlock block : row.getAvailableSeatBlocks()) {
				Optional<MultiRowBlock> multiBlock = getMultiRowBlock(block, Optional.empty(), numberOfRowsToSpan);
			
				if (multiBlock.isPresent() && multiBlock.get().getNumberOfSeats() >= numberOfSeats) {
					if (!bestMultiRowBlock.isPresent() || multiBlock.get().isBetterThan(bestMultiRowBlock.get())) {
						bestMultiRowBlock = multiBlock;
					}
				}
			}
		}
		return bestMultiRowBlock;
	}
	
	/**
	 * Find all blocks of contiguous open seats that span the requested 
	 * number of rows relative to an anchor block of seats. Compute the 
	 * contiguity/grouping factor for the entire block of seats.
	 * @param block - the anchor block around which to find contiguous blocks of seats
	 * @param multiBlock 
	 * @param numberOfRowsToSpan - the number of rows that the block of open seats can span
	 * @return an {@link Optional} object that contains a block of seats relative to the requested anchor block
	 */
	protected Optional<MultiRowBlock> getMultiRowBlock(SeatBlock block, Optional<MultiRowBlock> multiBlock, int numberOfRowsToSpan) {
		//if (debug) System.out.println("getMultiRowBlock for block:" + block.toString());
 		int nextRowNbr = block.getRowNumber() + 1;
		if (nextRowNbr >= rows.size()) {
			return multiBlock;
		}
		SeatRow nextRow = rows.get(nextRowNbr);
		if (nextRow.getAvailableSeatBlocks().size() > 0) {
			List<SeatBlock> blocks = nextRow.getAvailableSeatBlocks();
			for (SeatBlock b : blocks) {
				if (b.overlapsWith(block)) {
					int groupFactor = b.intersectWith(block);
					if (!multiBlock.isPresent()) {
						multiBlock = Optional.of(new MultiRowBlock(block));
					}
					multiBlock.get().addBlock(b, groupFactor);
					if (numberOfRowsToSpan > 2) {
						return getMultiRowBlock(b, multiBlock, --numberOfRowsToSpan);
					}
				}
			}
		}
		return multiBlock;
	}
 }
