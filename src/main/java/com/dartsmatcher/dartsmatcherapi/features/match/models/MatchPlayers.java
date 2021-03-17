package com.dartsmatcher.dartsmatcherapi.features.match.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import javax.validation.Valid;
import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchPlayers {
	// TODO: No duplicate names.
//	@Valid
//	private ArrayList<MatchPlayer<ObjectId>> registered;
//
//	@Valid
//	private ArrayList<MatchPlayer<String>> anonymous;
}
