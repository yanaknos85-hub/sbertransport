package ru.sberbank.ditsib.transport.request.database.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Distance units.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum DistanceUnit {

	METERS(1),

	KILOMETERS(1000),

	MILES(1_609.344),

	YARDS(0.9144),

	NAUTICAL_MILES(1_852);

	private final double toMeters;

	public double toMeters(double distance) {
		return distance * toMeters;
	}

	public Double fromMeters(Double distanceInMeters) {
		if ( distanceInMeters == null ) {
			return null;
		}

		return distanceInMeters / toMeters;
	}
}
