package model;

import org.junit.jupiter.api.Test;

public class PositionEvaluatorTest {

    @Test
    void test1() {
        ChessBoard chessBoard = new ChessBoard("r2q1rk1/pp3Npp/2p1bn2/2bp2B1/2B5/2PK4/P2N1PPP/R2Q3R b - - 0 13");
        PositionEvaluator evaluator = PositionEvaluator.getInstance();
        System.out.println(evaluator.getEvaluation(chessBoard));
    }

    @Test
    void test2() {
        ChessBoard chessBoard = new ChessBoard("r4rk1/3qn2p/b2p2p1/p1pP4/1pP1Pp1B/5P2/P1N4K/1R3BQ1 w - - 6 27");
        ChessEngine chessEngine = new ChessEngine(1);
        System.out.println(chessEngine.getMove(chessBoard));
    }
}
