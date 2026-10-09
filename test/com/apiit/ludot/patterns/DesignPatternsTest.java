package com.apiit.ludot.patterns;

import com.apiit.ludot.adapters.presenter.EventCollectorPresenter;
import com.apiit.ludot.domain.model.Board;
import com.apiit.ludot.domain.model.CellType;
import com.apiit.ludot.domain.model.Color;
import com.apiit.ludot.domain.model.Direction;
import com.apiit.ludot.domain.model.Piece;
import com.apiit.ludot.domain.model.Player;
import com.apiit.ludot.domain.service.ICoin;
import com.apiit.ludot.domain.service.MockCoin;
import com.apiit.ludot.patterns.command.NullCommand;
import com.apiit.ludot.patterns.command.SpawnPieceCommand;
import com.apiit.ludot.patterns.factory.CellFactory;
import com.apiit.ludot.patterns.factory.StrategyFactory;
import com.apiit.ludot.patterns.gateway.InMemorySimulationGateway;
import com.apiit.ludot.patterns.nullobject.NullMove;
import com.apiit.ludot.patterns.observer.GameEventPublisher;
import com.apiit.ludot.patterns.observer.events.DiceRolledEvent;
import com.apiit.ludot.patterns.observer.events.GameStartedEvent;
import com.apiit.ludot.patterns.observer.events.PieceSpawnedEvent;
import com.apiit.ludot.patterns.singleton.BoardRegistry;
import com.apiit.ludot.patterns.strategy.RedAggressiveStrategy;
import com.apiit.ludot.patterns.strategy.YellowRacerStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Design Patterns Tests")
class DesignPatternsTest {

    private Board board;
    private GameEventPublisher publisher;
    private EventCollectorPresenter collector;

    @BeforeEach
    void setUp() {
        board = new Board();
        publisher = new GameEventPublisher();
        collector = new EventCollectorPresenter();
        publisher.subscribe(collector);
    }

    @Test
    @DisplayName("[Singleton] BoardRegistry always returns the same instance")
    void boardRegistryAlwaysReturnsSameInstance() {
        BoardRegistry first = BoardRegistry.getInstance();
        BoardRegistry second = BoardRegistry.getInstance();
        assertSame(first, second, "BoardRegistry must be a singleton");
    }

    @Test
    @DisplayName("[Singleton] BoardRegistry alpha cell is at position 8")
    void boardRegistryAlphaCellPosition() {
        assertEquals(8, BoardRegistry.getInstance().getAlphaCellIndex(),
                "Alpha cell must be at index 8 (Yellow approach 51 + 9 offset mod 52)");
    }

    @Test
    @DisplayName("[Singleton] BoardRegistry beta cell is at position 26")
    void boardRegistryBetaCellPosition() {
        assertEquals(26, BoardRegistry.getInstance().getBetaCellIndex(),
                "Beta cell must be at position 26");
    }

    @Test
    @DisplayName("[Command] SpawnPieceCommand places piece on start X and publishes event")
    void spawnPieceCommandPlacesPieceOnStartX() {
        Player player = new Player(Color.RED, new RedAggressiveStrategy());
        Piece piece = player.getPiece(1);
        ICoin coin = new MockCoin(Direction.CLOCKWISE);

        SpawnPieceCommand cmd = new SpawnPieceCommand(player, piece, board, coin, publisher);
        cmd.execute();

        assertTrue(cmd.getResult().success(), "Spawn command should succeed");
        assertTrue(piece.getState().isOnTrack(), "Piece should be on track after spawn");
        assertEquals(Color.RED.getStartXIndex(), piece.getTrackIndex(),
                "Piece should be at Red's starting X");
        assertTrue(collector.hasEventOfType(PieceSpawnedEvent.class),
                "PieceSpawnedEvent should be published");
    }

    @Test
    @DisplayName("[Command] SpawnPieceCommand grants bonus roll (roll of 6)")
    void spawnCommandGrantsBonusRoll() {
        Player player = new Player(Color.YELLOW, new YellowRacerStrategy());
        Piece piece = player.getPiece(1);
        ICoin coin = new MockCoin(Direction.CLOCKWISE);

        SpawnPieceCommand cmd = new SpawnPieceCommand(player, piece, board, coin, publisher);
        cmd.execute();

        assertTrue(cmd.getResult().bonusRollEarned(),
                "Spawning on 6 should grant a bonus roll");
    }

    @Test
    @DisplayName("[Command] NullCommand executes without error and returns failed result")
    void nullCommandExecutesSafely() {
        NullCommand nullCmd = new NullCommand("No legal move available");
        assertDoesNotThrow(nullCmd::execute, "NullCommand should not throw");
        assertFalse(nullCmd.getResult().success(),
                "NullCommand result should indicate failure");
        assertEquals("No legal move available", nullCmd.getResult().description());
    }

    @Test
    @DisplayName("[Observer] Subscribed listener receives published events")
    void subscribedListenerReceivesEvents() {
        publisher.publish(new GameStartedEvent(
                java.util.List.of(Color.RED, Color.GREEN, Color.YELLOW, Color.BLUE)));

        assertTrue(collector.hasEventOfType(GameStartedEvent.class),
                "Collector should have received GameStartedEvent");
    }

