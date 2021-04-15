package com.dartsmatcher.dartsmatcherapi.features.match;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchPlayer {

	@NotNull
	private String playerId;

	@NotNull
	private PlayerType playerType;

	@JsonIgnore
	public ObjectId getPlayerObjectId() {
		if (playerType.equals(PlayerType.REGISTERED) && ObjectId.isValid(playerId)) {
			return new ObjectId(playerId);
		} else {
			return null;
		}
	}

}
