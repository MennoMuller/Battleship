package com.mennomuller;

public class Tile {
    private final int x;
    private final int y;
    private boolean isHit = false;
    private Ship shipHere = null;

    public Tile(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean isOccupied() {
        return shipHere != null;
    }

    public Ship getShipHere() {
        return shipHere;
    }

    public void addShip(Ship s) {
        shipHere = s;
    }

    @Override
    public String toString() {
        if (shipHere != null) {
            if (isHit) {
                return "\u001B[91m#\u001B[0m";//red hashtag
            } else {
                return "#";
            }
        } else {
            if (isHit) {
                return "X";
            } else {
                return "\u001B[94m~\u001B[0m";//blue tilde
            }
        }
    }

    public String displayHit() {
        if (isHit) {
            if (shipHere == null) {
                return "X";
            } else {
                return "\u001B[91mX\u001B[0m";//red X
            }
        } else {
            return ".";
        }
    }

    public boolean processHit() throws TileAlreadyHitException {
        if (isHit) {
            throw new TileAlreadyHitException();
        }
        System.out.println("Fired at " + (char) ('A' + y) + x);
        isHit = true;
        if (isOccupied()) {
            shipHere.getHit();
            return true;
        } else {
            System.out.println("SPLASH!");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return false;
        }
    }
}
