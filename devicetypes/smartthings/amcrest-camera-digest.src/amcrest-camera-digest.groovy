/**
 *  Amcrest Camera Device
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
 *  You are free to use this code, provided the following conditions are met:
 *   - This software is free for Private Use.  You may use and modify the software without distributing it.
 *   - This software and any derivatives of it may not be used for commercial purposes.
 *   - The images and/or files that originate from "http://smartthings.belgarion.s3.amazonaws.com/" are for use within
 *     THIS code only and any reference to these resources beyond this use is expressly prohibited.
 *
 *  Citations:
 *   - Belgarion: The original author of this DTH (programmer_dave@yahoo.com)
 *   - Tommy Goode: For his work on Digest Authentication
 *   - pstuart: For the efforts he put into his Generic Camera device, various posts and "Live Code Fridays"!
 *   - tgauchat and RBoy: For the "convertHostnameToIPAddress" macro.
 *   - slagle, eparkerjr and scottinpollock: For their efforts on the great Foscam & D-Link device types.
 *   - RBoy: For the code making the JPEG image available to other apps.
 *   - JBethancourt: Works with multiple camera HDCVI systems like the 4-camera AMDV7204-4B: just pick channels 0 through 3 and set up as individual cameras.
 *
 *  Release History:
 *    2018-02-25: v3.0.0 = Fork: Adding Digest Authentication
 *    2017-03-11: v2.3.5 = Snapshots now work with the new storeTemporaryImage function.
 *    2017-02-08: v2.3.4 = Fixed a bug that prevented video streaming from working when using a public IP.
 *    2017-01-27: v2.3.3 = Increased the range of channels to 0..15 to accommodate 16 camera NVR systems and fixed snapshots.
 *    2017-01-19: v2.3.2 = Fixed a typo in the return value for 'convertHostnameToIPAddress'.  Also, ST revoked S3 access so snapshots are
 *                         no longer functional.  Also, I separated the camera channel from the video channel, so your configurations must
 *                         be refiled!
 *    2017-01-19: v2.3.1 = Fixed a typo and a missing line in convertHostnameToIPAddress (thx jjslegacy!).
 *    2017-01-18: v2.3.0 = Fixed the HostName resolution, allow user-defined RTSP port and allow for multi-camera HDCVI systems (e.g. 4-camera
 *                         AMDV7204-4B: just pick channels 0 through 3 and set up as individual cameras, thx to JBethancourt), updated debugging
 *                         for readability (thx to ady624 for the formatting code), changed the default state of recording to "Auto" instead of "Off"
 *                         and added a function (queryMotion) to return ON/OFF based on current state.
 *    2016-04-27: v2.2.0 = Assigned the device.switch capability to the "Motion Sensor" toggle.
 *    2016-04-23: v2.1.0 = Allow toggle between MJPEG and RTSP live video streaming.
 *    2016-04-23: v2.0.0 = Added video streaming and a complete UI redesign.
 *    2016-04-21: v1.1.0 = Added the 'Actuator' capability (use with caution!) and a version command.
 *    2016-04-12: v1.0.0 = Initial release.
 *
 **/

