package com.dsilvaj.ticket.domain;

import java.util.ArrayList;
import java.util.List;

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
	
}
