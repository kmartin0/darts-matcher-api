package nl.kmartin.dartsmatcherapi.features.x01.x01match.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.kmartin.dartsmatcherapi.features.x01.model.X01Match;
import org.bson.types.ObjectId;

public sealed interface X01MatchEvent {

    @JsonProperty("eventType")
    X01MatchEventType eventType();

    @JsonIgnore
    ObjectId getMatchId();

    record ProcessMatch(X01Match payload) implements X01MatchEvent {
        @Override
        public X01MatchEventType eventType() {
            return X01MatchEventType.PROCESS_MATCH;
        }

        @Override
        public ObjectId getMatchId() {
            return payload.getId();
        }
    }

    record AddHumanTurn(X01Match payload) implements X01MatchEvent {
        @Override
        public X01MatchEventType eventType() {
            return X01MatchEventType.ADD_HUMAN_TURN;
        }

        @Override
        public ObjectId getMatchId() {
            return payload.getId();
        }
    }

    record AddBotTurn(X01Match payload) implements X01MatchEvent {
        @Override
        public X01MatchEventType eventType() {
            return X01MatchEventType.ADD_BOT_TURN;
        }

        @Override
        public ObjectId getMatchId() {
            return payload.getId();
        }
    }

    record EditTurn(X01Match payload) implements X01MatchEvent {
        @Override
        public X01MatchEventType eventType() {
            return X01MatchEventType.EDIT_TURN;
        }

        @Override
        public ObjectId getMatchId() {
            return payload.getId();
        }
    }

    record DeleteLastTurn(X01Match payload) implements X01MatchEvent {
        @Override
        public X01MatchEventType eventType() {
            return X01MatchEventType.DELETE_LAST_TURN;
        }

        @Override
        public ObjectId getMatchId() {
            return payload.getId();
        }
    }

    record DeleteMatch(ObjectId payload) implements X01MatchEvent {
        @Override
        public X01MatchEventType eventType() {
            return X01MatchEventType.DELETE_MATCH;
        }

        @Override
        public ObjectId getMatchId() {
            return payload;
        }
    }

    record ResetMatch(X01Match payload) implements X01MatchEvent {
        @Override
        public X01MatchEventType eventType() {
            return X01MatchEventType.RESET_MATCH;
        }

        @Override
        public ObjectId getMatchId() {
            return payload.getId();
        }
    }
}