metadata {
    definition (name: "Amcrest Camera Digest", namespace: "smartthings", author: "jhamstead") {
        capability "Actuator"
        capability "Configuration"
        capability "Image Capture"
        capability "Motion Sensor"
        capability "Polling"
        capability "Refresh"
        capability "Sensor"
        capability "Switch"
        capability "Switch Level"
        capability "Video Camera"
        capability "Video Capture"

        attribute "hubactionMode", "string"
        attribute "imageDataJpeg", "string"
        attribute "streamType", "string"

        command "appVersion"
        command "changeNvLED"
        command "changeRecord"
        command "changeRotation"
        command "moveDown"
        command "moveLeft"
        command "moveRight"
        command "moveUp"
        command "presetCmd1"
        command "presetCmd2"
        command "presetCmd3"
        command "presetCmd4"
        command "presetCmd5"
        command "presetCmd6"
        command "queryMotion"
        command "rebootNow"
        command "setLevelSpeed"
        command "setLevelSensitivity"
        command "toggleFlip"
        command "toggleMirror"
        command "toggleMotion"
        command "toggleStreamType"
        command "videoStart"
        command "videoStop"
        command "videoSetResHD"
        command "videoSetResSD"
        command "zoomIn"
        command "zoomOut"
    }

    preferences {
        input(title: "", description:"Amcrest Camera Handler v${appVersion()}", displayDuringSetup: false, type: "paragraph", element: "paragraph")
        input("camIP", "string", title: "Hostname or IP Address", description: "Enter the Hostname or IP Address of the camera", required: true, displayDuringSetup: true)
        input("camPort", "string", title: "Port", description: "Enter the Port Number to use (a local IP typically uses 80)", required: true, displayDuringSetup: true)
        input(title: "", description:"Enter the RTSP Port Number to use (normally 554) when viewing an RTSP video stream.", displayDuringSetup: false, type: "paragraph", element: "paragraph")
        input("camRTSPPort", "string", title:"RTSP Port", description: "", required: true, displayDuringSetup: true)
        input("camUser", "string", title: "Account Username", description: "Enter the Account Username", required: true, displayDuringSetup: true)
        input("camPassword", "password", title: "Account Password", description: "Enter the Password for this Account Username", required: true, displayDuringSetup: true)
        input(title: "", description:"For 'Camera Channel', specify the camera channel to use (typically 0) for snapshots, presets, etc..  If this is an NVR system, this is the number of the individual camera being accessed.", displayDuringSetup: false, type: "paragraph", element: "paragraph")
        input("camChannel", "range: 0..15", title: "Camera Channel", description: "", required: true, displayDuringSetup: true)
        input(title: "", description:"For 'Video Channel', specify the video streaming channel to use", displayDuringSetup: false, type: "paragraph", element: "paragraph")
        input("camVideoChannel", "range: 0..9", title: "Video Channel", description: "", required: true, displayDuringSetup: true)
        input(title: "", description:"If snapshots and RTSP video are from different channels, turn this on.", displayDuringSetup: false, type: "paragraph", element: "paragraph")
        input("channelFix", "bool", title:"Channel Problem Fix", description: "", required: false, displayDuringSetup: true)
        input(title: "", description:"Enable to display debugging information in the 'Live Logging' view.", displayDuringSetup: false, type: "paragraph", element: "paragraph")
        input("camDebug", "bool", title: "Camera Debug Mode", description: "", required: false, displayDuringSetup: true)
    }


    simulator {
        //
    }


    tiles(scale: 2) {
        multiAttributeTile(name: "videoPlayer", type: "videoPlayer", width: 6, height: 4) {
            tileAttribute("device.video", key: "CAMERA_STATUS") {
                attributeState("on", label: "Active", action: "switch.off", icon: "http://smartthings.belgarion.s3.amazonaws.com/images/IPM-721S.png", backgroundColor: "#00A0DC", defaultState: true)
                attributeState("off", label: "Inactive", action: "switch.on", icon: "http://smartthings.belgarion.s3.amazonaws.com/images/IPM-721S.png", backgroundColor: "#ffffff")
                attributeState("restarting", label: "Connecting", icon: "http://smartthings.belgarion.s3.amazonaws.com/images/IPM-721S.png", backgroundColor: "#53a7c0")
                attributeState("unavailable", label: "Unavailable", action: "refresh.refresh", icon: "http://smartthings.belgarion.s3.amazonaws.com/images/IPM-721S.png", backgroundColor: "#F22000")
            }
            tileAttribute("device.errorMessage", key: "CAMERA_ERROR_MESSAGE") {
                attributeState("errorMessage", label: "", value: "", defaultState: true)
            }
            tileAttribute("device.camera", key: "PRIMARY_CONTROL") {
                attributeState("on", label: "Active", icon: "http://smartthings.belgarion.s3.amazonaws.com/images/IPM-721S.png", backgroundColor: "#00A0DC", defaultState: true)
                attributeState("off", label: "Inactive", icon: "http://smartthings.belgarion.s3.amazonaws.com/images/IPM-721S.png", backgroundColor: "#ffffff")
                attributeState("restarting", label: "Connecting", icon: "http://smartthings.belgarion.s3.amazonaws.com/images/IPM-721S.png", backgroundColor: "#53a7c0")
                attributeState("unavailable", label: "Unavailable", icon: "http://smartthings.belgarion.s3.amazonaws.com/images/IPM-721S.png", backgroundColor: "#F22000")
            }
            tileAttribute("device.startLive", key: "START_LIVE") {
                attributeState("live", action: "videoStart", defaultState: true)
            }
            tileAttribute("device.stream", key: "STREAM_URL") {
                attributeState("activeURL", defaultState: true)
            }
            tileAttribute("device.profile", key: "STREAM_QUALITY") {
                attributeState("hd", label: "HD", action: "videoSetResHD", defaultState: true)
                attributeState("sd", label: "SD", action: "videoSetResSD")
            }
            tileAttribute("device.betaLogo", key: "BETA_LOGO") {
                attributeState("betaLogo", label: "", value: "", defaultState: true)
            }
        }

        // Row 1
        standardTile("take", "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
            state "take", label: "Take", action: "Image Capture.take", icon: "http://smartthings.belgarion.s3.amazonaws.com/images/IPM-721S.png", backgroundColor: "#FFFFFF", nextState:"taking"
            state "taking", label:"Taking", action: "", icon: "st.camera.take-photo", backgroundColor: "#53a7c0"
            state "image", label: "Take", action: "Image Capture.take", icon: "st.camera.camera", backgroundColor: "#FFFFFF", nextState:"taking"
        }
        standardTile("ledState", "device.ledStatus", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
            state "off", label: "", action: "changeNvLED", icon: "http://smartthings.belgarion.s3.amazonaws.com/images/IR-LED-Off.png", backgroundColor: "#FFFFFF", nextState: "..."
            state "on", label: "", action: "changeNvLED", icon: "http://smartthings.belgarion.s3.amazonaws.com/images/IR-LED-On.png", backgroundColor: "#FFFFFF", nextState: "..."
            state "auto", label: "", action: "changeNvLED", icon: "http://smartthings.belgarion.s3.amazonaws.com/images/IR-LED-Auto.png", backgroundColor: "#FFFFFF", nextState: "..."
            state "...", label: "...", action: "", nextState: "..."
        }
        standardTile("motionStatus", "device.switch", width: 1, height: 1, canChangeIcon: false, decoration: "flat") {
            state "off", label: "", action: "switch.off", icon: "http://smartthings.belgarion.s3.amazonaws.com/images/Motion-Off.png", backgroundColor: "#FFFFFF", nextState: "..."
            state "on", label: "", action: "switch.on", icon: "http://smartthings.belgarion.s3.amazonaws.com/images/Motion-On.png", backgroundColor: "#FFFFFF", nextState: "..."
            state "...", label: "...", action: "", nextState: "..."
        }
        standardTile("zoomOut", "device.zoomOut", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false) {
            state "zoomOut", label: "", action: "zoomOut", icon: "st.secondary.less", backgroundColor: "#FFFFFF", nextState: "zoomingOut"
            state "zoomingOut", label: "", action: "", icon: "st.secondary.less", backgroundColor: "#00FF00", nextState: "zoomOut"
        }
        standardTile("up", "device.up", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
            state "up", label: "Up", action: "moveUp", icon: "st.thermostat.thermostat-up", backgroundColor: "#FFFFFF", nextState: "upish"
            state "upish", label: "Up", action: "", icon: "st.thermostat.thermostat-up", backgroundColor: "#00FF00", nextState: "up"
        }
        standardTile("zoomIn", "device.zoomIn", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false) {
            state "zoomIn", label: "", action: "zoomIn", icon: "st.secondary.more", backgroundColor: "#FFFFFF", nextState: "zoomingIn"
            state "zoomingIn", label: "", action: "", icon: "st.secondary.more", backgroundColor: "#00FF00", nextState: "zoomIn"
        }

        // Row 2
        /*
        standardTile("image", "device.image", width: 3, height: 2) {
        }
        */
        carouselTile("camDetails", "device.image", width: 3, height: 2) {
        }
        standardTile("left", "device.left", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
            state "left", label: "Left", action: "moveLeft", icon: "st.thermostat.thermostat-left", backgroundColor: "#FFFFFF", nextState: "leftish"
            state "leftish", label: "Left", action: "", icon: "st.thermostat.thermostat-left", backgroundColor: "#00FF00", nextState: "left"
        }
        standardTile("refresh", "device.refresh", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false) {
            state "refresh", label: "", action: "refresh.refresh", icon: "st.secondary.refresh", backgroundColor: "#FFFFFF"
        }
        standardTile("right", "device.right", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
            state "right", label: "Right", action: "moveRight", icon: "st.thermostat.thermostat-right", backgroundColor: "#FFFFFF", nextState: "rightish"
            state "rightish", label: "Right", action: "", icon: "st.thermostat.thermostat-right", backgroundColor: "#00FF00", nextState: "right"
        }

        // Row 3
        standardTile("recState", "device.recStatus", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false) {
            state "off", label: "Off", action: "changeRecord", icon: "st.Electronics.electronics7", backgroundColor: "#FFFFFF", nextState: "..."
            state "on", label: "On", action: "changeRecord", icon: "st.Electronics.electronics7", backgroundColor: "#FFFFFF", nextState: "..."
            state "auto", label: "Auto", action: "changeRecord", icon: "st.Electronics.electronics7", backgroundColor: "#FFFFFF", nextState: "..."
            state "...", label: "...", action: "", backgroundColor: "#00FF00", nextState: "..."
        }
        standardTile("down", "device.down", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
            state "down", label: "Down", action: "moveDown", icon: "st.thermostat.thermostat-down", backgroundColor: "#FFFFFF", nextState: "downish"
            state "downish", label: "Down", action: "", icon: "st.thermostat.thermostat-down", backgroundColor: "#00FF00", nextState: "down"
        }
        standardTile("reboot", "device.reboot", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false) {
            state "reboot", label: "Reboot", action: "rebootNow", icon: "st.Appliances.appliances17", backgroundColor: "#FFFFFF", nextState: "rebooting"
            state "rebooting", label: "...", action: "", icon: "st.Appliances.appliances17", backgroundColor: "#00FF00", nextState: "reboot"
        }

        // Row 4
        standardTile("labelSpeed", "device.level", width: 3, height: 1, inactiveLabel: false) {
            state "speedLabel", label: "PTZ Speed", action: ""
        }
        standardTile("preset1", "device.presetStatus", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
            state "preset1", label: "", action: "presetCmd1", icon: "http://smartthings.belgarion.s3.amazonaws.com/images/Preset1-ON.png", backgroundColor: "#FFFFFF", nextState: "preset1On"
            state "preset1On", label: "", action: "", icon: "http://smartthings.belgarion.s3.amazonaws.com/images/Preset1-OFF.png", backgroundColor: "#FFFFFF", nextState: "preset1"
        }
        standardTile("preset2", "device.presetStatus", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
            state "preset2", label: "", action: "presetCmd2", icon: "http://smartthings.belgarion.s3.amazonaws.com/images/Preset2-ON.png", backgroundColor: "#FFFFFF", nextState: "preset2On"
            state "preset2On", label: "", action: "", icon: "http://smartthings.belgarion.s3.amazonaws.com/images/Preset2-OFF.png", backgroundColor: "#FFFFFF", nextState: "preset2"
        }
        standardTile("preset3", "device.presetStatus", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
            state "preset3", label: "", action: "presetCmd3", icon: "http://smartthings.belgarion.s3.amazonaws.com/images/Preset3-ON.png", backgroundColor: "#FFFFFF", nextState: "preset3On"
            state "preset3On", label: "", action: "", icon: "http://smartthings.belgarion.s3.amazonaws.com/images/Preset3-OFF.png", backgroundColor: "#FFFFFF", nextState: "preset3"
        }

        // Row 5
        controlTile("levelSliderControlSpeed", "device.levelSpeed", "slider", width: 3, height: 1, inactiveLabel: false, range:"(1..8)") {
            state "speed", action: "setLevelSpeed"
        }
        standardTile("preset4", "device.presetStatus", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
            state "preset4", label: "", action: "presetCmd4", icon: "http://smartthings.belgarion.s3.amazonaws.com/images/Preset4-ON.png", backgroundColor: "#FFFFFF", nextState: "preset4On"
            state "preset4On", label: "", action: "", icon: "http://smartthings.belgarion.s3.amazonaws.com/images/Preset4-OFF.png", backgroundColor: "#FFFFFF", nextState: "preset4"
        }
        standardTile("preset5", "device.presetStatus", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
            state "preset5", label: "", action: "presetCmd5", icon: "http://smartthings.belgarion.s3.amazonaws.com/images/Preset5-ON.png", backgroundColor: "#FFFFFF", nextState: "preset5On"
            state "preset5On", label: "", action: "", icon: "http://smartthings.belgarion.s3.amazonaws.com/images/Preset5-OFF.png", backgroundColor: "#FFFFFF", nextState: "preset5"
        }
        standardTile("preset6", "device.presetStatus", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
            state "preset6", label: "", action: "presetCmd6", icon: "http://smartthings.belgarion.s3.amazonaws.com/images/Preset6-ON.png", backgroundColor: "#FFFFFF", nextState: "preset6On"
            state "preset6On", label: "", action: "", icon: "http://smartthings.belgarion.s3.amazonaws.com/images/Preset6-OFF.png", backgroundColor: "#FFFFFF", nextState: "preset6"
        }

        // Row 6
        controlTile("levelSliderControlSensitivity", "device.levelSensitivity", "slider", width: 3, height: 1, inactiveLabel: false, range:"(1..6)") {
            state "sensitivity", action: "setLevelSensitivity"
        }
        standardTile("flipStatus", "device.flipStatus", width: 1, height: 1, canChangeIcon: false, decoration: "flat") {
            state "off", label: "Flip", action: "toggleFlip", icon: "http://smartthings.belgarion.s3.amazonaws.com/images/3D-Slider-OFF-Top.png", backgroundColor: "#FFFFFF", nextState: "..."
            state "on", label: "Flip", action: "toggleFlip", icon: "http://smartthings.belgarion.s3.amazonaws.com/images/3D-Slider-ON-Top.png", backgroundColor: "#FFFFFF", nextState: "..."
            state "...", label: "...", action: "", nextState: "..."
        }
        standardTile("mirrorStatus", "device.mirrorStatus", width: 1, height: 1, canChangeIcon: false, decoration: "flat") {
            state "off", label: "Mirror", action: "toggleMirror", icon: "http://smartthings.belgarion.s3.amazonaws.com/images/3D-Slider-OFF-Top.png", backgroundColor: "#FFFFFF", nextState: "..."
            state "on", label: "Mirror", action: "toggleMirror", icon: "http://smartthings.belgarion.s3.amazonaws.com/images/3D-Slider-ON-Top.png", backgroundColor: "#FFFFFF", nextState: "..."
            state "...", label: "...", action: "", nextState: "..."
        }
        standardTile("rotateStatus", "device.rotateStatus", width: 1, height: 1, canChangeIcon: false, decoration: "flat") {
            state "off", label: "Rotation", action: "changeRotation", icon: "http://smartthings.belgarion.s3.amazonaws.com/images/3D-Slider-OFF-Top.png", backgroundColor: "#FFFFFF", nextState: "..."
            state "cw", label: "90°", action: "changeRotation", icon: "http://smartthings.belgarion.s3.amazonaws.com/images/Rotate-CW.png", backgroundColor: "#FFFFFF", nextState: "..."
            state "ccw", label: "90°", action: "changeRotation", icon: "http://smartthings.belgarion.s3.amazonaws.com/images/Rotate-CCW.png", backgroundColor: "#FFFFFF", nextState: "..."
            state "...", label: "...", action: "", nextState: "..."
        }

        // Row 7
        standardTile("labelSensitivity", "device.level",  width: 3, height: 1,inactiveLabel: false) {
            state "sensitivityLabel", label: "Motion Sensitivity", action: ""
        }
        standardTile("streamType", "device.streamType", width: 3, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
            state "MJPEG", label: "MJPEG Stream", action: "toggleStreamType", icon: "", backgroundColor: "#FFFFFF"
            state "RTSP", label: "RTSP Stream", action: "toggleStreamType", icon: "", backgroundColor: "#FFFFFF"
        }

        main "videoPlayer"
        //main "image"
        details(["videoPlayer", "take", "ledState", "motionStatus", "zoomOut", "up", "zoomIn",
                 "camDetails", "left", "refresh", "right",
                 "recState", "down", "reboot",
                 "labelSpeed", "preset1", "preset2", "preset3",
                 "levelSliderControlSpeed", "preset4", "preset5", "preset6",
                 "labelSensitivity", "flipStatus", "mirrorStatus", "rotateStatus",
                 "levelSliderControlSensitivity", "streamType"])
    }
}