    @Test
    @DisplayName("[Observer] Unsubscribed listener no longer receives events")
    void unsubscribedListenerNoLongerReceivesEvents() {
        publisher.unsubscribe(collector);
        publisher.publish(new GameStartedEvent(java.util.List.of(Color.RED)));

        assertFalse(collector.hasEventOfType(GameStartedEvent.class),
                "Unsubscribed collector should not receive events");
    }

    @Test
    @DisplayName("[Observer] Multiple listeners all receive the same event")
    void multipleListenersAllReceiveEvents() {
        EventCollectorPresenter second = new EventCollectorPresenter();
        publisher.subscribe(second);

        publisher.publish(new DiceRolledEvent(Color.RED, 6));

        assertTrue(collector.hasEventOfType(DiceRolledEvent.class));
        assertTrue(second.hasEventOfType(DiceRolledEvent.class),
                "Both listeners should receive the event");
    }

    @Test
    @DisplayName("[Factory] StrategyFactory creates correct strategy per colour")
    void strategyFactoryCreatesCorrectStrategy() {
        assertEquals("RedAggressiveStrategy",
                StrategyFactory.createStrategyForColor(Color.RED).getStrategyName());
        assertEquals("GreenBlockerStrategy",
                StrategyFactory.createStrategyForColor(Color.GREEN).getStrategyName());
        assertEquals("YellowRacerStrategy",
                StrategyFactory.createStrategyForColor(Color.YELLOW).getStrategyName());
        assertEquals("BlueMysteryStrategy",
                StrategyFactory.createStrategyForColor(Color.BLUE).getStrategyName());
    }

    @Test
    @DisplayName("[Factory] StrategyFactory returns NullStrategy for null colour")
    void strategyFactoryHandlesNullColor() {
        assertEquals("NullStrategy",
                StrategyFactory.createStrategyForColor(null).getStrategyName(),
                "Null colour should yield NullPlayerStrategy");
    }

    @Test
    @DisplayName("[Factory] CellFactory creates correct track cell types")
    void cellFactoryCreatesCorrectCellTypes() {
        assertEquals(CellType.STARTING_X, CellFactory.createTrackCell(26).getCellType(),
                "Cell 26 should be Red's STARTING_X");
        assertEquals(CellType.APPROACH, CellFactory.createTrackCell(25).getCellType(),
                "Cell 25 should be Red's APPROACH");
        assertEquals(CellType.STANDARD, CellFactory.createTrackCell(5).getCellType(),
                "Cell 5 should be STANDARD");
    }

    @Test
    @DisplayName("[Factory] CellFactory creates home straight cells with correct type and colour")
    void cellFactoryCreatesHomeStraightCells() {
        var cell = CellFactory.createHomeStraightCell(Color.GREEN, 2);
        assertEquals(CellType.HOME_STRAIGHT, cell.getCellType());
        assertEquals(Color.GREEN, cell.getAssociatedColor());
        assertEquals("greenhomepath2", cell.getCellId());
    }

    @Test
    @DisplayName("[Gateway] InMemoryGateway records game start and turn logs")
    void inMemoryGatewayRecordsTurnLogs() {
        InMemorySimulationGateway gateway = new InMemorySimulationGateway();
        String simId = "test-001";

        gateway.recordGameStart(simId, java.util.List.of("red", "green", "yellow", "blue"));
        gateway.recordTurn(simId, 1, "red", 6, "Spawned R1 to 26");

        java.util.List<String> logs = gateway.getTurnLogs(simId);
        assertEquals(2, logs.size(), "Should have 2 log entries");
        assertTrue(logs.get(0).contains("GAME_START"));
        assertTrue(logs.get(1).contains("rolled 6"));
    }

    @Test
    @DisplayName("[Gateway] InMemoryGateway records game over with winner")
    void inMemoryGatewayRecordsGameOver() {
        InMemorySimulationGateway gateway = new InMemorySimulationGateway();
        gateway.recordGameStart("sim-001", java.util.List.of("red"));
        gateway.recordGameOver("sim-001", "yellow", 150);

        java.util.List<String> logs = gateway.getTurnLogs("sim-001");
        assertTrue(logs.stream().anyMatch(l -> l.contains("GAME_OVER") && l.contains("yellow")),
                "Game over log should record winner as yellow");
    }

    @Test
    @DisplayName("[Null Object] NullMove returns a decision with hasMove=false")
    void nullMoveHasNoMove() {
        var nullMove = NullMove.getInstance();
        assertFalse(nullMove.hasMove(), "NullMove should indicate no move available");
        assertNull(nullMove.getCandidate(), "NullMove should have null candidate");
    }

    @Test
    @DisplayName("[Null Object] Multiple calls to NullMove return same instance (Flyweight-like)")
    void nullMoveSameInstance() {
        var first = NullMove.getInstance();
        var second = NullMove.getInstance();
        assertSame(first, second, "NullMove should be the same singleton instance");
    }
}
