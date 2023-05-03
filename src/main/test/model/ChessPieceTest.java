package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChessPieceTest {

    @Test
    void testThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new ChessPiece(ChessPieceType.KING, ChessPieceColor.EMPTY));
    }

    @Test
    void testHasPiece() {
        ChessPiece chessPiece = new ChessPiece(ChessPieceType.BISHOP, ChessPieceColor.BLACK);
        assertTrue(chessPiece.hasPiece());
    }

    @Test
    void testHasPieceFalse() {
        ChessPiece chessPiece = new ChessPiece(ChessPieceType.EMPTY, ChessPieceColor.EMPTY);
        assertFalse(chessPiece.hasPiece());
    }

    @Test
    void testEnemyColor() {
        ChessPiece chessPiece = new ChessPiece(ChessPieceType.PAWN, ChessPieceColor.BLACK);
        assertTrue(chessPiece.isEnemyColorTo(ChessPieceColor.WHITE));
    }

    @Test
    void testGetAsChar() {
        ChessPiece chessPiece = new ChessPiece(ChessPieceType.QUEEN, ChessPieceColor.BLACK);
        assertEquals('q', chessPiece.getAsChar());
    }

    @Test
    void testGetAsChar2() {
        ChessPiece chessPiece = new ChessPiece(ChessPieceType.KNIGHT, ChessPieceColor.WHITE);
        assertEquals('N', chessPiece.getAsChar());
    }
}