mappings {
   path("/getInHomeURL") {
       action:
       [GET: "getInHomeURL"]
   }
   path("/getOutHomeURL") {
       action:
       [GET: "getOutHomeURL"]
   }
}

//*******************************  Commands  ***************************************

def appVersion() {
        return "2.3.3"
}

def changeNvLED() {
    doDebug("changeNvLED -> hubGet Enabled?: ${doHubGet ?: false}", "info", 0)
    if (!state.NvStatus || (state.NvStatus == "off")) {
        doDebug("Change NightVision: IR LED set to 'ON'", "info")
        state.NvSwitch = 3
        state.NvStatus = "on"
        sendEvent(name: "ledStatus", value: "on", isStateChange: true, displayed: false)
    }
    else if (state.NvStatus == "on") {
        doDebug("Change NightVision: IR LED set to 'AUTO'", "info")
        state.NvSwitch = 4
        state.NvStatus = "auto"
        sendEvent(name: "ledStatus", value: "auto", isStateChange: true, displayed: false)
    }
    else {
        doDebug("Change NightVision: IR LED set to 'OFF'", "info")
        state.NvSwitch = 0
        state.NvStatus = "off"
        sendEvent(name: "ledStatus", value: "off", isStateChange: true, displayed: false)
    }
    String apiCommand = setFlipMirrorMotionRotateNv()
    hubGet(apiCommand)
}

def changeRecord() {
    doDebug("changeRecord -> hubGet Enabled?: ${doHubGet ?: false}", "info", 0)
    if (!state.Record || (state.Record == "off")) {
        doDebug("Change Record: Recording set to 'ON'", "info")
        state.RecSwitch = 1
        state.Record = "on"
        sendEvent(name: "recStatus", value: "on", isStateChange: true, displayed: false)
    }
    else if (state.Record == "on") {
        doDebug("Change Record: Recording set to 'AUTO'", "info")
        state.RecSwitch = 0
        state.Record = "auto"
        sendEvent(name: "recStatus", value: "auto", isStateChange: true, displayed: false)
    }
    else {
        doDebug("Change Record: Recording set to 'OFF'", "info")
        state.RecSwitch = 2
        state.Record = "off"
        sendEvent(name: "recStatus", value: "off", isStateChange: true, displayed: false)
    }
    sendEvent(name: "recState", value: "", isStateChange: true, displayed: false)
    String apiCommand = setFlipMirrorMotionRotateNv()
    hubGet(apiCommand)
}

def changeRotation() {
    doToggleRotation(true)
}

def configure() {
    doDebug("configure -> Executing", "info", 0)
    state.camIP = camIP
    state.camPort = camPort
    state.camRTSPPort = camRTSPPort
    state.camUser = camUser
    state.camChannel = camChannel
    state.camVideoChannel = camVideoChannel
    state.channelFix = channelFix
    state.camDebug = camDebug
    sendEvent(name: "video", value: "on")
    if (!device.currentValue('streamType')) {
        toggleStreamType()
    }
    if (state.camPassword != camPassword || state.camPassword != camUser) {
		state.camPassword = camPassword
        state.camUser = camUser
		doDebug("New Camera User/Password", "info")
		clearDigestAuthData()
	}
    
}

def clearDigestAuthData() {
	doDebug("Clearing digest auth data.", "info")
	state.remove("digestAuthFields")
	state.remove("lastRequest")
    state.lastRequest = [:]
}

def getInHomeURL() {
         //[InHomeURL: parent.state.CameraStreamPath]
         state.CameraInStreamPath ? [InHomeURL: state.CameraInStreamPath] : null // return null if it's not initialized otherwise ST app crashes
}

def getOutHomeURL() {
         //[OutHomeURL: parent.state.CameraStreamPath]
         state.CameraOutStreamPath ? [OutHomeURL: state.CameraOutStreamPath] : null // return null if it's not initialized otherwise ST app crashes
}

def installed() {
    configure()
}

def moveDown() {
    doDebug("Panning Down", "info")
    sendEvent(name: "down", value: "down", isStateChange: true, displayed: false)
    doMoveCmd("start", "Down", "0", "${device.currentValue('levelSpeed') ?: 1}", "0", "0")
    //delayBetween([doMoveCmd("start", "Down", "0", "${device.currentValue('levelSpeed') ?: 1}", "0", "0"), doMoveCmd("stop", "Down", "0", "${device.currentValue('levelSpeed') ?: 1}", "0", "0")], msDelay())
}

def moveLeft() {
    doDebug("Panning Left", "info")
    sendEvent(name: "left", value: "left", isStateChange: true, displayed: false)
    doMoveCmd("start", "Left", "0", "${device.currentValue('levelSpeed') ?: 1}", "0", "0")
    //delayBetween([doMoveCmd("start", "Left", "0", "${device.currentValue('levelSpeed') ?: 1}", "0", "0"), doMoveCmd("stop", "Left", "0", "${device.currentValue('levelSpeed') ?: 1}", "0", "0")], msDelay())
}

def moveRight() {
    doDebug("Panning Right", "info")
    sendEvent(name: "right", value: "right", isStateChange: true, displayed: false)
    doMoveCmd("start", "Right", "0", "${device.currentValue('levelSpeed') ?: 1}", "0", "0")
    //delayBetween([doMoveCmd("start", "Right", "0", "${device.currentValue('levelSpeed') ?: 1}", "0", "0"), doMoveCmd("stop", "Right", "0", "${device.currentValue('levelSpeed') ?: 1}", "0", "0")], msDelay())
}

