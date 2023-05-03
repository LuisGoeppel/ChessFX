package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ChessPosTest {

    @Test
    void testConstructorException() {
        assertThrows(IllegalArgumentException.class, () -> new ChessPos('b', 9));
    }

    @Test
    void testConstructorException2() {
        assertThrows(IllegalArgumentException.class, () -> new ChessPos('k', 5));
    }

    @Test
    void testConstructorException3() {
        assertThrows(IllegalArgumentException.class, () -> new ChessPos("Hello"));
    }

    @Test
    void testConstructorException4() {
        assertThrows(IllegalArgumentException.class, () -> new ChessPos("HI"));
    }

    @Test
    void testConstructorNoException() {
        new ChessPos("G7");
    }

    @Test
    void testToString() {
        ChessPos chessPos = new ChessPos("F6");
        assertEquals("f6", chessPos.toString());
    }

    @Test
    void testEquals() {
        ChessPos pos1 = new ChessPos("F3");
        ChessPos pos2 = new ChessPos("G8");
        assertNotEquals(pos1, pos2);
    }

    @Test
    void testEqualsTrue() {
        ChessPos pos1 = new ChessPos("F3");
        ChessPos pos2 = new ChessPos("F3");
        assertEquals(pos1, pos2);
    }

    @Test
    void testGetAsCoordinates() {
        ChessPos chessPos = new ChessPos("b6");
        int x = chessPos.getAsCoordinates()[0];
        int y = chessPos.getAsCoordinates()[1];
        System.out.println(x + " " + y);
        assertEquals(1, x);
        assertEquals(2, y);
    }
}
