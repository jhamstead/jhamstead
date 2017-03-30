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
 */
metadata {
	definition (name: "Z-Wave Repeater", namespace: "smartthings", author: "jhamstead") {
		capability "Health Check"
		capability "Polling"
		capability "Refresh"

		fingerprint inClusters: "0x", deviceJoinName: "Z-Wave Repeater"
	}

	// simulator metadata
	simulator {

	}

	// tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name: "status", type: "generic", width: 6, height: 4) {
			tileAttribute("device.status", key: "PRIMARY_CONTROL") {
                attributeState "unknown", label: 'unknown', icon: "st.motion.motion.active", backgroundColor: "#ffffff"
				attributeState "online", label: 'online', icon: "st.motion.motion.active", backgroundColor: "#53a7c0"
				attributeState "offline", label: 'offline', icon: "st.motion.motion.inactive", backgroundColor: "#ffffff"
			}
		}

		standardTile("refresh", "device.refresh", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "status"
		details(["status","refresh"])
	}
}

def updated(){
// Device-Watch simply pings if no device events received for checkInterval duration of 32min = 2 * 15min + 2min lag time
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

def parse(String description) {
	def result = null
	def cmd = zwave.parse(description, [0x20: 1, 0x70: 1])
	if (cmd) {
		zwaveEvent(cmd)
	}
	log.debug "Parse returned ${cmd}"
	//return result
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	log.debug "manufacturerId:   ${cmd.manufacturerId}"
	log.debug "manufacturerName: ${cmd.manufacturerName}"
	log.debug "productId:        ${cmd.productId}"
	log.debug "productTypeId:    ${cmd.productTypeId}"
	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	updateDataValue("MSR", msr)
	updateDataValue("manufacturer", cmd.manufacturerName)
    Map myMap = [name: "status", value: 'online', descriptionText: "$device.displayName is online", isStateChange: true, displayed: true]
    //myMap.displayed = (device.currentValue('status') == 'offline') ? true : false
    log.debug "${myMap}"
    sendEvent(myMap)
    state.onlineStatus = 0
}


def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	[:]
}

def poll() {
	refresh()
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
    refresh()
}

def refresh() {
    state.onlineStatus = (state.onlineStatus.toString().isInteger()) ? state.onlineStatus + 1 : 0
    if (state.onlineStatus > 2 && device.currentValue('status') == 'online') {
        Map myMap = [name: "status", value: 'offline', descriptionText: "$device.displayName is offline", isStateChange: true, displayed: true]
        log.debug "${myMap}"
        sendEvent(myMap)
    }
    log.debug "state.onlineStatus = ${state.onlineStatus}"
	zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
}