package model.SysEng;

import model.ChessBoard;
import model.ChessPiece;
import model.ChessPieceColor;
import model.ChessPieceType;

import java.util.HashMap;
import java.util.Map;

public class PositionChecker {

    private static final int[] maxAmountPieces = {1, 1, 2, 2, 2, 8};


    public static boolean isStandardPosition(ChessBoard chessBoard) {
        int[] nWhitePieces = {0, 0, 0, 0, 0, 0};
        int[] nBlackPieces = {0, 0, 0, 0, 0, 0};
        int[] bishopColorComplex = {0, 0, 0, 0};

        for(int i = 0; i < 8; i++) {
            for(int k = 0; k < 8; k++) {
                ChessPiece chessPiece = chessBoard.getBoard()[i][k];
                if (chessPiece.getColor() == ChessPieceColor.WHITE) {
                    nWhitePieces[pieceTypeToInt(chessPiece.getPieceType())]++;

                    if (chessPiece.getPieceType() == ChessPieceType.BISHOP) {
                        bishopColorComplex[(i + k) % 2]++;
                    }
                } else if (chessPiece.getColor() == ChessPieceColor.BLACK) {
                    nBlackPieces[pieceTypeToInt(chessPiece.getPieceType())]++;

                    if (chessPiece.getPieceType() == ChessPieceType.BISHOP) {
                        bishopColorComplex[((i + k) % 2) + 2]++;
                    }
                }
            }
        }

        for (int i = 0; i < 6; i++) {
            if (nWhitePieces[i] > maxAmountPieces[i] || nBlackPieces[i] > maxAmountPieces[i]) {
                return false;
            }
        }
        for (int i = 0; i < 4; i++) {
            if (bishopColorComplex[i] > 1) {
                return false;
            }
        }

        return true;
    }

    private static int pieceTypeToInt(ChessPieceType pieceType) {
        switch (pieceType) {
            case KING:
                return 0;
            case QUEEN:
                return 1;
            case ROOK:
                return 2;
            case BISHOP:
                return 3;
            case KNIGHT:
                return 4;
            case PAWN:
                return 5;
            default:
                return 6;
        }
    }
}
