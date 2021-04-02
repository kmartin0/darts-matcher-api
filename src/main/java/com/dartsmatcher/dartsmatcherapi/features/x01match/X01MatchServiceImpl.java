package com.dartsmatcher.dartsmatcherapi.features.x01match;


import com.dartsmatcher.dartsmatcherapi.exceptionhandler.exception.ForbiddenException;
import com.dartsmatcher.dartsmatcherapi.exceptionhandler.exception.ResourceNotFoundException;
import com.dartsmatcher.dartsmatcherapi.features.user.User;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.X01Match;
import com.dartsmatcher.dartsmatcherapi.features.match.MatchPlayer;
import com.dartsmatcher.dartsmatcherapi.features.match.PlayerType;
import com.dartsmatcher.dartsmatcherapi.features.user.IUserService;
import com.dartsmatcher.dartsmatcherapi.utils.MessageResolver;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class X01MatchServiceImpl implements IX01MatchService {

	private final X01MatchRepository x01MatchRepository;

	private final IUserService userService;

	private final MessageResolver messageResolver;

	public X01MatchServiceImpl(X01MatchRepository x01MatchRepository, IUserService userService, MessageResolver messageResolver) {
		this.x01MatchRepository = x01MatchRepository;
		this.userService = userService;
		this.messageResolver = messageResolver;
	}

	@Override
	public X01Match saveMatch(X01Match x01Match) {
		// Prevent users from assigning an id.
		x01Match.setId(null);

		// Check if players exist
		for (MatchPlayer player : x01Match.getPlayers()) {
			if (player.getPlayerType() == PlayerType.REGISTERED)
				userService.getUser(new ObjectId(player.getPlayerId()));
		}

		// Add statistics and result.
		x01Match.updateResultAndStatisticsAndCurrentThrower();

		return x01MatchRepository.save(x01Match);
	}

	@Override
	public X01Match getMatch(ObjectId matchId) {
		// Find the match.
		return x01MatchRepository.findById(matchId).orElseThrow(() ->
				new ResourceNotFoundException(X01Match.class, matchId));
	}

	@Override
	public ArrayList<X01Match> getAuthenticatedUserMatches() {

		return x01MatchRepository.findAllByRegisteredPlayerId(userService.getAuthenticatedUser().getId());
	}

	@Override
	public X01Match updateMatch(X01Match x01Match, ObjectId matchId) {
		// Check if match exists.
		X01Match matchToUpdate = getMatch(matchId);

		// Set id to new match object.
		x01Match.setId(matchToUpdate.getId());

		// Check if players exist
		for (MatchPlayer player : x01Match.getPlayers()) {
			if (player.getPlayerType() == PlayerType.REGISTERED)
				userService.getUser(new ObjectId(player.getPlayerId()));
		}

		// Add statistics.
		x01Match.updateStatistics();

		return x01MatchRepository.save(x01Match);
	}

	@Override
	public void deleteMatchForAuthenticatedUser(ObjectId matchId) {
		// Find match and authenticated user
		X01Match matchToDelete = getMatch(matchId);
		User user = userService.getAuthenticatedUser();

		// Create new player list removing the user requesting the deletion.
		ArrayList<MatchPlayer> newPlayers = matchToDelete.getPlayers()
				.stream()
				.filter(matchPlayer -> !(
						matchPlayer.getPlayerType() == PlayerType.REGISTERED
								&& matchPlayer.getPlayerObjectId().equals(user.getId()))
				)
				.collect(Collectors.toCollection(ArrayList::new));

		// If the user trying to delete the match was not in the match return Forbidden
		if (newPlayers.size() == matchToDelete.getPlayers().size()) {
			throw new ForbiddenException(messageResolver.getMessage("exception.unauthorized"));
		}

		// If there are no registered players left, delete the match. Otherwise update the match with the new player list.
		Optional<MatchPlayer> registeredPlayerLeft = newPlayers.stream().
				filter(matchPlayer -> matchPlayer.getPlayerType().equals(PlayerType.REGISTERED)).
				findFirst();

		if (registeredPlayerLeft.isPresent()) {
			matchToDelete.setPlayers(newPlayers);
			updateMatch(matchToDelete, matchToDelete.getId());
		} else {
			x01MatchRepository.delete(matchToDelete);
		}
	}
}
