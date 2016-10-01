/*
 *  Universal Enhanced ZigBee Lock
 *
 *	2016-10-01 : Bug Fixes - Version Alpha 0.1b
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

		main "toggle"
		details(["toggle", "lock", "unlock", "battery", "refresh"])
	}
    
	preferences {
        section ("Enhanced Lock Attributes"){
            //input "unlockTimeout", "number", title: "Default Unlock With Timeout (seconds)", description: true, defaultValue: 30, required: false, range: "0..180"
        }
	}
}
// Globals
private getCLUSTER_POWER() { 0x0001 }
private getCLUSTER_DOORLOCK() { 0x0101 }

private getDOORLOCK_CMD_LOCK_DOOR() { 0x00 }
private getDOORLOCK_CMD_UNLOCK_DOOR() { 0x01 }
private getDOORLOCK_CMD_USER_CODE_SET() { 0x05 }
private getDOORLOCK_CMD_USER_CODE_GET() { 0x06 }
private getDOORLOCK_CMD_CLEAR_USER_CODE() { 0x07 }
private getDOORLOCK_RESPONSE_OPERATION_EVENT() { 0x20 }
private getDOORLOCK_RESPONSE_PROGRAMMING_EVENT() { 0x21 }
private getPOWER_ATTR_BATTERY_PERCENTAGE_REMAINING() { 0x0021 }
private getDOORLOCK_ATTR_LOCKSTATE() { 0x0000 }
private getDOORLOCK_ATTR_NUM_PIN_USERS() { 0x0012 }
private getDOORLOCK_ATTR_MAX_PIN_LENGTH() { 0x0017 }
private getDOORLOCK_ATTR_MIN_PIN_LENGTH() { 0x0018 }
private getDOORLOCK_ATTR_SEND_PIN_OTA() { 0x0032 }

//private getTYPE_BOOL() { 0x10 }
private getTYPE_U8() { 0x20 }
private getTYPE_ENUM8() { 0x30 }

// Public methods
def installed() {
    log.trace "installed()"
}

def uninstalled() {
    log.trace "uninstalled()"
}

def configure() {
    state.disableLocalPINStore = 0
    state.duplicateCode = false
    state.MAX_PIN_LENGTH = 4
    state.MIN_PIN_LENGTH = 8
    state.NUM_PIN_USERS = 30
    
    def cmds =
        zigbee.configureReporting(CLUSTER_DOORLOCK, DOORLOCK_ATTR_LOCKSTATE,
                                  TYPE_ENUM8, 0, 3600, null) +
        zigbee.configureReporting(CLUSTER_POWER, POWER_ATTR_BATTERY_PERCENTAGE_REMAINING,
                                  TYPE_U8, 600, 21600, 0x01)
        
    log.info "configure() --- cmds: $cmds"
    return cmds + refresh() // send refresh cmds as part of config     
}

def refresh() {
    def cmds =
        zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_LOCKSTATE) +
        zigbee.readAttribute(CLUSTER_POWER, POWER_ATTR_BATTERY_PERCENTAGE_REMAINING) +
        //zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_SEND_PIN_OTA) +
        zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_MAX_PIN_LENGTH) + 
        zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_MIN_PIN_LENGTH) +
        zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_NUM_PIN_USERS)
    state.queueRunning = false
    state.queue = []
    log.info "refresh() --- cmds: $cmds"
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

// provides compatibility with Erik Thayer's "Lock Code Manager"
def reportAllCodes() { //from garyd9's lock DTH
    def map = [ name: "reportAllCodes", data: [:], displayed: false, isStateChange: false, type: "physical" ]
    state.each { entry ->
        //iterate through all the state entries and add them to the event data to be handled by application event handlers
        if ( entry.key ==~ /^code\d{1,}/ && entry.value.startsWith("~") ) {
		    map.data.put(entry.key, decrypt(entry.value))
        } else {
    	    map.data.put(entry.key, entry.value)
        }
    }
    sendEvent(map)
}

// Polling capability commands
def poll(){
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

// Lock Code capability commands
def setCode(codeNumber, code) {
    def octetCode = ""
    state.queue as ArrayList
    if (code.toString().size() <= state.MAX_PIN_LENGTH && code.toString().size() >= state.MIN_PIN_LENGTH && code.isNumber() && codeNumber.toInteger() >= 1 && codeNumber.toInteger() <= state.NUM_PIN_USERS){
         log.debug "Setting code $codeNumber to $code: added to queue"
         code.toString().split('').each {
            if(it.trim()) octetCode += " 3${it}"
         }
        state.queue.push("${CLUSTER_DOORLOCK}:${DOORLOCK_CMD_USER_CODE_SET}:${zigbee.convertToHexString(codeNumber.toInteger(),2)}00 01 00 0${code.toString().size()}${octetCode}:${codeNumber}")
        state["code${codeNumber}"] = encrypt(code)
        if ( ! state.queueRunning ) executeQueue() // Unfortunately I can't verify code is set properly - must use queue and state variables to keep codeNumber
    }else{
        log.debug "Invalid Input: Unable to set code $codeNumber to $code"
    }
}

def requestCode(codeNumber) {
    if (state.disableLocalPINStore){
        if (codeNumber.toInteger() >= 0 && codeNumber.toInteger() <= state.NUM_PIN_USERS){
	        log.debug "Getting code $codeNumber: added to queue"
            state.queue.push("${CLUSTER_DOORLOCK}:${DOORLOCK_CMD_CLEAR_CODE_GET}:${zigbee.convertToHexString(codeNumber.toInteger(),2)}00:${codeNumber}")
            if ( ! state.queueRunning ) executeQueue()
        }else{
            log.debug "Invalid Input: Unable to get code for $codeNumber"
        }
    }else{
       if (state["code${codeNumber}"]) {
           log.debug "requestCode: Code recovered for user $codeNumber: ${decrypt(state["code${codeNumber}"])}"
       } else {
           log.debug "requestCode: Code not available user $codeNumber"
       }
    }
}

def deleteCode(codeNumber) {
    state.queue as ArrayList
    if (codeNumber.toInteger() >= 0 && codeNumber.toInteger() <= state.NUM_PIN_USERS){
	    log.debug "Deleting code $codeNumber: added to queue"
        state.queue.push("${CLUSTER_DOORLOCK}:${DOORLOCK_CMD_CLEAR_USER_CODE}:${zigbee.convertToHexString(codeNumber.toInteger(),2)}00:${codeNumber}")
        if ( ! state.queueRunning ) executeQueue()
    }else{
        log.debug "Invalid Input: Unable to delete code for $codeNumber"
    }
}

def reloadAllCodes() {
    state.each { entry ->
        def codeNumber = entry.key.find(/\d+/)
        if ( entry.key ==~ /^code(\d+)$/ && entry.value) {
            log.debug "Reloading Code for User ${codeNumber}"
            setCode(codeNumber, decrypt(entry.value))
        }
    }
}

def updateCodes(codeSettings) {
	if(codeSettings instanceof String) codeSettings = util.parseJson(codeSettings)
	codeSettings.each { name, updated ->
		def current = decrypt(state[name])
		if (name.startsWith("code")) {
			def n = name[4..-1].toInteger()
			log.debug "$name was $current, set to $updated"
			if (updated?.size() >= state.MIN_PIN_LENGTH && updated?.size() <= state.MAX_PIN_LENGTH && updated != current) {
				setCode(n, updated)
			} else if ((current && updated == "") || updated == "0") {
				deleteCode(n)
			}/* else if (updated && ( updated.size() < state.MIN_PIN_LENGTH || updated.size() > state.MAX_PIN_LENGTH )) {
				// Entered code was too short or too long
				codeSettings["code$n"] = current
			}*/
		} else log.warn("unexpected entry $name: $updated")
	}
}

