package cjwrobot.utils;

import robocode.AdvancedRobot;

/**
 * Created by IntelliJ IDEA.
 * User: wfender
 * Date: 5/6/12
 * Time: 7:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class TeamWave extends Wave
{
    AdvancedRobot _teamMate;
    private double _ahead;
    private double _turn;
    private double _velocity;
    private double _x;
    private double _y;

    public TeamWave(AdvancedRobot teamMate)
    {
        _teamMate = teamMate;
    }

    public void setAhead(double ahead)
    {
        _ahead = ahead;
    }

    public void setTurn(double turn)
    {
        _turn = turn;
    }

    public void setVelocity(double velocity)
    {
        _velocity = velocity;
    }

    public void setX(double x)
    {
        _x = x;
    }

    public void setY(double y)
    {
        _y = y;
    }

    public double getAhead()
    {
        return _ahead;
    }

    public double getTurn()
    {
        return _turn;
    }

    public double getVelocity()
    {
        return _velocity;
    }

    public double getX()
    {
        return _x;
    }

    public double getY()
    {
        return _y;
    }
}
