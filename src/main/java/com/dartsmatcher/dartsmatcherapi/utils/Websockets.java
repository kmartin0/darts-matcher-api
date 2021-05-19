package com.dartsmatcher.dartsmatcherapi.utils;

public class Websockets {

	private Websockets() {
	}

	public static final String X01_MATCH = "/topic/matches/{matchId}";
	public static final String X01_THROW_DART_BOT = "/topic/matches/{matchId}:throw-dart-bot";
	public static final String X01_ADD_THROW = "/topic/matches/{matchId}:update";
	public static final String X01_DELETE_THROW = "/topic/matches/{matchId}:delete-throw";
	public static final String X01_DELETE_SET = "/topic/matches/{matchId}:delete-set";
	public static final String X01_DELETE_LEG = "/topic/matches/{matchId}:delete-leg";

	public static final String ERROR_QUEUE = "/queue/errors";


}
