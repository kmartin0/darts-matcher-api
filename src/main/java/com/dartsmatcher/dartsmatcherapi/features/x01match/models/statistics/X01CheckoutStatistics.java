package com.dartsmatcher.dartsmatcherapi.features.x01match.models.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01CheckoutStatistics {

	private int checkoutHighest;

	private int checkoutTonPlus;

	private int checkoutPercentage;

	private int checkoutsMissed;

	private int checkoutsHit;

}
