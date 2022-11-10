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
    private final ArrayList<Integer> searchList = new ArrayList<>();
    private final boolean silentMode = false;
    private final double variance = 0.2;
    private boolean isAI;
    private Player enemy;
    private int x, y, increment = 1, currShipLength;
    private SearchType searchType = SearchType.RANDOM;
    private double factor1 = 0.4783100925981559;
    private double factor2 = 0.43930534194168797;
    private double factor3 = 1.331990547523071;
    private double factor4 = 0.7235819601067863;

    public Player(boolean isAI) {
        this.isAI = isAI;
        if (isAI) {
            this.name = "Computer";
        } else {
            searchType = SearchType.HUMAN;
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

    public Player(double factor1, double factor2, double factor3, double factor4) {
        this.name = "Computer";
        this.isAI = true;
        this.factor1 = factor1;
        this.factor2 = factor2;
        this.factor3 = factor3;
        this.factor4 = factor4;
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
                System.out.print(board[i][j]);
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

    public void resetEnemy() {
        this.enemy = new Player(true, "Placeholder");
    }

    public void setEnemy(Player enemy) {
        if (this.enemy.isNotReal()) {
            this.enemy = enemy;
            this.enemy.setEnemy(this);
        }
    }

    public String displayHit(int x, int y) {
        try {
            return board[x][y].displayHit();
        } catch (ArrayIndexOutOfBoundsException e) {
            return "X";
        }

    }

    public void addShip(int length) {
        if (!isAI) {
            printBoard();
        }
        ships.add(new Ship(length, board, isAI));
        searchList.add(length);
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
        searchList.clear();
        addShip(6);
        addShips(2, 4);
        addShips(3, 3);
        addShips(4, 2);
        if (aiDrivenDecision) {
            isAI = false;
        }
    }

    public Tile.HitResult processHit(int x, int y) throws TileAlreadyKnownException, ArrayIndexOutOfBoundsException {

        return (board[x][y].processHit(enemy.isAI));
    }

    public void shoot() {
        if (!stillInGame()) {
            return;
        }
        boolean pvp = !isAI && !enemy.isAI;
        Scanner input = new Scanner(System.in);
        if (pvp) {
            System.out.println("\n".repeat(SCREEN_CLEAR));
            System.out.println("It's " + name + "'s turn. (press enter)");
            input.nextLine();
        } else {
            System.out.println("It's " + name + "'s turn.");
        }
        boolean myTurn = true;
        while (myTurn && enemy.stillInGame()) {
            if (isAI) {
                try {
                    switch (searchType) {
                        case HOR_FINDER, DECIDER -> x += increment;
                        case VERT_FINDER -> y += increment;
                        default -> findBestSpot();
                    }
                    Tile.HitResult result = enemy.processHit(x, y);

                    myTurn = result.isHit;
                    reactToHit(result);
                    if (!enemy.stillInGame()) {
                        enemy.printBoard();
                        System.out.println("Computer wins!");
                        return;
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
                        Tile.HitResult result = enemy.processHit(x, y);
                        myTurn = result.isHit;
                        reactToHit(result);
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
        currShipLength++;
        enemy.mark(x - 1, y - 1);
        enemy.mark(x - 1, y + 1);
        enemy.mark(x + 1, y - 1);
        enemy.mark(x + 1, y + 1); //we know there are no ships diagonally adjacent
        switch (searchType) {
            case RANDOM -> searchType = SearchType.DECIDER;
            case DECIDER -> searchType = SearchType.HOR_FINDER;
        }
    }

    public void sunk() {
        hit();
        if (enemy.isMarked(x - 1, y) && enemy.isMarked(x + 1, y)) {
            while (enemy.displayHit(x, y).equals("\u001B[91mX\u001B[0m")) {
                y++;
            }
            enemy.mark(x, y);
            enemy.mark(x, y - (currShipLength + 1));
        } else {
            while (enemy.displayHit(x, y).equals("\u001B[91mX\u001B[0m")) {
                x++;
            }
            enemy.mark(x, y);
            enemy.mark(x - (currShipLength + 1), y);
        }
        resetSearch();
    }

    private void resetSearch() {
        searchList.remove((Integer) currShipLength);
        increment = 1;
        currShipLength = 0;
        searchType = SearchType.RANDOM;
    }

    private void reactToHit(Tile.HitResult result) {
        switch (result) {
            case SPLASH -> noHit();
            case BOOM -> hit();
            case SUNK -> sunk();
        }
    }

    public boolean isMarked(int x, int y) {
        try {
            return board[x][y].isMarked();
        } catch (ArrayIndexOutOfBoundsException e) {
            return true;
        }
    }

    public boolean isKnown(int x, int y) {
        try {
            return board[x][y].isKnown();
        } catch (ArrayIndexOutOfBoundsException e) {
            return true;
        }
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

    public void findBestSpot() {
        double bestScore = 0.0;
        ArrayList<Integer> bestXes = new ArrayList<>();
        ArrayList<Integer> bestYs = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                double score = calculateScore(i, j);
                if (score > bestScore) {
                    bestScore = score;
                    bestXes.clear();
                    bestYs.clear();
                    bestXes.add(i);
                    bestYs.add(j);
                } else if (score == bestScore) {
                    bestXes.add(i);
                    bestYs.add(j);
                }
            }
        }
        if (bestXes.size() > 1) {
            int pick = random.nextInt(bestXes.size());
            x = bestXes.get(pick);
            y = bestYs.get(pick);
        } else {
            x = bestXes.get(0);
            y = bestYs.get(0);
        }
    }

    public double calculateScore(int x, int y) {
        if (enemy.isKnown(x, y)) {
            return -1;
        }
        int north = 0, south = 0, east = 0, west = 0;
        for (int i = 1; i < 10; i++) {
            if (enemy.isMarked(x + i, y)) {
                break;
            }
            east++;
        }
        for (int i = 1; i < 10; i++) {
            if (enemy.isMarked(x - i, y)) {
                break;
            }
            west++;
        }
        for (int i = 1; i < 10; i++) {
            if (enemy.isMarked(x, y + i)) {
                break;
            }
            north++;
        }
        for (int i = 1; i < 10; i++) {
            if (enemy.isMarked(x, y - i)) {
                break;
            }
            south++;
        }
        return factor1 * Math.max(Math.min(north, south), Math.min(east, west))
                + factor2 * (Math.min(north, south) + Math.min(east, west))
                + factor3 * (north + south + east + west)
                + factor4 * random.nextDouble();
    }

    public ArrayList<Player> getChildren(int amount) {
        ArrayList<Player> children = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            children.add(new Player(factor1 + (0.5 - random.nextDouble()) * variance,
                    factor2 + (0.5 - random.nextDouble()) * variance,
                    factor3 + (0.5 - random.nextDouble()) * variance,
                    factor4 + (0.5 - random.nextDouble()) * variance));
        }
        return children;
    }

    public void printFactors() {
        System.out.println("Factor 1: " + factor1);
        System.out.println("Factor 2: " + factor2);
        System.out.println("Factor 3: " + factor3);
        System.out.println("Factor 4: " + factor4);
    }

    public enum SearchType {
        RANDOM, VERT_FINDER, HOR_FINDER, DECIDER, HUMAN
    }
}

class TileAlreadyKnownException extends Exception {
}