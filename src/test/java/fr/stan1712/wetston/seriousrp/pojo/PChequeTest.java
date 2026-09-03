package fr.stan1712.wetston.seriousrp.pojo;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PChequeTest {

	@Test
	void constructorCopiesAuthorAndValue() {
		UUID uuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
		Player player = mock(Player.class);
		when(player.getName()).thenReturn("stan");
		when(player.getDisplayName()).thenReturn("Stan");
		when(player.getUniqueId()).thenReturn(uuid);

		PCheque cheque = new PCheque(player, 42.5);

		assertEquals("stan", cheque.getAuthor());
		assertEquals("Stan", cheque.getAuthorDisplayName());
		assertEquals(uuid, cheque.getAuthorUUID());
		assertEquals(42.5, cheque.getValue());
		assertEquals(
			DateTimeFormatter.ofPattern("MM/dd/yyyy @ HH:mm:ss").withZone(ZoneId.systemDefault()).format(cheque.getCreationDate()),
			cheque.getParsedCreationDate()
		);
	}

	@Test
	void epochMillisAdapterRoundTripsInstantAndNull() throws IOException {
		PCheque.EpochMillisInstantAdapter adapter = new PCheque.EpochMillisInstantAdapter();
		Instant instant = Instant.ofEpochMilli(1_700_000_000_000L);

		StringWriter written = new StringWriter();
		try (JsonWriter writer = new JsonWriter(written)) {
			adapter.write(writer, instant);
		}
		assertEquals("1700000000000", written.toString());

		try (JsonReader reader = new JsonReader(new StringReader(written.toString()))) {
			assertEquals(instant, adapter.read(reader));
		}

		StringWriter nullWritten = new StringWriter();
		try (JsonWriter writer = new JsonWriter(nullWritten)) {
			adapter.write(writer, null);
		}
		assertEquals("null", nullWritten.toString());

		try (JsonReader reader = new JsonReader(new StringReader("null"))) {
			assertNull(adapter.read(reader));
		}
	}
}
