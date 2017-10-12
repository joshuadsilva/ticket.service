package com.dsilvaj.walmart.ticket.domain;

import org.joda.time.DateTime;

public abstract class AbstractExpirable {

	protected DateTime heldAt;
	protected DateTime expiresAt;

	public AbstractExpirable() {
		super();
	}

	public boolean hasNotExpired() {
		if (heldAt == null) {
			return false;
		}
		return expiresAt.isAfter(DateTime.now());
	}

}