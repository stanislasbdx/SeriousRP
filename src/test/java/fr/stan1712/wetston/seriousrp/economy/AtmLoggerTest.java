package fr.stan1712.wetston.seriousrp.economy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

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
}
