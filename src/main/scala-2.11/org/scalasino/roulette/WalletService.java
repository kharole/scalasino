package org.scalasino.roulette;

import scala.concurrent.Future;

/**
 * Created by kharole on 15.01.17.
 */
public interface WalletService {

    Future<String> withdraw(String request);
    Future<String> rollback(String request);
    Future<String> deposit(String request);
}
