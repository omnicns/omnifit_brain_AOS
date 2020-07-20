# OmnifitBrain 
> 다른 언어로 읽기: [한국어](README.md), [ENGLISH](README.en.md)

> Abstract

This library is intended to support the creation of Android apps that can provide services using devices corresponding to the Brain products of the OMNIFIT product family. The provided functions of the library are organized as follows.

 * Device search
 * Connect/disconnect a device
 * EEG measurement/end
 * Device status monitoring (connection status, electrode sensor attachment status, battery level)

## Instruction for Library usage 

### Library initialization
> Java
```java
  public class YourApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        OmnifitBrain.init(this);
    }
  }
```
> Kotlin
```kotlin
  class YourApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        
        OmnifitBrain.init(this)
    }
  }
```

### Device search
> Java
```java
  HeadsetProperties properties;  
  OmnifitBrain.find(
          () -> {          
            // Start searching for devices            
            return Unit.INSTANCE;
          },
          headsetProperties -> {            
            // Device searched              
              properties = headsetProperties
              return Unit.INSTANCE;
          },
          () -> {
              // End device search              
              return Unit.INSTANCE;
          },
          throwable -> {
              // Device search exception              
              if (throwable instanceof BluetoothNotAvailableException) {
                // Bluetooth not supported
              }
              else if (throwable instanceof LocationPermissionNotGrantedException) {
                // Disable location service permission
              }
              else if (throwable instanceof BluetoothNotEnabledException) {
                // Disable Bluetooth adapter
              }
              else if (throwable instanceof LocationServicesNotEnabledException) {
                // Disable location service
              }
              else {
              }
              return Unit.INSTANCE;
          }
  );
```
> Kotlin
```kotlin
  HeadsetProperties properties;  
  OmnifitBrain.find(
      {
          // Start searching for devices
      },
      { heaheadsetPropertiesdset ->      
          // Device searched              
          properties = headsetProperties
      },
      {
          // End device search
      },
      { throwable ->      
          // Device search exception          
          when (throwable) {
              is BluetoothNotAvailableException -> {
                // Bluetooth not supported
              }
              is LocationPermissionNotGrantedException -> {
                // Disable location service permission
              }
              is BluetoothNotEnabledException -> {
                // Disable Bluetooth adapter
              }
              is LocationServicesNotEnabledException -> {
                // Disable location service
              }
              else -> {
              }
          }
      }
  )
```

### Device Connection
Before connecting the device, starts the monitoring of the connection state change first.
> Java
```java
  OmnifitBrain.subscribeConnectionStateChange(
          state -> {
            switch (state) {
            case DISCONNECTED:                     break;
            case EEG_SENSOR_CONNECTING:            break;
            case EARPHONE_CONNECTING:              break;
            case CONNECTING:                       break;
            case EEG_SENSOR_CONNECTED:             break;
            case EARPHONE_CONNECTED:               break;
            case CONNECTED:                        break;
            case EEG_SENSOR_DISCONNECTING:         break;
            case EARPHONE_DISCONNECTING:           break;
            case DISCONNECTING:                    break;
            case ERROR_EARPHONE_CONNECTION_FAILED: break;
            }
            return Unit.INSTANCE;
          }
  );
```
> Kotlin
```kotlin
  OmnifitBrain.subscribeConnectionStateChange { state ->
      when (state) {
          ConnectionState.DISCONNECTED                     -> {}
          ConnectionState.EEG_SENSOR_CONNECTING)           -> {}
          ConnectionState.EARPHONE_CONNECTING)             -> {}
          ConnectionState.CONNECTING)                      -> {}
          ConnectionState.EEG_SENSOR_CONNECTED)            -> {}
          ConnectionState.EARPHONE_CONNECTED)              -> {}
          ConnectionState.CONNECTED)                       -> {}
          ConnectionState.EEG_SENSOR_DISCONNECTING)        -> {}
          ConnectionState.EARPHONE_DISCONNECTING)          -> {}
          ConnectionState.DISCONNECTING)                   -> {}
          ConnectionState.ERROR_EARPHONE_CONNECTION_FAILED -> {}
      }
  }
```

Device connection
> Java
```java
  OmnifitBrain.connect(getApplicationContext(), properties);
```
> Kotlin
```kotlin
  OmnifitBrain.connect(applicationContext, properties)
```

