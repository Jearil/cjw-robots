package cjwrobot.pmove;
import cjwrobot.RumbleBot;
import cjwrobot.TeamUtils;
import cjwrobot.utils.PUtils;
import cjwrobot.utils.TeamWave;
import cjwrobot.utils.Wave;
import robocode.*;
import java.util.*;
import java.awt.geom.*;

//Butterfly, a movement by PEZ. For Hydra - Float like a butterfly!
//http://robowiki.net/?Hydra

//This code is released under the RoboWiki Public Code Licence (RWPCL), datailed on:
//http://robowiki.net/?RWPCL
//(Basically it means you must keep the code public if you base any bot on it.)

//$Id: Butterfly.java,v 1.16 2007-02-28 06:14:58 peters Exp $


public class Butterfly {
	public static boolean isMC;

	public static boolean doGL; // GL

	static final double MAX_VELOCITY = 8;
	static final double MAX_TURN_RATE = 10;

	static final double MAX_WALL_SMOOTH_TRIES = 175;
	static final double WALL_MARGIN = 20;
	static final double DEFAULT_BLIND_MANS_STICK = 120;

	static public double wallDistance;
	static Rectangle2D fieldRectangle;
	static Point2D robotLocation = new Point2D.Double();
	static Point2D enemyLocation = new Point2D.Double();
	static double enemyAbsoluteBearing;
	static double enemyDistance;
	static int distanceIndex;
	static double enemyEnergy;
	static double enemyVelocity;
	static double enemyFirePower = 2.5;
	static int lastVelocityIndex;
	static double approachVelocity = 4;
	static double velocity;
	static int timeSinceVChange;
	static double lastForwardSmoothing;
	static double roundNum;
	static long lastScanTime;
	static long time;
	static int bulletsThisRound;

	double roundsLeft;
	AdvancedRobot robot;
    Map<String, TeamWave> _teamWave = new HashMap();

	public Butterfly(AdvancedRobot robot) {
		this.robot = robot;
		MovementWave.init();
		MovementWave.reset();
		enemyEnergy = 100;
		fieldRectangle = PUtils.fieldRectangle(robot, WALL_MARGIN);
		if (roundNum > 0) {
			System.out.println("range hits taken: " + (int)MovementWave.rangeHits + " (average / round: " + PUtils.formatNumber(MovementWave.rangeHits / roundNum) + ")");
		}

		roundsLeft = robot.getNumRounds() - roundNum - 1;
		roundNum++;
		bulletsThisRound = 0;
	}

    public void onTeamFiredBullet(AdvancedRobot bot, double velocity)
    {
        MovementWave wave = new MovementWave(bot, this);
        wave.setGunLocation(new Point2D.Double(bot.getX(), bot.getY()));
        wave.setStartBearing(bot.getGunHeading());
        wave.setBulletVelocity(velocity);
        MovementWave.waves.add(wave);
    }

    public void omTeamMovement(AdvancedRobot mover, double ahead, double turn, double velocity)
    {
        String name = mover.getName();
        TeamWave mate = _teamWave.get(name);
        if (mate == null)
        {
            mate = new TeamWave(mover);
            _teamWave.put(name, mate);
        }
        mate.setAhead(ahead);
        mate.setTurn(turn);
        mate.setVelocity(velocity);
        mate.setX(mover.getX());
        mate.setY(mover.getY());
    }

