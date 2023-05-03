package model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ChessBoardMoveTest {

    private static final String operaGame = "1. e4 e5 2. Nf3 d6 3. d4 Bg4 4. dxe5 Bxf3 5. Qxf3 dxe5 6. Bc4 Nf6 " +
            "7. Qb3 Qe7 8. Nc3 c6 9. Bg5 b5 10. Nxb5 cxb5 11. Bxb5+ Nbd7 12. O-O-O Rd8 " +
            "13. Rxd7 Rxd7 14. Rd1 Qe6 15. Bxd7+ Nxd7 16. Qb8+ Nxb8 17. Rd8#";
    private static final String immortalGame = "1. d4 d5 2. c4 dxc4 3. e3 b5 4. b3 c3 5. Nxc3 Nf6 6. Ba3 e6 " +
            "7. Bxf8 Rxf8 8. Bxb5+ c6 9. Bxc6+ Nxc6 10. Qd3 Bd7 11. Qc4 Ne7 12. Qd3 h6 13. Qe2 h5 14. Qf3 h4 " +
            "15. Ne4 Rc8 16. Qxf6 gxf6 17. Nd6#";
    private static final String evergreenGame = "1. e4 e5 2. Nf3 Nc6 3. Bc4 Bc5 4. b4 Bxb4 5. c3 Ba5 6. d4 exd4 " +
            "7. O-O d3 8. Qb3 Qf6 9. e5 Qg6 10. Re1 Nge7 11. Ba3 b5 12. Qxb5 Rb8 13. Qa4 Bb6 14. Nbd2 Bb7 " +
            "15. Ne4 Qf5 16. Bxd3 Qh5 17. Nf6+ gxf6 18. exf6 Rg8 19. Rad1 Qxf3 20. Rxe7+ Nxe7 " +
            "21. Qxd7+ Kxd7 22. Bf5+ Ke8 23. Bd7+ Kf8 24. Bxe7#";

    private static final String promotionGame = "1. d4 f5 2. c4 Nf6 3. g3 e6 4. Bg2 Bb4+ 5. Bd2 Bxd2+ 6. Nxd2 Nc6 " +
            "7. Ngf3 O-O 8. O-O d6 9. Qb3 Kh8 10. Qc3 e5 11. e3 a5 12. b3 Qe8 13. a3 Qh5 14. h4 Ng4 15. Ng5 Bd7 " +
            "16. f3 Nf6 17. f4 e4 18. Rfd1 h6 19. Nh3 d5 20. Nf1 Ne7 21. a4 Nc6 22. Rd2 Nb4 23. Bh1 Qe8 " +
            "24. Rg2 dxc4 25. bxc4 Bxa4 26. Nf2 Bd7 27. Nd2 b5 28. Nd1 Nd3 29. Rxa5 b4 30. Rxa8 bxc3 31. Rxe8 c2 " +
            "32. Rxf8+ Kh7 33. Nf2 c1=Q+ 34. Nf1 Ne1 35. Rh2 Qxc4 36. Rb8 Bb5 37. Rxb5 Qxb5 38. g4 Nf3+ 39. Bxf3 exf3 " +
            "40. gxf5 Qe2 41. d5 Kg8 42. h5 Kh7 43. e4 Nxe4 44. Nxe4 Qxe4 45. d6 cxd6 46. f6 gxf6 47. Rd2 Qe2 " +
            "48. Rxe2 fxe2 49. Kf2 exf1=Q+ 50. Kxf1 Kg7 51. Ke2 Kf7 52. Ke3 Ke6 53. Ke4 d5+";

    @Test
    void testScholarsMate() {
        ChessBoard chessBoard = new ChessBoard();
        boolean everyMoveLegal = chessBoard.movePiece(new Move("e2", "e4"));
        everyMoveLegal = everyMoveLegal && chessBoard.movePiece(new Move("e7", "e5"));
        everyMoveLegal = everyMoveLegal && chessBoard.movePiece(new Move("d1", "f3"));
        everyMoveLegal = everyMoveLegal && chessBoard.movePiece(new Move("b8", "c6"));
        everyMoveLegal = everyMoveLegal && chessBoard.movePiece(new Move("f1", "c4"));
        everyMoveLegal = everyMoveLegal && chessBoard.movePiece(new Move("b7", "b6"));
        everyMoveLegal = everyMoveLegal && chessBoard.movePiece(new Move("f3", "f7"));

        assertTrue(everyMoveLegal && chessBoard.isGameOver());
    }

    @Test
    void testStartBlackIllegal() {
        ChessBoard chessBoard = new ChessBoard();
        assertFalse(chessBoard.movePiece(new Move("e7", "e5")));
    }

    @Test
    void testIllegalMove() {
        ChessBoard chessBoard = new ChessBoard("r1bqkb1r/3n1ppp/p2ppn2/1p4B1/2BNPP2/2N5/PPP3PP/R2QK2R w KQkq - 0 9");
        assertFalse(chessBoard.movePiece(new Move("d1", "d6")));
    }

    @Test
    void testCorrectPosition() {
        ChessBoard chessBoard = new ChessBoard("r1bqkb1r/3n1ppp/p2ppn2/1p4B1/2BNPP2/2N5/PPP3PP/R2QK2R w KQkq - 0 9");
        chessBoard.movePiece(new Move("c4", "e6"));
        chessBoard.movePiece(new Move("f7", "e6"));
        chessBoard.movePiece(new Move("d4", "e6"));
        chessBoard.movePiece(new Move("d8", "b6"));
        chessBoard.movePiece(new Move("c3", "d5"));
        chessBoard.movePiece(new Move("f6", "d5"));
        chessBoard.movePiece(new Move("d1", "d5"));

        String fenAfterMoves = chessBoard.getAsFEN();
        assertEquals("r1b1kb1r/3n2pp/pq1pN3/1p1Q2B1/4PP2/8/PPP3PP/R3K2R b KQkq - 0 12", fenAfterMoves);
    }

    @Test
    void testCastleAllowed() {
        ChessBoard chessBoard = new ChessBoard("r1bqkb1r/3p1ppp/ppn1pn2/2p5/2B1P2P/2NP1N2/PPP2PP1/R1BQK2R w KQkq - 1 7");
        assertTrue(chessBoard.movePiece(new Move("e1", "g1")));
    }

    @Test
    void testCastleIllegal() {
        ChessBoard chessBoard = new ChessBoard("r1bqkb1r/3p1ppp/ppn1pn2/2p5/2B1P2P/2NP1N2/PPP2PP1/R1BQK2R w KQkq - 1 7");
        chessBoard.movePiece(new Move("h1", "h2"));
        chessBoard.movePiece(new Move("c8", "b7"));
        chessBoard.movePiece(new Move("h2", "h1"));
        chessBoard.movePiece(new Move("f8", "e7"));
        assertFalse(chessBoard.movePiece(new Move("e1", "g1")));
    }

    @Test
    void testImportMoves() {
        String moves = "1. e4 e5 2. f4 exf4 3. Nf3 g5 4. Bc4 g4 5. O-O gxf3 6. Qxf3 Qf6 7. e5 Qxe5 8. Bxf7+ Kxf7 9. d4 Qxd4+ " +
                "10. Kh1 Nc6 11. Bxf4 Qxf4 12. Qxf4+ Ke8 13. Qxf8#";
        ChessBoard chessBoard = new ChessBoard();
        boolean success = chessBoard.executeMoves(moves);
        assertTrue(success);
    }

    @Test
    void testImmortalMoves() {
        String immortalEndPos = "2rqkr2/p2bnp2/3Npp2/8/3P3p/1P2P3/P4PPP/R3K1NR b KQ - 1 17";
        
        ChessBoard chessBoard = new ChessBoard();
        chessBoard.executeMoves(immortalGame);
        String resultPos = chessBoard.getAsFEN();
        assertEquals(immortalEndPos, resultPos);
    }

    @Test
    void testEvergreenMoves() {
        ChessBoard chessBoard = new ChessBoard();
        chessBoard.executeMoves(evergreenGame);
        List<String> movesMade = chessBoard.getMoves();
        StringBuilder movesAsString = new StringBuilder();
        for (int i = 0; i < movesMade.size(); i++) {
            movesAsString.append(movesMade.get(i));
            if (i < movesMade.size() - 1) {
                movesAsString.append(" ");
            }
        }
        assertEquals(evergreenGame, movesAsString.toString());
    }

    @Test
    void testSicilianOpening() {
        String moves = "1. e4 c5 2. Nf3 d6 3. d4 cxd4 4. Nxd4 Nf6 5. Nc3 a6 6. Bg5 e6 " +
                "7. f4 Be7 8. Qd2 Nbd7 9. O-O-O O-O 10. Bd3 b5";
        ChessBoard chessBoard = new ChessBoard();
        chessBoard.executeMoves(moves);
        List<String> movesExecuted = chessBoard.getMoves();
        String movesExecutedString = String.join(" ", movesExecuted);
        assertEquals(moves, movesExecutedString);
    }

    @Test
    void testRookNotation() {
        String position = "1r2k1r1/pbppnp1p/1bn2P2/7q/Q7/B1PB1N2/P4PPP/R3R1K1 w - - 1 19";
        ChessBoard chessBoard = new ChessBoard(position);
        chessBoard.movePiece(new Move("a1", "d1"));
        chessBoard.movePiece(new Move("h5", "f3"));
        List<String> moves = chessBoard.getMoves();
        assertEquals("19. Rad1 Qxf3", moves.get(0));
    }

    @Test
    void testToString() {
        String position = "r2q1rk1/1b3p1p/1n1p1bp1/p4N2/1p2P2P/1P1B2P1/P1PQN3/2KR3R w - - 0 18";
        String expected =
                "r . . q . r k . \n" +
                ". b . . . p . p \n" +
                ". n . p . b p . \n" +
                "p . . . . N . . \n" +
                ". p . . P . . P \n" +
                ". P . B . . P . \n" +
                "P . P Q N . . . \n" +
                ". . K R . . . R \n";
        ChessBoard chessBoard = new ChessBoard(position);
        assertEquals(expected, chessBoard.toString());
    }

    @Test
    void testFallenPiecesValue() {
        ChessBoard chessBoard = new ChessBoard();
        chessBoard.executeMoves(operaGame);
        int valueFallenPieceWhite = chessBoard.getFallenPiecesValue(ChessPieceColor.WHITE);
        int valueFallenPieceBlack = chessBoard.getFallenPiecesValue(ChessPieceColor.BLACK);

        assertEquals(24, valueFallenPieceWhite);
        assertEquals(14, valueFallenPieceBlack);
    }

    @Test
    void testGetWinner() {
        ChessBoard chessBoard = new ChessBoard();
        chessBoard.executeMoves(operaGame);

        assertTrue(chessBoard.isGameOver());
        assertEquals(ChessWinner.WHITE, chessBoard.getWinner());
    }

    @Test
    void testCurrentPlayer() {
        ChessBoard chessBoard = new ChessBoard();
        chessBoard.executeMoves(evergreenGame);

        assertEquals(ChessPieceColor.BLACK, chessBoard.getCurrentPlayer());
    }

    @Test
    void testPromotion() {
        String position = "r1bqkbnr/pp2pppp/2n5/8/2BP4/1Q3N2/p4PPP/RNB1K2R b KQkq - 1 8";
        String endPos = "r1bqkbnr/pp2pppp/2n5/8/2BP4/1Q3N2/5PPP/RrB1K2R w KQkq - 0 9";
        ChessBoard chessBoard = new ChessBoard(position);
        ChessPos from = new ChessPos("a2");
        ChessPos to = new ChessPos("b1", ChessPieceType.ROOK);
        chessBoard.movePiece(new Move(from, to));
        assertEquals(endPos, chessBoard.getAsFEN());
    }

    @Test
    void testPromotionGame() {
        ChessBoard chessBoard = new ChessBoard();
        boolean allMovesCompleted = chessBoard.executeMoves(promotionGame);

        assertTrue(allMovesCompleted);
        assertEquals(promotionGame, String.join(" ", chessBoard.getMoves()));
    }
}