### EEG measurement
> Java
```java
  // @param duration 60(Sec), Measurement progress time
  // @parma eyesState EyesState.CLOSED, When the measurement is in progress, the measured data differs depending on the closed eyes and open eyes conditions.
  OmnifitBrain.measure(
          state -> {
              if (state instanceof MeasurementState.Start) {            // measurement time
                state.getElapsedTime();           // Progress time (second)
                state.getElectrodeState();        // Electrode attached state
                state.getSignalStabilityState();  // Signal stability
              }
              else if (state instanceof MeasurementState.Measuring) {   // Measuring
                state.getValue();                 // Measurement data at 2 second intervals, double[272]
              }  
              else if (state instanceof MeasurementState.Stop) {        // End measurement
                state.getResult();                // Data list every 2 seconds for the entire duration, List<double>
              }       
              return Unit.INSTANCE;
          }
  );
```
> Kotlin
```kotlin
  // @param duration 60(Sec), Measurement progress time
  // @parma eyesState EyesState.CLOSED, When the measurement is in progress, the measured data differs depending on the closed eyes and open eyes conditions.
  OmnifitBrain.measure(
      onState = { state ->
          when (state) {
              is MeasurementState.Start     -> {  // measurement time 
                state.elapsedTime           // Progress time (second)
                state.electrodeState        // Electrode attached state
                state.signalStabilityState  // Signal stability
              } 
              is MeasurementState.Measuring -> {  // Measuring
                state.value                 // Measurement data at 2 second intervals, double[272]
              } 
              is MeasurementState.Stop      -> {  // End measurement
                state.result                // Data list every 2 seconds for the entire duration, List<DoubleArray>
              } 
          }
      }
  )
```
During measurement (every 2 seconds), measurement data is returned at the end of measurement. `Data array[272]`

| Index | Data classification | meaning | Range |
|---|:---:|:---:|---:|
| [0] | `Right brain theta size judgment value` | Right brain theta wave (less than 4-8Hz) Size determination of absolute power | 0 to 10 |
| [1] | `Left brain theta size judgment value` | Left brain theta wave (less than 4-8Hz) Size determination of absolute power | 0 to 10 |
| [2] | `Right brain alpha size judgment value` | Right brain alpha (less than 8-12Hz) Size determination of absolute power | 0 to 10 |
| [3] | `Left brain alpha size judgment value` | Left brain alpha (less than 8-12HZ) Size determination of absolute power | 0 to 10 |
| [4] | `Right Brain Low Beta size judgment value` | Right brain Low beta wave (below 12-15Hz) Size determination of absolute power | 0 to 10 |
| [5] | `Left Brain Low Beta size judgment value` | Left brain low beta wave (below 12-15Hz) Size determination of absolute power | 0 to 10 |
| [6] | `Right brain middle beta size judgment value` | Right brain Middle beta wave (less than 15-20Hz) Size determination of absolute power | 0 to 10 |
| [7] | `Left brain middle beta size judgment value` | Left brain middle beta wave (less than 15-20Hz) Size determination of absolute power | 0 to 10 |
| [8] | `Right brain high beta size judgment value` | Right brain high beta wave (less than 20-30Hz) Size determination of absolute power | 0 to 10 |
| [9] | `Left Brain High Beta size judgment value` | Left brain high beta wave (less than 20-30Hz) Size determination of absolute power | 0 to 10 |
| [10] | `Right brain gamma size judgment value` | Right brain gamma wave (less than 30-40Hz) Size determination of absolute power | 0 to 10 |
| [11] | `Left brain gamma size judgment value` | Left brain gamma wave (less than 30-40Hz) Size determination of absolute power | 0 to 10 |
| [12] | `Concentration size judgment value` | Concentration size determination value | 0 to 10 |
| [13] | `Right brain relaxation size judgment value` | Right brain relaxation size determination value | 0 ~ 10 |
| [14] | `Left brain relaxation size judgment value` | Left brain relaxation size determination value | 0 to 10 |
| [15] | `Left and right brain balance size judgment value` | Left-right brain balance size determination value | 0 to 10 |
| [16] ~ [23] (if [16] == [0], [0] ~ [7]) | `Left brain delta power spectrum` | It is the value multiplied by the gain factor (100). Therefore, to obtain the actual value, divide 100 by the value in the right range. | 0 ~ 65535 |
| [24] ~ [31] (if [16] == [0], [8] ~ [15]) | `Left brain theta power spectrum` | It is the value multiplied by the gain factor (100). Therefore, to obtain the actual value, divide 100 by the value in the right range. | 0 ~ 65535 |
| [32] ~ [39] (if [16] == [0], [16] ~ [23]) | `Left brain alpha power spectrum` | It is the value multiplied by the gain factor (100). Therefore, to obtain the actual value, divide 100 by the value in the right range. | 0 ~ 65535 |
| [40] ~ [45] (if [16] == [0], [24] ~ [29]) | `Left Brain Low Beta Power Spectrum` | It is the value multiplied by the gain factor (100). Therefore, to obtain the actual value, divide 100 by the value in the right range. | 0 ~ 65535 |
| [46] ~ [55] (if [16] == [0], [30] ~ [39]) | `Left Brain Middle Beta Power Spectrum` | It is the value multiplied by the gain factor (100). Therefore, to obtain the actual value, divide 100 by the value in the right range. | 0 ~ 65535 |
| [56] ~ [76] (if [16] == [0], [40] ~ [60]) | `Left Brain High Beta Power Spectrum` | It is the value multiplied by the gain factor (100). Therefore, to obtain the actual value, divide 100 by the value in the right range. | 0 ~ 65535 |
| [77] ~ [97] (if [16] == [0], [61] ~ [81]) | `Left brain gamma power spectrum` | It is the value multiplied by the gain factor (100). Therefore, to obtain the actual value, divide 100 by the value in the right range. | 0 ~ 65535 |
| [144] ~ [151] (if [144] == [0], [0] ~ [7]) | `Right Brain Delta Power Spectrum` | It is the value multiplied by the gain factor (100). Therefore, to obtain the actual value, divide 100 by the value in the right range. | 0 ~ 65535 |
| [152] ~ [159] (if [144] == [0], [8] ~ [15]) | `Right brain theta power spectrum` | It is the value multiplied by the gain factor (100). Therefore, to obtain the actual value, divide 100 by the value in the right range. | 0 ~ 65535 |
| [160] ~ [167] (if [144] == [0], [16] ~ [23]) | `Right Brain Alpha Power Spectrum` | It is the value multiplied by the gain factor (100). Therefore, to obtain the actual value, divide 100 by the value in the right range. | 0 ~ 65535 |
| [168] ~ [173] (if [144] == [0], [24] ~ [29]) | `Right Brain Low Beta Power Spectrum` | It is the value multiplied by the gain factor (100). Therefore, to obtain the actual value, divide 100 by the value in the right range. | 0 ~ 65535 |
| [174] ~ [183] (if [144] == [0], [30] ~ [39]) | `Right Brain Middle Beta Power Spectrum` | It is the value multiplied by the gain factor (100). Therefore, to obtain the actual value, divide 100 by the value in the right range. | 0 ~ 65535 |
| [184] ~ [204] (if [144] == [0], [40] ~ [60]) | `Right Brain High Beta Power Spectrum` | It is the value multiplied by the gain factor (100). Therefore, to obtain the actual value, divide 100 by the value in the right range. | 0 ~ 65535 |
| [205] ~ [225] (if [144] == [0], [61] ~ [81]) | `Right brain gamma power spectrum` | It is the value multiplied by the gain factor (100). Therefore, to obtain the actual value, divide 100 by the value in the right range. | 0 ~ 65535 |

