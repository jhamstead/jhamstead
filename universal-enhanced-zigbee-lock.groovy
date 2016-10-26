/*
 *  Universal Enhanced ZigBee Lock
 *
 *  2016-10-26 : Major Design Changes: Optimization, Tamper Alarm, Additional Attributes - Version Alpha 0.3
 *  2016-10-17 : Add Auto Lock Time and One Touch Lock Capability - Version Alpha 0.2a
 *  2016-10-16 : Faster Responses by Removing Queue by Querying Lock Log (fix for Yale locks) - Version Alpha 0.2
 *	2016-10-03 : Bug Fixes - Version Alpha 0.1c - Initial Release
 *	2016-10-03 : Add Yale special Verification - Version Alpha 0.1b
 *	2016-10-01 : Bug Fixes - Version Alpha 0.1a
 *	2016-09-28 : Enhanced Capabilities Created - Version Alpha 0.1
 *
 *	This is a modification of work originally copyrighted by "SmartThings."	 All modifications to their work
 *	is released under the following terms:
 *
 *	The original licensing applies, with the following exceptions:
 *		1.	These modifications may NOT be used without freely distributing all these modifications freely
 *			and without limitation, in source form.	 The distribution may be met with a link to source code
 *			with these modifications.
 *		2.	These modifications may NOT be used, directly or indirectly, for the purpose of any type of
 *			monetary gain.	These modifications may not be used in a larger entity which is being sold,
 *			leased, or anything other than freely given.
 *		3.	To clarify 1 and 2 above, if you use these modifications, it must be a free project, and
 *			available to anyone with "no strings attached."	 (You may require a free registration on
 *			a free website or portal in order to distribute the modifications.)
 *		4.	The above listed exceptions to the original licensing do not apply to the holder of the
 *			copyright of the original work.	 The original copyright holder can use the modifications
 *			to hopefully improve their original work.  In that event, this author transfers all claim
 *			and ownership of the modifications to "SmartThings."
 *
 *  Copyright 2015 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
 metadata {
    definition (name: "Universal Enhanced ZigBee Lock", namespace: "smartthings", author: "jhamstead")
    {
        capability "Actuator"
        capability "Lock"
        capability "Refresh"
        capability "Sensor"
        capability "Battery"
        capability "Lock Codes"
        capability "Configuration"
        capability "Polling"
        capability "Tamper Alert"
        
        command "deleteAllCodes"
        command "autoLockToggle"
        command "oneTouchToggle"
        command "resetTamperAlert"
        
        attribute "autoLockTime", "number"
        attribute "oneTouch", "number"
        attribute "numPINUsers", "number"
        //attribute "maxPINLength", "number"
        //attribute "minPINLength", "number"

        fingerprint profileId: "0104", inClusters: "0000,0001,0003,0004,0005,0009,0020,0101,0402,0B05,FDBD", outClusters: "000A,0019", manufacturer: "Kwikset", model: "SMARTCODE_DEADBOLT_5", deviceJoinName: "Kwikset 5-Button Deadbolt"
        fingerprint profileId: "0104", inClusters: "0000,0001,0003,0004,0005,0009,0020,0101,0402,0B05,FDBD", outClusters: "000A,0019", manufacturer: "Kwikset", model: "SMARTCODE_LEVER_5", deviceJoinName: "Kwikset 5-Button Lever"
        fingerprint profileId: "0104", inClusters: "0000,0001,0003,0004,0005,0009,0020,0101,0402,0B05,FDBD", outClusters: "000A,0019", manufacturer: "Kwikset", model: "SMARTCODE_DEADBOLT_10", deviceJoinName: "Kwikset 10-Button Deadbolt"
        fingerprint profileId: "0104", inClusters: "0000,0001,0003,0004,0005,0009,0020,0101,0402,0B05,FDBD", outClusters: "000A,0019", manufacturer: "Kwikset", model: "SMARTCODE_DEADBOLT_10T", deviceJoinName: "Kwikset 10-Button Touch Deadbolt"
        fingerprint profileId: "0104", inClusters: "0000,0001,0003,0009,000A,0101,0020", outClusters: "000A,0019", manufacturer: "Yale", model: "YRL220 TS LL", deviceJoinName: "Yale Touch Screen Lever Lock"
        fingerprint profileId: "0104", inClusters: "0000,0001,0003,0009,000A,0101,0020", outClusters: "000A,0019", manufacturer: "Yale", model: "YRD210 PB DB", deviceJoinName: "Yale Push Button Deadbolt Lock"
        fingerprint profileId: "0104", inClusters: "0000,0001,0003,0009,000A,0101,0020", outClusters: "000A,0019", manufacturer: "Yale", model: "YRD220/240 TSDB", deviceJoinName: "Yale Touch Screen Deadbolt Lock"
        fingerprint profileId: "0104", inClusters: "0000,0001,0003,0009,000A,0101,0020", outClusters: "000A,0019", manufacturer: "Yale", model: "YRL210 PB LL", deviceJoinName: "Yale Push Button Lever Lock"
    }

    tiles(scale: 2) {
		multiAttributeTile(name:"toggle", type:"generic", width:6, height:4){
			tileAttribute ("device.lock", key:"PRIMARY_CONTROL") {
				attributeState "locked", label:'locked', action:"lock.unlock", icon:"st.locks.lock.locked", backgroundColor:"#79b821", nextState:"unlocking"
				attributeState "unlocked", label:'unlocked', action:"lock.lock", icon:"st.locks.lock.unlocked", backgroundColor:"#ffffff", nextState:"locking"
                attributeState "unknown", label:"unknown", action:"lock.lock", icon:"st.locks.lock.unknown", backgroundColor:"#ffffff", nextState:"locking"
				attributeState "locking", label:'locking', icon:"st.locks.lock.locked", backgroundColor:"#79b821"
				attributeState "unlocking", label:'unlocking', icon:"st.locks.lock.unlocked", backgroundColor:"#ffffff"
			}
            tileAttribute ("device.tamper", key:"SECONDARY_CONTROL") {
                attributeState "clear", label:""
                attributeState "detected", label:"Alert!", action:"resetTamperAlert", icon:"st.alarm.alarm.alarm", backgroundColor:"#ff0000"           
            }
		}
		standardTile("lock", "device.lock", inactiveLabel:false, decoration:"flat", width:2, height:2) {
			state "default", label:'lock', action:"lock.lock", icon:"st.locks.lock.locked", nextState:"locking"
		}
		standardTile("unlock", "device.lock", inactiveLabel:false, decoration:"flat", width:2, height:2) {
			state "default", label:'unlock', action:"lock.unlock", icon:"st.locks.lock.unlocked", nextState:"unlocking"
		}
		valueTile("battery", "device.battery", inactiveLabel:false, decoration:"flat", width:2, height:2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}
		standardTile("refresh", "device.refresh", inactiveLabel:false, decoration:"flat", width:2, height:2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        standardTile("autoLockTile", "device.autoLockTile", inactiveLabel:false, decoration:"flat", width:2, height:2) {
            state "autoLockDisabled", label:'Timed Auto Lock Disabled', action:"autoLockToggle", icon: "st.samsung.da.washer_ic_cancel", nextState: "autoLockDChanging"
            state "autoLockEnabled", label:"Timed Auto Lock Enabled", action:"autoLockToggle", icon:"st.Health & Wellness.health7", nextState:"autoLockEChanging"
            state "autoLockDChanging", label:'Updating . . .', icon: "st.samsung.da.washer_ic_cancel"
            state "autoLockEChanging", label:'Updating . . .', icon: "st.Health & Wellness.health7"
		}
        standardTile("oneTouch", "device.oneTouch", inactiveLabel:false, decoration:"flat", width:2, height:2) {
            state "0", label:'One Touch Lock Disabled', action:"oneTouchToggle", icon:"st.security.alarm.on", nextState:"oneTouch0Changing"
            state "1", label:'One Touch Lock Enabled', action:"oneTouchToggle", icon:"st.security.alarm.off", nextState:"oneTouch1Changing"
            state "oneTouch0Changing", label:'Updating . . .', icon:"st.security.alarm.on"
            state "oneTouch1Changing", label:'Updating . . .', icon:"st.security.alarm.off"
		}
		standardTile("tamper", "device.tamper", inactiveLabel:false, decoration:"flat", width:2, height:2) {
			state "default", label:'Reset Alert', action:"resetTamperAlert", icon:"st.alarm.alarm.alarm"
		}
		main "toggle"
		details(["toggle", "lock", "unlock", "battery", "refresh", "autoLockTile", "oneTouch", "tamper"])
	}
    
	preferences {
        section ("Lock Properties"){
            input "autoLock", "number", title: "Auto Lock Timeout (5-180 Seconds)", description: true, defaultValue: 30, required: true, range: "5..180"
        }
	}
}
// Globals
private getCLUSTER_POWER() { 0x0001 }
private getCLUSTER_ALARM() { 0x0009 }
private getCLUSTER_DOORLOCK() { 0x0101 }

private getDOORLOCK_CMD_LOCK_DOOR() { 0x00 }
private getDOORLOCK_CMD_UNLOCK_DOOR() { 0x01 }
private getDOORLOCK_CMD_GET_LOG_RECORD() { 0x04 }
private getDOORLOCK_CMD_USER_CODE_SET() { 0x05 }
private getDOORLOCK_CMD_USER_CODE_GET() { 0x06 }
private getDOORLOCK_CMD_CLEAR_USER_CODE() { 0x07 }
private getDOORLOCK_CMD_CLEAR_ALL_USER_CODE() { 0x08 }
private getDOORLOCK_RESPONSE_OPERATION_EVENT() { 0x20 }
private getDOORLOCK_RESPONSE_PROGRAMMING_EVENT() { 0x21 }
private getPOWER_ATTR_BATTERY_PERCENTAGE_REMAINING() { 0x0021 }
private getALARM_COUNT() { 0x0000 }
private getDOORLOCK_ATTR_LOCKSTATE() { 0x0000 }
private getDOORLOCK_ATTR_NUM_PIN_USERS() { 0x0012 }
private getDOORLOCK_ATTR_MAX_PIN_LENGTH() { 0x0017 }
private getDOORLOCK_ATTR_MIN_PIN_LENGTH() { 0x0018 }
private getDOORLOCK_ATTR_AUTO_RELOCK_TIME() { 0x0023 }
private getDOORLOCK_ATTR_ONE_TOUCH_LOCK() { 0x0029 }
private getDOORLOCK_ATTR_SEND_PIN_OTA() { 0x0032 }

private getTYPE_BOOL() { 0x10 }
private getTYPE_U8() { 0x20 }
private getTYPE_U16() { 0x21 }
private getTYPE_U32() { 0x23 }
private getTYPE_ENUM8() { 0x30 }

// Public methods
def installed() {
    log.trace "installed()"
}

def uninstalled() {
    log.trace "uninstalled()"
}

def configure() {
    state.disableLocalPINStore = false
    
    def cmds =
        zigbee.configureReporting(CLUSTER_DOORLOCK, DOORLOCK_ATTR_LOCKSTATE,
                                  TYPE_ENUM8, 0, 3600, null) +
        zigbee.configureReporting(CLUSTER_POWER, POWER_ATTR_BATTERY_PERCENTAGE_REMAINING,
                                  TYPE_U8, 600, 21600, 0x01) +
        zigbee.configureReporting(CLUSTER_ALARM, ALARM_COUNT,
                                  TYPE_U16, 0, 21600, null) +
        zigbee.configureReporting(CLUSTER_DOORLOCK, DOORLOCK_ATTR_AUTO_RELOCK_TIME,
                                  TYPE_U32, 0, 21600, null) +
        zigbee.configureReporting(CLUSTER_DOORLOCK, DOORLOCK_ATTR_ONE_TOUCH_LOCK,
                                  TYPE_BOOL, 0, 21600, null)
        
    log.info "configure() --- cmds: $cmds"
    return cmds + refresh() // send refresh cmds as part of config     
}

def refresh() { //refresh will override the queue and empty it - in case queue is hung
    def cmds =
        zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_LOCKSTATE) +
        zigbee.readAttribute(CLUSTER_POWER, POWER_ATTR_BATTERY_PERCENTAGE_REMAINING) +
        //zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_SEND_PIN_OTA) +
        //zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_MAX_PIN_LENGTH) + 
        //zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_MIN_PIN_LENGTH) +
        zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_NUM_PIN_USERS) +
        zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_AUTO_RELOCK_TIME) +
        zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_ONE_TOUCH_LOCK)

    log.info "refresh() --- cmds: $cmds"
    return cmds
}

def updated() {

}

def autoLockToggle() {
    def cmds = ""
    def myTime = 30
    //if ( settings.autoLock && device.getDataValue("manufacturer") == "Yale" ) myTime = settings.autoLock
    if ( settings.autoLock ) myTime = settings.autoLock
    if ( device.currentValue("autoLockTime") > 0 ) myTime = 0
    cmds = zigbee.writeAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_AUTO_RELOCK_TIME, TYPE_U32, zigbee.convertToHexString(myTime,8))
    log.debug "autoLockToggle() --- cmds: $cmds"
    return cmds    
}

def oneTouchToggle() {
    def cmds = ""
    def value = 1
    if ( device.currentValue("oneTouch") != 0 ) value = 0
    cmds = zigbee.writeAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_ONE_TOUCH_LOCK, TYPE_BOOL, value)
    log.debug "oneTouchToggle() --- cmds: $cmds"
    return cmds    
}

def parse(String description) {

    log.trace "parse() --- description: $description"
    Map map = [:]
    if (description?.startsWith('read attr -')) {
        map = parseReportAttributeMessage(description)
    }else {
        map = parseResponseMessage(description)
    }
    def result = map ? createEvent(map) : null
    
    log.debug "parse() --- returned: $result"
    
    return result
}

// Polling capability commands
def poll() {
    reportAllCodes()
}

// Lock capability commands
def lock() {
    def cmds = zigbee.command(CLUSTER_DOORLOCK, DOORLOCK_CMD_LOCK_DOOR)
    log.info "lock() -- cmds: $cmds"
    return cmds
}

def unlock() {
    def cmds = zigbee.command(CLUSTER_DOORLOCK, DOORLOCK_CMD_UNLOCK_DOOR)
    log.info "unlock() -- cmds: $cmds"
    return cmds
}

// Additional Command For Tamper Alert
def resetTamperAlert() {
    def resultMap = [:]
    if ( device.currentValue("tamper") == "detected" ) {
        resultMap = [ name: "tamper", descriptionText: "Tamper Alert Acknowledged", isStateChange: true, displayed: true, value: "cleared" ]
    }
    log.debug "resetTamperAlert() --- ${resultMap}"
    sendEvent(resultMap)
}

// Lock Code capability commands
def setCode(codeNumber, code) {
    def octetCode = ""
    def cmds = ""
    if (code.toString().size() <= getMaxPINLength() && code.toString().size() >= getMinPINLength() && code.isNumber() && codeNumber.toInteger() >= 1 && codeNumber.toInteger() <= getNumPINUsers() ){
         log.debug "Setting code $codeNumber to $code"
         code.toString().split('').each {
            if(it.trim()) octetCode += " 3${it}"
         }
        if ( device.getDataValue("manufacturer") == "Yale" ) {
           cmds = zigbee.command(CLUSTER_DOORLOCK, DOORLOCK_CMD_USER_CODE_SET, "${zigbee.convertToHexString(codeNumber.toInteger(),2)}00 01 00 0${code.toString().size()}${octetCode}") +
                  zigbee.command(CLUSTER_DOORLOCK, DOORLOCK_CMD_GET_LOG_RECORD, "0000") // Need to request response from Yale if code set as a programming event is not sent
        } else {
           cmds = zigbee.command(CLUSTER_DOORLOCK, DOORLOCK_CMD_USER_CODE_SET, "${zigbee.convertToHexString(codeNumber.toInteger(),2)}00 01 00 0${code.toString().size()}${octetCode}")
        }
        state["code${codeNumber}"] = encrypt(code)
    }else{
        log.debug "Invalid Input: Unable to set code $codeNumber to $code"
    }
    log.info "setCode() - ${cmds}"
    return cmds
}

def requestCode(codeNumber) {
    def cmds = ""
    if (state.disableLocalPINStore){
        if (codeNumber.toInteger() >= 0 && codeNumber.toInteger() <= getNumPINUsers() ){
	        log.debug "Getting code $codeNumber"
            cmds = zigbee.command(CLUSTER_DOORLOCK, DOORLOCK_CMD_CLEAR_CODE_GET, "${zigbee.convertToHexString(codeNumber.toInteger(),2)}00")
        }else{
            log.debug "Invalid Input: Unable to get code for $codeNumber"
        }
    }else{
       if (state["code${codeNumber}"]) {
           def resultMap = [ name: "codeReport", descriptionText: "Code recovered for user ${codeNumber}", isStateChange: true,
                             displayed: true, value: codeNumber, date: [ code: decrypt(state["code${codeNumber}"]) ] ]
           log.debug "requestCode: Code recovered for user $codeNumber: ${decrypt(state["code${codeNumber}"])}"
           sendEvent(resultMap)
       } else {
           log.debug "requestCode: Code not available user $codeNumber"
       }
    }
    log.info "requestCode() - ${cmds}"
    return cmds
}

def deleteCode(codeNumber) {
    def cmds = ""
    if (codeNumber.toInteger() >= 0 && codeNumber.toInteger() <= getNumPINUsers() ){
	    log.debug "Deleting code $codeNumber"
        cmds = zigbee.command(CLUSTER_DOORLOCK, DOORLOCK_CMD_CLEAR_USER_CODE, "${zigbee.convertToHexString(codeNumber.toInteger(),2)}00")
    }else{
        log.debug "Invalid Input: Unable to delete code for $codeNumber"
    }
    log.info "deleteCode() - ${cmds}"
    return cmds
}

def reloadAllCodes() {
    def cmds = ""
    def cmd = ""
    state.each { entry ->
        def codeNumber = entry.key.find(/\d+/)
        if ( entry.key ==~ /^code(\d+)$/ && entry.value ) {
            log.debug "Reloading Code for User ${codeNumber}"
            cmds = setCode(codeNumber, decrypt(entry.value))
            cmds += cmd
            fireCommand(cmd)
        }
    }
    log.info "reloadAllCodes() - ${cmds}"
    return cmds
}

def deleteAllCodes() {
    def cmds = ""
    cmds = zigbee.command(CLUSTER_DOORLOCK, DOORLOCK_CMD_CLEAR_ALL_USER_CODE)
    log.info "deleteAllCodes() - ${cmds}"
    return cmds
}

def updateCodes(codeSettings) {
    def cmds = ""
    def cmd = ""
	if(codeSettings instanceof String) codeSettings = util.parseJson(codeSettings)
	codeSettings.each { name, updated ->
		if (name ==~ /^code\d+$/) {
            def current = decrypt(state[name])
			def n = (name =~ /^code(\d+)$/)[0][1].toInteger()
            log.debug "updateCodes() - $name was $current, setting to $updated"
			if (updated.size() >= getMinPINLength() && updated.size() <= getMaxPINLength() && updated != current) {
                cmd = setCode(n, updated)
                cmds += cmd
                fireCommand(cmd) // Temporary Workaround
			} else if ( (!updated || updated == "0") && current ) {
				cmd = deleteCode(n)
                cmds += cmd
                fireCommand(cmd) // Temporary Workaround
			} else if ( updated.size() < getMinPINLength() || updated.size() > getMaxPINLength() ) {
                log.warn("updateCodes() - Invalid PIN length $name: $updated") 
            } else if ( updated == current ) {
                log.debug("updateCodes() - PIN unchanged for $name: $updated") 
            } else log.warn("updateCodes() - unexpected PIN for $name: $updated") 
		} else log.warn("updateCodes() - unexpected entry code name: $name")
	}
    log.info "updateCodes() - ${cmds}"
    return cmds
}

// Private methods
private fireCommand(List commands) { //Function used from SmartThings Lightify Dimmer Switch support by Adam Outler
    if (commands != null && commands.size() > 0) {
        log.trace("Executing commands:" + commands)
        for (String value : commands){
            sendHubCommand([value].collect {new physicalgraph.device.HubAction(it)})
        }
    }
}

// provides compatibility with Erik Thayer's "Lock Code Manager"
private reportAllCodes() { //from garyd9's lock DTH
    def resultMap = [ name: "reportAllCodes", data: [:], displayed: false, isStateChange: false, type: "physical" ]
    state.each { entry ->
        //iterate through all the state entries and add them to the event data to be handled by application event handlers
        if ( entry.value && entry.key ==~ /^code\d+$/) {
		    resultMap.data.put(entry.key, decrypt(entry.value))
        } else if ( entry.key ==~ /^code\d+$/ ) {
            resultMap.data.put(entry.key, entry.value)
        }
    }
    sendEvent(resultMap)
}

private getMaxPINLength() {
    def max_length = 8
    if ( device.currentValue("maxPINLength") ) max_length = device.currentValue("maxPINLength")
    return max_length
}

private getMinPINLength() {
    def min_length = 4
    if ( device.currentValue("minPINLength") ) min_length = device.currentValue("minPINLength")
    return min_length
}

private getNumPINUsers() {
    def num_users = 30
    if ( device.currentValue("numPINUsers") ) num_users = device.currentValue("numPINUsers")
    return num_users
}

private Map parseReportAttributeMessage(String description) {
    Map descMap = zigbee.parseDescriptionAsMap(description)
    Map resultMap = [:]
    if (descMap.clusterInt == CLUSTER_POWER && descMap.attrInt == POWER_ATTR_BATTERY_PERCENTAGE_REMAINING) {
        resultMap.name = "battery"
        resultMap.value = Math.round(Integer.parseInt(descMap.value, 16) / 2)
        if (device.getDataValue("manufacturer") == "Yale") {            //Handling issue with Yale locks incorrect battery reporting
            resultMap.value = Integer.parseInt(descMap.value, 16)
        }
    }else if (descMap.clusterInt == CLUSTER_ALARM && descMap.attrInt == ALARM_COUNT) {
        def value = Integer.parseInt(descMap.value, 16)
        def linkText = getLinkText(device)
        log.debug "Alarm Triggered: ${value}"
        resultMap = [ name: "tamper", displayed: true, value: "detected" ]
        if (value == 0) {
            resultMap.descriptionText = "${linkText} deadbolt jammed"
        } else if (value == 1) {
            resultMap.descriptionText = "${linkText} reset to factory defaults"
        } else if (value == 4) {
            resultMap.descriptionText = "${linkText} wrong entry limit reached"
        } else if (value == 5) {
            resultMap.descriptionText = "${linkText} front panel removed"
        } else if (value == 6) {
            resultMap.descriptionText = "${linkText} forced open"
        }
    }else if (descMap.clusterInt == CLUSTER_DOORLOCK && descMap.attrInt == DOORLOCK_ATTR_LOCKSTATE) {
        def value = Integer.parseInt(descMap.value, 16)
        def linkText = getLinkText(device)
        resultMap.name = "lock"
        if (value == 0) {
            resultMap.value = "unknown"
            resultMap.descriptionText = "${linkText} is not fully locked"
            resultMap.displayed = true
        } else if (value == 1) {
            resultMap.value = "locked"
            resultMap.descriptionText = "${linkText} is locked"
            resultMap.displayed = false
        } else if (value == 2) {
            resultMap.value = "unlocked"
            resultMap.descriptionText = "${linkText} is unlocked"
            resultMap.displayed = false
        } else {
            resultMap.value = "unknown"
            resultMap.descriptionText = "${linkText} is in unknown lock state"
            resultMap.displayed = true
        }
    } else if (descMap.clusterInt == CLUSTER_DOORLOCK && descMap.attrInt == DOORLOCK_ATTR_MIN_PIN_LENGTH && descMap.value) {
        def value = Integer.parseInt(descMap.value, 16)
        resultMap = [ name: "minPINLength", descriptionText: "Minimum PIN length: ${value}", value: value ]
    } else if (descMap.clusterInt == CLUSTER_DOORLOCK && descMap.attrInt == DOORLOCK_ATTR_MAX_PIN_LENGTH && descMap.value) {
        def value = Integer.parseInt(descMap.value, 16)
        resultMap = [ name: "maxPINLength", descriptionText: "Maximum PIN length: ${value}", value: value ]
    } else if (descMap.clusterInt == CLUSTER_DOORLOCK && descMap.attrInt == DOORLOCK_ATTR_NUM_PIN_USERS && descMap.value) {
        def value = Integer.parseInt(descMap.value, 16)
        resultMap = [ name: "numPINUsers", descriptionText: "Maximum Number of PIN Users: ${value}", value: value ]
    } else if (descMap.clusterInt == CLUSTER_DOORLOCK && descMap.attrInt == DOORLOCK_ATTR_AUTO_RELOCK_TIME && descMap.value) {
        def value = Integer.parseInt(descMap.value, 16)
        def autoLockMap = [ name: "autoLockTime", displayed: false, isStateChange: true, value: value, descriptionText: "autoLockTime = ${value}" ]
        resultMap = [ name: "autoLockTile", displayed: true, isStateChange: true, descriptionText: "Current Value of Auto Lock: ${value}" ]
        if ( value > 0 ){
            if (device.currentValue("autoLockTime") == value){
                autoLockMap.isStateChange = false
                resultMap.displayed = false
            }
            resultMap.value = "autoLockEnabled"
        } else {
            if (device.currentValue("autoLockTime") == 0){
                autoLockMap.isStateChange = false
                resultMap.displayed = false
            }
            resultMap.value = "autoLockDisabled"
        }
        log.debug "autoLockTime --- ${autoLockMap}"
        sendEvent(autoLockMap)
    } else if (descMap.clusterInt == CLUSTER_DOORLOCK && descMap.attrInt == DOORLOCK_ATTR_ONE_TOUCH_LOCK && descMap.value) {
        def value = Integer.parseInt(descMap.value, 16)
        resultMap = [name: "oneTouch", descriptionText: "Current Value of One Touch Lock: ${value}", isStateChange: true, value: value.toString() ]
        if ( device.currentValue("oneTouch") == value.toString() ) resultMap.isStateChange = false
    }/* else if (descMap.clusterInt == CLUSTER_DOORLOCK && descMap.attrInt == DOORLOCK_ATTR_SEND_PIN_OTA && descMap.value) {
        def value = Integer.parseInt(descMap.value, 16)
        if (value == 0) {
            def cmds = zigbee.writeAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_SEND_PIN_OTA, TYPE_BOOL, 1)
            log.debug "state.enablePINs cmds - ${cmds}"
        }
		state.disableLocalPINStore = value
    }*/ else {
        log.debug "parseReportAttributeMessage() --- ignoring attribute - ${description}"
    }
    return resultMap
}

