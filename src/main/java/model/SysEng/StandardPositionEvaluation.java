package model.SysEng;

import model.ChessBoard;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StandardPositionEvaluation {

    /*private static final List<String> gamePaths = Arrays.asList(
            "src/main/resources/grandmasterGames/Adams.txt",
            "src/main/resources/grandmasterGames/Alekhine.txt",
            "src/main/resources/grandmasterGames/Anand.txt",
            "src/main/resources/grandmasterGames/Invanchuk.txt",
            "src/main/resources/grandmasterGames/Karpov.txt",
            "src/main/resources/grandmasterGames/Kasparov.txt",
            "src/main/resources/grandmasterGames/Kosteniuk.txt",
            "src/main/resources/grandmasterGames/Morozevich.txt",
            "src/main/resources/grandmasterGames/Shirov.txt"
    );*/

    private static final List<String> gamePaths = Arrays.asList(
            "src/main/resources/otherGames/xFeralago.txt"
    );

    private static final List<String> results = Arrays.asList("1-0", "1/2-1/2", "0-1");


    public static void main(String[] args) {
        int nPosTotal = 0;
        int nPosTotalStandard = 0;
        int nGamesTotal = 0;

        for(String grandmasterGames : gamePaths) {
            int nPos = 0;
            int nPosStandard = 0;

            String[] games = extractGameMoves(readFile(grandmasterGames));
            nGamesTotal += games.length;

            for (String game : games) {
                ChessBoard chessBoard = new ChessBoard();

                String[] movesAsString = game.split("[ \\n]+");
                for (int i = 0; i < movesAsString.length; i++) {
                    String cleanedMove = movesAsString[i].replaceAll("^\\d+\\.", "");
                    if (!cleanedMove.isEmpty() && !results.contains(cleanedMove)) {
                        try {
                            chessBoard.executeMove(cleanedMove);
                            if (i > 10) {
                                if (PositionChecker.isStandardPosition(chessBoard)) {
                                    nPosStandard++;
                                }
                                nPos++;
                            }
                        } catch (Exception e) {
                            System.out.println("Error in Game: ");
                            System.out.println(game);
                            return;
                        }
                    }
                }
            }

            String grandmasterName = grandmasterGames.split("/")[grandmasterGames.split("/").length - 1].replaceAll(".txt", "");
            System.out.println(grandmasterName);
            System.out.println("#Games: " + games.length);
            System.out.println("Evaluated Positions: " + nPos);
            System.out.println("Standard Positions: "+ nPosStandard);
            System.out.println("Percentage: " + ((double)nPosStandard / (double)nPos));
            System.out.println();

            nPosTotalStandard += nPosStandard;
            nPosTotal += nPos;
        }

        System.out.println("Overall");
        System.out.println("#Games: " + nGamesTotal);
        System.out.println("Evaluated Positions: " + nPosTotal);
        System.out.println("Standard Positions: " + nPosTotalStandard);
        System.out.println("Percentage: " + ((double)nPosTotalStandard / (double)nPosTotal));
    }

/*
    public static void main(String[] args) {
        String game = "1.e4 c5 2.Nf3 d6 3.Bb5+ Bd7 4.Bxd7+ Qxd7 5.O-O Nc6 6.Re1 Nf6 7.c3 e6 8.d3\n" +
                "Be7 9.Nbd2 O-O 10.Nf1 b5 11.d4 d5 12.Ne5 Qb7 13.Nxc6 Qxc6 14.e5 Nd7 15.Qg4\n" +
                "Rfd8 16.Bg5 Bf8 17.h4 Rac8 18.h5 b4 19.Re3 bxc3 20.bxc3 cxd4 21.cxd4 Qa4\n" +
                "22.h6 g6 23.Qf4 Nb6 24.Rf3 Rc7 25.Ne3 Rdc8 26.Kh2 Nc4 27.Ng4 f5 28.Nf6+\n" +
                "Kh8 29.g4 Qc2 30.Rc1 Qxa2 31.gxf5 exf5 32.Nxd5 Rc6 33.Nc3 Qa6 34.d5 Rc5\n" +
                "35.Kg2 Qb7 36.e6 Bd6 37.Qd4+ Kg8 38.Re1 Ne5 39.Rxe5 Rc4 40.Qxc4 Rxc4 41.e7\n" +
                "Rg4+ 42.Rg3 Bxe7 43.Rxe7 Rxg3+ 44.Kxg3 Qb3 45.Rc7 a5 46.d6 Qb4 47.Bf4 g5\n" +
                "48.Rg7+ Kf8 49.Bxg5 Qxc3+ 50.f3 1/2-1/2";

        ChessBoard chessBoard = new ChessBoard();
        chessBoard.executeMoves(game);
        System.out.println(chessBoard);
    }*/

    private static String readFile(String filePath) {
        try {
            return Files.readString(Path.of(filePath));
        } catch (IOException e) {
            throw new RuntimeException("Can not find a file at the given Path: " + filePath);
        }
    }

    private static String[] extractGameMoves(String allGames) {
        // Step 1: Remove metadata lines (lines starting with '[') and empty lines
        String sanitizedInput = allGames
                .replaceAll("(?m)^\\[.*?\\]\\n?", "")
                .replaceAll("(?m)^\\s*\\n", "")
                .replaceAll("\r", "");

        // Step 2: Use regex to match games that start with "1." and end with a result
        ArrayList<String> gamesList = new ArrayList<>();
        String regex = "(1\\..*?(?:1-0|0-1|1/2-1/2|\\*))";

        var matcher = java.util.regex.Pattern.compile(regex, java.util.regex.Pattern.DOTALL).matcher(sanitizedInput);

        // Step 3: Extract all matches
        while (matcher.find()) {
            gamesList.add(matcher.group(1).trim());
        }

        // Convert ArrayList back to an array
        return gamesList.toArray(new String[0]);
    }
}
