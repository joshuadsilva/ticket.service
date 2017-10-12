package com.dsilvaj.walmart.ticket.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MultiRowBlock {

	private List<SeatBlock> blocks = new ArrayList<>();
	private int groupFactor = 0;
	
	public MultiRowBlock(SeatBlock block) {
		blocks.add(block);
	}
	
	public void addBlock(SeatBlock block, int groupFactor) {
		blocks.add(block);
		this.groupFactor += groupFactor;
	}
	
	public List<SeatBlock> getBlocks() {
		return blocks;
	}
	
	public int getGroupFactor() {
		return groupFactor;
	}
	
	public int getNumberOfSeats() {
		return blocks.stream().mapToInt(SeatBlock::getNumberOfSeats).sum();
	}
	
	public boolean isBetterThan(MultiRowBlock other) {
		return this.groupFactor > other.getGroupFactor();
	}
	
	public String toString() {
		return "groupFactor:" + groupFactor + " " + String.join(" ", blocks.stream().map(b -> b.toString()).collect(Collectors.toList()));
	}
}
