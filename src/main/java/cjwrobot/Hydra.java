package cjwrobot;

import java.awt.Color;

import cjwrobot.pgun.Bee;
import cjwrobot.pmove.Butterfly;

//This code is released under the RoboWiki Public Code Licence (RWPCL), datailed on:
//http://robowiki.net/?RWPCL
//(Basically it means you must keep the code public if you base any bot on it.)

//$Id: Hydra.java,v 1.9 2007-02-27 05:49:05 peters Exp $

public class Hydra extends RumbleBot
{
    public Hydra()
    {
        TeamUtils.addRumbleBot(this);
    }

	public void run() {
		floater = new Butterfly(this);
		stinger = new Bee(this, robotPredictor);
		setColors(new Color(60, 30, 10), Color.green, Color.black);
		super.run();
	}
}
