package cjwrobot;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.Robot;
import robocode.ScannedRobotEvent;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jkessler
 * Date: 4/21/12
 * Time: 4:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class TeamUtils
{
    private static List<RumbleBot> _team = new ArrayList<RumbleBot>();
    private static FileWriter _fStream;
    private static BufferedWriter _out;

    {
        try
        {
            _fStream  = new FileWriter("~wfender/CJWDebug.txt");
            _out = new BufferedWriter(_fStream);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public static boolean isTeamMate(String botName) {
        for(RumbleBot bot : _team) {
            if(bot.getName().equals(botName)) return true;
        }
        return false;
    }
    
    public static void teamUpAgainst(String botName) {
        for(RumbleBot bot : _team) {
            bot.setEnemy(botName);
        }
    }

    protected static void addRumbleBot(RumbleBot member)
    {
        _team.add(member);
    }

    protected static void notifyBulletHit(BulletHitEvent e)
    {
        for (RumbleBot bot : _team)
        {
            bot.onTeamBulletHit(e);
        }
    }

    protected static void notifyRobotDeath(robocode.RobotDeathEvent event)
    {
        for (RumbleBot bot : _team)
        {
            bot.onTeamRobotDeath(event);
        }
    }

    public static void notifyBulletFired(AdvancedRobot shooter)
    {
        for (RumbleBot bot : _team)
        {
            if (bot == shooter)
            {
                continue;
            }
            bot.onTeamBulletFired(shooter);
        }
    }

    public static void log(String message)
    {
        /*synchronized (_out)
        {
            try
            {
                _out.write(message);
                _out.flush();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }*/
        System.out.println(message);
    }



    public static ScannedRobotEvent fromPerspectiveOf(ScannedRobotEvent event, Robot source, Robot perspective)
    {
        // compute scanned robot's absolute position
        double sourceX = source.getX();
        double sourceY = source.getY();
        // I always did hate polar coordinates
        double bearing = event.getBearing();
        double sourceBearing = source.getHeading();
        // Calculate enemy bearing
        double enemyBearing = sourceBearing + bearing;
        // Calculate position
        double distance = event.getDistance();
        double enemyX = sourceX + distance * Math.sin(Math.toRadians(enemyBearing));
        double enemyY = sourceY + distance * Math.cos(Math.toRadians(enemyBearing));

        double dx = enemyX - perspective.getX();
        double dy = enemyY - perspective.getY();
        // Calculate angle to target
        double theta = Math.toDegrees(Math.atan2(dx, dy));
        double pDist = Math.sqrt((Math.pow(dx, 2) + Math.pow(dy, 2)));

        return new ScannedRobotEvent(
                event.getName(),
                event.getEnergy(),
                theta - perspective.getHeading(),s
               ,

        )


    }
}
