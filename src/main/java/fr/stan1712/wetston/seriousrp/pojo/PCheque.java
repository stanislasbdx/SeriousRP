package fr.stan1712.wetston.seriousrp.pojo;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class PCheque {
	@Getter @Setter private String author;
	@Getter @Setter private UUID authorUUID;
	@Getter @Setter private String authorDisplayName;
	@Getter @Setter private Double value;
	@Getter @Setter private Date creationDate;

	public PCheque(Player author, Double value) {
		this.author = author.getName();
		this.authorDisplayName = author.getDisplayName();
		this.authorUUID = author.getUniqueId();

		this.value = value;
		this.creationDate = new Date();
	}

	public String getParsedCreationDate() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy @ HH:mm:ss");

		return dateFormat.format(this.creationDate);
	}
}
