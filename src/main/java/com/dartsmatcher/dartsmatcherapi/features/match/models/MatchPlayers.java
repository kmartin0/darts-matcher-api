package com.dartsmatcher.dartsmatcherapi.features.match.models;

import com.dartsmatcher.dartsmatcherapi.validators.anonymousname.AnonymousName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchPlayers {
	private ArrayList<ObjectId> registered;

	@AnonymousName
	private ArrayList<String> anonymous;
}
