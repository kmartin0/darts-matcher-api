package nl.kmartin.dartsmatcherapi.features.x01.x01match.message;

import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;
import nl.kmartin.dartsmatcherapi.websocket.message.model.WebSocketMessage;
import org.bson.types.ObjectId;

/**
 * Defines the supported X01 match messages and their payload types.
 *
 * Each message fixes its message type and accepts the corresponding payload type.
 *
 * @param <P> the message payload type
 */
public sealed interface X01MatchMessage<P>
        extends WebSocketMessage<X01MatchMessageType, P>
        permits X01MatchMessage.ProcessMatch,
        X01MatchMessage.AddHumanTurn,
        X01MatchMessage.AddBotTurn,
        X01MatchMessage.EditTurn,
        X01MatchMessage.DeleteLastTurn,
        X01MatchMessage.DeleteMatch,
        X01MatchMessage.ResetMatch,
        X01MatchMessage.Rematch {

    /**
     * Contains the current or reprocessed match state.
     *
     * @param payload the match
     */
    record ProcessMatch(X01Match payload) implements X01MatchMessage<X01Match> {

        @Override
        public X01MatchMessageType messageType() {
            return X01MatchMessageType.PROCESS_MATCH;
        }
    }

    /**
     * Contains the match state after a human turn.
     *
     * @param payload the updated match
     */
    record AddHumanTurn(X01Match payload) implements X01MatchMessage<X01Match> {

        @Override
        public X01MatchMessageType messageType() {
            return X01MatchMessageType.ADD_HUMAN_TURN;
        }
    }

    /**
     * Contains the match state after a Dart Bot turn.
     *
     * @param payload the updated match
     */
    record AddBotTurn(X01Match payload) implements X01MatchMessage<X01Match> {

        @Override
        public X01MatchMessageType messageType() {
            return X01MatchMessageType.ADD_BOT_TURN;
        }
    }

    /**
     * Contains the match state after an existing turn is edited.
     *
     * @param payload the updated match
     */
    record EditTurn(X01Match payload) implements X01MatchMessage<X01Match> {

        @Override
        public X01MatchMessageType messageType() {
            return X01MatchMessageType.EDIT_TURN;
        }
    }

    /**
     * Contains the match state after deleting the last human turn and any following Dart Bot turns.
     *
     * @param payload the updated match
     */
    record DeleteLastTurn(X01Match payload) implements X01MatchMessage<X01Match> {

        @Override
        public X01MatchMessageType messageType() {
            return X01MatchMessageType.DELETE_LAST_TURN;
        }
    }

    /**
     * Identifies a deleted match.
     *
     * @param payload the deleted match id
     */
    record DeleteMatch(ObjectId payload) implements X01MatchMessage<ObjectId> {

        @Override
        public X01MatchMessageType messageType() {
            return X01MatchMessageType.DELETE_MATCH;
        }
    }

    /**
     * Contains the match state after a reset.
     *
     * @param payload the reset match
     */
    record ResetMatch(X01Match payload) implements X01MatchMessage<X01Match> {

        @Override
        public X01MatchMessageType messageType() {
            return X01MatchMessageType.RESET_MATCH;
        }
    }

    /**
     * Contains the original match with its rematch reference for broadcasts,
     * or the existing or newly created rematch for direct responses.
     *
     * @param payload the match appropriate to the response or broadcast
     */
    record Rematch(X01Match payload) implements X01MatchMessage<X01Match> {

        @Override
        public X01MatchMessageType messageType() {
            return X01MatchMessageType.REMATCH;
        }
    }
}