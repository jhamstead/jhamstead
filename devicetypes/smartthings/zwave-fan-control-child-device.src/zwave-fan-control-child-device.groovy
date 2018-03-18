/**
 *  Z-Wave Fan Control - Child Device
 * 
 *  A better functional Device Type for Z-Wave Smart Fan Control Switches
 *  Particularly the GE 12730 Z-Wave Smart Fan Control.
 *
 *  Copyright 2018 Jonathan Hamstead
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
 *  Contributors:
 *  ChadK: Original 3 Speed Switch Code
 *  Child Device Information From KOF Zigbee Fan Controller:
 *     Ranga Pedamallu; Stephan Hackett; Dale Coffing
 */
def version() {return "ver 0.1"}

metadata {
	definition (name: "Z-Wave Fan Control - Child Device", namespace: "smartthings", author: "jhamstead") {
		capability "Actuator"
        capability "Switch"
        capability "Light"
        capability "Sensor"

        attribute "fanSpeed", "string"
        
   }
   
   tiles(scale: 2) {

        standardTile("fanSpeed", "fanSpeed", inactiveLabel: false, width: 2, height: 2) {
            state "default01", label: 'LOW', action: "switch.on", icon:"st.Home.home30", backgroundColor: "#ffffff", nextState: "adjusting01"
			state "on01", label: 'LOW', action: "switch.off", icon:"st.Home.home30", backgroundColor: "#00A0DC", nextState: "adjusting01"
            state "adjusting01", label:'LOW', action: "switch.on", icon:"st.Home.home30", backgroundColor: "#30D0FF"
            state "default02", label: 'MED', action: "switch.on", icon:"st.Home.home30", backgroundColor: "#ffffff", nextState: "adjusting02"
			state "on02", label: 'MED', action: "switch.off", icon:"st.Home.home30", backgroundColor: "#00A0DC", nextState: "adjusting02"
            state "adjusting02", label:'MED', action: "switch.on", icon:"st.Home.home30", backgroundColor: "#30D0FF"
            state "default03", label: 'HIGH', action: "switch.on", icon:"st.Home.home30", backgroundColor: "#ffffff", nextState: "adjusting03"
			state "on03", label: 'HIGH', action: "switch.off", icon:"st.Home.home30", backgroundColor: "#00A0DC", nextState: "adjusting03"
            state "adjusting03", label:'HIGH', action: "switch.on", icon:"st.Home.home30", backgroundColor: "#30D0FF"
		}
    	main(["fanSpeed"])        
		details(["fanSpeed"])
        
	}
}

def on() {
    log.info "CHILD TURNED ON"
	if("${device.getDataValue('speedVal')}" == "01") { parent.lowSpeed() }
    else if("${device.getDataValue('speedVal')}" == "02") { parent.medSpeed() }
    else if("${device.getDataValue('speedVal')}" == "03") { parent.highSpeed() }
}

def off() {
    log.info "CHILD OFF"  
	parent.off()
}

def getFanAbbr() { 
	[
    "01":"LOW",
    "02":"MED",
    "03":"HIGH"
	]
}