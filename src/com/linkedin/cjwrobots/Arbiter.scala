package com.linkedin.cjwrobots

import scala.collection.mutable.Map

object Arbiter {

  val overminds : Map[String, OverMind] = Map[String, OverMind]()
  
  def register(bot : Hydralisk) : OverMind = {
    val team = bot.getTeammates
    val sorted_team = team.sortWith(_.compareTo(_) < 0)
    
    val team_name = ("" /: sorted_team) {(name, total) =>
      total + name
    }
    
    val overmind = overminds.getOrElse(team_name, new OverMind())
    if (!overminds.contains(team_name))
      overminds.put(team_name, overmind)
    
    overmind.register(bot)
    overmind
  }
}