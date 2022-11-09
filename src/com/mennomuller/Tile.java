package com.mennomuller;

public class Tile {
    public static final boolean DISPLAY_MARKS = true;
    public static final String MARKED_SEA = " o";
    private final int x;
    private final int y;
    private boolean isHit = false, isMarked = false;
    private Ship shipHere = null;
    private Section mySection = Section.SEA;

    public Tile(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean isOccupied() {
        return shipHere != null;
    }

    public boolean isMarked() {
        return isMarked;
    }

    public void mark() {
        isMarked = true;
    }

    public void addShip(Ship ship, Section section) {
        shipHere = ship;
        mySection = section;
    }

    @Override
    public String toString() {

        if (isHit) {
            return mySection.hitLook;
        } else {
            if (isMarked && DISPLAY_MARKS) {
                return MARKED_SEA;
            }
            return mySection.defaultLook;
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
            if (isMarked && DISPLAY_MARKS) {
                return "o";
            }
            return "\u001B[1;90m.\u001B[0m";
        }
    }

    public HitResult processHit(boolean isAI) throws TileAlreadyKnownException {
        if (isHit || (isMarked && isAI)) {
            throw new TileAlreadyKnownException();
        }
        System.out.println("Fired at " + (char) ('A' + y) + x);
        isHit = true;
        if (isOccupied()) {
            return shipHere.getHit();
        } else {
            System.out.println("SPLASH!");
            mark();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return HitResult.SPLASH;
        }
    }

    public enum HitResult {
        BOOM(true),
        SPLASH(false),
        SUNK(true);
        public final boolean isHit;

        HitResult(boolean isHit) {
            this.isHit = isHit;
        }
    }

    public enum Section {
        SEA("\u001B[1;94m ~\u001B[0m", "\u001B[1;97m X\u001B[0m"),
        TOP_END(" \u001B[1;4;37mA\u001B[0m", "\u001B[1;91m X\u001B[0m"),
        BOTTOM_END(" \u001B[1;53;37mV\u001B[0m", "\u001B[1;91m X\u001B[0m"),
        LEFT_END("\u001B[1;37m <\u001B[0m", "\u001B[1;91m X\u001B[0m"),
        RIGHT_END("\u001B[1;37mE>\u001B[0m", "\u001B[1;37mE\u001B[91mX\u001B[0m"),
        VERTICAL(" \u001B[1;51;37mH\u001B[0m", " \u001B[1;91mX\u001B[0m"),
        HORIZONTAL("\u001B[1;37mEE\u001B[0m", "\u001B[1;37mE\u001B[91mX\u001B[0m");
        public final String defaultLook, hitLook;

        Section(String defaultLook, String hitLook) {
            this.defaultLook = defaultLook;
            this.hitLook = hitLook;
        }
    }
}
