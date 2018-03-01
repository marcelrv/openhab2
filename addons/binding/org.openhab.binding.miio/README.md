# Xiaomi Robot Vacuum Binding

This Binding is used to control Xiaomi Mi IO devices. This is the set of devices from Xiaomi that are part of the Mi Ecosystem which is branded as MiJia.

![MIIO logo](doc/miio.png)

## Supported Things

This Binding supports Xiaomi Mi IO devices (Several Xiaomi wifi devices).
Currently mainly the Xiaomi Robot Vacuum is supported, remaining devices are experimental.
Experimental means that most likely you can send commands to the devices.

The following things are available:

| Thing Type | Description |
|--------|---------| 
| miio:generic | Generic type for discovered devices. Once the token is available and the device model is determined, this Thing Type will automatically change to the appropriate Thing Type |
| miio:vacuum | For Xiaomi Robot Vacuum |
| miio:basic | For several basic devices like yeelights, airpurifiers. Channels and commands are determined by database configuration |
| miio:unsupported | For experimenting with other devices which use the Mi IO protocol)

## Mi IO Devices

| Device | Thing Type | Auto token | Device Model  | Supported | Remark |
| ----------- | --------- |:---:| ---------- | ------ | -----------| 
| [Xiaomi Robot Vacuum](doc/vacuum.md) | miio:vacuum | No | rockrobo.vacuum.v1 | Yes |
| [Xiaomi Robot Vacuum 2](doc/vacuum.md) | miio:vacuum | No | roborock-vacuum-s5 | Yes |


# Discovery

The binding has 2 methods for discovering devices. Depending on your network setup and the device model, your device may be discovered by one or both methods. If both methods discover your device, 2 discovery results may be in your inbox for the same device.

The MDNS discovery method will discover your device type, but won't discover a (required) token.
The basic discovery will not discovery the type, but will discover a token for models that support it.

## Tokens

The binding needs a token from the Xiaomi Mi Device in order to be able to control it.
Some devices provide the token upon discovery. For several devices this depends on the firmware version.

If the device does not discover your token, it needs to be retrieved from the Mi Home app.
The token needs to be retrieved from the application database. The easiest way on Android to do is by using [MiToolkit](https://github.com/ultrara1n/MiToolkit/releases).

On Android open a backup file, or browse a rooted device, find the mio2db file with and read it sqlite.

For iPhone, use an un-encrypted iTunes-Backup and unpack it and use a sqlite tool to view the files in it: 
Then search in "RAW, com.xiaomi.home," for "USERID_mihome.sqlite" and look for the 32-digit-token or 96 digit encrypted token.

Note. Vacuum clearer & possible other devices change the token when inclusion is done. Hence if you get your token from a reseted device and than include it with the Mi Home app, the token will change.

e.g. for the rockrobo vacuum cleaner below  firmware version 3.3.9_003073:
In order to fetch the token, reset the robot vacuum, connect to its the network its announcing (rockrobo-XXXX) and run the discovery again. After the token is retrieved you can connect the vacuum to your phone again.
Once connected to your phone & the regular wifi network, run discovery once more to retrieve the new ipaddress.

## Binding Configuration

No binding configuration is required.

## Thing Configuration

The binding needs ip address and token to be able to communicate. See discovery for details.
Optional configuration is the refresh interval and the deviceID. Note that the deviceID is automatically retrieved when it is left blank.

## Channels

Depending on the device, different  channels are available.
See the detailed device files for the available channels

## Channels

note: the  `actions#commands` channel can be used to send commands that are not automated via the binding.
e.g. `smarthome:send actionCommand  "upd_timer['1498595904821', 'on']"` would enable a pre-configured timer. See https://github.com/marcelrv/XiaomiRobotVacuumProtocol for all known available commands.


## Example item file Rockrobo vacuum

```
Group  gVac     "Xiaomi Robot Vacuum"      <fan>
Group  gVacStat "Status Details"           <status> (gVac)
Group  gVacCons "Consumables Usage"        <line-increase> (gVac)
Group  gVacDND  "Do Not Disturb Settings"  <moon> (gVac)
Group  gVacHist "Cleaning History"         <calendar> (gVac)

String actionControl  "Vacuum Control"          {channel="xiaomivacuum:vacuum:034F0E45:actions#control" }
String actionCommand  "Vacuum Command"          {channel="xiaomivacuum:vacuum:034F0E45:actions#commands" }

Number statusBat    "Battery Level [%1.0f%%]" <battery>   (gVac,gVacStat) {channel="xiaomivacuum:vacuum:034F0E45:status#battery" }
Number statusArea    "Cleaned Area [%1.0fm²]" <zoom>   (gVac,gVacStat) {channel="xiaomivacuum:vacuum:034F0E45:status#clean_area" }
Number statusTime    "Cleaning Time [%1.0f']" <clock>   (gVac,gVacStat) {channel="xiaomivacuum:vacuum:034F0E45:status#clean_time" }
String  statusError    "Error [%s]"  <error>  (gVac,gVacStat) {channel="xiaomivacuum:vacuum:034F0E45:status#error_code" }
Number statusFanPow    "Fan Power [%1.0f %%]"  <signal>   (gVacStat) {channel="xiaomivacuum:vacuum:034F0E45:status#fan_power" } 
Number statusClean    "In Cleaning Status [%1.0f]"   <switch>  (gVacStat) {channel="xiaomivacuum:vacuum:034F0E45:status#in_cleaning" }
Switch statusDND    "DND Activated"    (gVacStat) {channel="xiaomivacuum:vacuum:034F0E45:status#dnd_enabled" }
Number statusStatus    "Status [%1.0f]"  <status>  (gVacStat) {channel="xiaomivacuum:vacuum:034F0E45:status#state"} 

Number consumableMain    "Main Brush [%1.0f]"    (gVacCons) {channel="xiaomivacuum:vacuum:034F0E45:consumables#main_brush_time"}
Number consumableSide    "Side Brush [%1.0f]"    (gVacCons) {channel="xiaomivacuum:vacuum:034F0E45:consumables#side_brush_time"}
Number consumableFilter    "Filter Time[%1.0f]"    (gVacCons) {channel="xiaomivacuum:vacuum:034F0E45:consumables#filter_time" }
Number consumableSensor    "Sensor [%1.0f]"    (gVacCons) {channel="xiaomivacuum:vacuum:034F0E45:consumables#sensor_dirt_time"}

Switch dndFunction   "DND Function" <moon>   (gVacDND) {channel="xiaomivacuum:vacuum:034F0E45:dnd#dnd_function"}
String dndStart   "DND Start Time [%s]" <clock>   (gVacDND) {channel="xiaomivacuum:vacuum:034F0E45:dnd#dnd_start"}
String dndEnd   "DND End Time [%s]"   <clock-on>  (gVacDND) {channel="xiaomivacuum:vacuum:034F0E45:dnd#dnd_end"}

Number historyArea    "Total Cleaned Area [%1.0fm²]" <zoom>    (gVacHist) {channel="xiaomivacuum:vacuum:034F0E45:history#total_clean_area"}
String historyTime    "Total Clean Time [%s]"   <clock>     (gVacHist) {channel="xiaomivacuum:vacuum:034F0E45:history#total_clean_time"}
Number historyCount    "Total # Cleanings [%1.0f]"  <office>  (gVacHist) {channel="xiaomivacuum:vacuum:034F0E45:history#total_clean_count"}
```

