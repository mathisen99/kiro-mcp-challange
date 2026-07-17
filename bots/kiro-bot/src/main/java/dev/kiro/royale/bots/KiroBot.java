package dev.kiro.royale.bots;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.events.HitByBulletEvent;
import dev.robocode.tankroyale.botapi.events.HitBotEvent;
import dev.robocode.tankroyale.botapi.events.HitWallEvent;
import dev.robocode.tankroyale.botapi.events.ScannedBotEvent;

/**
 * The primary editable Kiro strategy.
 * It controls engagement range, changes orbit direction proactively, and predicts a
 * wall-bounded firing position while keeping gun, radar, and body independent.
 */
public final class KiroBot extends Bot {
    private static final double WALL_MARGIN = 55;
    private static final double PREFERRED_DISTANCE = 300;
    private static final int ORBIT_REVERSAL_INTERVAL = 32;

    private int movementDirection = 1;
    private int lastScanTurn = -100;
    private int lastDirectionChangeTurn;

    public static void main(String[] args) {
        new KiroBot().start();
    }

    @Override
    public void run() {
        setAdjustGunForBodyTurn(true);
        setAdjustRadarForBodyTurn(true);
        setAdjustRadarForGunTurn(true);
        setMaxSpeed(8);

        while (isRunning()) {
            if (getTurnNumber() - lastScanTurn > 5) {
                // Search aggressively while continuing a broad evasive circle.
                setTurnRadarRight(60);
                setTurnRight(10 * movementDirection);
                setForward(120);
            }
            go();
        }
    }

    @Override
    public void onScannedBot(ScannedBotEvent event) {
        lastScanTurn = getTurnNumber();
        changeOrbitDirectionIfDue();

        double distance = distanceTo(event.getX(), event.getY());
        double power = chooseFirepower(distance);
        double[] aimPoint = predictAimPoint(event, distance, power);
        double gunTurn = gunBearingTo(aimPoint[0], aimPoint[1]);

        setTurnGunLeft(gunTurn);
        setTurnRadarLeft(radarBearingTo(event.getX(), event.getY()) * 2);

        // Blend perpendicular orbiting with approach/retreat pressure to hold range.
        double rangeAdjustment = clamp((distance - PREFERRED_DISTANCE) / 5, -35, 35);
        double orbitBearing = bearingTo(event.getX(), event.getY())
                + 90 * movementDirection - rangeAdjustment;
        setTurnLeft(normalizeRelativeAngle(orbitBearing));
        setForward(145);

        // Require tighter alignment as distance increases to conserve energy.
        double firingTolerance = clamp(10 - distance / 75, 3, 9);
        if (Math.abs(gunTurn) <= firingTolerance
                && getGunHeat() <= 0.01
                && getEnergy() > power + 1) {
            setFire(power);
        }
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        reverseMovement();
        setTurnLeft(normalizeRelativeAngle(
                calcBearing(event.getBullet().getDirection()) + 90 * movementDirection));
        setForward(150);
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        reverseMovement();
        setTurnRight(80 * movementDirection);
        setForward(170);
    }

    @Override
    public void onHitBot(HitBotEvent event) {
        double gunTurn = gunBearingTo(event.getX(), event.getY());
        setTurnGunLeft(gunTurn);
        if (Math.abs(gunTurn) < 8 && getGunHeat() <= 0.01 && getEnergy() > 3.5) {
            setFire(2.5);
        }
        reverseMovement();
        setTurnLeft(normalizeRelativeAngle(
                bearingTo(event.getX(), event.getY()) + 110 * movementDirection));
        setForward(130);
    }

    private double[] predictAimPoint(ScannedBotEvent event, double distance, double power) {
        double predictedX = event.getX();
        double predictedY = event.getY();
        double enemyDirection = Math.toRadians(event.getDirection());
        double bulletSpeed = calcBulletSpeed(power);
        int travelTurns = Math.min(55, (int) Math.ceil(distance / bulletSpeed));

        // Step the estimate turn by turn so wall clamping takes effect before firing.
        for (int turn = 0; turn < travelTurns; turn++) {
            predictedX = clamp(predictedX + Math.cos(enemyDirection) * event.getSpeed(),
                    WALL_MARGIN, getArenaWidth() - WALL_MARGIN);
            predictedY = clamp(predictedY + Math.sin(enemyDirection) * event.getSpeed(),
                    WALL_MARGIN, getArenaHeight() - WALL_MARGIN);
        }
        return new double[] {predictedX, predictedY};
    }

    private double chooseFirepower(double distance) {
        if (getEnergy() < 15) return 0.8;
        if (distance < 170 && getEnergy() > 40) return 2.8;
        if (distance < 350) return 2.0;
        if (distance < 550) return 1.35;
        return 0.9;
    }

    private void changeOrbitDirectionIfDue() {
        if (getTurnNumber() - lastDirectionChangeTurn >= ORBIT_REVERSAL_INTERVAL) {
            reverseMovement();
        }
    }

    private void reverseMovement() {
        if (getTurnNumber() - lastDirectionChangeTurn < 8) return;
        movementDirection = -movementDirection;
        lastDirectionChangeTurn = getTurnNumber();
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
