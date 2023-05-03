package model;

public class ChessPiece {
    private final ChessPieceType piece;
    private final ChessPieceColor color;

    public ChessPiece(ChessPieceType piece, ChessPieceColor color) {
        if (piece.equals(ChessPieceType.EMPTY) && !color.equals(ChessPieceColor.EMPTY) ||
                !piece.equals(ChessPieceType.EMPTY) && color.equals(ChessPieceColor.EMPTY)) {
            throw new IllegalArgumentException("If the Piece is empty both Params should be EMPTY");
        }
        this.piece = piece;
        this.color = color;
    }

    public ChessPiece() {
        this(ChessPieceType.EMPTY, ChessPieceColor.EMPTY);
    }

    public ChessPieceType getPieceType() {
        return piece;
    }

    public ChessPieceColor getColor() {
        return color;
    }

    public boolean isEnemyColorTo(ChessPieceColor color) {
        return (this.color.equals(ChessPieceColor.BLACK) && color.equals(ChessPieceColor.WHITE)) ||
                (this.color.equals(ChessPieceColor.WHITE) && color.equals(ChessPieceColor.BLACK));
    }

    public boolean hasPiece() {
        return !piece.equals(ChessPieceType.EMPTY);
    }

    public char getAsChar() {
        switch (piece) {
            case PAWN: return color.equals(ChessPieceColor.WHITE) ? 'P' : 'p';
            case KNIGHT: return color.equals(ChessPieceColor.WHITE) ? 'N' : 'n';
            case BISHOP: return color.equals(ChessPieceColor.WHITE) ? 'B' : 'b';
            case ROOK: return color.equals(ChessPieceColor.WHITE) ? 'R' : 'r';
            case QUEEN: return color.equals(ChessPieceColor.WHITE) ? 'Q' : 'q';
            case KING: return color.equals(ChessPieceColor.WHITE) ? 'K' : 'k';
            default: return 'E';
        }
    }

    @Override
    public ChessPiece clone() {
        return new ChessPiece(piece, color);
    }

    @Override
    public String toString() {
        return piece.toString() + color.toString();
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) {
            return false;
        }
        if (!(rhs instanceof ChessPiece)) {
            return false;
        }
        ChessPiece rhsPiece = (ChessPiece) (rhs);
        return piece.equals(rhsPiece.piece) && color.equals(rhsPiece.color);
    }
}
