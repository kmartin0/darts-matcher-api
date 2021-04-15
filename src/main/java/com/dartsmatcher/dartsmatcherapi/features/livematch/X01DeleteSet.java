package com.dartsmatcher.dartsmatcherapi.features.livematch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
// TODO: Add PlayerId so that not everyone can simply delete a set.
public class X01DeleteSet {

	private ObjectId matchId;

//	@NotEmpty
	private String playerId;

	@Min(0)
	private int set;

}