	public void onScannedRobot(ScannedRobotEvent e) {
		time = robot.getTime();
		if (RumbleBot.enemyIsRammer()) {
			fieldRectangle = PUtils.fieldRectangle(robot, 70);
		}
		MovementWave wave = new MovementWave(robot, this);
		wave.startTime = robot.getTime() - 2;

		double wallDamage = 0;
		if (Math.abs(e.getVelocity()) == 0 && Math.abs(enemyVelocity) > 2.0) {
			wallDamage = Math.max(0, Math.abs(enemyVelocity) / 2 - 1);
		}
		enemyVelocity = e.getVelocity();

		wave.setGunLocation(new Point2D.Double(enemyLocation.getX(), enemyLocation.getY()));
		wave.setStartBearing(wave.gunBearing(robotLocation));

		double enemyDeltaEnergy = enemyEnergy - e.getEnergy() - wallDamage;
		if (enemyDeltaEnergy > 0 && enemyDeltaEnergy <= 3.1) {
			enemyFirePower = enemyDeltaEnergy;
			MovementWave.bullets.add(wave);
			MovementWave.surfables.add(wave);
			bulletsThisRound++;
		}
		enemyEnergy = e.getEnergy();
		double bulletVelocity = PUtils.bulletVelocity(enemyFirePower);
		wave.setBulletVelocity(bulletVelocity);

		double orbitDirection = robotOrbitDirection(wave.gunBearing(robotLocation));
		wave.setOrbitDirection(wave.maxEscapeAngle() * orbitDirection / (double)MovementWave.MIDDLE_FACTOR);

		approachVelocity = velocity * -Math.cos(robot.getHeadingRadians() - (enemyAbsoluteBearing + Math.PI));
		wave.approachIndex = PUtils.index(MovementWave.APPROACH_SLICES, approachVelocity);

		distanceIndex = PUtils.index(MovementWave.DISTANCE_SLICES, enemyDistance);
		wave.bulletPower = enemyFirePower;
		wave.distanceIndex = distanceIndex;
		int velocityIndex = PUtils.index(MovementWave.VELOCITY_SLICES, Math.abs(velocity));
		velocity = robot.getVelocity();
		wave.accelIndex = 0;
		if (velocityIndex != lastVelocityIndex) {
			timeSinceVChange = 0;
			wave.accelIndex = velocityIndex < lastVelocityIndex ? 1 : 2;
		}
		wave.velocityIndex = velocityIndex;
		wave.lastVelocityIndex = lastVelocityIndex;
		lastVelocityIndex = velocityIndex;

		wave.setTargetLocation(robotLocation);

		wave.vChangeIndex = PUtils.index(MovementWave.TIMER_SLICES, timeSinceVChange++ / wave.travelTime());
		wallDistance = wave.wallDistance(1, fieldRectangle);
		wave.wallIndex = PUtils.index(MovementWave.WALL_SLICES, wallDistance);
		double wallDistanceReverse = wave.wallDistance(-1, fieldRectangle);
		wave.wallIndexReverse = PUtils.index(MovementWave.WALL_SLICES_REVERSE, wallDistanceReverse);


		robotLocation.setLocation(new Point2D.Double(robot.getX(), robot.getY()));
		enemyAbsoluteBearing = robot.getHeadingRadians() + e.getBearingRadians();
		enemyLocation.setLocation(PUtils.project(robotLocation, enemyAbsoluteBearing, enemyDistance));
		enemyDistance = e.getDistance();

		MovementWave.waves.add(wave);

		move(wave, orbitDirection);

		MovementWave.reset();
		lastScanTime = robot.getTime();
	}

	public void onHitByBullet(HitByBulletEvent e) {
		Bullet b = e.getBullet();
		if (false) {
			Hit hit = new Hit(b.getPower(), enemyDistance, robotLocation, enemyLocation);
			MovementWave wave = (MovementWave)Wave.findClosest(MovementWave.bullets, new Point2D.Double(b.getX(), b.getY()), b.getVelocity());
			if (wave != null) {
				hit.gf = wave.getGF(new Point2D.Double(b.getX(), b.getY()));
			}
			Hit.hits.add(hit);
			hit.print();
		}
		MovementWave.hitsTaken++;
		if (b.getPower() > 1.2 && enemyDistance > 150) {
			MovementWave.rangeHits++;
		}
		MovementWave.registerHit(e.getBullet());
		enemyEnergy += 3 * e.getBullet().getPower();
	}

	public void onBulletHit(BulletHitEvent e){
		double power = e.getBullet().getPower();
		enemyEnergy -= 4 * power + Math.max(2 * power - 1, 0);
	}

	public void onBulletHitBullet(BulletHitBulletEvent e) {
		MovementWave.registerHit(e.getHitBullet());
	}

