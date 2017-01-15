package org.scalasino.roulette;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class Roulette {

    public static class GameCommand implements Serializable {
    }

    public static class Spin extends GameCommand {
        private int[] betPockets;
        private int bet;

        public Spin(int[] betPockets, int bet) {
            this.betPockets = betPockets;
            this.bet = bet;
        }

        public int[] getBetPockets() {
            return betPockets;
        }

        public int getBet() {
            return bet;
        }
    }

    public static class Confirm extends GameCommand {
    }

    public static class GameEvent implements Serializable {
    }

    public static class Spun extends GameEvent {
        private final Outcome outcome;

        public Spun(Outcome outcome) {
            this.outcome = outcome;
        }

        public Outcome getOutcome() {
            return outcome;
        }
    }

    public static class Confirmed extends GameEvent {
    }

    public static class IllegalCommandException extends RuntimeException {
    }

    public static class IllegalEventException extends RuntimeException {
    }

    public interface RouletteState {
        List<GameEvent> handleCommand(GameCommand command) throws IllegalCommandException;

        RouletteState applyEvent(GameEvent event) throws IllegalEventException;
    }

    public static class SpinAwaitingState implements RouletteState {

        private final int roundId;
        private final String gameId;
        private final String playerId;

        public SpinAwaitingState(int roundId, String gameId, String playerId) {
            this.roundId = roundId;
            this.gameId = gameId;
            this.playerId = playerId;
        }

        @Override
        public List<GameEvent> handleCommand(GameCommand command) {
            if (command instanceof Spin) {
                Spin spin = (Spin) command;
                Outcome outcome = calcOutcome(spin.getBetPockets());
                return Collections.singletonList(new Spun(outcome));
            } else
                throw new IllegalCommandException();
        }

        @Override
        public RouletteState applyEvent(GameEvent event) {
            if (event instanceof Spun) {
                Spun spun = (Spun) event;
                return new ConfirmationAwaitingState(roundId, spun.outcome);
            } else
                throw new IllegalEventException();

        }

        private Outcome calcOutcome(int[] pockets) {
            //run rng (thread local rng?)
            throw new RuntimeException("to be implemented");
        }


    }

    public static class ConfirmationAwaitingState implements RouletteState {

        private final int roundId;
        private final Outcome outcome;

        public ConfirmationAwaitingState(int roundId, Outcome outcome) {
            this.roundId = roundId;
            this.outcome = outcome;
        }

        @Override
        public List<GameEvent> handleCommand(GameCommand command) throws IllegalCommandException {
            return null;
        }

        @Override
        public RouletteState applyEvent(GameEvent event) {
            return null;
        }
    }


    private static class Outcome {
        private int betPockets;
        private int bet;
        private int winPocket;
        private int win;

        public Outcome(int betPockets, int bet, int winPocket, int win) {
            this.betPockets = betPockets;
            this.bet = bet;
            this.winPocket = winPocket;
            this.win = win;
        }

        public int getBetPockets() {
            return betPockets;
        }

        public int getBet() {
            return bet;
        }

        public int getWinPocket() {
            return winPocket;
        }

        public int getWin() {
            return win;
        }
    }
}
