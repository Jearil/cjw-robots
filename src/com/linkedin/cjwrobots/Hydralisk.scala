package com.linkedin.cjwrobots

import robocode._


/**
 * Basically a drone for the OverMind.
 */
class Hydralisk extends TeamRobot {

  var shouldFireNext = false
  var foundRobot = false
  var overmind : OverMind = null
  
  override def run() {
    om()
  }
  
  override def onScannedRobot(event : ScannedRobotEvent) {
    om()

    overmind.onScannedRobot(getName, event)
  }

  override def onStatus(event : StatusEvent) {
    om()

    overmind.onStatus(getName, event)
  }

  override def onBulletHit(event: BulletHitEvent) {
    om()
    
    overmind.onBulletHit(getName, event)
  }

  override def onBulletHitBullet(event: BulletHitBulletEvent) {
    om()
    
    overmind.onBulletHitBullet(getName, event)
  }

  override def onBulletMissed(event: BulletMissedEvent) {
    om()
    
    overmind.onBulletMissed(getName, event)
  }

  override def onHitByBullet(event: HitByBulletEvent) {
    om()
    
    overmind.onHitByBullet(getName, event)
  }

  override def onHitRobot(event: HitRobotEvent) {
    om()
    
    overmind.onHitRobot(getName, event)
  }

  override def onHitWall(event: HitWallEvent) {
    om()
    
    overmind.onHitWall(getName, event)
  }

  override def onRobotDeath(event: RobotDeathEvent) {
    om()
    
    overmind.onRobotDeath(getName, event)
  }

  override def onWin(event: WinEvent) {
    om()
    
    overmind.onWin(getName, event)
  }

  override def onRoundEnded(event: RoundEndedEvent) {
    om()
    
    overmind.onRoundEnded(getName, event)
  }

  override def onBattleEnded(event: BattleEndedEvent) {
    om()
    
    overmind.onBattleEnded(getName, event)
  }

  override def onDeath(event: DeathEvent) {
    om()

    overmind.onDeath(getName, event)
  }

  def om() {
    if (overmind == null) {
      overmind = Arbiter.register(this)
    }
  }
}