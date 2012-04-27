package cjwrobot.pgun;

import cjwrobot.RumbleBot;
import cjwrobot.TeamUtils;
import cjwrobot.utils.PUtils;
import cjwrobot.utils.RobotPredictor;
import robocode.*;

import java.awt.geom.*;

//Bee, a gun by PEZ. For Hydra - Sting like a bee!
//http://robowiki.net/?Hydra

//This code is released under the RoboWiki Public Code Licence (RWPCL), datailed on:
//http://robowiki.net/?RWPCL
//(Basically it means you must keep the code public.)

//$Id$

public abstract class Stinger {
	public static boolean isTC = false; // TargetingChallenge

	static final double WALL_MARGIN = 18;
	static final double MAX_BULLET_POWER = 3.0;
	static final double BULLET_POWER = 1.9;

	static double roundNum;
	static double rangeHits;
	static Rectangle2D fieldRectangle;

	static String enemyName = "";

	long lastScanTime;
	GunWave lastWave;
	AdvancedRobot robot;
	RobotPredictor robotPredictor;
	double distance = 0;
	
	public Stinger(AdvancedRobot robot, RobotPredictor robotPredictor) {
		this.robot = robot;
		this.robotPredictor = robotPredictor;
		fieldRectangle = PUtils.fieldRectangle(robot, WALL_MARGIN);
		lastWave = null;
	}

	abstract void scannedRobot(ScannedRobotEvent e);
	abstract void initRound();
	abstract void bulletHit(BulletHitEvent e);
    abstract void teamBulletHit(BulletHitEvent e);
    abstract void teamRobotDeath(robocode.RobotDeathEvent event);
	
	public void onScannedRobot(ScannedRobotEvent e) {
		distance = e.getDistance();
		if (enemyName.equals("") && !TeamUtils.isTeamMate(e.getName())) {
            TeamUtils.log("Robot(" + robot.getName() + ") setting target to " + e.getName());
			enemyName = e.getName();
            TeamUtils.teamUpAgainst(enemyName);
		} else if (e.getName().equals(enemyName)) { // ignore things other than our current target
            if (lastScanTime == 0) {
                initRound();
                System.out.println("range hits given: " + (int)rangeHits + " (average / round: " + java.text.NumberFormat.getNumberInstance().format(hitRate()) + ")");
                roundNum++;
            }

            if (robot.getTime() > lastScanTime) {
                scannedRobot(e);
            }
            lastScanTime = robot.getTime();
        }
	}

	double bulletPower(double distance, double eEnergy, double rEnergy) {
		if (isTC || distance < 130 || RumbleBot.enemyIsRammer()) {
			return MAX_BULLET_POWER;
		}
		double bulletPower = BULLET_POWER;
		if (rEnergy < 10 && eEnergy > rEnergy) {
			bulletPower = 1.0;
		}
		else if (rEnergy < 20 && eEnergy > rEnergy) {
			bulletPower = 1.4;
		}
		else if (rEnergy < 10 && eEnergy > 3) {
			bulletPower = 1.4;
		}
		else if (rEnergy < 20 && eEnergy > 8) {
			bulletPower = 1.6;
		}
		return Math.min(bulletPower, eEnergy / 4.0);
	
	}

	public void roundOver() {
	}

	static double hitRate() {
		if (Bee.roundNum > 0) {
			return rangeHits / roundNum;
		}
		return 0;
	}

	public void onBulletHit(BulletHitEvent e) {
		if (distance > 150 && e.getBullet().getPower() > 1.2) {
			rangeHits++;
		}
		bulletHit(e);
	}

    public void onTeamBulletHit(BulletHitEvent e) {
		teamBulletHit(e);
	}

    public void onTeamRobotDeath(robocode.RobotDeathEvent event) {
		teamRobotDeath(event);
	}
    
    public void setEnemy(String botName) {
        enemyName = botName;
    }
}