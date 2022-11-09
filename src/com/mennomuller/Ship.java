package com.mennomuller;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Ship {
    int length, health;
    boolean isVertical;
    ArrayList<Tile> location = new ArrayList<>();

    public Ship(int length, Tile[][] board, boolean isAI) {
        this.length = length;
        health = length;
        boolean isUnset = true;
        while (isUnset) {
            try {
                setLocation(board, isAI);
                isUnset = false;
            } catch (BoatCollisionException e) {
                if (!isAI) {
                    System.out.println("Too close to another ship.");
                }
            } catch (IndexOutOfBoundsException e) {
                if (!isAI) {
                    System.out.println("Can't place outside the board.");
                }
            }
        }

    }

    public void setLocation(Tile[][] board, boolean isAI) throws BoatCollisionException, IndexOutOfBoundsException {
        location.clear();
        int x = -1, y = -1;
        Random random = new Random();
        Scanner input = new Scanner(System.in);
        if (isAI) {
            x = random.nextInt(10);
            y = random.nextInt(10);
        } else {
            System.out.println("Setting the location of a ship of length " + length + ".");
            boolean coordsUnset = true;
            do {
                try {
                    System.out.print("Please give the location of the upper left corner: ");
                    String coords = input.next().toUpperCase();
                    x = coords.charAt(1) - '0';
                    y = coords.charAt(0) - 'A';
                    if (x > 9 || x < 0 || y > 9 || y < 0) {
                        throw new Exception();
                    } else {
                        coordsUnset = false;
                    }
                } catch (Exception e) {
                    System.out.println("Invalid coordinates.");
                }
            } while (coordsUnset);
        }
        addTile(x, y, board);
        if (isAI) {
            isVertical = random.nextBoolean();
        } else {
            boolean directionUnset = true;
            do {
                System.out.print("Place vertically? ");
                String response = input.next().toUpperCase();
                if (response.equals("YES")) {
                    isVertical = true;
                    directionUnset = false;
                } else if (response.equals("NO")) {
                    isVertical = false;
                    directionUnset = false;
                } else {
                    System.out.println("Please answer YES or NO.");
                }
            } while (directionUnset);
        }
        if (!isVertical) {
            for (int i = 1; i < length; i++) {
                addTile(x, y + i, board);
            }
        } else {
            for (int i = 1; i < length; i++) {
                addTile(x + i, y, board);
            }
        }
        for (Tile t : location) {
            t.addShip(this);
        }
    }

    private void addTile(int x, int y, Tile[][] board) throws BoatCollisionException, IndexOutOfBoundsException {
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                if (i >= 0 && i < 10 && j >= 0 && j < 10 && board[i][j].isOccupied()) {
                    throw new BoatCollisionException();
                }
            }
        }
        location.add(board[x][y]);
    }

    public void getHit() {
        System.out.println("BOOM!");
        if (--health <= 0) {
            System.out.println("Sunk a ship!");
        }
    }

    public boolean isSunk() {
        return health <= 0;
    }
}

/*
A
#
#
V ~ ~ < # # # # >
 */

class BoatCollisionException extends Exception {
}