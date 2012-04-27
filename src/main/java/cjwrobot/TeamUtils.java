package cjwrobot;

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


    /*
    public static ScannedRobotEvent fromPerspectiveOf(ScannedRobotEvent event, Robot source, Robot Perspective)
    {
        // compute scanned robot's absolute position
        double x;
        double y;
    }*/
}
