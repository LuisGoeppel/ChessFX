package model;

import javafx.util.Pair;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

public class Stockfish {

    private static final String stockfishBinaryPath = "src/main/resources/engines/" +
            "stockfish_15.1_win_x64_avx2/stockfish-windows-2022-x86-64-avx2.exe";
    private PrintWriter writer;
    private BufferedReader reader;
    private GameBase gameBase;
    private boolean isRunning;


    public Stockfish() {
        gameBase = new GameBase();
        isRunning = false;
    }

    public void startEngine() throws IOException {

        Process process = Runtime.getRuntime().exec(stockfishBinaryPath);
        OutputStream outputStream = process.getOutputStream();
        writer = new PrintWriter(outputStream);
        reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        // Test if the engine was started successfully
        writer.println("uci");
        writer.flush();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("uciok")) {
                System.out.println("Engine started successfully");
                isRunning = true;
                break;
            }
        }
    }

    public Move getBestMove(String fen) throws IOException {
        if(!isRunning) {
            throw new IllegalStateException("Engine is not running!");
        }
        writer.println("position fen " + fen);
        writer.println("go movetime 1000");
        writer.flush();

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("bestmove")) {
                break;
            }
        }
        assert line != null;
        String moveAsString = line.split(" ")[1];
        String from = moveAsString.substring(0, 2);
        String to = moveAsString.substring(2);
        if (to.contains("=")) {
            ChessPos posFrom = new ChessPos(from);
            ChessPos posTo;
            switch (to.charAt(to.indexOf("=") + 1)) {
                case 'Q':
                    posTo = new ChessPos(to.substring(0, 2), ChessPieceType.QUEEN);
                    break;
                case 'R':
                    posTo = new ChessPos(to.substring(0, 2), ChessPieceType.ROOK);
                    break;
                case 'N':
                    posTo = new ChessPos(to.substring(0, 2), ChessPieceType.KNIGHT);
                    break;
                case 'B':
                    posTo = new ChessPos(to.substring(0, 2), ChessPieceType.BISHOP);
                    break;
                default:
                    posTo = new ChessPos(to.substring(0, 2));
            }
            return new Move(posFrom, posTo);
        }
        return new Move(moveAsString.substring(0, 2), moveAsString.substring(2));
    }

    public Move getMove(ChessBoard chessBoard, List<String> previousMoves) throws IOException {
        List<String> previousMovesFiltered = previousMoves.stream()
                .map(move -> move.replaceAll("^\\d{1,2}\\.\\s*", ""))
                .collect(Collectors.toList());
        List<Pair<String, Integer>> moves = gameBase.getMoves(previousMovesFiltered);
        if (moves.isEmpty()) {
            return getBestMove(chessBoard.getAsFEN());
        }
        String nextMoveString = RandomProbabilitySelector.selectRandomItem(moves);
        return chessBoard.getMoveFromString(nextMoveString);
    }

    public void stopEngine() throws IOException {
        if (isRunning) {
            writer.println("quit");
            writer.flush();
            writer.close();
            reader.close();
        }
        isRunning = false;
        System.out.println("Engine stopped");
    }
}