def moveUp() {
    doDebug("Panning Up", "info")
    sendEvent(name: "up", value: "up", isStateChange: true, displayed: false)
    doMoveCmd("start", "Up", "0", "${device.currentValue('levelSpeed') ?: 1}", "0", "0")
    //delayBetween([doMoveCmd("start", "Up", "0", "${device.currentValue('levelSpeed') ?: 1}", "0", "0"), doMoveCmd("stop", "Up", "0", "${device.currentValue('levelSpeed') ?: 1}", "0", "0")], msDelay())
}

def off() {  // switch.off
    doDebug("off -> Motion Sensor turned OFF", "info")
    return doToggleMotion(true)
}

def on() {  // switch.on
    doDebug("on -> Motion Sensor turned ON", "info")
    return doToggleMotion(true)
}

def poll() {  // Polling capability: this command will be called approximately every 5 minutes to check the device's state
    doDebug("Poll", "trace", 0)
    doDebug("poll -> BEGIN", "info", 1)
    sendEvent(name: "hubactionMode", value: "local", displayed: false)

    def cmds = []  // Build our commands list
    String apiCommand = setFlipMirrorMotionRotateNv()  // Get the commands to set the Flip/Mirror/Motion/NightVision/Rotate camera states

    cmds << hubGet("/cgi-bin/configManager.cgi?action=getConfig&name=VideoInOptions&name=MotionDetect")  // Current Flip, Mirroring, Motion Dectection, Rotate90 & Night Vision settings
    cmds << hubGet(apiCommand)  // Send the commands

    doDebug("poll -> Executing cmds: ${cmds.inspect()}", "info", -1)
    delayBetween(cmds, msDelay())
}

def presetCmd(presetNum) {
    doDebug("Moving to Preset # ${presetNum}", "info")
    sendEvent(name: "presetStatus", value: "", isStateChange: true, displayed: false)
    hubGet("/cgi-bin/ptz.cgi?action=start&channel=${state.camChannel}&code=GotoPreset&arg1=0&arg2=${presetNum}&arg3=0&arg4=0")
}

def presetCmd1() {
    presetCmd(1)
}

def presetCmd2() {
    presetCmd(2)
}

def presetCmd3() {
    presetCmd(3)
}

def presetCmd4() {
    presetCmd(4)
}

def presetCmd5() {
    presetCmd(5)
}

def presetCmd6() {
    presetCmd(6)
}

def queryMotion() {
    doDebug("queryMotion -> BEGIN", "info", 0)
    def result = null
    if (!state.Motion || (state.Motion == "off")) {
        result = "OFF"
        doDebug("'Motion Sensor' is currently OFF", "info")
    } else {
        result = "ON"
        doDebug("'Motion Sensor' is currently ON", "info")
    }
    doDebug("queryMotion -> END; Returning '$result'", "info", -1)
    return result
}

def rebootNow() {
    doDebug("Rebooting...", "info", 0)
    sendEvent(name: "reboot", value: "reboot", isStateChange: true, displayed: false)
    hubGet("/cgi-bin/magicBox.cgi?action=reboot")
}

def refresh() {
    doDebug("Refreshing Values...", "info", 0)
    poll()
}

def setLevelSpeed(int value) {
    doDebug("Adjusting PTZ speed...", "info", 0)
    def oldSpeed = device.currentValue('levelSpeed')
    doDebug("setLevel -> PTZ Speed changed from '${device.currentValue('levelSpeed') ?: 'Default:1'}' to '$value'", "info", -1)
    sendEvent(name: "levelSpeed", value: value, isStateChange: true, displayed: false)
}

def setLevelSensitivity(int value) {
    doDebug("Adjusting Motion Detect Sensitivity...", "info", 0)
    def oldSensitivity = device.currentValue('levelSensitivity')
    state.MotionSensitivity = value
    doDebug("setLevel -> Motion Detect Sensitivity changed from '${device.currentValue('levelSensitivity') ?: 'Default:1'}' to '${state.MotionSensitivity}'", "info", 1)
    sendEvent(name: "levelSensitivity", value: state.MotionSensitivity, isStateChange: true, displayed: false)
    doToggleMotionSensitivity(true)
}

def take() {
    doDebug("Taking Photo", "info", 0)
    // Set our image taking mode
    sendEvent(name: "hubactionMode", value: "s3", displayed: false)
    hubGetImage("/cgi-bin/snapshot.cgi?channel=${state.camChannel}")
}

def toggleFlip() {
    doToggleFlip(true)
}

def toggleMirror() {
    doToggleMirror(true)
}

def toggleMotion() {
    doToggleMotion(true)
}

def toggleStreamType() {
    doDebug("Toggling Streaming Mode", "info", 0)
    if (device.currentValue('streamType') != "MJPEG") {
        doDebug("toggleStreamType -> Image Streaming Mode Now 'MJPEG'", "info", -1)
        sendEvent(name: "streamType", value: "MJPEG", isStateChange: true, displayed: false)
    }
    else {
        doDebug("toggleStreamType -> Image Streaming Mode Now 'RTSP'", "info", -1)
        sendEvent(name: "streamType", value: "RTSP", isStateChange: true, displayed: false)
    }
}

def updated() {
    doDebug("'updated()' called...", "info", 0)
    configure()
}

def videoSetProfile(profile) {
    doDebug("videoSetProfile -> ${profile}", "info", 0)
    sendEvent(name: "profile", value: profile, displayed: false)
}

def videoSetResHD() {
    doDebug("videoSetResHD -> Set video to HD stream", "info", 0)
    sendEvent(name: "profile", value: "hd", displayed: false)
}

def videoSetResSD() {
    doDebug("videoSetResSD -> Set video to SD stream", "info", 0)
    sendEvent(name: "profile", value: "sd", displayed: false)
}

def videoStart() {
    doDebug("videoStart -> Turning Video Streaming ON", "info", 0)

    def userPassAscii = "${camUser}:${camPassword}"
    def apiCommand = ""
    def usePort = ""
    def useProtocol = ""
    def camChannelMod = 0
    def camChannelModString = ""

    if (device.currentValue('streamType') == "MJPEG") {
        apiCommand = "/cgi-bin/mjpg/video.cgi?channel=${state.camVideoChannel}&subtype=0"
        usePort = camPort
        useProtocol = "http://"
    }
    else {  // Allow the user to define the RTSP port (default to 554) and increment the channel by 1 if 'channelFix' is turned on.
        camChannelModString = camVideoChannel

        if (channelFix){
            camChannelMod = camChannelModString.toInteger()
            camChannelMod = camChannelMod + 1
            camChannelModString = camChannelMod.toString()
        }

        apiCommand = "/cam/realmonitor?channel=$camChannelModString&subtype=0"
        if (!camRTSPPort) {
            usePort = 554
        }
        else {
            usePort = camRTSPPort
        }
        useProtocol = "rtsp://"
    }
    def uri = useProtocol + userPassAscii + "@${camIP}:${usePort}" + apiCommand
    doDebug("videoStart -> camVideoChannel = ${camVideoChannel}, state.camVideoChannel = ${state.camVideoChannel}, camChannelModString = ${camChannelModString}", "info", 1)
    doDebug("videoStart -> Streaming ${device.currentValue('streamType')} video; apiCommand = ${apiCommand}, IP = ${camIP}, Port = ${usePort}", "info", 1)

    // Store the paths in state for callbacks
    state.CameraInStreamPath = uri
    state.CameraOutStreamPath = ""
    if (isIpAddress(camIP) != true) {
        state.CameraOutStreamPath = uri
    }
    else if (ipIsLocal(camIP) != true) {
        state.CameraOutStreamPath = uri
    }

    // Only Public IP's work for 'OutHomeURL'
    def dataLiveVideo = [
            //OutHomeURL  : parent.state.CameraStreamPath,
            //InHomeURL   : parent.state.CameraStreamPath,
            OutHomeURL  : state.CameraOutStreamPath,
            InHomeURL   : state.CameraInStreamPath,
            ThumbnailURL: "http://cdn.device-icons.smartthings.com/camera/dlink-indoor@2x.png",
            cookie      : [key: "key", value: "value"]
    ]

    def event = [
            name           : "stream",
            value          : groovy.json.JsonOutput.toJson(dataLiveVideo).toString(),
            data           : groovy.json.JsonOutput.toJson(dataLiveVideo),
            descriptionText: "Starting the livestream",
            eventType      : "VIDEO",
            displayed      : false,
            isStateChange  : true
    ]
    sendEvent(event)
}

def videoStop() {
    doDebug("videoStop -> Turning Video Streaming OFF", "info", -1)
}

def zoomIn() {
    doDebug("Zooming In", "info", 0)
    sendEvent(name: "zoomIn", value: "", isStateChange: true, displayed: false)
    doMoveCmd("start", "ZoomTele", "0", "${device.currentValue('levelSpeed') ?: 1}", "0", "0")
    //delayBetween([doMoveCmd("start", "ZoomTele", "0", "${device.currentValue('levelSpeed') ?: 1}", "0", "0"), doMoveCmd("stop", "ZoomTele", "0", "${device.currentValue('levelSpeed') ?: 1}", "0", "0")], msDelay())
}