	void move(MovementWave wave, double direction) {
		MovementWave.updateWaves();
		MovementWave closest = (MovementWave)Wave.findClosest(MovementWave.surfables, robotLocation);
		Point2D orbitCenter = orbitCenter(closest);
		if (closest != null) {
			updateDirectionStats(MovementWave.surfables, closest);
		}
        for (TeamWave mate : _teamWave.values())
        {
            updateDirectionStats(MovementWave.surfables, mate, closest);
        }
		Move forward = wallSmoothedDestination(robotLocation, orbitCenter, direction);
		double forwardSmoothingDanger = forward.smoothingDanger();
		lastForwardSmoothing = forward.normalizedSmoothing();
		Move reverse = wallSmoothedDestination(robotLocation, orbitCenter, -direction);
		double reverseSmoothingDanger = reverse.smoothingDanger();
		if (RumbleBot.enemyIsRammer() && (!(forward.normalizedSmoothing() > 75 && reverse.normalizedSmoothing() > 75))) {
			MovementWave.dangerForward += forwardSmoothingDanger;
			MovementWave.dangerReverse += reverseSmoothingDanger;
		}
		else if (!(forward.normalizedSmoothing() > 20 && reverse.normalizedSmoothing() > 20)) {
			MovementWave.dangerForward += forwardSmoothingDanger;
			MovementWave.dangerReverse += reverseSmoothingDanger;
		}
		if (RumbleBot.enemyIsRammer() || forwardSmoothingDanger > 0 && reverseSmoothingDanger > 0) {
			MovementWave.dangerStop = MovementWave.dangerForward + MovementWave.dangerReverse;
		}
		Point2D destination = forward.location;
		double wantedVelocity = MAX_VELOCITY;
		if (MovementWave.hitsTaken == 0 && robot.getEnergy() > 25 && ((roundsLeft < 6 && enemyFirePower < 0.3) || (roundsLeft < 3 && enemyFirePower < (3.01 - roundsLeft)))) {
			if (!isMC) {
				wantedVelocity = 0;
			}
		}
		else if (enemyEnergy > 0 && !RumbleBot.enemyIsRammer() && MovementWave.bullets.size() == 0) {
			if (enemyLocation.distance(reverse.location) / enemyLocation.distance(forward.location) > 1.03) {
				destination = reverse.location;
			}
		}
		else if (!RumbleBot.enemyIsRammer() && MovementWave.dangerStop < MovementWave.dangerReverse && MovementWave.dangerStop < MovementWave.dangerForward) {
			wantedVelocity = 0;
		}
		else if (MovementWave.dangerReverse < MovementWave.dangerForward) {
			destination = reverse.location;
		}
		double newHeading = PUtils.absoluteBearing(robotLocation, destination);
		double oldHeading = robot.getHeadingRadians();

        double ahead = PUtils.backAsFrontDirection(newHeading, oldHeading) * 50;
        double turn =  PUtils.backAsFrontTurn(newHeading, oldHeading);

		robot.setAhead(ahead);
		robot.setTurnRightRadians(turn);
		robot.setMaxVelocity(wantedVelocity);

        TeamUtils.notifyMovement(robot, ahead, turn, wantedVelocity);
	}

	static Move wallSmoothedDestination(Point2D location, Point2D orbitCenter, double direction) {
		Point2D destination = new Point2D.Double();
		destination.setLocation(location);
		double distance = enemyLocation.distance(location);
		double evasion = evasion(distance);
		double blindStick = RumbleBot.enemyIsRammer() ? PUtils.minMax(enemyDistance / 1.7, 40, DEFAULT_BLIND_MANS_STICK) : DEFAULT_BLIND_MANS_STICK;
		double smoothing = 0;
		while (!fieldRectangle.contains(destination = PUtils.project(location,
				PUtils.absoluteBearing(location, orbitCenter) - direction * ((evasion - smoothing / 100) * Math.PI / 2), blindStick)) && smoothing < MAX_WALL_SMOOTH_TRIES) {
			smoothing += 5;
		}
		return new Move(destination, smoothing, evasion, distance, destination.distance(enemyLocation));
	}

	static double evasion(double distance) {
		double evasion;
		if (time < 16) {
			evasion = PUtils.minMax(distance / 700, 1.3, 5.0);
		}
		else {
			if (RumbleBot.enemyIsRammer()) {
				evasion = PUtils.minMax(150.0 / distance, 1.45, 1.65);
			}
			else if (time > 30 && bulletsThisRound == 0) {
				evasion = PUtils.minMax(300.0 / distance, 0.75, 1.5);
			}
			else if (MovementWave.isLowHitRate()) {
				evasion = PUtils.minMax(410.0 / distance, 0.95, 1.25);
			}
			else {
				evasion = PUtils.minMax((300 * Math.pow(MovementWave.hitRate(), 1.2)) / distance, 1.03, 1.3);
			}
		}
		return evasion;
	}

	void updateDirectionStats(List _waves, MovementWave closest) {
		Move move = waveImpactLocation(closest, 1.0, MAX_VELOCITY);
		MovementWave.dangerForward += impactDanger(_waves, move.location);
		move = waveImpactLocation(closest, -1.0, MAX_VELOCITY);
		MovementWave.dangerReverse += impactDanger(_waves, move.location);
		move = waveImpactLocation(closest, 1.0, 0);
		MovementWave.dangerStop += impactDanger(_waves, move.location);
	}

