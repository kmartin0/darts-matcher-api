package com.dartsmatcher.dartsmatcherapi.features.x01.x01match.models.playerresult;

import com.dartsmatcher.dartsmatcherapi.features.basematch.ResultType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01PlayerResult {

	private String playerId;

	private int legsWon;

	private int setsWon;

	private ResultType result;

}
