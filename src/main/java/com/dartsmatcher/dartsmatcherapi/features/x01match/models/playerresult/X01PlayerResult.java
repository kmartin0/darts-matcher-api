package com.dartsmatcher.dartsmatcherapi.features.x01match.models.playerresult;

import com.dartsmatcher.dartsmatcherapi.features.match.ResultType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01PlayerResult {

	private String playerId;

	private int score;

	private ResultType result;

}
