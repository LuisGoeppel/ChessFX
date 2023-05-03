package model;

public class Move {
    public ChessPos from;
    public ChessPos to;

    public Move(ChessPos from, ChessPos to) {
        this.from = from;
        this.to = to;
    }

    public Move(String pFrom, String pTo) {
        from = new ChessPos(pFrom);
        to = new ChessPos(pTo);
    }

    @Override
    public String toString() {
        return from + " " + to;
    }
}
