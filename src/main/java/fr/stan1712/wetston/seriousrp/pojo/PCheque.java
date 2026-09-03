package fr.stan1712.wetston.seriousrp.pojo;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class PCheque {
	private static final DateTimeFormatter CREATION_DATE_FORMATTER =
		DateTimeFormatter.ofPattern("MM/dd/yyyy @ HH:mm:ss").withZone(ZoneId.systemDefault());

	@Getter @Setter private String author;
	@Getter @Setter private UUID authorUUID;
	@Getter @Setter private String authorDisplayName;
	@Getter @Setter private Double value;
	@Getter @Setter
	@JsonAdapter(EpochMillisInstantAdapter.class)
	private Instant creationDate;

	public PCheque(Player author, Double value) {
		this.author = author.getName();
		this.authorDisplayName = author.getDisplayName();
		this.authorUUID = author.getUniqueId();

		this.value = value;
		this.creationDate = Instant.now();
	}

	public String getParsedCreationDate() {
		return CREATION_DATE_FORMATTER.format(this.creationDate);
	}

	public static final class EpochMillisInstantAdapter extends TypeAdapter<Instant> {
		@Override
		public void write(JsonWriter out, Instant value) throws IOException {
			if (value == null) {
				out.nullValue();
				return;
			}
			out.value(value.toEpochMilli());
		}

		@Override
		public Instant read(JsonReader in) throws IOException {
			if (in.peek() == JsonToken.NULL) {
				in.nextNull();
				return null;
			}
			return Instant.ofEpochMilli(in.nextLong());
		}
	}
}