    void updateDirectionStats(List _waves, TeamWave mate, MovementWave closest) {
		Move move = waveImpactLocation(closest, mate, 1.0, MAX_VELOCITY);
		MovementWave.dangerForward += impactDanger(_waves, move.location);
		move = waveImpactLocation(closest, mate, -1.0, MAX_VELOCITY);
		MovementWave.dangerReverse += impactDanger(_waves, move.location);
		move = waveImpactLocation(closest, mate, 1.0, 0);
		MovementWave.dangerStop += impactDanger(_waves, move.location);
	}

    final double collision_fear_factor = 100;

	double impactDanger(List _waves, Point2D impact) {
		double danger = 0;
		for (int i = 0, n = _waves.size(); i < n; i++) {
			danger += ((MovementWave)_waves.get(i)).danger(impact);
		}
        for (TeamWave tw: _teamWave.values()) {
            danger += collision_fear_factor / impact.distance(tw.getX(), tw.getY());
        }
		return danger;
	}

	Move waveImpactLocation(MovementWave closest, double direction, double maxVelocity) {
		double currentDirection = robotOrbitDirection(closest.gunBearing(robotLocation));
		double v = Math.abs(robot.getVelocity()) * PUtils.sign(direction);
		double h = robot.getHeadingRadians();
		Point2D orbitCenter = orbitCenter(closest);
		Point2D impactLocation = new Point2D.Double(robot.getX(), robot.getY());
		Move smoothed = wallSmoothedDestination(impactLocation, orbitCenter, currentDirection * direction);
		double wantedHeading = PUtils.absoluteBearing(impactLocation, smoothed.location);
		h += PUtils.backAsFrontDirection(wantedHeading, h) < 0 ? Math.PI : 0.0;
		int time = 0;
		do {
			double maxTurn = Math.toRadians(MAX_TURN_RATE - 0.75 * Math.abs(v));
			h += PUtils.minMax(PUtils.backAsFrontTurn(wantedHeading, h), -maxTurn, maxTurn);
			if (v < maxVelocity) {
				v = Math.min(maxVelocity, v + (v < 0 ? 2 : 1));
			}
			else {
				v = Math.max(maxVelocity, v - 2);
			}
			impactLocation = PUtils.project(impactLocation, h, v);
			smoothed = wallSmoothedDestination(impactLocation, orbitCenter, currentDirection * direction);
			wantedHeading = PUtils.absoluteBearing(impactLocation, smoothed.location);
		} while (closest.distanceFromTarget(impactLocation, time++) > 18);
		return new Move(impactLocation, smoothed.smoothing, smoothed.wantedEvasion, smoothed.oldDistance, impactLocation.distance(enemyLocation));
	}

    Move waveImpactLocation(MovementWave closest, TeamWave mate, double direction, double maxVelocity) {
		double currentDirection = robotOrbitDirection(mate.gunBearing(robotLocation));
		double v = Math.abs(robot.getVelocity()) * PUtils.sign(direction);
		double h = robot.getHeadingRadians();
		Point2D orbitCenter = orbitCenter(closest);
		Point2D impactLocation = new Point2D.Double(robot.getX(), robot.getY());
		Move smoothed = wallSmoothedDestination(impactLocation, orbitCenter, currentDirection * direction);
		double wantedHeading = PUtils.absoluteBearing(impactLocation, smoothed.location);
		h += PUtils.backAsFrontDirection(wantedHeading, h) < 0 ? Math.PI : 0.0;
		int time = 0;
		do {
			double maxTurn = Math.toRadians(MAX_TURN_RATE - 0.75 * Math.abs(v));
			h += PUtils.minMax(PUtils.backAsFrontTurn(wantedHeading, h), -maxTurn, maxTurn);
			if (v < maxVelocity) {
				v = Math.min(maxVelocity, v + (v < 0 ? 2 : 1));
			}
			else {
				v = Math.max(maxVelocity, v - 2);
			}
			impactLocation = PUtils.project(impactLocation, h, v);
			smoothed = wallSmoothedDestination(impactLocation, orbitCenter, currentDirection * direction);
			wantedHeading = PUtils.absoluteBearing(impactLocation, smoothed.location);
		} while (mate.distanceFromTarget(impactLocation, time++) > 18);
		return new Move(impactLocation, smoothed.smoothing, smoothed.wantedEvasion, smoothed.oldDistance, impactLocation.distance(enemyLocation));
	}

	Point2D orbitCenter(MovementWave wave) {
		return wave != null ? wave.getGunLocation() : enemyLocation;
	}

