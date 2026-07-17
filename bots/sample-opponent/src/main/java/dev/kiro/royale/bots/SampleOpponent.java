package dev.kiro.royale.bots;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.events.HitBotEvent;
import dev.robocode.tankroyale.botapi.events.HitWallEvent;
import dev.robocode.tankroyale.botapi.events.ScannedBotEvent;

/** A deterministic event-response opponent with no random decisions. */
public final class SampleOpponent extends Bot {
    private int movementDirection = 1;

    public static void main(String[] args) {
        new SampleOpponent().start();
    }

    @Override
    public void run() {
        setAdjustGunForBodyTurn(true);
        while (isRunning()) {
            setTurnRight(10_000 * movementDirection);
            setMaxSpeed(5);
            forward(10_000 * movementDirection);
        }
    }

    @Override
    public void onScannedBot(ScannedBotEvent event) {
        fire(1.5);
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        movementDirection = -movementDirection;
        setTurnRight(90 * movementDirection);
        forward(140 * movementDirection);
    }

    @Override
    public void onHitBot(HitBotEvent event) {
        fire(2.0);
        movementDirection = -movementDirection;
        back(80);
    }
}
