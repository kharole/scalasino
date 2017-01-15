package org.scalasino.roulette;

import akka.persistence.UntypedPersistentActor;

/**
 * Created by kharole on 15.01.17.
 */
public class RouletteActor extends UntypedPersistentActor {

    private String playerId;
    private String gameId;

    private int roundId;

    @Override
    public String persistenceId() {
        return "roulette-" + playerId + "-" + gameId;
    }

    @Override
    public void onReceiveRecover(Object msg) throws Exception {

    }

    @Override
    public void onReceiveCommand(Object msg) throws Exception {

    }
}