Power spectrum section (0.488 Hz in increments of frequency per index)
| Data classification | Hz range | Index section | Quantity |
|---|:---:|:---:|---:|
| DELTA  | 0  ~ 4Hz  less | (L/RPS_[ 0 ~  7]) |  8 |
| THETA  | 4  ~ 8Hz  less | (L/RPS_[ 8 ~ 15]) |  8 |
| ALPHA  | 8  ~ 12Hz less | (L/RPS_[16 ~ 23]) |  8 |
| L-BETA | 12 ~ 15Hz less | (L/RPS_[24 ~ 29]) |  6 |
| M-BETA | 15 ~ 20Hz less | (L/RPS_[30 ~ 39]) | 10 |
| H-BETA | 20 ~ 30Hz less | (L/RPS_[40 ~ 60]) | 21 |
| GAMMA  | 30 ~ 40Hz Below | (L/RPS_[61 ~ 81]) | 21 |

### Status Monitoring
Electrode sensor attached status
> Java
```java
  OmnifitBrain.subscribeElectrodeStateChange(state -> {
      switch (state) {
        case DETACHED:              break;
        case L_EEG_SENSOR_DETACHED: break;
        case R_EEG_SENSOR_DETACHED: break;
        case L_EARPHONE_DETACHED:   break;
        case R_EARPHONE_DETACHED:   break;
        case ATTACHED:              break;
      }
      return Unit.INSTANCE;
  });
```
> Kotlin
```kotlin
  OmnifitBrain.subscribeElectrodeStateChange { state ->
      when (state) {
        ElectrodeState.DETACHED               -> {}             
        ElectrodeState.L_EEG_SENSOR_DETACHED  -> {}
        ElectrodeState.R_EEG_SENSOR_DETACHED  -> {}
        ElectrodeState.L_EARPHONE_DETACHED    -> {} 
        ElectrodeState.R_EARPHONE_DETACHED    -> {} 
        ElectrodeState.ATTACHED               -> {}            
      }
  }
```
Battery level status
> Java
```java
  OmnifitBrain.subscribeBatteryLevelChange(level -> {      
      return Unit.INSTANCE;
  });
```
> Kotlin
```kotlin
  OmnifitBrain.subscribeBatteryLevelChange { level ->
  }
```

### Library Reference Settings
> Gradle

Project level
```groovy
  dependencies {
      ...
      classpath 'org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.31'
  }

  allprojects {
    repositories {
        google()
        jcenter()
        ...
        maven { url 'http://maven.omnicns.co.kr:8081/nexus/content/repositories/releases/' }
    }
}
```
App level
```groovy
  dependencies {
    ...
    implementation "omnifit.sdk:brain-kotlin:0.1.0"
  }
```
## License

    Copyright 2019 omniC&S

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


  