	double robotOrbitDirection(double bearing) {
		return PUtils.sign(robot.getVelocity() * Math.sin(robot.getHeadingRadians() - bearing));
	}
}

class MovementWave extends Wave {
	static final int FACTORS = 31;
	static final int ACCEL_INDEXES = 3;
	static final int MIDDLE_FACTOR = (FACTORS - 1) / 2;
	static final double[] APPROACH_SLICES = { -3, 1, 3};
	static final double[] DISTANCE_SLICES = { 300, 450, 550, 650 };
	static final double[] VELOCITY_SLICES = { 1, 3, 5, 7 };
	static final double[] WALL_SLICES = { 0.1, 0.2, 0.35, 0.55 };
	static final double[] WALL_SLICES_REVERSE = { 0.35, 0.7 };
	static final double[] TIMER_SLICES = { 0.15, 0.3, 0.7, 1.3 };
	static final int APPROACH_INDEXES = APPROACH_SLICES.length + 1;
	static final int DISTANCE_INDEXES = DISTANCE_SLICES.length + 1;
	static final int VELOCITY_INDEXES = VELOCITY_SLICES.length + 1;
	static final int TIMER_INDEXES = TIMER_SLICES.length + 1;
	static final int WALL_INDEXES = WALL_SLICES.length + 1;
	static final int WALL_INDEXES_REVERSE = WALL_SLICES_REVERSE.length + 1;

	static float[][][][][][] visitCounts;
	static float[] visitCountsFast;
	static float[][][][][] visitCountsTimerWalls;
	static float[][][][][] visitCountsTimer;
	static float[][][][][] visitCountsDistanceVelocityWalls;
	static float[][][][] visitCountsWalls;
	static float[][][][] visitCountsDVA;
	static float[][][] visitCountsVelocityAccel;
	static float[][][] visitCountsVelocityApproach;
	static float[][][] visitCountsDistanceVelocity;
	static float[][] visitCountsVelocity;
	static float[][][][][] hitCountsTimerWalls;
	static float[][][][][] hitCountsTimer;
	static float[][][][][] hitCountsDistanceVelocityWalls;
	static float[][][][] hitCountsWalls;
	static float[][][][] hitCountsDVA;
	static float[][][] hitCountsVelocityAccel;
	static float[][][] hitCountsVelocityApproach;
	static float[][][] hitCountsDistanceVelocity;
	static float[][] hitCountsVelocity;
	static float[] fastHitCounts;
	static float[] randomCounts;

	static double rangeHits;
	static double dangerForward;
	static double dangerReverse;
	static double dangerStop;
	static List waves;
	static List bullets;
	static List surfables;
	static double hitsTaken;

	long startTime;
	Butterfly floater;
	double bulletPower;
	int distanceIndex;
	int velocityIndex;
	int lastVelocityIndex;
	int accelIndex;
	int vChangeIndex;
	int wallIndex;
	int wallIndexReverse;
	int approachIndex;
	boolean visitRegistered;

	static void initStatBuffers() {
		visitCounts = new float[DISTANCE_INDEXES][VELOCITY_INDEXES][ACCEL_INDEXES][TIMER_INDEXES][WALL_INDEXES][FACTORS];
		visitCountsTimerWalls = new float[DISTANCE_INDEXES][VELOCITY_INDEXES][TIMER_INDEXES][WALL_INDEXES][FACTORS];
		visitCountsTimer = new float[DISTANCE_INDEXES][VELOCITY_INDEXES][ACCEL_INDEXES][TIMER_INDEXES][FACTORS];
		visitCountsDistanceVelocityWalls = new float[DISTANCE_INDEXES][VELOCITY_INDEXES][ACCEL_INDEXES][WALL_INDEXES][FACTORS];
		visitCountsDVA = new float[DISTANCE_INDEXES][VELOCITY_INDEXES][ACCEL_INDEXES][FACTORS];
		visitCountsWalls = new float[VELOCITY_INDEXES][ACCEL_INDEXES][WALL_INDEXES][FACTORS];
		visitCountsVelocityAccel = new float[VELOCITY_INDEXES][ACCEL_INDEXES][FACTORS];
		visitCountsVelocityApproach = new float[VELOCITY_INDEXES][APPROACH_INDEXES][FACTORS];
		visitCountsDistanceVelocity = new float[DISTANCE_INDEXES][VELOCITY_INDEXES][FACTORS];
		visitCountsVelocity = new float[VELOCITY_INDEXES][FACTORS];		visitCountsFast = new float[FACTORS];
		hitCountsTimerWalls = new float[DISTANCE_INDEXES][VELOCITY_INDEXES][TIMER_INDEXES][WALL_INDEXES][FACTORS];
		hitCountsTimer = new float[DISTANCE_INDEXES][VELOCITY_INDEXES][ACCEL_INDEXES][TIMER_INDEXES][FACTORS];
		hitCountsDistanceVelocityWalls = new float[DISTANCE_INDEXES][VELOCITY_INDEXES][ACCEL_INDEXES][WALL_INDEXES][FACTORS];
		hitCountsDVA = new float[DISTANCE_INDEXES][VELOCITY_INDEXES][ACCEL_INDEXES][FACTORS];
		hitCountsWalls = new float[VELOCITY_INDEXES][ACCEL_INDEXES][WALL_INDEXES][FACTORS];
		hitCountsVelocityAccel = new float[VELOCITY_INDEXES][ACCEL_INDEXES][FACTORS];
		hitCountsVelocityApproach = new float[VELOCITY_INDEXES][APPROACH_INDEXES][FACTORS];
		hitCountsDistanceVelocity = new float[DISTANCE_INDEXES][VELOCITY_INDEXES][FACTORS];
		hitCountsVelocity = new float[VELOCITY_INDEXES][FACTORS];
		fastHitCounts = new float[FACTORS];
		fastHitCounts[MIDDLE_FACTOR] = 50;
		randomCounts = new float[FACTORS];
	}

