/**
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
 *  Version 1.00 : Initial Release
 */
metadata {
	definition (name: "ZigBee Repeater", namespace: "smartthings", author: "jhamstead") {
		capability "Health Check"
		capability "Refresh"
        capability "Configuration"

		fingerprint profileId: "0104", inClusters: "0000"
	}

	// simulator metadata
	simulator {

	}

	// tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name: "status", type: "generic", width: 6, height: 4) {
			tileAttribute("device.status", key: "PRIMARY_CONTROL") {
                attributeState "unknown", label: 'unknown', icon: "st.motion.motion.inactive", backgroundColor: "#ffffff"
				attributeState "online", label: 'online', icon: "st.motion.motion.active", backgroundColor: "#00A0DC"
				attributeState "offline", label: 'offline', icon: "st.motion.motion.inactive", backgroundColor: "#ffffff"
			}
		}

		standardTile("refresh", "device.refresh", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        
        standardTile("reconfigure", "device.reconfigure", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'Force Reconfigure', action:"configure", icon:"st.secondary.refresh"
		}

		main "status"
		details(["status","refresh","reconfigure"])
	}
}

private getCLUSTER_BASIC() { 0x0000 }
private getPOWER_SOURCE() { 0x0007 }

def configure() {
    state.failedTries = 0
    unschedule()
    runEvery15Minutes(sendRequest)
    // Device-Watch simply pings if no device events received for checkInterval duration of 32min = 2 * 15min + 2min lag time
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
    log.debug "configure() - checkinterval duration of 32min"
    return refresh()
}

def parse(String description) {
    log.debug "description is $description"
    if (description?.startsWith('read attr -')) {
       Map descMap = zigbee.parseDescriptionAsMap(description)
       Map resultMap = [:]
       if (descMap.clusterInt == CLUSTER_BASIC && descMap.attrInt == POWER_SOURCE) {
          if ( device.currentValue('status') != 'online' || state.manualPress ) {
             sendEvent([name: "status", isStateChange: true, displayed: true, value: 'online', descriptionText: "$device.displayName is online" ])
             state.manualPress = false
          } else {
             sendEvent([name: "status", isStateChange: true, displayed: false, value: 'online', descriptionText: "$device.displayName is online" ])
          }
       }
    }
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
    return sendRequest()
}

def refresh() {
    state.manualPress = true
    log.debug "refresh() - Manual refresh"
    return sendRequest()
}

// Private methods
def sendRequest() {
    def cmds
    Map myMap = [name: "status", isStateChange: true, displayed: true, value: 'offline', descriptionText: "$device.displayName is offline" ]
    if (state.failedTries >= 2) {
        if ( device.currentValue('status') != 'offline' || state.manualPress ) {
           log.debug "${myMap}"
           sendEvent(myMap)
           state.manualPress = false
        }
        log.info "Device is offline"
    }
    state.failedTries = state.failedTries + 1 
	cmds = zigbee.readAttribute(CLUSTER_BASIC, POWER_SOURCE)
    
    return cmds
}