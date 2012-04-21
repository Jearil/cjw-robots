#!/bin/sh

# Please check out the contents of robocode/battles/test.battle for the definition of the battle which will be invoked.
# This should then reference the team files which can be found under robocode/robots/<team>.
# Eg Both the definition and sample of the team can be found udner robocode/robots/sampleteam (MyFirstTeam.team & *.class)

cd robocode
java -Xmx2048M -cp libs/robocode.jar robocode.Robocode -battle test -record sample1
