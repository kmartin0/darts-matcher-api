package com.dartsmatcher.dartsmatcherapi.features.match.models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchPlayerStatistics {
	
	@NotNull
	private String player;

	private int pointsThrown;

	private int dartsThrown;
	
	private int average;
	
	private int tonPlus;
	
	private int tonForty;
	
	private int tonEighty;
	
	private int checkoutHighest;

	private int checkoutTonPlus;

	private int checkoutPercentage;

	private int checkoutsMissed;

	private int checkoutsHit;
	
}
