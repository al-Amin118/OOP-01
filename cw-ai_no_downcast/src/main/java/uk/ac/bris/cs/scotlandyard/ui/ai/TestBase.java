package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import javax.annotation.Nonnull;

import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.*;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.DETECTIVE_LOCATIONS;

public class TestBase {
    /**
     * @return a working black player with the default tickets and default location, see
     * {@link ScotlandYard#defaultMrXTickets} and {@link ScotlandYard#MRX_LOCATIONS}
     */
    @Nonnull
    static Player blackPlayer() {
        return new Player(MRX, defaultMrXTickets(), MRX_LOCATIONS.get(0));
    }
    /**
     * @return a working red player with the default tickets and default location, see
     * {@link ScotlandYard#defaultDetectiveTickets} and {@link ScotlandYard#DETECTIVE_LOCATIONS}
     */
    @Nonnull static Player redPlayer() {
        return new Player(RED, defaultDetectiveTickets(), DETECTIVE_LOCATIONS.get(1));
    }
    /**
     * @return a working green player with the default tickets and default location, see
     * {@link ScotlandYard#defaultDetectiveTickets} and {@link ScotlandYard#DETECTIVE_LOCATIONS}
     */
    @Nonnull static Player greenPlayer() {
        return new Player(GREEN, defaultDetectiveTickets(), DETECTIVE_LOCATIONS.get(2));
    }
    /**
     * @return a working blue player with the default tickets and default location, see
     * {@link ScotlandYard#defaultDetectiveTickets} and {@link ScotlandYard#DETECTIVE_LOCATIONS}
     */
    @Nonnull static Player bluePlayer() {
        return new Player(BLUE, defaultDetectiveTickets(), DETECTIVE_LOCATIONS.get(3));
    }
    /**
     * @return a working yellow player with the default tickets and default location, see
     * {@link ScotlandYard#defaultDetectiveTickets} and {@link ScotlandYard#DETECTIVE_LOCATIONS}
     */
    @Nonnull static Player yellowPlayer() {
        return new Player(YELLOW, defaultDetectiveTickets(), DETECTIVE_LOCATIONS.get(4));
    }
    /**
     * @return a working white player with the default tickets and default location, see
     * {@link ScotlandYard#defaultDetectiveTickets} and {@link ScotlandYard#DETECTIVE_LOCATIONS}
     */
    @Nonnull static Player whitePlayer() {
        return new Player(WHITE, defaultDetectiveTickets(), DETECTIVE_LOCATIONS.get(5));
    }
}
