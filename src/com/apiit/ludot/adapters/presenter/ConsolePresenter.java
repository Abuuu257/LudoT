package com.apiit.ludot.adapters.presenter;

import com.apiit.ludot.domain.model.Color;
import com.apiit.ludot.domain.model.Direction;
import com.apiit.ludot.patterns.observer.GameEvent;
import com.apiit.ludot.patterns.observer.GameEventListener;
import com.apiit.ludot.patterns.observer.events.DiceRolledEvent;
import com.apiit.ludot.patterns.observer.events.FirstPlayerChosenEvent;
import com.apiit.ludot.patterns.observer.events.GameOverEvent;
import com.apiit.ludot.patterns.observer.events.GameStartedEvent;
import com.apiit.ludot.patterns.observer.events.MysterySpawnedEvent;
import com.apiit.ludot.patterns.observer.events.MysteryTeleportEvent;
import com.apiit.ludot.patterns.observer.events.PieceBlockedEvent;
import com.apiit.ludot.patterns.observer.events.PieceCapturedEvent;
import com.apiit.ludot.patterns.observer.events.PieceMovedEvent;
import com.apiit.ludot.patterns.observer.events.PieceSpawnedEvent;
import com.apiit.ludot.patterns.observer.events.RoundEndedEvent;
import com.apiit.ludot.usecases.dto.PieceDTO;
import com.apiit.ludot.usecases.dto.PlayerStatusDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConsolePresenter implements GameEventListener {
    private final List<GameOverEvent> finalSummaryEvents = new ArrayList<>();

    public ConsolePresenter(boolean verbose) {

    }

    @Override
    public void onEvent(GameEvent event) {
        String message = formatEvent(event);
        if (message != null && !message.isEmpty()) {
            System.out.println(message);
        }
    }

    private String formatEvent(GameEvent event) {
        return switch (event) {
            case GameStartedEvent e -> formatGameStarted(e);
            case FirstPlayerChosenEvent e -> formatFirstPlayerChosen(e);
            case DiceRolledEvent e -> formatDiceRolled(e);
            case PieceSpawnedEvent e -> formatPieceSpawned(e);
            case PieceMovedEvent e -> formatPieceMoved(e);
            case PieceBlockedEvent e -> formatPieceBlocked(e);
            case PieceCapturedEvent e -> formatPieceCaptured(e);
            case MysterySpawnedEvent e -> formatMysterySpawned(e);
            case MysteryTeleportEvent e -> formatMysteryTeleport(e);
            case RoundEndedEvent e -> formatRoundEnded(e);
            case GameOverEvent e -> formatGameOver(e);
            default -> null;
        };
    }

    private String formatGameStarted(GameStartedEvent e) {
        StringBuilder sb = new StringBuilder();
        List<Color> colors = e.participatingColors();
        for (int i = 0; i < colors.size(); i++) {
            Color color = colors.get(i);
            String p = color.getShortPrefix();
            sb.append("The ").append(color.getDisplayName()).append(" player has four (04) pieces named ")
                    .append(p).append("1, ").append(p).append("2, ").append(p).append("3, and ").append(p).append("4.");
            if (i < colors.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    private String formatFirstPlayerChosen(FirstPlayerChosenEvent e) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Color, Integer> entry : e.initialRolls().entrySet()) {
            sb.append(entry.getKey().getDisplayName()).append(" rolls ").append(entry.getValue()).append("\n");
        }
        sb.append(e.chosenStartingPlayer().getDisplayName()).append(" player has the highest roll and will begin the game.\n");

        List<Color> order = e.roundOrder();
        sb.append("The order of a single round is ");
        for (int i = 0; i < order.size(); i++) {
            if (i == order.size() - 1) {
                sb.append("and ").append(order.get(i).getDisplayName()).append(".");
            } else {
                sb.append(order.get(i).getDisplayName()).append(", ");
            }
        }
        return sb.toString();
    }

    private String formatDiceRolled(DiceRolledEvent e) {
        return e.playerColor().getCapitalizedName() + " player rolled " + e.rollValue() + ".";
    }

    private String formatPieceSpawned(PieceSpawnedEvent e) {
        String dirString = (e.direction() == Direction.CLOCKWISE) ? "clockwise" : "counter-clockwise";
        return e.playerColor().getCapitalizedName() + " player flips a coin: "
                + e.coinTossOutcome() + " - piece " + e.pieceId() + " will move " + dirString + ".\n"
                + e.playerColor().getCapitalizedName() + " player moves piece " + e.pieceId() + " to the starting point.\n"
                + e.playerColor().getCapitalizedName() + " player now has "
                + e.piecesOnBoard() + "/4 on pieces on the board and "
                + e.piecesInBase() + "/4 pieces on the base.";
    }

    private String formatPieceMoved(PieceMovedEvent e) {
        String dirString = (e.direction() == Direction.CLOCKWISE) ? "clockwise" : "counter-clockwise";
        return e.playerColor().getCapitalizedName() + " moves piece " + e.pieceId()
                + " from location " + e.fromLocation() + " to " + e.toLocation()
                + " by " + e.units() + " units in " + dirString + " direction.";
    }

    private String formatPieceBlocked(PieceBlockedEvent e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.playerColor().getCapitalizedName()).append(" piece ").append(e.pieceName())
                .append(" is blocked from moving from ").append(e.fromLocation()).append(" to ")
                .append(e.intendedLocation()).append(" by ").append(e.blockingColor().getCapitalizedName())
                .append(" piece ").append(e.blockingPieceName()).append(".\n");

        sb.append(e.playerColor().getCapitalizedName())
                .append(" does not have other pieces in the board to move instead of the blocked piece.\n");

        if (e.movedToAdjacentBeforeBlock()) {
            sb.append("Moved the piece to square ").append(e.adjacentLocationL3())
                    .append(" which is the cell before the block.");
        } else {
            sb.append("Ignoring the throw and moving on to the next player.");
        }
        return sb.toString();
    }

    private String formatPieceCaptured(PieceCapturedEvent e) {
        String capturedNames = String.join(", ", e.capturedPieceNames());
        return e.capturingColor().getCapitalizedName() + " piece " + e.capturingPieceName()
                + " lands on square " + e.locationL1()
                + ", captures " + e.capturedColor().getCapitalizedName() + " piece " + capturedNames
                + ", and returns it to the base.\n"
                + e.capturingColor().getCapitalizedName() + " player now has "
                + e.capturerPiecesOnBoard() + "/4 on pieces on the board and "
                + e.capturerPiecesInBase() + "/4 pieces on the base.";
    }

    private String formatMysterySpawned(MysterySpawnedEvent e) {
        return "A mystery cell has spawned in location " + e.locationL1()
                + " and will be at this location for the next four rounds.";
    }

    private String formatMysteryTeleport(MysteryTeleportEvent e) {
        StringBuilder sb = new StringBuilder();
        String col = e.playerColor().getCapitalizedName();
        String name = e.pieceName();

        sb.append(col).append(" player lands on a mystery cell and is teleported to ")
                .append(e.targetDestinationName()).append(".\n");

        switch (e.effect()) {
            case ENERGIZED -> {
                sb.append(col).append(" piece ").append(name).append(" teleported to Alpha.\n");
                sb.append(col).append(" piece ").append(name).append(" feels energized, and movement speed doubles.");
            }
            case SICK -> {
                sb.append(col).append(" piece ").append(name).append(" teleported to Alpha.\n");
                sb.append(col).append(" piece ").append(name).append(" feels sick, and movement speed halves.");
            }
            case BRIEFING -> {
                sb.append(col).append(" piece ").append(name).append(" teleported to Beta.\n");
                sb.append(col).append(" piece ").append(name).append(" attends briefing and cannot move for four rounds.");
            }
            case BRIEFING_ROLL_THREE_TELEPORT_BASE ->
                sb.append(col).append(" piece ").append(name)
                        .append(" is movement-restricted and has rolled three consecutively. Teleporting piece ")
                        .append(name).append(" to base.");
            case GAMMA_REVERSE_TO_COUNTER_CLOCKWISE -> {
                sb.append(col).append(" piece ").append(name).append(" teleported to Gamma.\n");
                sb.append("The ").append(col).append(" piece ").append(name)
                        .append(", which was moving clockwise, has changed to moving counterclockwise.");
            }
            case GAMMA_TO_BETA -> {
                sb.setLength(0);
                sb.append(col).append(" player lands on a mystery cell and is teleported to Gamma.\n");
                sb.append(col).append(" piece ").append(name).append(" teleported to Gamma.\n");
                sb.append("The ").append(col).append(" piece ").append(name)
                        .append(" is moving in a counterclockwise direction. Teleporting to Beta from Gamma.");
            }
            case NONE -> {
                if ("Approach".equalsIgnoreCase(e.targetDestinationName())) {
                    sb.append(col).append(" piece ").append(name).append(" teleported to Approach.");
                } else if ("X".equalsIgnoreCase(e.targetDestinationName())) {
                    sb.append(col).append(" piece ").append(name).append(" teleported to X.");
                } else if ("Base".equalsIgnoreCase(e.targetDestinationName())) {
                    sb.append(col).append(" piece ").append(name).append(" teleported to Base.");
                } else {
                    sb.append(col).append(" piece ").append(name).append(" teleported to ")
                            .append(e.targetDestinationName()).append(".");
                }
            }
        }
        return sb.toString();
    }

    private String formatRoundEnded(RoundEndedEvent e) {
        StringBuilder sb = new StringBuilder();
        for (PlayerStatusDTO status : e.playerStatuses()) {
            String col = status.color().getCapitalizedName();
            sb.append(col).append(" player now has ")
                    .append(status.piecesOnBoardCount()).append("/4 on pieces on the board and ")
                    .append(status.piecesInBaseCount()).append("/4 pieces on the base.\n");
            sb.append("============================\n");
            sb.append("Location of pieces [").append(col).append("]\n");
            sb.append("============================\n");

            for (PieceDTO piece : status.pieces()) {
                String loc = piece.cellId();
                if (piece.isInHome()) {
                    loc = "Home";
                } else if (piece.isAtBase()) {
                    loc = "Base";
                }
                sb.append("Piece ").append(piece.id()).append(" -> ").append(loc).append(".\n");
            }
        }

        if (e.mysteryCellActive()) {
            sb.append("The mystery cell is at ").append(e.mysteryCellLocation())
                    .append(" and will be at that location for the next ")
                    .append(e.mysteryRoundsRemaining()).append(" values.");
        }
        return sb.toString().stripTrailing();
    }

    private String formatGameOver(GameOverEvent e) {
        if (!e.isSummary()) {
            if (e.finishPosition() == 1) {
                return e.winnerColor().getCapitalizedName() + " player wins!!!";
            }
            String suffix = switch (e.finishPosition()) {
                case 2 -> "2nd";
                case 3 -> "3rd";
                case 4 -> "4th";
                default -> e.finishPosition() + "th";
            };
            return e.winnerColor().getCapitalizedName() + " player finishes in " + suffix + " place.";
        }

        finalSummaryEvents.add(e);
        if (finalSummaryEvents.size() == 4) {
            StringBuilder sb = new StringBuilder();
            sb.append("\n============================================================\n");
            sb.append("                 FINAL TOURNAMENT SUMMARY\n");
            sb.append("============================================================\n");

            for (GameOverEvent item : finalSummaryEvents) {
                String pos = switch (item.finishPosition()) {
                    case 1 -> "1st Place (Winner)";
                    case 2 -> "2nd Place";
                    case 3 -> "3rd Place";
                    case 4 -> "4th Place";
                    default -> item.finishPosition() + "th Place";
                };
                sb.append(String.format("%-18s: %-8s player | Round: %-3d | Moves: %d%n",
                        pos,
                        item.winnerColor().getCapitalizedName(),
                        item.totalRounds(),
                        item.movesTaken()));
            }
            sb.append("============================================================");
            return sb.toString();
        }

        return null;
    }
}