def zoomOut() {
    doDebug("Zooming Out", "info", 0)
    sendEvent(name: "zoomOut", value: "", isStateChange: true, displayed: false)
    doMoveCmd("start", "ZoomWide", "0", "${device.currentValue('levelSpeed') ?: 1}", "0", "0")
    //delayBetween([doMoveCmd("start", "ZoomWide", "0", "${device.currentValue('levelSpeed') ?: 1}", "0", "0"), doMoveCmd("stop", "ZoomWide", "0", "${device.currentValue('levelSpeed') ?: 1}", "0", "0")], msDelay())
}

//*******************************  Private Commands  *******************************

private doMoveCmd(String action, String motion, String argOne, String argTwo, String argThree, String argFour) {
    def apiCommand = "/cgi-bin/ptz.cgi?action=${action}&channel=${state.camChannel}&code=${motion}&arg1=${argOne}&arg2=${argTwo}&arg3=${argThree}&arg4=${argFour}"
    hubGet(apiCommand)
}

private doToggleFlip(Boolean doHubGet) {
    doDebug("doToggleFlip -> BEGIN (hubGet Enabled?: ${doHubGet ?: false})", "info", 0)
    if (doHubGet == true) {
        if (!state.Flip || (state.Flip == "off")) {
            doDebug("Toggle Image Flip: Turning ON", "info", 1)
            state.Flip = "on"
            sendEvent(name: "flipStatus", value: "on", isStateChange: true, displayed: false)
        }
        else {
            doDebug("Toggle Image Flip: Turning OFF", "info", 1)
            state.Flip = "off"
            sendEvent(name: "flipStatus", value: "off", isStateChange: true, displayed: false)
        }
        String apiCommand = setFlipMirrorMotionRotateNv()
        hubGet(apiCommand)
    }
    else {
        if (flipStatus == "off" && (device.currentValue("flipStatus") == "on")) {
            doDebug("Toggle Image Flip: Turning ON", "info", 1)
        }
        else if (flipStatus == "on" && (device.currentValue("flipStatus") == "off")) {
            doDebug("Toggle Image Flip: Turning OFF", "info", 1)
        }
    }
}

private doToggleMirror(Boolean doHubGet) {
    doDebug("doToggleMirror -> BEGIN (hubGet Enabled?: ${doHubGet ?: false})", "info", 0)
    if (doHubGet == true) {
        if (!state.Mirror || (state.Mirror == "off")) {
            doDebug("Toggle Image Mirroring: Turning ON", "info", 1)
            state.Mirror = "on"
            sendEvent(name: "mirrorStatus", value: "on", isStateChange: true, displayed: false)
        }
        else {
            doDebug("Toggle Image Mirroring: Turning OFF", "info", 1)
            state.Mirror = "off"
            sendEvent(name: "mirrorStatus", value: "off", isStateChange: true, displayed: false)
        }
        String apiCommand = setFlipMirrorMotionRotateNv()
        hubGet(apiCommand)
    }
    else {
        if (mirrorStatus == "off" && (device.currentValue("mirrorStatus") == "on")) {
            doDebug("Toggle Image Mirroring: Turning ON", "info", 1)
        }
        else if (mirrorStatus == "on" && (device.currentValue("mirrorStatus") == "off")) {
            doDebug("Toggle Image Mirroring: Turning OFF", "info", 1)
        }
    }
}

private doToggleMotion(Boolean doHubGet) {
    doDebug("doToggleMotion -> BEGIN (hubGet Enabled?: ${doHubGet ?: false})", "info", 0)
    def result = null
    if (!doHubGet) {
        if (!state.Motion || (state.Motion == "off")) {
            doDebug("Toggle Motion: Turning 'Motion Sensor' ON", "info", 1)
            state.Motion = "on"
        }
        else {
            doDebug("Toggle Motion: Turning 'Motion Sensor' OFF", "info", 1)
            state.Motion = "off"
        }
    }
    else {
        if (!state.Motion || (state.Motion == "off")) {
            doDebug("Toggle Motion: Turning 'Motion Sensor' ON", "info", 1)
            state.Motion = "on"
            sendEvent(name: "switch", value: "on", isStateChange: true, displayed: false)
        }
        else {
            doDebug("Toggle Motion: Turning 'Motion Sensor' OFF", "info", 1)
            state.Motion = "off"
            sendEvent(name: "switch", value: "off", isStateChange: true, displayed: false)
        }
        String apiCommand = setFlipMirrorMotionRotateNv()
        result = hubGet(apiCommand)
    }
    return result
}

private doToggleMotionSensitivity(Boolean doHubGet) {
    doDebug("doToggleMotionSensitivity -> BEGIN (hubGet Enabled?: ${doHubGet ?: false})", "info", 0)
    if (doHubGet == true) {
        if (!state.MotionSensitivity) {
            doDebug("Setting Motion Sensitivity: Level set to 1", "info", 1)
            state.MotionSensitivity = 1
        }
        else {
            doDebug("Setting Motion Sensitivity: Level set to ${state.MotionSensitivity}", "info", 1)
        }
        String apiCommand = setFlipMirrorMotionRotateNv()
        hubGet(apiCommand)
    }
}

private doToggleRotation(Boolean doHubGet) {
    doDebug("doToggleRotation -> BEGIN (hubGet Enabled?: ${doHubGet ?: false})", "info", 0)
    if (doHubGet == true) {
        if (!state.Rotation || (state.Rotation == "off")) {
            doDebug("Toggle 90° Rotation: Rotation set to 'Clockwise'", "info", 1)
            state.Rotate = 1
            state.Rotation = "cw"
            sendEvent(name: "rotateStatus", value: "cw", isStateChange: true, displayed: false)
        }
        else if (state.Rotation == "cw") {
            doDebug("Toggle 90° Rotation: Rotation set to 'Counter-Clockwise'", "info", 1)
            state.Rotate = 2
            state.Rotation = "ccw"
            sendEvent(name: "rotateStatus", value: "ccw", isStateChange: true, displayed: false)
        }
        else {
            doDebug("Toggle 90° Rotation: Rotation turned 'OFF'", "info", 1)
            state.Rotate = 0
            state.Rotation = "off"
            sendEvent(name: "rotateStatus", value: "off", isStateChange: true, displayed: false)
        }
        String apiCommand = setFlipMirrorMotionRotateNv()
        hubGet(apiCommand)
    }
    else {
        if (rotateStatus == "off" && (state.Rotation == "cw")) {
            doDebug("Toggle 90° Rotation: Rotation set to 'Clockwise'", "info", 1)
        }
        else if (rotateStatus == "cw" && (state.Rotation == "ccw")) {
            doDebug("Toggle 90° Rotation: Rotation set to 'Counter-Clockwise'", "info", 1)
        }
        else if (rotateStatus == "ccw" && (state.Rotation == "off")) {
            doDebug("Toggle 90° Rotation: Rotation turned 'OFF'", "info", 1)
        }
    }
}

private String setFlipMirrorMotionRotateNv() {  // Return the string of commands needed to set the Flip/Mirror/Motion/NightVision/Rotate camera states
    doDebug("setFlipMirrorMotionRotateNv -> Current: flipStatus = ${state.Flip}, mirrorStatus = ${state.Mirror}, motionStatus = ${state.Motion}, nvStatus = ${state.NvStatus}, recordStatus = ${state.Record}, rotateStatus = ${state.Rotation} (movement speed = ${device.currentValue('levelSpeed') ?: 'Default:1'}, motion sensitivity = ${state.MotionSensitivity ?: 'Default:1'})", "info", 1)

    // Until I turn this into a Parent -> Child relationship, make sure all of the state values exist
    // Default the Record/RecSwitch state to Auto/0 to recover from UI changes that are out-of-sync with the WebView app (Thx ElwoodBlues)
    if (!state.Flip) { state.Flip = "off"}
    if (!state.Mirror) { state.Mirror = "off"}
    if (!state.Motion) { state.Motion = "off"}
    if (!state.MotionSensitivity) { state.MotionSensitivity = 1 }
    if (!state.NvStatus) { state.NvStatus = "off"}
    if (!state.NvSwitch) { state.NvSwitch = 0}
    if (!state.Record) { state.Record = "auto"}
    if (!state.RecSwitch) { state.RecSwitch = 0}
    if (!state.Rotate) { state.Rotate = 0}
    if (!state.Rotation) { state.Rotation = "off"}

    String apiCommand = "/cgi-bin/configManager.cgi?action=setConfig" +
                        "&MotionDetect[${state.camChannel}].Enable=${state.Motion == 'off' ? false : true}" +
                        "&MotionDetect[${state.camChannel}].Level=${state.MotionSensitivity}" +
                        "&RecordMode[${state.camChannel}].Mode=${state.RecSwitch}" +
                        "&VideoInOptions[${state.camChannel}].Flip=${state.Flip == 'off' ? false : true}" +
                        "&VideoInOptions[${state.camChannel}].NightOptions.Flip=${state.Flip == 'off' ? false : true}" +
                        "&VideoInOptions[${state.camChannel}].Mirror=${state.Mirror == 'off' ? false : true}" +
                        "&VideoInOptions[${state.camChannel}].NightOptions.Mirror=${state.Mirror == 'off' ? false : true}" +
                        "&VideoInOptions[${state.camChannel}].NightOptions.SwitchMode=${state.NvSwitch}" +
                        "&VideoInOptions[${state.camChannel}].NightOptions.Rotate90=${state.Rotate}" +
                        "&VideoInOptions[${state.camChannel}].Rotate90=${state.Rotate}"
    doDebug("'apiCommand' command string built", "info", -1)
    return apiCommand

}

