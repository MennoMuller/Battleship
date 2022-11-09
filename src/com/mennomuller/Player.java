package com.mennomuller;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Player {
    public static final int SCREEN_CLEAR = 40;
    private final Tile[][] board = new Tile[10][10];
    private final ArrayList<Ship> ships = new ArrayList<>();
    private final String name;
    private final Random random = new Random();
    private boolean isAI;
    private Player enemy;
    private int x, y, increment = 1;
    private SearchType searchType = SearchType.RANDOM;

    public Player(boolean isAI) {
        this.isAI = isAI;
        if (isAI) {
            this.name = "Computer";
        } else {
            System.out.print("Input name: ");
            this.name = new Scanner(System.in).nextLine();
        }
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                board[i][j] = new Tile(i, j);
            }
        }
        this.enemy = new Player(true, "Placeholder");
        addShips();
    }

    public Player(boolean isAI, String name) {
        this.isAI = isAI;
        this.name = name;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                board[i][j] = new Tile(i, j);
            }
        }
    }

    public void printBoard() {
        System.out.println("  A B C D E F G H I J  ".repeat(2));
        for (int i = 0; i < 10; i++) {
            System.out.print(i);
            for (int j = 0; j < 10; j++) {
                System.out.print(" " + board[i][j]);
            }
            System.out.print("  " + i);
            for (int j = 0; j < 10; j++) {
                System.out.print(" " + enemy.displayHit(i, j));
            }
            System.out.println();
        }
    }

    public boolean isNotReal() {
        return name.equals("Placeholder");
    }

    public void setEnemy(Player enemy) {
        if (this.enemy.isNotReal()) {
            this.enemy = enemy;
            this.enemy.setEnemy(this);
        }
    }

    public String displayHit(int x, int y) {
        return board[x][y].displayHit();
    }

    public void addShip(int length) {
        if (!isAI) {
            printBoard();
        }
        ships.add(new Ship(length, board, isAI));
    }

    public void addShips(int count, int length) {
        for (int i = 0; i < count; i++) {
            addShip(length);
        }
    }

    public void addShips() {
        boolean aiDrivenDecision = false;
        if (!isAI) {
            boolean undecided = true;
            Scanner input = new Scanner(System.in);
            do {
                System.out.print("Randomize ship locations? ");
                String response = input.next().toUpperCase();
                if (response.equals("YES")) {
                    aiDrivenDecision = true;
                    isAI = true;
                    undecided = false;
                } else if (response.equals("NO")) {
                    undecided = false;
                } else {
                    System.out.println("Please answer YES or NO.");
                }
            } while (undecided);
        }
        ships.clear();
        addShip(6);
        addShips(2, 4);
        addShips(3, 3);
        addShips(4, 2);
        if (aiDrivenDecision) {
            isAI = false;
        }
    }

    public boolean processHit(int x, int y) throws TileAlreadyKnownException, ArrayIndexOutOfBoundsException {
        return (board[x][y].processHit(isAI));
    }

    public void shoot() {
        if (!stillInGame()) {
            return;
        }
        boolean pvp = !isAI && !enemy.isAI;
        Scanner input = new Scanner(System.in);
        if (pvp) {
            System.out.println("\n".repeat(SCREEN_CLEAR));
            System.out.println(name + "'s turn. (press enter)");
            input.nextLine();
        } else {
            System.out.println(name + "'s turn.");
        }
        boolean myTurn = true;
        while (myTurn && enemy.stillInGame()) {
            if (isAI) {
                try {
                    switch (searchType) {
                        case HOR_FINDER, DECIDER -> x += increment;
                        case VERT_FINDER -> y += increment;
                        default -> {
                            x = random.nextInt(10);
                            y = random.nextInt(10);
                        }
                    }
                    myTurn = enemy.processHit(x, y);
                    if (!enemy.stillInGame()) {
                        System.out.println("Computer wins!");
                        return;
                    }
                    if (myTurn) {
                        hit();
                    } else {
                        noHit();
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    noHit();
                } catch (TileAlreadyKnownException e) {
                    if (enemy.isMarked(x, y)) {
                        noHit();
                    }
                }
            } else {
                printBoard();
                try {
                    System.out.print("Shoot where? ");
                    String coords = input.next().toUpperCase();
                    x = coords.charAt(1) - '0';
                    y = coords.charAt(0) - 'A';
                    if (x > 9 || x < 0 || y > 9 || y < 0) {
                        throw new Exception();
                    } else {
                        myTurn = enemy.processHit(x, y);
                        if (myTurn) {
                            hit();
                        }
                        if (!enemy.stillInGame()) {
                            printBoard();
                            System.out.println("Congratulations! You win, " + name + "!");
                            return;
                        }
                    }
                } catch (TileAlreadyKnownException e) {
                    System.out.println("You already shot there!");
                } catch (Exception e) {
                    System.out.println("Invalid coordinates.");
                }
            }
        }
    }

    private void noHit() {
        switch (searchType) {
            case DECIDER -> {
                if (increment == 1) {
                    increment = -1;
                } else {
                    x = x + 1;
                    increment = 1;
                    searchType = SearchType.VERT_FINDER;
                }
            }
            case VERT_FINDER, HOR_FINDER -> {
                if (increment == 1) {
                    increment = -1;
                } else {
                    increment = 1;
                    searchType = SearchType.RANDOM;
                }
            }
        }
    }

    public void hit() {
        enemy.mark(x - 1, y - 1);
        enemy.mark(x - 1, y + 1);
        enemy.mark(x + 1, y - 1);
        enemy.mark(x + 1, y + 1); //we know there are no ships diagonally adjacent
        switch (searchType) {
            case RANDOM -> searchType = SearchType.DECIDER;
            case DECIDER -> searchType = SearchType.HOR_FINDER;
        }
    }

    public boolean isMarked(int x, int y) {
        return board[x][y].isMarked();
    }

    public void mark(int x, int y) {
        try {
            board[x][y].mark();
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }
    }

    public boolean stillInGame() {
        for (Ship s : ships) {
            if (!s.isSunk()) {
                return true;
            }
        }
        return false;
    }

    public enum SearchType {
        RANDOM, VERT_FINDER, HOR_FINDER, DECIDER
    }
}

class TileAlreadyKnownException extends Exception {
}