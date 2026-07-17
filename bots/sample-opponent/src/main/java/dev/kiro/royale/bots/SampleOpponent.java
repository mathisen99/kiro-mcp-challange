package dev.kiro.royale.bots;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.events.HitByBulletEvent;
import dev.robocode.tankroyale.botapi.events.HitBotEvent;
import dev.robocode.tankroyale.botapi.events.HitWallEvent;
import dev.robocode.tankroyale.botapi.events.ScannedBotEvent;

/** A deterministic direct-aim opponent with no random decisions. */
public final class SampleOpponent extends Bot {
    private int movementDirection = 1;
    private int lastScanTurn = -100;
    private int lastDirectionChangeTurn = -100;

    public static void main(String[] args) {
        new SampleOpponent().start();
    }

    @Override
    public void run() {
        setAdjustGunForBodyTurn(true);
        setAdjustRadarForBodyTurn(true);
        setAdjustRadarForGunTurn(true);
        setMaxSpeed(6);
        while (isRunning()) {
            if (getTurnNumber() - lastScanTurn > 6) {
                setTurnRadarLeft(55);
                setTurnLeft(10 * movementDirection);
                setForward(120 * movementDirection);
            }
            if (getTurnNumber() > 0 && getTurnNumber() % 70 == 0) reverseMovement();
            go();
        }
    }

    @Override
    public void onScannedBot(ScannedBotEvent event) {
        lastScanTurn = getTurnNumber();
        double distance = distanceTo(event.getX(), event.getY());
        double gunTurn = gunBearingTo(event.getX(), event.getY());
        setTurnGunLeft(gunTurn);
        setTurnRadarLeft(radarBearingTo(event.getX(), event.getY()) * 2);
        setTurnLeft(normalizeRelativeAngle(bearingTo(event.getX(), event.getY()) + 90));
        setForward(110 * movementDirection);

        double power = distance < 220 ? 2.2 : distance < 500 ? 1.5 : 0.9;
        if (Math.abs(gunTurn) < 10 && getGunHeat() <= 0.01 && getEnergy() > power + 1) {
            setFire(power);
        }
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        reverseMovement();
        setTurnRight(90 * movementDirection);
        setForward(140 * movementDirection);
    }

    @Override
    public void onHitBot(HitBotEvent event) {
        setTurnGunLeft(gunBearingTo(event.getX(), event.getY()));
        if (getGunHeat() <= 0.01 && getEnergy() > 3) setFire(2.5);
        reverseMovement();
        setForward(90 * movementDirection);
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        reverseMovement();
        setTurnLeft(65 * movementDirection);
        setForward(120 * movementDirection);
    }

    private void reverseMovement() {
        if (getTurnNumber() - lastDirectionChangeTurn < 8) return;
        movementDirection = -movementDirection;
        lastDirectionChangeTurn = getTurnNumber();
    }
}