// Required to create a queue system to confirm remote code is set (need to wait for response)
def executeQueue() {
    def cmds
    state.queueRunning = true
    log.debug "executeQueue() - Queue Size ${state.queue.size()}"
    if ( state.queue.size() > 0){
        def cmd = state.queue.head().toString().tokenize('[]:')
        state.currentState = ""
        state.queue = state.queue.drop(1)
        state.currentUser = cmd[3]
        cmds = zigbee.command(cmd[0], cmd[1], cmd[2])
        fireCommand(cmds)
        log.info "executeQueue() -- ${cmds}"
        runIn(20, executeQueue, [overwrite: false])
    } else {
        state.queueRunning = false
        reportAllCodes()
    }
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

private Map parseReportAttributeMessage(String description) {
    Map descMap = zigbee.parseDescriptionAsMap(description)
    Map resultMap = [:]
    if (descMap.clusterInt == CLUSTER_POWER && descMap.attrInt == POWER_ATTR_BATTERY_PERCENTAGE_REMAINING) {
        resultMap.name = "battery"
        resultMap.value = Math.round(Integer.parseInt(descMap.value, 16) / 2)
        if (device.getDataValue("manufacturer") == "Yale") {            //Handling issue with Yale locks incorrect battery reporting
            resultMap.value = Integer.parseInt(descMap.value, 16)
        }
    }
    else if (descMap.clusterInt == CLUSTER_DOORLOCK && descMap.attrInt == DOORLOCK_ATTR_LOCKSTATE) {
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
        resultMap.name = "MIN_PIN_LENGTH"
        resultMap.descriptionText = "Minimum PIN length: ${value}"
		state.MIN_PIN_LENGTH = value
    } else if (descMap.clusterInt == CLUSTER_DOORLOCK && descMap.attrInt == DOORLOCK_ATTR_MAX_PIN_LENGTH && descMap.value) {
        def value = Integer.parseInt(descMap.value, 16)
        resultMap.name = "MAX_PIN_LENGTH"
        resultMap.descriptionText = "Maximum PIN length: ${value}"
		state.MAX_PIN_LENGTH = value
    } else if (descMap.clusterInt == CLUSTER_DOORLOCK && descMap.attrInt == DOORLOCK_ATTR_NUM_PIN_USERS && descMap.value) {
        def value = Integer.parseInt(descMap.value, 16)
        resultMap.name = "NUM_PIN_USERS"
        resultMap.descriptionText = "Maximum Number of PIN Users: ${value}"
		state.NUM_PIN_USERS = value
    } else if (descMap.clusterInt == CLUSTER_DOORLOCK && descMap.attrInt == DOORLOCK_ATTR_SEND_PIN_OTA && descMap.value) {
        def value = Integer.parseInt(descMap.value, 16)
        if (value == 0) {
            def cmds = zigbee.writeAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_SEND_PIN_OTA, TYPE_BOOL, 1)
            log.debug "state.enablePINs cmds - ${cmds}"
        }
		state.disableLocalPINStore = value
    } else {
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
                resultMap.name = "lock"
                resultMap.value = user
                resultMap.data = [ usedCode: user ]
            }
        } else if (value == 1){
            type = "remotely"
            if (user && user != 255) {
                type += " by user ${user}"
                resultMap.name = "lock"
                resultMap.data = [ usedCode: user ]
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
        switch (Integer.parseInt(descMap.data[1], 16)) {
            case 1: 
                resultMap.descriptionText = "Master code changed ${type}"
                resultMap.value = 0
				break
            case 2:
                resultMap.descriptionText = "User ${Integer.parseInt(descMap.data[2], 16)}'s PIN code added ${type}"
                resultMap.value = Integer.parseInt(descMap.data[2], 16)
                resultMap.data = [ code: "" ]
                state["code${Integer.parseInt(descMap.data[2], 16)}"] = ""  // This only reports when done from keypad (cannot get code so setting to empty string)
				break
            case 3:
                resultMap.descriptionText = "User ${Integer.parseInt(descMap.data[2], 16)}'s PIN code deleted ${type}"
                resultMap.value = Integer.parseInt(descMap.data[2], 16)
                resultMap.data = [ code: null ]
                state["code${Integer.parseInt(descMap.data[2], 16)}"] = ""
				break
            case 4:
                resultMap.descriptionText = "User ${Integer.parseInt(descMap.data[2], 16)}'s PIN code changed ${type}"
                resultMap.value = Integer.parseInt(descMap.data[2], 16)
                resultMap.data = [ code: "" ]
                state["code${Integer.parseInt(descMap.data[2], 16)}"] = ""  // This only reports when done from keypad (cannot get code so setting to empty string)
				break
            default:
                return
				break
        }
        resultMap.name="codeReport"
        resultMap.displayed = true
        resultMap.isStateChange = true
    } else if (descMap.clusterInt == CLUSTER_DOORLOCK && cmd == DOORLOCK_CMD_USER_CODE_SET) { //Needed because tested Yale lock does not send Programming Event
        def value = Integer.parseInt(descMap.data[0], 16)
        resultMap.name="codeReport"
        if (value == 0 && state.currentState != "${state.currentUser}0"){ //currentState needed due to receiving multiple responses when executing command
            state.currentState = "${state.currentUser}0"
            resultMap.descriptionText = "User ${state.currentUser}'s PIN code added remotely"
            resultMap.isStateChange = true
            resultMap.value = state.currentUser
            resultMap.data = [ code: decrypt(state["code${state.currentUser}"]) ]
        } else if (value == 1 && state.currentState != "${state.currentUser}1") {
            state.currentState = "${state.currentUser}1"
            resultMap.descriptionText = "User ${state.currentUser}'s PIN code addition failed"
            resultMap.isStateChange = true
            resultMap.value = state.currentUser
            resultMap.data = [ code: null ]
        } else if (value == 2 && state.currentState != "${state.currentUser}2") {
            state.currentState = "${state.currentUser}2"
            resultMap.descriptionText = "User ${state.currentUser}'s PIN code addition failed: Memory Full"
            resultMap.isStateChange = true
            resultMap.value = state.currentUser
            resultMap.data = [ code: null ]
        } else if (value == 3 && state.currentState != "${state.currentUser}3") {
            state.currentState = "${state.currentUser}3"
            resultMap.descriptionText = "User ${state.currentUser}'s PIN code addition failed: Duplicate Code Error"
            resultMap.isStateChange = true
            resultMap.value = state.currentUser
            resultMap.data = [ code: null ]
        } else {
            resultMap.isStateChange = false
        }
        resultMap.displayed = true
    } else {
    
       log.debug "parseResponseMessage() --- ignoring response - ${description}"
       
    }
    return resultMap
}
