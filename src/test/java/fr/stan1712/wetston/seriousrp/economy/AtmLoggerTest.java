package fr.stan1712.wetston.seriousrp.economy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AtmLoggerTest {

	@TempDir
	Path tempDir;

	@Test
	void appendsLinesToLogFile() throws Exception {
		Path file = tempDir.resolve("logs").resolve("atm.log");
		AtmLogger logger = new AtmLogger(file);
		logger.log("OPEN", "player=alice");
		logger.log("DEPOSIT", "amount=10");
		String content = Files.readString(file);
		assertTrue(content.contains("OPEN player=alice"));
		assertTrue(content.contains("DEPOSIT amount=10"));
	}

	@Test
	void recentLinesKeepCassetteMovementsNewestFirst() throws Exception {
		Path file = tempDir.resolve("logs").resolve("atm.log");
		AtmLogger logger = new AtmLogger(file);
		logger.log("OPEN", "player=alice");
		logger.log("CASSETTE_FILL", "player=alice cassette=#f4de5 amount=10");
		logger.log("DEPOSIT", "player=bob cassettes=#aaaa1 amount=5");
		logger.log("CASSETTE_EMPTY", "player=alice cassette=#bbbb2 amount=4");

		List<String> all = logger.recentCassetteLines(10, null);
		assertEquals(3, all.size());
		assertTrue(all.get(0).contains("CASSETTE_EMPTY"));
		assertTrue(all.get(2).contains("CASSETTE_FILL"));

		List<String> one = logger.recentCassetteLines(10, "#f4de5");
		assertEquals(1, one.size());
		assertTrue(one.get(0).contains("cassette=#f4de5"));
		assertEquals("§eCASSETTE_FILL", CassetteLog.actionName(one.get(0)));
		assertTrue(CassetteLog.loreLines(one.get(0)).stream().anyMatch(line -> line.contains("cassette=#f4de5")));
	}
}
