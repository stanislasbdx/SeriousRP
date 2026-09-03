package fr.stan1712.wetston.seriousrp.commands;

import java.util.Optional;
import java.util.Random;

public final class RtpLocationSampler {
	static final int MAX_ATTEMPTS = 16;

	private RtpLocationSampler() {
		throw new IllegalStateException("Utility class");
	}

	public record HorizontalLocation(int x, int z) {
		public double horizontalDistanceTo(int originX, int originZ) {
			return hypotDelta(x, originX, z, originZ);
		}
	}

	public static Optional<HorizontalLocation> sample(int originX, int originZ, int maxBlockRange, Random random) {
		if (maxBlockRange <= 0 || random == null) {
			return Optional.empty();
		}

		double minRange = maxBlockRange / 3.0;
		double maxRange = maxBlockRange;
		HorizontalLocation last = new HorizontalLocation(originX, originZ);

		for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
			double radius = minRange + random.nextDouble() * (maxRange - minRange);
			double angle = random.nextDouble() * Math.PI * 2;
			int x = originX + (int) Math.round(Math.cos(angle) * radius);
			int z = originZ + (int) Math.round(Math.sin(angle) * radius);
			last = new HorizontalLocation(x, z);
			double dist = last.horizontalDistanceTo(originX, originZ);
			if (dist >= minRange && dist <= maxRange) {
				return Optional.of(last);
			}
		}

		return Optional.of(clampToRange(originX, originZ, last, minRange, maxRange));
	}

	static HorizontalLocation clampToRange(int originX, int originZ, HorizontalLocation location, double minRange, double maxRange) {
		double dx = (double) location.x() - originX;
		double dz = (double) location.z() - originZ;
		double dist = Math.hypot(dx, dz);

		if (dist == 0.0) {
			return fallbackAlongX(originX, originZ, minRange, maxRange);
		}

		double target = Math.clamp(dist, minRange, maxRange);
		int x = originX + (int) Math.round(dx * (target / dist));
		int z = originZ + (int) Math.round(dz * (target / dist));
		double newDist = hypotDelta(x, originX, z, originZ);
		if (newDist >= minRange && newDist <= maxRange) {
			return new HorizontalLocation(x, z);
		}

		return fallbackAlongX(originX, originZ, minRange, maxRange);
	}

	private static HorizontalLocation fallbackAlongX(int originX, int originZ, double minRange, double maxRange) {
		int fallback = (int) Math.clamp(Math.round(minRange), 1L, (long) maxRange);
		return new HorizontalLocation(originX + fallback, originZ);
	}

	private static double hypotDelta(int x, int originX, int z, int originZ) {
		return Math.hypot((double) x - originX, (double) z - originZ);
	}
}
