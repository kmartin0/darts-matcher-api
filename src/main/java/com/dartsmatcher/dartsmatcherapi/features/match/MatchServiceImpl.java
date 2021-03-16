package com.dartsmatcher.dartsmatcherapi.features.match;


import com.dartsmatcher.dartsmatcherapi.exceptionhandler.exception.ForbiddenException;
import com.dartsmatcher.dartsmatcherapi.exceptionhandler.exception.ResourceNotFoundException;
import com.dartsmatcher.dartsmatcherapi.features.match.models.Match;
import com.dartsmatcher.dartsmatcherapi.features.user.IUserService;
import com.dartsmatcher.dartsmatcherapi.features.user.User;
import com.dartsmatcher.dartsmatcherapi.utils.MessageResolver;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.ArrayList;

@Service
public class MatchServiceImpl implements IMatchService {

	private final MatchRepository matchRepository;

	private final IUserService userService;

	private final Clock clock;

	private final MessageResolver messageResolver;

	public MatchServiceImpl(MatchRepository matchRepository, IUserService userService, Clock clock, MessageResolver messageResolver) {
		this.matchRepository = matchRepository;
		this.userService = userService;
		this.clock = clock;
		this.messageResolver = messageResolver;
	}

	@Override
	public Match saveMatch(Match match) {
		// Prevent users from assigning an id.
		match.setId(null);

		if (match.getPlayers().getRegistered() != null) {
			// Check if players exist
			for (ObjectId playerId : match.getPlayers().getRegistered()) {
				userService.getUser(playerId);
			}
		}

		// Add statistics and result.
		match.updateResultAndStatistics(clock);

		return matchRepository.save(match);
	}

	@Override
	public Match getMatch(ObjectId matchId) {

		// Find the match.
		return matchRepository.findById(matchId).orElseThrow(() ->
				new ResourceNotFoundException(Match.class, matchId));

//		 Find the match.
//		Match match = matchRepository.findById(matchId).orElseThrow(() ->
//				new ResourceNotFoundException(Match.class, matchId));
//
		// Check if user has authorization to view this match.
//		boolean hasPermission = match.getPlayers().getRegistered().stream()
//				.anyMatch(objectId -> objectId.equals(userService.getAuthenticatedUser().getId()));

//		if (hasPermission) return match;
//		else throw new ForbiddenException(messageResolver.getMessage("exception.unauthorized"));
	}

	@Override
	public ArrayList<Match> getAuthenticatedUserMatches() {
		return matchRepository.findAllByPlayersRegisteredContaining(userService.getAuthenticatedUser().getId());
	}

	@Override
	public Match updateMatch(Match match, ObjectId matchId) {

		// Check if match exists.
		Match matchToUpdate = getMatch(matchId);

		// Set id to new match object.
		match.setId(matchToUpdate.getId());

		if (match.getPlayers().getRegistered() != null) {
			// Check if players exist
			for (ObjectId playerId : match.getPlayers().getRegistered()) {
				userService.getUser(playerId);
			}
		}

		// Add statistics and result.
		match.updateResultAndStatistics(clock);

		return matchRepository.save(match);
	}

	@Override
	public void deleteMatchForAuthenticatedUser(ObjectId matchId) {
		// Find match and authenticated user
		Match matchToDelete = getMatch(matchId);
		User user = userService.getAuthenticatedUser();

		// If there are more players in the match only remove the authenticated player. Otherwise remove the match.
		if (matchToDelete.getPlayers().getRegistered().size() > 1) {
			matchToDelete.getPlayers().getRegistered().remove(user.getId());
			updateMatch(matchToDelete, matchToDelete.getId());
		} else matchRepository.delete(matchToDelete);
	}
}
