package com.linkedin.cjwrobots

import robocode.ScannedRobotEvent
import java.lang.Double
import scala.collection.mutable.Map
import robocode._

/**
 * Singleton that controls all of the other robots.
 */
object OverMind {
  var enemyRange : Double = null
  val hive : Map[String, Hydralisk] = Map[String, Hydralisk]()
  
  def register(bot : Hydralisk) {
    hive.put(bot.getName(), bot)
  }
  
  def onScannedRobot(hydralisk : String, event : ScannedRobotEvent) {
    //TODO HIGH cmiller: Do something with this
  }

  def onStatus(hydralisk : String,  event : StatusEvent) {
    //TODO HIGH cmiller: Do something with this
  }

  def onBulletHit(hydralisk : String, event: BulletHitEvent) {
    //TODO HIGH cmiller: Do Something
  }

  def onBulletHitBullet(hydralisk : String, event: BulletHitBulletEvent) {
    //TODO HIGH cmiller: Do Something
  }

  def onBulletMissed(hydralisk : String, event: BulletMissedEvent) {
    //TODO HIGH cmiller: Do Something
  }

  def onHitByBullet(hydralisk : String, event: HitByBulletEvent) {
    //TODO HIGH cmiller: Do Something
  }

  def onHitRobot(hydralisk : String, event: HitRobotEvent) {
    //TODO HIGH cmiller: Do Something
  }

  def onHitWall(hydralisk : String, event: HitWallEvent) {
    //TODO HIGH cmiller: Do Something
  }

  def onRobotDeath(hydralisk : String, event: RobotDeathEvent) {
    //TODO HIGH cmiller: Do Something
  }

  def onWin(hydralisk : String, event: WinEvent) {
    //TODO HIGH cmiller: Do Something
  }

  def onRoundEnded(hydralisk : String, event: RoundEndedEvent) {
    //TODO HIGH cmiller: Do Something
  }

  def onBattleEnded(hydralisk : String, event: BattleEndedEvent) {
    //TODO HIGH cmiller: Do Something
  }

  def onDeath(hydralisk : String, event: DeathEvent) {
    //TODO HIGH cmiller: Do Something
  }

  def onSkippedTurn(hydralisk : String, event: SkippedTurnEvent) {
    //TODO HIGH cmiller: Do Something
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