//*******************************  Network Commands  *******************************

private hubGet(def apiCommand) {  // Called for all non-Image requests and Local IP Image requests
    doDebug("hubGet -> BEGIN", "info", 0)
    doDebug("hubGet -> apiCommand = $apiCommand (size = ${apiCommand.size()})", "info")

    // Make sure we have an IP
    if (isIpAddress(camIP) != true) {
        state.Host = convertHostnameToIPAddress(camIP)
        doDebug("hubGet -> Host name '$camIP' resolved to IP '${state.Host}'", "info")
    }
    else {
        state.Host = camIP
        doDebug("hubGet -> Using IP '${state.Host}'", "info")
    }

    // Do the deed
    def action = createCameraRequest("GET", apiCommand, true)
	sendHubCommand(action)
}

private hubGetImage(def apiCommand) {  // Called when taking a picture
    doDebug("hubGetImage -> BEGIN", "info", 0)
    doDebug("apiCommand = $apiCommand", "info")

    // Make sure we have an IP
    if (isIpAddress(camIP) != true) {
        state.Host = convertHostnameToIPAddress(camIP)
        doDebug("hubGetImage -> Host name '$camIP' resolved to IP '${state.Host}'", "info")
    }
    else {
        state.Host = camIP
        doDebug("hubGetImage -> Using IP '${state.Host}'", "info")
    }

    // Do the deed
    def action = createCameraRequest("GET", apiCommand, true, [outputMsgToS3: true])
	sendHubCommand(action)
}

private physicalgraph.device.HubAction createCameraRequest(method, uri, useAuth = false, options = null, isRetry = false, startRequest = null, startHeader = null) {
    doDebug("createCameraRequest -> BEGIN", "info", 1)
	doDebug("Creating camera request with method: ${method}, uri: ${uri}, options: ${options}, isRetry: ${isRetry}", "info")

	try {
		def headers = [
			HOST: "${state.camIP}:${state.camPort}"
		]
		if (useAuth && state.digestAuthFields) {
			// Increment nonce count and generate new client nonce (cheat: just MD5 the nonce count)
			if (!state.digestAuthFields.nc) {
				//log.debug("Resetting nc to 1")
				state.digestAuthFields.nc = 1
			}
			else {
				state.digestAuthFields.nc = (state.digestAuthFields.nc + 1) % 1000
				//log.debug("Incremented nc: ${state.digestAuthFields.nc}")
			}
			state.digestAuthFields.cnonce = md5("${state.digestAuthFields.nc}")
			//log.debug("Updated cnonce: ${state.digestAuthFields.cnonce}")

			headers.Authorization = generateDigestAuthHeader(method, uri)
		}

		def data = [
			method: method,
			path: uri,
			headers: headers
		]

		// Use a custom callback because this seems to bypass the need for DNI to be hex IP:port or MAC address
		//def action = new physicalgraph.device.HubAction(data, null, [callback: parseResponse])
        def action
        if(isRetry && options) {
        	options.put('callback', 'parseResponse')
            action = new physicalgraph.device.HubAction(data, null, options)
        } else {
            action = new physicalgraph.device.HubAction(data, null, [callback: parseResponse])
        }
		//log.debug("Created new HubAction, requestId: ${action.requestId}")

		// Persist request info in case we need to repeat it
        state.lastRequest = ["${action.requestId}": [method: method, uri: uri, useAuth: useAuth, options: options, isRetry: isRetry, startRequest: startRequest, startHeader: startHeader] ]

		return action
	}
	catch (Exception e) {
		doDebug("Exception creating HubAction for method: ${method} and URI: ${uri}", "warn")
	}
}

private void handleWWWAuthenticateHeader(header) {
	doDebug("handleWWWAuthenticateHeader -> BEGIN", "info", 1)
	// Create digestAuthFields map if it doesn't exist
	if (!state.digestAuthFields) {
		state.digestAuthFields = [:]
	}

	// `Digest realm="iPolis", nonce="abc123", qop="auth"`
	header.tokenize(',').collect {
		def tokens = it.trim().tokenize('=')
		if (tokens[0] == "Digest realm") tokens[0] = "realm"
		state.digestAuthFields[tokens[0]] = tokens[1].replaceAll("\"", "")
	}
	doDebug("Used authenticate header (${header}) to update digestAuthFields: ${state.digestAuthFields}", "info")
}

private String generateDigestAuthHeader(method, uri) {
	/*
	HA1=MD5(username:realm:password)
	HA2=MD5(method:digestURI)
	response=MD5(HA1:nonce:nonceCount:cnonce:qop:HA2)
	*/
	def ha1 = md5("${state.camUser}:${state.digestAuthFields.realm}:${state.camPassword}")
	// log.debug("ha1: ${ha1} (${state.camUser}:${state.digestAuthFields.realm}:${state.camPassword})")

	def ha2 = md5("${method}:${uri}")
	// log.debug("ha2: ${ha2} (${method}:${uri})")

	def digestAuth = md5("${ha1}:${state.digestAuthFields.nonce}:${state.digestAuthFields.nc}:${state.digestAuthFields.cnonce}:${state.digestAuthFields.qop}:${ha2}")
	// log.debug("digestAuth: ${digestAuth} (${ha1}:${state.digestAuthFields.nonce}:${state.digestAuthFields.nc}:${state.digestAuthFields.cnonce}:${state.digestAuthFields.qop}:${ha2})")
	def authHeader = "Digest username=\"${state.camUser}\", realm=\"${state.digestAuthFields.realm}\", nonce=\"${state.digestAuthFields.nonce}\", uri=\"${uri}\", qop=\"${state.digestAuthFields.qop}\", nc=\"${state.digestAuthFields.nc}\", cnonce=\"${state.digestAuthFields.cnonce}\", response=\"${digestAuth}\""
	return authHeader
}

private String md5(String str) {
	def digest = java.security.MessageDigest.getInstance("MD5").digest(str.getBytes("UTF-8"))
	return digest.encodeHex() as String
}

//*******************************  Process Responses  ******************************

def parseResponse(physicalgraph.device.HubResponse response) {
	doDebug("parseResponse -> BEGIN", "info")
	return parse(response.description)
}

def parse(String description) {  // 'parse' Method: Parse events into attributes or save an image (used with a public IP address)
	def response = parseLanMessage(description)
    def retResult = []

    doDebug("parse -> BEGIN", "info", 1)
    //doDebug("parse -> response = ${response}", "info")

	// Handle unknown responses
	if (!state.lastRequest["${response.requestId}"]) {
		doDebug("parse -> received message likely meant for other device handler (requestIds don't match): ${response}", "info")
		return
	}

    if(response.tempImageKey){
        try {
            storeTemporaryImage(response.tempImageKey, getPictureName())
        } catch (Exception e) {
            doDebug("parse -> ${e}", "error", -1)
        }
    }
    else if (response.status == 200) {
        def lastRequest = state.lastRequest["${response.requestId}"]
		state.lastRequest.remove("${response.requestId}")
        retResult = processResponse(lastRequest.body)
    }
    else if (response.status == 401) {
		// NEED MORE AUTH
		handleNeedsAuthResponse(response)
        return
	}
    else { // Otherwise process the camera response codes
        doDebug("Error response from ${state.Host}:${camPort}, HTTP Response code = $response.status", "warn")
    }
    doDebug("parse -> END", "info", -1)
    return retResult
}

def handleNeedsAuthResponse(msg) {
	doDebug("handleNeedsAuthResponse -> BEGIN, headers: ${msg.headers}, requestId: ${msg.requestId}", "info", 1)
    def lastRequest = state.lastRequest["${msg.requestId}"]
    
	// Parse out the digest auth fields
    def wwwAuthHeader = msg.headers['www-authenticate']
    if(!(lastRequest.uri.contains('start') || lastRequest.uri.contains('stop'))) {
	    handleWWWAuthenticateHeader(wwwAuthHeader)
    }

	// Retry the request if we haven't already
	if (!lastRequest || lastRequest.isRetry) {
		return
	}

	retryLastRequest([requestId: msg.requestId], wwwAuthHeader)
}

