package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import model.*;
import sounds.*;

import java.io.IOException;
import java.util.*;

@SuppressWarnings("FieldCanBeLocal")
public class ChessGameController {

    @FXML
    private Rectangle gameRect;
    @FXML
    private AnchorPane gameAnchorPane;

    @FXML
    private ImageView soundImageView;

    private ChessBoard chessBoard;
    private ChessEngine chessEngine;
    private Stockfish stockfish;
    private CEngine cEngine;

    private ImageView[][] squareImageViews;
    private ImageView mouseImage;
    private final Map<String, Image> pieceImages = new HashMap<>();
    private ChessPos from, to;
    private double squareSize;
    private double mouseImageSize;
    private boolean promotionMode;
    private boolean playVsComputer;
    private boolean useCustomEngine;
    private boolean playSounds;

    private static final int BOARD_SIZE = 8;
    private static final double MOUSE_IMAGE_SCALE = 1.15;
    private static final double HIGHLIGHT_OPACITY_MOVE = 0.2;
    private static final double HIGHLIGHT_OPACITY_MOVE_POSSIBILITIES = 0.2;
    private static final double HIGHLIGHT_OPACITY_PROMOTION = 0.5;
    private static final int ENGINE_LEVEL = 3;

    private static Sound gameStartsSound;
    private static Sound moveSound;
    private static Sound captureSound;
    private static Sound checkSound;
    private static Sound castleSound;
    private static Sound gameOverSound;

