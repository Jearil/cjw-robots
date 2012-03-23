package com.linkedin.cjwrobots

import robocode.robotinterfaces.peer.IBasicRobotPeer
import java.io.PrintStream
import robocode.ScannedRobotEvent
import robocode.TeamRobot

/**
 * Basically a drone for the OverMind.
 */
class Hydralisk extends TeamRobot {

  var shouldFireNext = false
  var foundRobot = false
  
  override def run() {
    
  }
  
  override def onScannedRobot(event : ScannedRobotEvent) {

  }
}