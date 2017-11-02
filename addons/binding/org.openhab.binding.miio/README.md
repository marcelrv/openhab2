# Xiaomi Robot Vacuum Binding

This Binding is used to control Xiaomi Mi IO devices.

![MIIO logo](doc/miio.png)

## Supported Things

This Binding supports Xiaomi Mi IO devices (Several Xiaomi wifi devices).
Currently mainly the Xiaomi Robot Vacuum is supported, remaining devices are experimental.
Experimental means that most likely you can send commands to the devices

The following things are available

| Thing Type | Description |
|--------|---------| 
| miio:generic | Generic type for discovered devices. Once the tolken is available and the device model is determined, this thingtype will automatically change to the appropriate thingtype for the model |
| miio:vacuum | For Xiaomi Robot Vacuum |
| miio:basic | For several basic devices like yeelights, airpurifiers. Channels and commands are determined by database configuration |
| miio:unsupported | For experimenting with other devices which use the Mi IO protocol)

## Mi IO Devices

| Device | Thing Type | Auto tolken | Device Model  | Remark |
| ----------- | --------- |:---:| ---------- | -----------| 
| [Xiaomi Robot Vacuum](doc/vacuum.md) | miio:vacuum | No | rockrobo.vacuum.v1 |


## Discovery

The binding needs a token from the Xiaomi Mi Device in order to be able to control it.

Depending on the Mi IO device, this tolken is automatically provided or it needs to be extracted from the MiHome app.

The token needs to be retrieved from the application database (find the mio2db file with and read it sqlite). The easiest way on Android to do is by using [MiToolkit](https://github.com/ultrara1n/MiToolkit/releases)

For iPhone, use an un-encrypted iTunes-Backup and unpack it and use a sqlite tool to view the files in it: 
Then search in "RAW, com.xiaomi.home," for "USERID_mihome.sqlite" and look for the 32-digit-token.

## Binding Configuration

No binding configuration is required.

## Thing Configuration

The binding needs ip address and token to be able to communicate. See discovery for details.
Optional configuration is the refresh interval and the deviceID. Note that the deviceID is automatically retrieved when it is left blank.

## Channels

Depending on the device, the channels are available.
See the detailed device files for the available channels


