package com.linkedin.cjwrobots

import robocode.robotinterfaces.peer.IBasicRobotPeer
import java.io.PrintStream
import robocode.Robot
import robocode.ScannedRobotEvent

/**
 * Basically a drone for the OverMind, but I like Pirates
 */
class PirateBot extends Robot {

  var shouldFireNext = false
  var foundRobot = false
  
  override def run() {
    
  }
  
  override def onScannedRobot(event : ScannedRobotEvent) {

  }
}