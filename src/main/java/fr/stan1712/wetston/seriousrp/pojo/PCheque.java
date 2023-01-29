package fr.stan1712.wetston.seriousrp.pojo;

import lombok.Getter;
import lombok.Setter;

public class PCheque {
	@Getter @Setter private String author;
	@Getter @Setter private Double value;

	public PCheque(String author, Double value) {
		this.author = author;
		this.value = value;
	}
}