private Map parseResponseMessage(String description) {
    Map descMap = zigbee.parseDescriptionAsMap(description)
    Map resultMap = [:]
    def linkText = getLinkText(device)
    def cmd = Integer.parseInt(descMap.command,16)
    if (descMap.clusterInt == CLUSTER_DOORLOCK && cmd == DOORLOCK_RESPONSE_OPERATION_EVENT) {
        def value = Integer.parseInt(descMap.data[0], 16)
        def user = Integer.parseInt(descMap.data[2], 16)
        def type = ""
        resultMap.name = "operationEvent"
        if (value == 0){
            type = "locally"
            if (user && user != 255) {
                type += " by user ${user}"
                resultMap = [ name: "lock", data: [ usedCode: user ] ]
            }
        } else if (value == 1){
            type = "remotely"
            if (user && user != 255) {
                type += " by user ${user}"
                resultMap = [ name: "lock", data: [ usedCode: user ] ]
            }
        } else if (value == 2){
            type = "manually"
        } else {
            log.info "Operation Event -- ignored"
            return
        }
        switch (Integer.parseInt(descMap.data[1], 16)) {
            case 1:
                resultMap.descriptionText = "${linkText} locked ${type}"
                resultMap.value = "locked"
				break
            case 2:
                resultMap.descriptionText = "${linkText} unlocked ${type}"
                resultMap.value = "unlocked"
				break
            case 3: //Lock Failure Invalid Pin
            case 4: //Lock Failure Invalid Schedule
                resultMap.descriptionText = "Invalid PIN entered ${type}"
                break
            case 5: //Unlock Invalid PIN
            case 6: //Unlock Invalid Schedule
                resultMap.descriptionText = "Invalid PIN entered ${type}"
				break
            case 7:
                resultMap.descriptionText = "${linkText} locked ${type} from keypad"
                resultMap.value = "locked"
				break
            case 8:
                resultMap.descriptionText = "${linkText} locked ${type} with key"
                resultMap.value = "locked"
				break
            case 9:
                resultMap.descriptionText = "${linkText} unlocked ${type} with key"
                resultMap.value = "unlocked"
				break
            case 10:
                resultMap.descriptionText = "${linkText} locked automatically"
                resultMap.value = "locked"
				break
            case 13:
                resultMap.descriptionText = "${linkText} locked ${type}"
                resultMap.value = "locked"
				break
            case 14:
                resultMap.descriptionText = "${linkText} unlocked ${type}"
                resultMap.value = "unlocked"
				break
        }
        resultMap.displayed = true
        resultMap.isStateChange = true
    } else if (descMap.clusterInt == CLUSTER_DOORLOCK && cmd == DOORLOCK_RESPONSE_PROGRAMMING_EVENT) {
        def value = Integer.parseInt(descMap.data[0], 16)
        def type = ""
        if (value == 0){
            type = "locally"
        } else if (value == 1){
            type = "remotely"
        } else {
            log.info "Programming Event -- ignored"
            return
        }
        def codeNumber = Integer.parseInt(descMap.data[2], 16)
        resultMap.isStateChange = true
        switch (Integer.parseInt(descMap.data[1], 16)) {
            case 1: 
                resultMap.descriptionText = "Master code changed ${type}"
                resultMap.value = 0
				break
            case 2:
                resultMap.descriptionText = "User ${codeNumber}'s PIN code added ${type}"
                resultMap.value = codeNumber
                if ( type == "locally" ) { // Reports when done from keypad (cannot get code so setting to empty string)
                    resultMap.data = [ code: "" ]
                    state["code${codeNumber}"] = ""
                } else {
                    resultMap.data = [ code: decrypt(state["code${codeNumber}"]) ]
                }
				break
            case 3:
                resultMap.descriptionText = "User ${codeNumber}'s PIN code deleted ${type}"
                if ( resultMap.value == codeNumber && resultMap.data["code"] == "" ){
                    resultMap.isStateChange = false
                }
                resultMap.value = codeNumber
                resultMap.data = [ code: "" ]
                state["code${codeNumber}"] = ""
				break
            case 4:
                resultMap.descriptionText = "User ${codeNumber}'s PIN code changed ${type}"
                resultMap.value = codeNumber
                if ( type == "locally" ) { // Reports when done from keypad (cannot get code so setting to empty string)
                    resultMap.data = [ code: "" ]
                    state["code${codeNumber}"] = ""
                } else {
                    resultMap.data = [ code: decrypt(state["code${codeNumber}"]) ]
                }
				break
            default:
                return
				break
        }
        resultMap.name="codeReport"
        resultMap.displayed = true
    } else if (descMap.clusterInt == CLUSTER_DOORLOCK && cmd == DOORLOCK_CMD_GET_LOG_RECORD && descMap.data[6]) {
        if (Integer.parseInt(descMap.data[6], 16) == 1 && device.getDataValue("manufacturer") == "Yale") { //Needed because tested Yale lock does not send Programming Event
            def codeNumber = Integer.parseInt(descMap.data[9], 16)
            resultMap.name="codeReport"
            resultMap.descriptionText = "User ${codeNumber}'s PIN code added remotely"
            if ( resultMap.value == codeNumber && resultMap.data["code"] == decrypt(state["code${codeNumber}"]) ){
                resultMap.isStateChange = false
            } else {
                resultMap.isStateChange = true
            }
            resultMap.displayed = true
            resultMap.value = codeNumber
            resultMap.data = [ code: decrypt(state["code${codeNumber}"]) ]
        }
    } else {
    
       log.debug "parseResponseMessage() --- ignoring response - ${description}"
       
    }
    return resultMap
}
