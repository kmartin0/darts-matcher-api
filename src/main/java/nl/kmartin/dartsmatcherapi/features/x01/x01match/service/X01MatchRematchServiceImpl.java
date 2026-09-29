package nl.kmartin.dartsmatcherapi.features.x01.x01match.service;

import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.repository.IX01MatchRepository;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

/**
 * Resolves or initializes rematches and clears invalid or incoming rematch references.
 *
 * Persistence and broadcasting are handled by the coordinating match service.
 */
@Service
@Validated
public class X01MatchRematchServiceImpl implements IX01MatchRematchService {

    private final IX01MatchRepository matchRepository;
    private final IX01MatchSetupService matchSetupService;

    public X01MatchRematchServiceImpl(
            IX01MatchRepository matchRepository,
            IX01MatchSetupService matchSetupService
    ) {
        this.matchRepository = matchRepository;
        this.matchSetupService = matchSetupService;
    }

    @Override
    public X01Match getOrCreateRematch(X01Match match) {
        return Optional.ofNullable(match.getRematchId())
                .flatMap(matchRepository::findById)
                .orElseGet(() -> {
                    match.setRematchId(null);
                    return matchSetupService.initializeRematch(match);
                });
    }

    @Override
    public void validateAndUpdateRematchId(X01Match match) {
        ObjectId rematchId = match.getRematchId();
        if (rematchId == null) {
            return;
        }

        if (!matchRepository.existsById(rematchId)) {
            match.setRematchId(null);
        }
    }

    @Override
    public List<X01Match> clearRematchReferencesToMatch(ObjectId matchId) {
        List<X01Match> matches = matchRepository.findAllByRematchId(matchId);

        matches.forEach(match -> match.setRematchId(null));

        return matches;
    }
}
