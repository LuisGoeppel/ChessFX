package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChessBoardFENTest {

    private static final String DEFAULT_POSITION = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    String customFEN = "r1bq1k1r/1p1npp1p/p2p1n1b/8/1P1NPPp1/P1NBB3/2P3PP/R2Q1RK1 b - f3 0 11";
    String customFEN2 = "r1b2k1r/1p2pp1p/pn1pqn1b/8/1P1NPPp1/P1NBB3/2P1Q1PP/R4RK1 w - - 9 16";

    @Test
    void testDefaultFEN() {
        ChessBoard c = new ChessBoard();
        String FEN = c.getAsFEN();
        assertEquals(DEFAULT_POSITION, FEN);
    }

    @Test
    void testCustomFEN() {
        ChessBoard c = new ChessBoard(customFEN);
        assertEquals(customFEN, c.getAsFEN());
    }

    @Test
    void testCustomFEN2() {
        ChessBoard c = new ChessBoard(customFEN2);
        assertEquals(customFEN2, c.getAsFEN());
    }

    @Test
    void testCorrectFENImplementation() {
        ChessBoard c = new ChessBoard(customFEN);
        String expected =
                "r . b q . k . r \n" +
                ". p . n p p . p \n" +
                "p . . p . n . b \n" +
                ". . . . . . . . \n" +
                ". P . N P P p . \n" +
                "P . N B B . . . \n" +
                ". . P . . . P P \n" +
                "R . . Q . R K . \n";
        assertEquals(expected.trim(), c.toString().trim());
    }
}
