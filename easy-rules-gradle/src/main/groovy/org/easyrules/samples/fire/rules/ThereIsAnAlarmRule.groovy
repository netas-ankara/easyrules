package org.easyrules.samples.fire.rules

import Action
import Condition
import Rule
import Priority

@Rule(description='The alarm is detected at the fire station')
class ThereIsAnAlarmRule {

    def theWorld

    @Condition
    boolean when() {
    	theWorld.alarm != null
    }

    @Action
    def then() { 
        println "At the Fire Station: There is an Alarm at ${theWorld.alarm.address}"
    }

    @Priority
    int getPriority() { 5 }

}
