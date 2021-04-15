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
// TODO: Add PlayerId so that not everyone can simply delete a leg.
public class X01DeleteLeg {

	private ObjectId matchId;

	@Min(0)
	private int leg;

}
