package com.mennomuller;

import java.util.ArrayList;
import java.util.Collections;

public class Evolver {
    ArrayList<Player> genePool;
    Game game;

    public Evolver(Player player) {
        genePool = player.getChildren(10);
        for (int i = 0; i < 20000; i++) {
            Collections.shuffle(genePool);
            game = new Game(genePool.get(0), genePool.get(1));
            game.play();
            genePool.remove(game.getLoser());
            genePool.addAll(game.getWinner().getChildren(2));
        }
        Collections.shuffle(genePool);
        genePool.get(0).printFactors();
    }
}
