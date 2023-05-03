package model;

public enum ChessPieceColor {
    WHITE,
    BLACK,
    EMPTY;

    public ChessPieceColor cloneEnum() {
        return ChessPieceColor.valueOf(this.name());
    }
}