	static void init() {
		if (fastHitCounts == null) {
			initStatBuffers();
		}
		waves = new ArrayList();
		bullets = new ArrayList();
		surfables = new ArrayList();
	}

	static void reset() {
		dangerForward = 0;
		dangerReverse = 0;
		dangerStop = 0;
	}

	public MovementWave(AdvancedRobot robot, Butterfly floater) {
		init(robot, FACTORS);
		this.floater = floater;
	}

	static void updateWaves() {
		List reap = new ArrayList();
		for (int i = 0, n = waves.size(); i < n; i++) {
			MovementWave wave = (MovementWave)waves.get(i);
			wave.setDistanceFromGun((robot.getTime() - wave.startTime) * wave.getBulletVelocity());
			if (wave.passed(10)) {
				if (!wave.visitRegistered) {
					wave.registerVisit();
					wave.visitRegistered = true;
				}
			}
			if (wave.passed(wave.getBulletVelocity() * 2)) {
				surfables.remove(wave);
			}
			if (wave.passed(-15)) {
				reap.add(wave);
				bullets.remove(wave);
			}
		}
		for (int i = 0, n = reap.size(); i < n; i++) {
			waves.remove(reap.get(i));
		}
	}

	void registerVisit() {
		int index = visitingIndex();
		float[] visits = visitCounts[distanceIndex][velocityIndex][accelIndex][vChangeIndex][wallIndex];
		float[] visitsFast = visitCountsFast;
		float[] visitsTimerWalls = visitCountsTimerWalls[distanceIndex][velocityIndex][vChangeIndex][wallIndex];
		float[] visitsTimer = visitCountsTimer[distanceIndex][velocityIndex][accelIndex][vChangeIndex];
		float[] visitsDistanceVelocityWalls = visitCountsDistanceVelocityWalls[distanceIndex][velocityIndex][accelIndex][wallIndex];
		float[] visitsWalls = visitCountsWalls[velocityIndex][accelIndex][wallIndex];
		float[] visitsDVA = visitCountsDVA[distanceIndex][velocityIndex][accelIndex];
		float[] visitsVelocityAccel = visitCountsVelocityAccel[velocityIndex][accelIndex];
		float[] visitsVelocityApproach = visitCountsVelocityApproach[velocityIndex][approachIndex];
		float[] visitsDistanceVelocity = visitCountsDistanceVelocity[distanceIndex][velocityIndex];
		float[] visitsVelocity = visitCountsVelocity[velocityIndex];
		registerHit(visits, index, 100.0, 1000.0);
		registerHit(visitsFast, index, 100.0, 1000.0);
		registerHit(visitsTimerWalls, index, 100.0, 1000.0);
		registerHit(visitsTimer, index, 100.0, 1000.0);
		registerHit(visitsDistanceVelocityWalls, index, 100.0, 1000.0);
		registerHit(visitsWalls, index, 100.0, 1000.0);
		registerHit(visitsDVA, index, 100.0, 1000.0);
		registerHit(visitsVelocityAccel, index, 100.0, 1000.0);
		registerHit(visitsVelocityApproach, index, 100.0, 1000.0);
		registerHit(visitsDistanceVelocity, index, 100.0, 1000.0);
		registerHit(visitsVelocity, index, 100.0, 1000.0);
		registerHit(randomCounts, (int)(Math.random() * (FACTORS - 1) + 1), PUtils.minMax(Math.pow(hitRate() * 2.1, 2), 0, 60), 10.0);
	}

