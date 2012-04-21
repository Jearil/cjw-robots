package com.linkedin.cjwrobots

import robocode.ScannedRobotEvent
import java.lang.Double

/**
 * Singleton that controls all of the other robots.
 */
object OverMind {
  var enemyRange : Double = null
  
  def register(bot : Hydralisk) {
    
  }

  def onScannedRobot(bot : Hydralisk, event : ScannedRobotEvent) {
    if (isTeammate(e.getName)) {
      return
    }
    var heading: Double = e.getHeading
    var bearing: Double = e.getBearing
    var range: Double = e.getDistance
    if (enemyRange == null || enemyRange.doubleValue > range) {
      enemyRange = range
      enemyBearing = normalizeHeading(heading + bearing)
    }
  }
}