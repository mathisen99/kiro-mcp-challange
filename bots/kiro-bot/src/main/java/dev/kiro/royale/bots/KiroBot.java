package dev.kiro.royale.bots;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.events.HitByBulletEvent;
import dev.robocode.tankroyale.botapi.events.ScannedBotEvent;

/**
 * The deliberately small, primary editable Kiro strategy.
 * It patrols in a predictable arc and fires whenever its radar sees the opponent.
 */
public final class KiroBot extends Bot {
    public static void main(String[] args) {
        new KiroBot().start();
    }

    @Override
    public void run() {
        setAdjustGunForBodyTurn(true);
        setAdjustRadarForGunTurn(true);
        while (isRunning()) {
            setTurnRight(32);
            setTurnGunLeft(360);
            forward(180);
            setTurnLeft(48);
            setTurnGunRight(360);
            back(120);
        }
    }

    @Override
    public void onScannedBot(ScannedBotEvent event) {
        fire(getEnergy() > 20 ? 2.0 : 1.0);
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        turnRight(90 - calcBearing(event.getBullet().getDirection()));
        forward(80);
    }
}