	static void registerHit(Bullet bullet) {
		Point2D bulletLocation = new Point2D.Double(bullet.getX(), bullet.getY());
		MovementWave wave = (MovementWave) Wave.findClosest(bullets, bulletLocation, bullet.getVelocity());
		if (wave != null) {
			wave.registerHit(bullet.getHeadingRadians());
		}
	}

	void registerHit(double bearing) {
		registerHit(visitingIndex(bearing));
	}

	void registerHit(Point2D hitLocation) {
		registerHit(visitingIndex(hitLocation));
	}

	void registerHit(float[] buffer, int index, double weight, double depth) {
		for (int i = 0; i < FACTORS; i++) {
			buffer[i] =  (float)PUtils.rollingAvg(buffer[i], index == i ? weight : 0.0, depth);
		}
	}

	void registerHit(int index) {
		float[] hitsTimerWalls = hitCountsTimerWalls[distanceIndex][velocityIndex][vChangeIndex][wallIndex];
		float[] hitsTimer = hitCountsTimer[distanceIndex][velocityIndex][accelIndex][vChangeIndex];
		float[] hitsDistanceVelocityWalls = hitCountsDistanceVelocityWalls[distanceIndex][velocityIndex][accelIndex][wallIndex];
		float[] hitsWalls = hitCountsWalls[velocityIndex][accelIndex][wallIndex];
		float[] hitsDVA = hitCountsDVA[distanceIndex][velocityIndex][accelIndex];
		float[] hitsVelocityAccel = hitCountsVelocityAccel[velocityIndex][accelIndex];
		float[] hitsVelocityApproach = hitCountsVelocityApproach[velocityIndex][approachIndex];
		float[] hitsDistanceVelocity = hitCountsDistanceVelocity[distanceIndex][velocityIndex];
		float[] hitsVelocity = hitCountsVelocity[velocityIndex];
		float[] fastHits = fastHitCounts;
		registerHit(hitsTimerWalls, index, 103.0, 1.0);
		registerHit(hitsTimer, index, 103.0, 1.0);
		registerHit(hitsDistanceVelocityWalls, index, 103.0, 1.0);
		registerHit(hitsWalls, index, 103.0, 1.0);
		registerHit(hitsDVA, index, 103.0, 1.0);
		registerHit(hitsVelocityAccel, index, 103.0, 1.0);
		registerHit(hitsVelocityApproach, index, 103.0, 1.0);
		registerHit(hitsDistanceVelocity, index, 103.0, 1.0);
		registerHit(hitsVelocity, index, 103.0, 1.0);
		registerHit(fastHits, index, 103.0, 1.0);
	}

	double danger(Point2D destination) {
		return danger(visitingIndex(destination));
	}

	double danger(int index) {
		return dangerUnWeighed(index) * dangerWeight();
	}