    public void init() {
        chessBoard = new ChessBoard();
        chessEngine = new ChessEngine(ENGINE_LEVEL);
        stockfish = new Stockfish();
        cEngine = new CEngine();

        initPieceImages();
        initializeSounds();
        promotionMode = false;
        playVsComputer = true;
        useCustomEngine = false;
        playSounds = false;
        squareSize = gameRect.getWidth() / (double) BOARD_SIZE;
        squareImageViews = new ImageView[BOARD_SIZE][BOARD_SIZE];

        mouseImage = new ImageView();
        mouseImageSize = squareSize * MOUSE_IMAGE_SCALE;
        mouseImage.setFitWidth(mouseImageSize);
        mouseImage.setFitHeight(mouseImageSize);

        createGameGrid();
        createMouseEvents();

        gameAnchorPane.getChildren().add(mouseImage);

        if (playVsComputer && chessBoard.getCurrentPlayer()
                .equals(ChessPieceColor.BLACK)) {
            makeEngineMove();
        }

        try {
            cEngine.startEngine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (playSounds) {
            gameStartsSound.play();
        }
    }

    private void createMouseEvents() {
        gameAnchorPane.addEventHandler(MouseEvent.MOUSE_MOVED, mouseEvent -> {
            if (from == null) {
                gameAnchorPane.setCursor(Cursor.OPEN_HAND);
            }
        });
        gameAnchorPane.addEventHandler(MouseEvent.MOUSE_DRAGGED, mouseEvent -> {
            if (from != null) {
                setMouseImagePosition(mouseEvent);
            }
        });
        gameAnchorPane.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
            if (from == null) {
                double xCord = mouseEvent.getX();
                double yCord = mouseEvent.getY();
                int x = (int)(xCord / squareSize);
                int y = (int)(yCord / squareSize);

                from = getFromMouseCords(xCord, yCord);
                Image squareImage = squareImageViews[x][y].getImage();

                if ((!playVsComputer && chessBoard.getBoard()[x][y].hasPiece()) ||
                        (playVsComputer && chessBoard.getBoard()[x][y].getColor().
                                equals(ChessPieceColor.WHITE))) {
                    squareImageViews[x][y].setImage(null);
                    setMouseImagePosition(mouseEvent);
                    mouseImage.setImage(squareImage);
                    markPossibleMoves(from);
                    gameAnchorPane.setCursor(Cursor.CLOSED_HAND);
                }
            }
        });
        gameAnchorPane.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEvent -> {
            if (from != null && !promotionMode) {
                mouseImage.setImage(null);
                gameAnchorPane.setCursor(Cursor.OPEN_HAND);
                to = getFromMouseCords(mouseEvent.getX(), mouseEvent.getY());

                gameAnchorPane.getChildren().removeIf(child -> child instanceof Circle);

                if (chessBoard.isPromotionMove(new Move(from, to))) {
                    handlePromotion(mouseEvent);
                } else {
                    MoveType moveType = chessBoard.getMoveType(new Move(from, to));
                    if (chessBoard.movePiece(new Move(from, to))) {
                        gameAnchorPane.getChildren().removeIf(child -> child instanceof Rectangle);
                        highlightMove(from, to);
                        playSound(moveType);

                        if (playVsComputer) {
                            makeEngineMove();
                        }
                    }
                    updateBoard();
                    from = null;
                    to = null;
                }
            }
        });
    }

    private void createGameGrid() {
        GridPane gridPane = new GridPane();
        for (int i = 0; i < BOARD_SIZE; i++) {
            gridPane.addColumn(i);
        }
        for (int i = 0; i < BOARD_SIZE; i++) {
            gridPane.addRow(i);
        }

        for (int i = 0; i < BOARD_SIZE * BOARD_SIZE; i++) {
            int x = i % BOARD_SIZE;
            int y = i / BOARD_SIZE;
            squareImageViews[x][y] = new ImageView();
            squareImageViews[x][y].setFitWidth(squareSize);
            squareImageViews[x][y].setFitHeight(squareSize);

            if (chessBoard.getBoard()[x][y].hasPiece()) {
                String pieceName = chessBoard.getBoard()[x][y].toString();
                squareImageViews[x][y].setImage(pieceImages.get(pieceName));
            }
            gridPane.add(squareImageViews[x][y], x, y);
        }
        gameAnchorPane.getChildren().add(gridPane);
    }

    private void initPieceImages() {
        String directory = System.getProperty("user.dir") + "\\src\\main\\resources\\images\\";
        List<String> pieces = Arrays.asList("PawnBlack", "KnightBlack", "BishopBlack", "RookBlack", "QueenBlack",
                "KingBlack", "PawnWhite", "KnightWhite", "BishopWhite", "RookWhite", "QueenWhite", "KingWhite");
        for (String piece : pieces) {
            Image pieceImage = new Image(directory + piece + ".png");
            pieceImages.put(piece.toUpperCase(), pieceImage);
        }
    }

    private void setMouseImagePosition(MouseEvent mouseEvent) {
        double mouseX = mouseEvent.getX();
        double mouseY = mouseEvent.getY();
        mouseImage.setX(mouseX - mouseImageSize / 2 - squareSize / 40); //1.11
        mouseImage.setY(mouseY - mouseImageSize / 2); //1.6
    }

    private ChessPos getFromMouseCords(double xCord, double yCord) {
        int row = (int)(yCord / squareSize);
        int column = (int)(xCord / squareSize);

        return new ChessPos(column, row);
    }

    private void updateBoard() {
        for (int i = 0; i < BOARD_SIZE * BOARD_SIZE; i++) {
            int x = i % BOARD_SIZE;
            int y = i / BOARD_SIZE;
            if (chessBoard.getBoard()[x][y].hasPiece()) {
                squareImageViews[x][y].setImage(pieceImages.get(chessBoard.getBoard()[x][y].toString()));
            } else {
                squareImageViews[x][y].setImage(null);
            }
        }
    }

    private double centerMouseCord (double mouseCord) {
        int square = (int) (mouseCord / squareSize);
        return squareSize / 2 + square * squareSize;
    }

    private ChessPieceType getPromoteTo (double centerYCord) {
        double dCenter = Math.abs(gameAnchorPane.getHeight() / 2 - centerYCord);
        int yCordChessPos = (int) (dCenter / squareSize);
        switch (yCordChessPos) {
            case 0: return ChessPieceType.BISHOP;
            case 1: return ChessPieceType.ROOK;
            case 2: return ChessPieceType.KNIGHT;
            default: return ChessPieceType.QUEEN;
        }
    }

    private void highlightMove(ChessPos from, ChessPos to) {
        Rectangle squareFrom = new Rectangle(from.getColumn() * squareSize,
                (BOARD_SIZE - from.getRow()) * squareSize, squareSize, squareSize);
        Rectangle squareTo = new Rectangle(to.getColumn() * squareSize,
                (BOARD_SIZE - to.getRow()) * squareSize, squareSize, squareSize);
        squareFrom.setFill(Color.LIGHTGREEN);
        squareFrom.setOpacity(HIGHLIGHT_OPACITY_MOVE);
        gameAnchorPane.getChildren().add(squareFrom);
        squareTo.setFill(Color.LIGHTGREEN);
        squareTo.setOpacity(HIGHLIGHT_OPACITY_MOVE);
        gameAnchorPane.getChildren().add(squareTo);
        squareFrom.toBack();
        squareTo.toBack();
    }

    private void makeEngineMove() {
        Thread engineThread = new Thread(() -> {
            Move move = new Move("a2", "a3");
            if (useCustomEngine) {
                List<String> previousMoves = chessBoard.getMoves();
                move = chessEngine.getMove(chessBoard, previousMoves);
            } else {
                try {
                    List<String> previousMoves = chessBoard.getMoves();
                    move = cEngine.getMove(chessBoard, previousMoves);
                } catch (IOException e) {
                    System.out.println("Something went wrong with the engine");
                }
            }
            Move finalMove = move;
            MoveType moveType = chessBoard.getMoveType(move);
            Platform.runLater(() -> chessBoard.movePiece(finalMove));
            Platform.runLater(() -> gameAnchorPane.getChildren().removeIf(child -> child instanceof Rectangle));
            Platform.runLater(() -> highlightMove(finalMove.from, finalMove.to));
            Platform.runLater(this::updateBoard);
            Platform.runLater(() -> playSound(moveType));
        });
        engineThread.start();
    }

    @FXML
    public void closeStockfish() {
        try {
            stockfish.stopEngine();
            cEngine.stopEngine();;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handlePromotion(MouseEvent mouseEvent) {
        squareImageViews[from.getColumn()][from.getRow()].setImage(null);
        double xCordCircle = centerMouseCord(mouseEvent.getX());
        double yCordCircle = centerMouseCord(mouseEvent.getY());
        double radiusCircle = squareSize / 2;
        int direction = mouseEvent.getY() < gameAnchorPane.getHeight() / 2 ? 1 : -1;

        //Initialize Circles
        for (int i = 0; i < 4; i++) {
            Circle circle = new Circle(xCordCircle, yCordCircle + i * direction * squareSize, radiusCircle);
            circle.setFill(Color.WHITESMOKE);
            circle.setOpacity(HIGHLIGHT_OPACITY_PROMOTION);
            circle.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
                circle.setFill(Color.DARKORANGE);
            });
            circle.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
                circle.setFill(Color.WHITESMOKE);
            });
            circle.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
                promotionMode = false;
                ChessPieceType promoteTo = getPromoteTo(event.getY());
                to.addPromoteTo(promoteTo);

                gameAnchorPane.getChildren().removeIf(child -> child instanceof Circle);
                chessBoard.movePiece(new Move(from, to));
                if (playVsComputer) {
                    makeEngineMove();
                }
                updateBoard();
                from = null;
                to = null;
            });
            gameAnchorPane.getChildren().add(circle);
        }

        ChessPieceColor color = direction == 1 ? ChessPieceColor.WHITE : ChessPieceColor.BLACK;

        Image imageQueen = pieceImages.get(new ChessPiece(ChessPieceType.QUEEN, color).toString());
        Image imageKnight = pieceImages.get(new ChessPiece(ChessPieceType.KNIGHT, color).toString());
        Image imageRook = pieceImages.get(new ChessPiece(ChessPieceType.ROOK, color).toString());
        Image imageBishop = pieceImages.get(new ChessPiece(ChessPieceType.BISHOP, color).toString());

        squareImageViews[to.getColumn()][(BOARD_SIZE - to.getRow())].setImage(imageQueen);
        squareImageViews[to.getColumn()][(BOARD_SIZE - to.getRow()) + direction].setImage(imageKnight);
        squareImageViews[to.getColumn()][(BOARD_SIZE - to.getRow()) + 2 * direction].setImage(imageRook);
        squareImageViews[to.getColumn()][(BOARD_SIZE - to.getRow()) + 3 * direction].setImage(imageBishop);

        promotionMode = true;
    }

    public void markPossibleMoves(ChessPos from) {
        double radius = squareSize / 5;
        List<ChessPos> possibleMoves = chessBoard.getPossibleMoves(from);
        for (ChessPos pos : possibleMoves) {
            double xCordCircle = pos.getColumn() * squareSize + squareSize / 2;
            double yCordCircle = (BOARD_SIZE - pos.getRow()) * squareSize + squareSize / 2;
            Circle circle = new Circle(xCordCircle, yCordCircle, radius);
            circle.setFill(Color.GREEN);
            circle.setOpacity(HIGHLIGHT_OPACITY_MOVE_POSSIBILITIES);
            gameAnchorPane.getChildren().add(circle);
            circle.toBack();
        }
    }

    private void initializeSounds() {
        String soundPath = "src/main/resources/sounds/";

        gameStartsSound = new SoundEffect(soundPath + "GameStarts.wav");
        gameOverSound = new SoundEffect(soundPath + "GameOver.wav");
        captureSound = new SoundEffect(soundPath + "Capture.wav");
        castleSound = new SoundEffect(soundPath + "Castle.wav");
        checkSound = new SoundEffect(soundPath + "Check.wav");
        moveSound = new SoundEffect(soundPath + "Move.wav");

        moveSound.setVolume(0);
        moveSound.play();
        moveSound.setVolume(1);
    }

    private void playSound(MoveType moveType) {
        if (playSounds) {
            switch (moveType) {
                case GAME_OVER:
                    gameOverSound.play();
                    break;
                case CASTLE:
                    castleSound.play();
                    break;
                case CAPTURE:
                    captureSound.play();
                    break;
                case CHECK:
                    checkSound.play();
                    break;
                default:
                    moveSound.play();
            }
        }
    }

    @FXML
    public void toggleSound() {
        playSounds = !playSounds;

        String directory = System.getProperty("user.dir") + "\\src\\main\\resources\\icons\\";
        if (playSounds) {
            soundImageView.setImage(new Image(directory + "IconSoundOn.png"));
        } else {
            soundImageView.setImage(new Image(directory + "IconSoundOff.png"));
        }
    }

    @FXML
    public void restartGame() {
        chessBoard = new ChessBoard();
        from = null;
        to = null;
        promotionMode = false;

        gameAnchorPane.getChildren().removeIf(child -> child instanceof Circle);
        gameAnchorPane.getChildren().removeIf(child -> child instanceof Rectangle);

        updateBoard();
    }

    @FXML
    public void copyFENtoClipboard() {
        String boardFEN = chessBoard.getAsFEN();
        StringSelection selection = new StringSelection(boardFEN);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, null);
    }

    @FXML
    public void copyMovesToClipboard() {
        String moves = String.join(" ", chessBoard.getMoves());
        StringSelection selection = new StringSelection(moves);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, null);
    }
}