def retryLastRequest(data, wwwHeader) {
	doDebug("retryLastRequest -> BEGIN, requestId: ${data.requestId}", "info", 1)
    def lastRequest = state.lastRequest["${data.requestId}"]
    
	if (!lastRequest || lastRequest.isRetry) {
		doDebug("Error: failed attempting to retry a request. lastRequest: ${lastRequest}", "warn")
		return
	}
	doDebug("retryLastRequest -> lastRequest = ${lastRequest}", "info")

    if(lastRequest.uri.contains('start')) {
        def action = createCameraRequest(lastRequest.method, lastRequest.uri.replaceAll('start','stop'), lastRequest.useAuth, lastRequest.options, false, lastRequest, wwwHeader)
        sendHubCommand(action)
    }else if(lastRequest.uri.contains('stop')) {
        doDebug("retryLastRequest -> Start = ${lastRequest.startRequest}", "info")
        doDebug("retryLastRequest -> Stop = ${lastRequest}", "info")
        delayBetween([setDigest(lastRequest.startRequest, lastRequest.startHeader), setDigest(lastRequest, wwwHeader)], msDelay())
    } else {
       setDigest(lastRequest, wwwHeader)
    }
}

private setDigest(lastRequest, wwwHeader) {
    doDebug("setDigest -> BEGIN", "info", 1)
    doDebug("setDigest -> ACTION = ${lastRequest}", "info")
    doDebug("setDigest -> HEADER = ${wwwHeader}", "info")
    handleWWWAuthenticateHeader(wwwHeader)
    def action = createCameraRequest(lastRequest.method, lastRequest.uri, lastRequest.useAuth, lastRequest.options, true)
    sendHubCommand(action)
}

//*******************************  Image Handling  *********************************

private getPictureName() {
    def pictureUuid = java.util.UUID.randomUUID().toString().replaceAll('-', '').toUpperCase()
    "Amcrest" + "_$pictureUuid" + ".jpg"
}

//******************************  Parse HubAction/httpGet/Polling Responses  *******************************

// Process any non-image response (used for both local HubAction and public httpGet requests)
private processResponse(def bodyIn) {
    doDebug("processResponse -> BEGIN", "info", 1)

    def retResult = []

    String body = new String(bodyIn.toString())
    body = body.replaceAll("\\r\\n|\\r|\\n", " ")
    doDebug("processResponse -> Body size = ${body.size()}", "info")

    try {
        // Check for errors
        if (body.find("401 Unauthorized")) {
            doDebug("processResponse -> END -> Camera responded with a 401 Unauthorized error: Error = ${body}", "warn", -1)
            return retResult
        }
        if (body.find("404 Not Found")) {
            doDebug("processResponse -> END -> Camera responded with a 404 Not Found error: Error = ${body}", "warn", -1)
            return retResult
        }

        // Check for a single word result
        if (body.find("OK")) {
            doDebug("processResponse -> END -> Command successful", "info", -1)
            return retResult
        }
        else if (body.find("ERROR")) {
            doDebug("processResponse -> END -> Command failed", "warn", -1)
            return retResult
        }

        // Flip
        if (body.contains("VideoInOptions[$camChannel].Flip=false") && (state.Flip != "off")) {
            doDebug("processResponse -> Turning Flip Image 'OFF'", "trace")
            state.Flip = "off"
            sendEvent(name: "flipStatus", value: "off", isStateChange: true, displayed: false)
        }
        else if (body.contains("VideoInOptions[$camChannel].Flip=true") && (state.Flip != "on")) {
            doDebug("processResponse -> Turning Flip Image 'ON'", "trace")
            state.Flip = "on"
            sendEvent(name: "flipStatus", value: "on", isStateChange: true, displayed: false)
        }

        // Mirror
        if (body.contains("VideoInOptions[$camChannel].Mirror=false") && (state.Mirror != "off")) {
            doDebug("processResponse -> Turning Mirror Image 'OFF'", "trace")
            state.Mirror = "off"
            sendEvent(name: "mirrorStatus", value: "off", isStateChange: true, displayed: false)
        }
        else if (body.contains("VideoInOptions[$camChannel].Mirror=true") && (state.Mirror != "on")) {
            doDebug("processResponse -> Turning Mirror Image 'ON'", "trace")
            state.Mirror = "on"
            sendEvent(name: "mirrorStatus", value: "on", isStateChange: true, displayed: false)
        }

        // Motion Detection
        if (body.contains("MotionDetect[$camChannel].Enable=false") && (state.Motion != "off")) {
            doDebug("processResponse -> Turning Motion Sensor 'OFF'", "trace")
            state.Motion = "off"
            sendEvent(name: "switch", value: "off", isStateChange: true, displayed: false)
        }
        else if (body.contains("MotionDetect[$camChannel].Enable=true") && (state.Motion != "on")) {
            doDebug("processResponse -> Turning Motion Sensor 'ON'", "trace")
            state.Motion = "on"
            sendEvent(name: "switch", value: "on", isStateChange: true, displayed: false)
        }

        // Motion Sensitivity
        if (body.contains("MotionDetect[$camChannel].Level=1")) {
            doDebug("processResponse -> Setting Motion Sensitivity to '1' (Low)", "trace")
            sendEvent(name: "levelSensitivity", value: 1, isStateChange: true, displayed: false)
        }
        else if (body.contains("MotionDetect[$camChannel].Level=2")) {
            doDebug("processResponse -> Setting Motion Sensitivity to '2' (Medium-Low)", "trace")
            sendEvent(name: "levelSensitivity", value: 2, isStateChange: true, displayed: false)
        }
        else if (body.contains("MotionDetect[$camChannel].Level=3")) {
            doDebug("processResponse -> Setting Motion Sensitivity to '3' (Medium)", "trace")
            sendEvent(name: "levelSensitivity", value: 3, isStateChange: true, displayed: false)
        }
        else if (body.contains("MotionDetect[$camChannel].Level=4")) {
            doDebug("processResponse -> Setting Motion Sensitivity to '4' (Medium-High)", "trace")
            sendEvent(name: "levelSensitivity", value: 4, isStateChange: true, displayed: false)
        }
        else if (body.contains("MotionDetect[$camChannel].Level=5")) {
            doDebug("processResponse -> Setting Motion Sensitivity to '5' (High)", "trace")
            sendEvent(name: "levelSensitivity", value: 5, isStateChange: true, displayed: false)
        }
        else if (body.contains("MotionDetect[$camChannel].Level=6")) {
            doDebug("processResponse -> Setting Motion Sensitivity to '6' (Highest)", "trace")
            sendEvent(name: "levelSensitivity", value: 6, isStateChange: true, displayed: false)
        }

        // Rotation
        if (body.contains("VideoInOptions[$camChannel].Rotate90=0") && (state.Rotate != 0)) {
            doDebug("processResponse -> Setting Rotation to 0°", "trace")
            state.Rotate = 0
            state.Rotation = "off"
            sendEvent(name: "rotateStatus", value: "off", isStateChange: true, displayed: false)
        }
        else if (body.contains("VideoInOptions[$camChannel].Rotate90=1") && (state.Rotate != 1)) {
            doDebug("processResponse -> Setting Rotation to 90°", "trace")
            state.Rotate = 1
            state.Rotation = "cw"
            sendEvent(name: "rotateStatus", value: "cw", isStateChange: true, displayed: false)
        }
        else if (body.contains("VideoInOptions[$camChannel].Rotate90=2") && (state.Rotate != 2)) {
            doDebug("processResponse -> Setting Rotation to 270°", "trace")
            state.Rotate = 2
            state.Rotation = "ccw"
            sendEvent(name: "rotateStatus", value: "ccw", isStateChange: true, displayed: false)
        }

        // NightVision
        if (body.contains("VideoInOptions[$camChannel].NightOptions.SwitchMode=0") && (state.NvSwitch != 0)) {
            doDebug("processResponse -> Turning Night Vision 'OFF'", "trace")
            state.NvSwitch = 0
            state.NvStatus = "off"
            sendEvent(name: "ledStatus", value: "off", isStateChange: true, displayed: false)
        }
        else if (body.contains("VideoInOptions[$camChannel].NightOptions.SwitchMode=3") && (state.NvSwitch != 3)) {
            doDebug("processResponse -> Turning Night Vision 'ON'", "trace")
            state.NvSwitch = 3
            state.NvStatus = "on"
            sendEvent(name: "ledStatus", value: "on", isStateChange: true, displayed: false)
        }
        else if (body.contains("VideoInOptions[$camChannel].NightOptions.SwitchMode=4") && (state.NvSwitch != 4)) {
            doDebug("processResponse -> Turning Night Vision to 'AUTO'", "trace")
            state.NvSwitch = 4
            state.NvStatus = "auto"
            sendEvent(name: "ledStatus", value: "auto", isStateChange: true, displayed: false)
        }

        // Record
        if (body.contains("RecordMode[$camChannel].Mode=2") && (state.RecSwitch != 2)) {
            doDebug("processResponse -> Turning Recording Mode 'OFF'", "trace")
            state.RecSwitch = 2
            state.Record = "off"
            sendEvent(name: "recStatus", value: "off", isStateChange: true, displayed: false)
        }
        else if (body.contains("RecordMode[$camChannel].Mode=1") && (state.RecSwitch != 1)) {
            doDebug("processResponse -> Turning Recording Mode 'ON'", "trace")
            state.RecSwitch = 1
            state.Record = "on"
            sendEvent(name: "recStatus", value: "on", isStateChange: true, displayed: false)
        }
        else if (body.contains("RecordMode[$camChannel].Mode=0") && (state.RecSwitch != 0)) {
            doDebug("processResponse -> Turning Recording Mode to 'AUTO'", "trace")
            state.RecSwitch = 0
            state.Record = "auto"
            sendEvent(name: "recStatus", value: "auto", isStateChange: true, displayed: false)
        }
    }
    catch (Exception e) {
        doDebug("processResponse -> Exception thrown: $e", "error")
    }
    doDebug("processResponse -> New: flipStatus = ${state.Flip}, mirrorStatus = ${state.Mirror}, motionStatus = ${state.Motion}, nvStatus = ${state.NvStatus}, recordStatus = ${state.Record}, rotateStatus = ${state.Rotation} (movement speed = ${device.currentValue('levelSpeed') ?: 'Default:1'}, motion sensitivity = ${state.MotionSensitivity ?: 'Default:1'})", "info")
    doDebug("processResponse -> END", "info", -1)
    return retResult
}

