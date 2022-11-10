package com.mennomuller;

import java.util.Scanner;

public class Game {
    private Player player1, player2, winner, loser;

    public Game() {
        boolean undecided = true;
        Scanner input = new Scanner(System.in);
        do {
            System.out.print("Play against AI? ");
            String response = input.next().toUpperCase();
            switch (response) {
                case "YES" -> {
                    player1 = new Player(false);
                    player2 = new Player(true);
                    undecided = false;
                }
                case "NO" -> {
                    player1 = new Player(false);
                    System.out.println("\n".repeat(Player.SCREEN_CLEAR));
                    player2 = new Player(false);
                    undecided = false;
                }
                case "AI_VS_AI" -> {
                    player1 = new Player(true);
                    player2 = new Player(true);
                    undecided = false;
                }
                default -> System.out.println("Please answer YES or NO.");
            }
        } while (undecided);
    }

    public Game(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    public void play() {
        player1.resetEnemy();
        player2.resetEnemy();
        player1.setEnemy(player2);
        do {
            player1.shoot();
            player2.shoot();
        } while (player1.stillInGame() && player2.stillInGame());
        if (player1.stillInGame()) {
            winner = player1;
        } else {
            winner = player2;
        }
    }

    public Player getWinner() {
        return winner;
    }

    public Player getLoser() {
        return loser;
    }
}
