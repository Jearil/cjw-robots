package com.linkedin.cjwrobots

import scala.collection.mutable.Map
import robocode._

/**
 * Singleton that controls all of the other robots.
 */
class OverMind {
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
}