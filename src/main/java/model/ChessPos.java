package model;

/**
 * Represents a position on a chess board. The rows are represented with
 * decreasing numbers from 8 to 1 (going from top to bottom), the columns are represented
 * with increasing numbers from 0 to 7 (going from left to right) where 0 would correspond
 * to 'a', etc...
 */
public class ChessPos {
    private final int row;
    private final int column;
    private ChessPieceType promoteTo;

    public ChessPos(char column, int row) {
        if (row <= 0 || row > 8) {
            throw new IllegalArgumentException("That row doesn't exist!");
        }
        if (Character.toLowerCase(column) - 'a' < 0 || Character.toLowerCase(column) - 'a' >= 8) {
            throw new IllegalArgumentException("That column doesn't exist!");
        }
        this.row = row;
        this.column = toInt(column);
        this.promoteTo = ChessPieceType.EMPTY;
   }

    public ChessPos(int x, int y) {
        this(x, y, ChessPieceType.EMPTY);
    }

    public ChessPos(int x, int y, ChessPieceType promoteTo) {
        if (y < 0 || y >= 8 || x < 0 || x > 8) {

            throw new IllegalArgumentException("Values have to be in the range 0 - 7 (inclusive)!" +
                    " Actual Values: " + x + ", " + y);
        }
        row = 8 - y;
        column = x;
        this.promoteTo = promoteTo;
    }

    public ChessPos(String input) {
        this(input, ChessPieceType.EMPTY);
    }

    public ChessPos(String input, ChessPieceType promoteTo) {
        if (input.length() != 2) {
            throw new IllegalArgumentException("Input has the wrong length!");
        }
        try {
            int row = Integer.parseInt(String.valueOf(input.charAt(1)));
            int column = toInt(input.charAt(0));
            if (row <= 0 || row > 8) {
                throw new IllegalArgumentException("That row doesn't exist!");
            }
            if (column < 0 || column >= 8) {
                throw new IllegalArgumentException("That column doesn't exist!");
            }
            this.row = row;
            this.column = column;
            this.promoteTo = promoteTo;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Wrong Format!");
        }
    }

    public void addPromoteTo (ChessPieceType promoteTo) {
        this.promoteTo = promoteTo;
    }
    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }
    public int[] getAsCoordinates() {
        int[] output = new int[2];
        output[0] = column;       //x-Coordinate
        output[1] = 8 - row;      //y-Coordinate
        return output;
    }

    private int toInt(char c) {
        return Character.toLowerCase(c) - 'a';
    }

    public boolean hasPromotionInput() {
        return !promoteTo.equals(ChessPieceType.EMPTY);
    }

    public ChessPieceType getPromoteTo() {
        return promoteTo;
    }

    @Override
    public ChessPos clone() {
        return new ChessPos(column, 8 - row, promoteTo.cloneEnum());
    }

    @Override
    public String toString() {
        char columnChar = (char) ('a' + column);
        if (hasPromotionInput()) {
            return String.valueOf(columnChar) + row + "=" + promoteTo.getPieceChar();
        }
        return String.valueOf(columnChar) + row;
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) {
            return false;
        }
        if (!(rhs instanceof ChessPos)) {
            return false;
        }
        ChessPos rhsPos = (ChessPos) (rhs);
        return column == rhsPos.column && row == rhsPos.row;
    }
}