	double dangerUnWeighed(int index) {
		float[] visits = visitCounts[distanceIndex][velocityIndex][accelIndex][vChangeIndex][wallIndex];
		float[] visitsFast = visitCountsFast;
		float[] visitsTimerWalls = visitCountsTimerWalls[distanceIndex][velocityIndex][vChangeIndex][wallIndex];
		float[] visitsTimer = visitCountsTimer[distanceIndex][velocityIndex][accelIndex][vChangeIndex];
		float[] visitsDistanceVelocityWalls = visitCountsDistanceVelocityWalls[distanceIndex][velocityIndex][accelIndex][wallIndex];
		float[] visitsWalls = visitCountsWalls[velocityIndex][accelIndex][wallIndex];
		float[] visitsDVA = visitCountsDVA[distanceIndex][velocityIndex][accelIndex];
		float[] visitsVelocityAccel = visitCountsVelocityAccel[velocityIndex][accelIndex];
		float[] visitsVelocityApproach = visitCountsVelocityApproach[velocityIndex][approachIndex];
		float[] visitsDistanceVelocity = visitCountsDistanceVelocity[distanceIndex][velocityIndex];
		float[] visitsVelocity = visitCountsVelocity[velocityIndex];
		float[] hitsTimerWalls = hitCountsTimerWalls[distanceIndex][velocityIndex][vChangeIndex][wallIndex];
		float[] hitsTimer = hitCountsTimer[distanceIndex][velocityIndex][accelIndex][vChangeIndex];
		float[] hitsDistanceVelocityWalls = hitCountsDistanceVelocityWalls[distanceIndex][velocityIndex][accelIndex][wallIndex];
		float[] hitsDVA = hitCountsDVA[distanceIndex][velocityIndex][accelIndex];
		float[] hitsWalls = hitCountsWalls[velocityIndex][accelIndex][wallIndex];
		float[] hitsVelocityAccel = hitCountsVelocityAccel[velocityIndex][accelIndex];
		float[] hitsVelocityApproach = hitCountsVelocityApproach[velocityIndex][approachIndex];
		float[] hitsDistanceVelocity = hitCountsDistanceVelocity[distanceIndex][velocityIndex];
		float[] hitsVelocity = hitCountsVelocity[velocityIndex];
		float[] fastHits = fastHitCounts;
		double danger = 0;
		for (int i = 1; i < FACTORS; i++) {
			danger += ((hitRate() > 2.0 ? visitsFast[i] + visits[i] + visitsTimerWalls[i] + visitsTimer[i] + visitsDistanceVelocityWalls[i] + visitsWalls[i] + visitsDistanceVelocity[i] + visitsVelocityAccel[i] + visitsVelocityApproach[i] + visitsDVA[i] + visitsVelocity[i] : 0) +
					hitsTimerWalls[i] + hitsTimer[i] + hitsDistanceVelocityWalls[i] + hitsWalls[i] + hitsDistanceVelocity[i] + hitsVelocityAccel[i] + hitsVelocityApproach[i] + hitsDVA[i] + hitsVelocity[i] + fastHits[i]) / roots[Math.abs(index - i)];
			//danger += ((isHighHitRate() ? visitsFast[i] + visits[i] + hitsTimerWalls[i] + hitsTimer[i] + hitsDistanceVelocityWalls[i] + hitsVelocityApproach[i] : 0) + hitsDVA[i] + hitsWalls[i] + hitsDistanceVelocity[i] + hitsVelocityAccel[i] + hitsVelocity[i] + fastHits[i]) / roots[Math.abs(index - i)];
		}
		return danger;
	}

	double dangerWeight() {
		double t = travelTime(Math.abs(distanceFromTarget(0)));
		return bulletPower / t;
	}

	static boolean isHighHitRate() {
		return Butterfly.roundNum > 5 && hitRate() > 2.0;
	}

	static boolean isLowHitRate() {
		return hitRate() < 1.0;
	}

	static double hitRate() {
		return rangeHits / (Butterfly.roundNum + 1);
	}
}

class Move {
	Point2D location;
	double smoothing;
	double wantedEvasion;
	double oldDistance;
	double newDistance;

	Move(Point2D location, double smoothing, double wantedEvasion, double oldDistance, double newDistance) {
		this.location = location;
		this.smoothing = smoothing;
		this.wantedEvasion = wantedEvasion;
		this.oldDistance = oldDistance;
		this.newDistance = newDistance;
	}

	double smoothingDanger() {
		if (normalizedSmoothing() > 65 || (oldDistance > 220 && newDistance < 250) && normalizedSmoothing() > 20) {
			return (1 + smoothing) * 50;
		}
		return 0;
	}

	double normalizedSmoothing() {
		return smoothing / wantedEvasion;
	}
}

class Hit {
	static List hits = new ArrayList();
	double bulletPower;
	int distance;
	int robotX;
	int robotY;
	int enemyX;
	int enemyY;
	double gf = -100;

	Hit(double bulletPower, double distance, Point2D robotLocation, Point2D enemyLocation) {
		this.bulletPower = (double)((int)(bulletPower * 100) / 100.0);
		this.distance = (int)distance;
		this.robotX = (int)robotLocation.getX();
		this.robotY = (int)robotLocation.getY();
		this.enemyX = (int)enemyLocation.getX();
		this.enemyY = (int)enemyLocation.getY();
	}

	void print() {
		System.out.println("GF: " + PUtils.formatNumber(gf) + " - bp: " + bulletPower + " - distance: " + distance + " - robotLocation: " + robotX + ":" + robotY + " - enemyLocation: " + enemyX + ":" + enemyY);
	}

	static void printAll() {
		for (int i = 0, n = Hit.hits.size(); i < n; i++) {
			Hit hit = (Hit)Hit.hits.get(i);
			hit.print();
		}
	}
}
