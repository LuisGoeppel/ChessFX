package model;

public enum ChessWinner {
    WHITE,
    BLACK,
    DRAW,
    STALEMATE,
    NONE;

    public ChessWinner cloneEnum() {
        return ChessWinner.valueOf(this.name());
    }
}