//*******************************  IP & Port Related  ******************************

private Integer convertHexToInt(hex) {
    Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
    doDebug("convertHexToIP -> Convert hex to ip = $hex", "info")
    [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private String OLDconvertHostnameToIPAddress(hostname) { // thanks go to cosmicpuppy and RBoy!
    def params = [
                  uri: "http://api.myiponline.net/dig?url=" + hostname
                 ]
    def retVal = null
    try {
        retVal = httpGet(params) { response ->
                    doDebug("convertHostnameToIPAddress -> Request was successful, data = $response.data, status=$response.status")
                    for(result in response.data) {
                        for(subresult in result) {
                            if (subresult.type == "A") {
                                return subresult.ip
                            }
                        }
                    }
        }
    }
    catch (Exception e) {
        doDebug("Unable to convert hostname to IP Address, Error: $e", "error")
    }
    return retVal
}

private String convertHostnameToIPAddress(hostname) { // thanks go to cosmicpuppy and RBoy!
    doDebug("convertHostnameToIPAddress -> BEGIN", "info", 1)
    def params = [
                  uri: "http://dns.google.com/resolve?name=" + hostname,
                  contentType: 'application/json'
                 ]
    def retVal = null
    try {
        retVal = httpGet(params) { response ->
                    doDebug("convertHostnameToIPAddress -> Request was successful, data = $response.data, status=$response.status", "info")
                    if (response.data?.Status == 0) { // Resolved
                        for(result in response.data?.Answer) {
                            if (isIpAddress(result?.data)) {
                                doDebug("=> Resolved: ${result?.name} has IP Address ${result?.data}", "info", 1)
                                return result?.data
                            }
                            else {
                                doDebug("=> Redirected: ${result?.name} redirects to ${result?.data}", "info", 1)
                            }
                        }
                    }
                    else if (response.data?.Status == 2) { // NameServer refused
                        doDebug("=> NameServers refused the query: ${response.data?.Question[0]?.name}, Error: ${response.data?.Comment}", "info", 1)
                    }
                    else if (response.data?.Status == 3) { // HostName not found
                        doDebug("=> HostName not found: ${response.data?.Question[0]?.name}, Error: ${response.data?.Comment}", "info", 1)
                    }
        }
    }
    catch (Exception e) {
        doDebug("Unable to convert hostname to IP Address, Error: $e", "error")
    }
    doDebug("convertHostnameToIPAddress -> END", "info", -1)
    return retVal
}

private String convertIPtoBinary(ipAddress) {
    try {
        def bin = ""
        def oct = ""
        ipAddress.tokenize( '.' ).collect {
            oct = String.format( '%8s', Integer.toString(it.toInteger(), 2) ).replace(' ', '0')
            bin = bin + oct
        }
        doDebug("convertIPtoBinary -> IP address passed in is $ipAddress and the converted binary is $bin", "info")
        return bin
    }
    catch ( Exception e ) {
        doDebug("IP Address is invalid ($ipAddress), Error: $e", "warn")
        return null // Nothing to return
    }
}

private String convertIPtoHex(ipAddress) {
    try {
        String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
        doDebug("convertIPtoHex -> IP address passed in is $ipAddress and the converted hex code is $hex", "info")
        return hex
    }
    catch ( Exception e ) {
        doDebug("IP Address is invalid ($ipAddress), Error: $e", "warn")
        return null //Nothing to return
    }
}

private String convertPortToHex(port) {
    try {
        String hexport = port.toString().format( '%04x', port.toInteger() )
        doDebug("convertPortToHex -> Port passed in is $port and the converted hex code is $hexport", "info")
        return hexport
    }
    catch ( Exception e ) {
        doDebug("Port is invalid ($ipAddress), Error: $e", "warn")
        return null //Nothing to return
    }
}

private Boolean isIpAddress(String str) {
    // See: http://stackoverflow.com/questions/18157217/how-can-i-check-if-a-string-is-an-ip-in-groovy
    try {
        String[] parts = str.split("\\.")
        if (parts.length != 4) {
            return false
        }
        for (int i = 0; i < 4; ++i) {
            int p = Integer.parseInt(parts[i])
            if (p > 255 || p < 0) {
                return false
            }
        }
        return true
    }
    catch ( Exception e ) {
        doDebug("Unable to determine if IP Address is valid ($str), Error: $e", "warn")
        return false
    }
}

private Boolean ipIsLocal(ipAddress) {
    List ipPrivateRanges = ["00000000",          // 'LOCAL IP',  # 0/8
                            "00001010",          // 'LOCAL IP',  # 10/8
                            "01111111",          // 'LOCAL IP',  # 127.0/8
                            "1010100111111110",  // 'LOCAL IP',  # 169.254/16
                            "101011000001",      // 'LOCAL IP',  # 172.16/12
                            "1100000010101000"]  // 'LOCAL IP',  # 192.168/16
    def size = 17
    Boolean localAns = false
    try {
        def ipBinary = convertIPtoBinary(ipAddress)
        ipBinary = ipBinary.take(size)

        while (size-- > 7) {
            if (ipPrivateRanges.contains(ipBinary)) {
                doDebug("ipIsLocal -> Found = $ipBinary", "info")
                localAns = true
                break
            }
            ipBinary = ipBinary.take(size)
        }
        doDebug("ipIsLocal -> Host IP '$ipAddress' is ${localAns ? 'Local' : 'Public' }", "info")
        return localAns
    }
    catch (Exception e) {
        doDebug("Exception: $e", "error")
        return false
    }
    return localAns
}

private Integer msDelay() {
    return 500
}

//***********************************  Debugging  **********************************

private OLDdoDebug(Object... dbgStr) {
    if (camDebug) {
        log.debug dbgStr
    }
}

private doDebug(dbgStr, dbgType = null, shift = null, err = null) {  // Thanks go to ady624 for the formatting code
    dbgType = dbgType ? dbgType : "debug"
    def debugging = settings.camDebug

    // Suppress everything except for "warn" & "error" if debugging is NOT enabled
    if (!debugging && ((dbgType != "warn") && (dbgType != "error"))) {
        return
    }

    // If desired, define preferences to suppress certain types of logging and enable this section
    //if (!settings["log#$dbgType"]) {
    //    return
    //}

    //mode is
    // 0  = initialize level, level set to 0, prefix set to nill, pad stays as "|"
    // 1  = start of routine, level up, prefix set to "+", pad set to "-"
    // -1 = end of routine, level down (if we are >1 the -1 else set to 0), prefix set to "+", pad set to "-"
    // anything else, stay the course
    def maxLevel = 4
    def level = state.debugLevel ? state.debugLevel : 0
    def levelDelta = 0
    def prefix = "¦"
    def pad = "¦"
    switch (shift) {
        case 0:
            level = 0
            prefix = ""
            break
        case 1:
            level += 1
            prefix = "+"
            pad = "-"
            break
        case -1:
            levelDelta = -(level > 0 ? 1 : 0)
            prefix = "+"
            pad = "-"
        break
    }

    if (level > 0) {
        prefix = prefix.padLeft(level, "¦").padRight(maxLevel, pad)
    }

    level += levelDelta
    state.debugLevel = level

    if (debugging) {
        prefix += " "
    } else {
        prefix = ""
    }

    if (dbgType == "info") {
        log.info "$prefix$dbgStr"
    } else if (dbgType == "trace") {
        log.trace "$prefix$dbgStr"
    } else if (dbgType == "warn") {
        log.warn "$prefix$dbgStr"
    } else if (dbgType == "error") {
        log.error "$prefix$dbgStr"
    } else {
        log.debug "$prefix$dbgStr"
    }
}