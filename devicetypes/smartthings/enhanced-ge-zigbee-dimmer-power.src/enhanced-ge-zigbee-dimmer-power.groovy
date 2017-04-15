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
* 3/7/17 - Updated Color Scheme
* 2/2/17 - Added fade ability to GE Zigbee switches
*
*/
metadata {
    definition (name: "Enhanced GE ZigBee Dimmer Power", namespace: "smartthings", author: "jhamstead", ocfDeviceType: "oic.d.light") {
        capability "Actuator"
        capability "Configuration"
        capability "Refresh"
        capability "Power Meter"
        capability "Sensor"
        capability "Switch"
        capability "Switch Level"
        capability "Health Check"
        capability "Light"

        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B04"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0702"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0702, 0B05", outClusters: "0019", manufacturer: "sengled", model: "Z01-CIA19NAE26", deviceJoinName: "Sengled Element touch"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0702, 0B05", outClusters: "000A, 0019", manufacturer: "Jasco Products", model: "45852", deviceJoinName: "GE Zigbee Plug-In Dimmer"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0702, 0B05", outClusters: "000A, 0019", manufacturer: "Jasco Products", model: "45857", deviceJoinName: "GE Zigbee In-Wall Dimmer"
    }

    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00A0DC", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00A0DC", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
            }
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action:"switch level.setLevel"
            }
            tileAttribute ("power", key: "SECONDARY_CONTROL") {
                attributeState "power", label:'${currentValue} W'
            }
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        main "switch"
        details(["switch", "refresh"])
    }
    preferences {
        section ("Zigbee Dimmer Properties"){
            input "transitionTime", "number", title: "Tranistion Time (0-15)", description: true, defaultValue: 5, required: false, range: "0..15"
            input "minLevel", "number", title: "Minimum Dimmer Level (1-50)", description: true, defaultValue: 1, required: false, range: "1..50"
            input "maxLevel", "number", title: "Maximum Dimmer Level (51-100)", description: true, defaultValue: 100, required: false, range: "51..100"
        }
    }
}

private getCLUSTER_LEVEL() { 0x0008 }
private getLEVEL_CMD_MOVE_TO_LEVEL_ON_OFF() { 0x04 }
//private getLEVEL_ATTR_ON_OFF_TRANSITION_TIME() { 0x0010 }
private getLEVEL_ATTR_ON_LEVEL() { 0x0011 }

    // Parse incoming device messages to generate events
def parse(String description) {
    log.debug "description is $description"

    def event = zigbee.getEvent(description)
    if (event) {
        log.info event
        if (event.name == "power") {
            if (device.getDataValue("manufacturer") != "OSRAM") {       //OSRAM devices do not reliably update power
                event.value = (event.value as Integer) / 10             //TODO: The divisor value needs to be set as part of configuration
                sendEvent(event)
            }
        }
        else {
            sendEvent(event)
        }
    }
    else {
        def cluster = zigbee.parse(description)
        if (cluster && cluster.clusterId == 0x0006 && cluster.command == 0x07) {
            if (cluster.data[0] == 0x00){
                log.debug "ON/OFF REPORTING CONFIG RESPONSE: " + cluster
                sendEvent(name: "checkInterval", value: 60 * 12, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
            }
            else {
                log.warn "ON/OFF REPORTING CONFIG FAILED- error code:${cluster.data[0]}"
            }
        }
        else {
            log.warn "DID NOT PARSE MESSAGE for description : $description"
            log.debug zigbee.parseDescriptionAsMap(description)
        }
    }
}

def off() {
    //zigbee.off()
    state.currentLevel = (device.currentValue('level')) ? device.currentValue('level') : state.currentLevel
    def onLevel = (state.currentLevel && state.currentLevel < 100) ? state.currentLevel * 2.55 : 254
    def cmds = setLevel(0, true) + zigbee.writeAttribute(CLUSTER_LEVEL, LEVEL_ATTR_ON_LEVEL, 0x20, zigbee.convertToHexString(onLevel,2))
    log.info "off() -- cmds: $cmds -- $state.currentLevel"
    return cmds
}

def on() {
    //zigbee.on()
    def value = (state.currentLevel) ? state.currentLevel : myMaxLevel
    def cmds = setLevel(value)
    log.info "on() -- cmds: $cmds -- $state.currentLevel"
    return cmds
}

def setLevel(value, dontSetOnLevel=false) {
    //zigbee.setLevel(value)
    def cmds
    def myTime = (transitionTime) ? transitionTime : 5
    def myMinLevel = (minLevel) ? minLevel : 1
    def myMaxLevel = (maxLevel) ? maxLevel : 100
    if ( value && value < myMinLevel ) {
        value = myMinLevel
    } else if ( value > myMaxLevel ) {
        value = myMaxLevel
    }
    def myLevel = ( value < 100 ) ? value * 2.55 : 254
    if (dontSetOnLevel) {
        cmds = zigbee.command(CLUSTER_LEVEL, LEVEL_CMD_MOVE_TO_LEVEL_ON_OFF, zigbee.convertToHexString(myLevel,2) + zigbee.convertToHexString(myTime,2) + "00")
    } else {
        def myOnLevel = myLevel
        state.currentLevel = value
        if ( ! value ) {
            myOnLevel = ( myMaxLevel < 100 ) ? myMaxLevel * 2.55 : 254
            state.currentLevel = myMaxLevel
        }
        cmds = zigbee.command(CLUSTER_LEVEL, LEVEL_CMD_MOVE_TO_LEVEL_ON_OFF, zigbee.convertToHexString(myLevel,2) + zigbee.convertToHexString(myTime,2) + "00") +
               zigbee.writeAttribute(CLUSTER_LEVEL, LEVEL_ATTR_ON_LEVEL, 0x20, zigbee.convertToHexString(myOnLevel,2))
    }
    log.info "setLevel() -- cmds: $cmds -- $state.currentLevel"
    return cmds
}

    /**
     * PING is used by Device-Watch in attempt to reach the Device
     * */
def ping() {
    return zigbee.onOffRefresh()
}

def refresh() {
    zigbee.onOffRefresh() + zigbee.levelRefresh() + zigbee.simpleMeteringPowerRefresh() + zigbee.electricMeasurementPowerRefresh() + zigbee.onOffConfig(0, 300) + zigbee.levelConfig() + zigbee.simpleMeteringPowerConfig() + zigbee.electricMeasurementPowerConfig()
}

def configure() {
    log.debug "Configuring Reporting and Bindings."

    // Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
    // enrolls with default periodic reporting until newer 5 min interval is confirmed
    sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
    refresh()
}