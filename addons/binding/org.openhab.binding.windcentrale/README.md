# WindCentraleBinding

This Binding is used to display the details of a WindCentrale wind mill. 

## Supported Things

This Binding supports WindCentrale mills devices.

## Discovery

There is no discovery available for this binding.

## Binding Configuration

No binding configuration required.

## Thing Configuration

The thing mandatory configuration is the selection of the mill.
Optional configuration is the number of Wind Parts ("Wind Delen") and the refresh interval.

## Channels

- **windSpeed** Measured current wind speed 
- **windDirection** Current wind direction
- **powerAbsTot** Total Power
- **powerAbsWd** Power provided for your windparts
- **powerRel** Relative power
- **kwh** Current Energy
- **kwhForecast** Energy Forecast
- **runPercentage** Run Percentage this year
- **timestamp** Timestamp of the last update


## Full example
```
Group   gReiger "WindCentrale Reiger"   <wind>

Number  ReigerWindSpeed         "Wind Snelheid[%1.0f Bf]"         <wind>    (gReiger) {channel="windcentrale:mill:reiger:windSpeed")
String  ReigerWindDirection     "Wind Richting [%s]"              <wind>    (gReiger) {channel="windcentrale:mill:reiger:windDirection")
Number  ReigerPowerAbsTot       "Productie molen [%1.0f kW]"      <wind>    (gReiger) {channel="windcentrale:mill:reiger:powerAbsTot")
Number  ReigerPowerAbsWd        "WD Power [%1.0f W]"              <wind>    (gReiger) {channel="windcentrale:mill:reiger:powerAbsWd")
Number  ReigerPowerRel          "Productie Vermogen [%1.0f %%]"   <wind>    (gReiger) {channel="windcentrale:mill:reiger:powerRel")
Number  ReigerKwh               "kwh [%1.0f]"                     <wind>    (gReiger) {channel="windcentrale:mill:reiger:kwh")
Number  ReigerKwhForecast       "Productie Forecast [%1.0f]"      <wind>    (gReiger) {channel="windcentrale:mill:reiger:kwhForecast")
Number  ReigerRunPercentage     "Run Percentage [%1.0f %%]"       <wind>    (gReiger) {channel="windcentrale:mill:reiger:runPercentage")
Number  ReigerTimestamp         "Update Timestamp [%1$ta %1$tR]"  <wind>    (gReiger) {channel="windcentrale:mill:reiger:timestamp")

```