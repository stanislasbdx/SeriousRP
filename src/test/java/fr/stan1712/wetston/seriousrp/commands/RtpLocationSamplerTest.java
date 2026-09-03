package fr.stan1712.wetston.seriousrp.commands;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RtpLocationSamplerTest {

	@Test
	void sampleStaysWithinConfiguredHorizontalRange() {
		int originX = 12_480;
		int originZ = -8_192;
		int maxBlockRange = 1000;
		double minRange = maxBlockRange / 3.0;
		Random random = new Random(42);

		for (int i = 0; i < 2_000; i++) {
			Optional<RtpLocationSampler.HorizontalLocation> sample =
				RtpLocationSampler.sample(originX, originZ, maxBlockRange, random);

			assertTrue(sample.isPresent());
			double distance = sample.get().horizontalDistanceTo(originX, originZ);
			assertTrue(distance >= minRange - 1.0, "distance too small: " + distance);
			assertTrue(distance <= maxBlockRange + 1.0, "distance too large: " + distance);
		}
	}

	@Test
	void sampleDoesNotMirrorWorldCoordinatesThroughOrigin() {
		int originX = 5_000;
		int originZ = 5_000;
		int maxBlockRange = 1000;
		Random random = new Random(7);

		for (int i = 0; i < 500; i++) {
			RtpLocationSampler.HorizontalLocation location =
				RtpLocationSampler.sample(originX, originZ, maxBlockRange, random).orElseThrow();

			assertTrue(location.x() > 0, "x mirrored through origin: " + location.x());
			assertTrue(location.z() > 0, "z mirrored through origin: " + location.z());
			assertTrue(Math.abs(location.x() - originX) <= maxBlockRange + 1);
			assertTrue(Math.abs(location.z() - originZ) <= maxBlockRange + 1);
		}
	}

	@Test
	void sampleReturnsEmptyWhenRangeIsNotPositive() {
		Random random = new Random(1);

		assertTrue(RtpLocationSampler.sample(0, 0, 0, random).isEmpty());
		assertTrue(RtpLocationSampler.sample(0, 0, -10, random).isEmpty());
		assertTrue(RtpLocationSampler.sample(0, 0, 1000, null).isEmpty());
	}

	@Test
	void clampToRangePullsOversizedOffsetBackInsideMax() {
		RtpLocationSampler.HorizontalLocation clamped = RtpLocationSampler.clampToRange(
			0,
			0,
			new RtpLocationSampler.HorizontalLocation(10_000, 0),
			1000 / 3.0,
			1000
		);

		assertEquals(1000, clamped.x());
		assertEquals(0, clamped.z());
		assertEquals(1000.0, clamped.horizontalDistanceTo(0, 0));
	}

	@Test
	void unusedRandomDoesNotThrowForInvalidRange() {
		assertFalse(RtpLocationSampler.sample(100, 100, 0, new Random()).isPresent());
	}

	@Test
	void clampToRangeFallsBackAlongXWhenSampleIsOnOrigin() {
		RtpLocationSampler.HorizontalLocation clamped = RtpLocationSampler.clampToRange(
			10,
			20,
			new RtpLocationSampler.HorizontalLocation(10, 20),
			100,
			1000
		);

		assertEquals(110, clamped.x());
		assertEquals(20, clamped.z());
	}

	@Test
	void sampleRejectsCandidatesThatRoundPastMaxRange() {
		Random alwaysCorner = new Random() {
			private int calls;

			@Override
			public double nextDouble() {
				calls++;
				return calls % 2 == 1 ? 1.0 : 0.125;
			}
		};

		Optional<RtpLocationSampler.HorizontalLocation> sample =
			RtpLocationSampler.sample(0, 0, 1, alwaysCorner);

		assertTrue(sample.isPresent());
		assertEquals(1, sample.get().x());
		assertEquals(0, sample.get().z());
	}

	@Test
	void sampleFallsBackToClampWhenEveryAttemptRoundsOutsideRange() {
		Random alwaysZero = new Random() {
			@Override
			public double nextDouble() {
				return 0.0;
			}
		};

		Optional<RtpLocationSampler.HorizontalLocation> sample =
			RtpLocationSampler.sample(0, 0, 1, alwaysZero);

		assertTrue(sample.isPresent());
		assertEquals(1, sample.get().x());
		assertEquals(0, sample.get().z());
	}

	@Test
	void clampFallsBackWhenRoundingUndershootsMinRange() {
		RtpLocationSampler.HorizontalLocation clamped = RtpLocationSampler.clampToRange(
			0,
			0,
			new RtpLocationSampler.HorizontalLocation(1, 0),
			2.4,
			2.4
		);

		assertEquals(2, clamped.x());
		assertEquals(0, clamped.z());
	}

	@Test
	void clampFallsBackWhenRoundingLeavesTheRequestedBand() {
		RtpLocationSampler.HorizontalLocation clamped = RtpLocationSampler.clampToRange(
			0,
			0,
			new RtpLocationSampler.HorizontalLocation(1, 1),
			2.4,
			2.4
		);

		assertEquals(2, clamped.x());
		assertEquals(0, clamped.z());
	}

	@Test
	void constructorIsHidden() throws Exception {
		var constructor = RtpLocationSampler.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		var thrown = org.junit.jupiter.api.Assertions.assertThrows(
			java.lang.reflect.InvocationTargetException.class,
			constructor::newInstance
		);
		assertEquals(IllegalStateException.class, thrown.getCause().getClass());
	}
}
