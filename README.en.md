# OmnifitBrain 
> 다른 언어로 읽기: [한국어](README.md), [ENGLISH](README.en.md)

> 개요

본 라이브러리는 옴니핏 제품의 브레인 제품군에 해당하는 장치를 사용하여 서비스를 제공 할수 있는 안드로이드 앱을 만들 수 있도록 지원 하기 위한 것입니다.
라이브러리의 제공되는 기능은 아래와 같이 구성 됩니다.

 * 브레인 장치 검색
 * 브레인 장치 연결/해제
 * 뇌파 측정/종료
 * 장치 상태 모니터링 (연결 상태, 전극 센서 부착 상태, 배터리 잔량)

## 라이브러리 사용 설명 

### 라이브러리 초기화
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

### 브레인 장치 검색
> Java
```java
  HeadsetProperties properties;  
  OmnifitBrain.find(
          () -> {          
            // 브레인 장치 검색 시작            
            return Unit.INSTANCE;
          },
          headsetProperties -> {            
            // 브레인 장치 검색 됨              
              properties = headsetProperties
              return Unit.INSTANCE;
          },
          () -> {
              // 브레인 장치 검색 종료              
              return Unit.INSTANCE;
          },
          throwable -> {
              // 브레인 장치 검색 예외              
              if (throwable instanceof BluetoothNotAvailableException) {
                // 블루투스 미지원
              }
              else if (throwable instanceof LocationPermissionNotGrantedException) {
                // 위치 서비스 권한 비활성화
              }
              else if (throwable instanceof BluetoothNotEnabledException) {
                // 블루투스 아답타 비활성화
              }
              else if (throwable instanceof LocationServicesNotEnabledException) {
                // 위치 서비스 비활성화
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
          // 브레인 장치 검색 시작
      },
      { heaheadsetPropertiesdset ->      
          // 브레인 장치 검색 됨              
          properties = headsetProperties
      },
      {
          // 브레인 장치 검색 종료
      },
      { throwable ->      
          // 브레인 장치 검색 예외          
          when (throwable) {
              is BluetoothNotAvailableException -> {
                // 블루투스 미지원
              }
              is LocationPermissionNotGrantedException -> {
                // 위치 서비스 권한 비활성화
              }
              is BluetoothNotEnabledException -> {
                // 블루투스 아답타 비활성화
              }
              is LocationServicesNotEnabledException -> {
                // 위치 서비스 비활성화
              }
              else -> {
              }
          }
      }
  )
```

### 브레인 장치 연결
장치 연결전 연결 상태 변화 모니터링을 먼저 시작한다.
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

장치 연결
> Java
```java
  OmnifitBrain.connect(getApplicationContext(), properties);
```
> Kotlin
```kotlin
  OmnifitBrain.connect(applicationContext, properties)
```

### 뇌파 측정
> Java
```java
  // @param duration 60(초), 측정진행시간
  // @parma eyesState EyesState.CLOSED, 측정진행시 눈감은(CLOSED), 눈뜬(OPEND)상태에 따라 측정된 데이터가 상이함.
  OmnifitBrain.measure(
          state -> {
              if (state instanceof MeasurementState.Start) {            // 측정 시작
                state.getElapsedTime();           // 진행시간(초)
                state.getElectrodeState();        // 전극부착상태
                state.getSignalStabilityState();  // 신호안정상태
              }
              else if (state instanceof MeasurementState.Measuring) {   // 측정 중
                state.getValue();                 // 2초 간격의 측정 데이터, double[272]
              }  
              else if (state instanceof MeasurementState.Stop) {        // 측정 종료
                state.getResult();                // 전체 진행시간 동안의 2초 간격 데이터 리스트, List<double>
              }       
              return Unit.INSTANCE;
          }
  );
```
> Kotlin
```kotlin
  // @param duration 60(초), 측정진행시간
  // @parma eyesState EyesState.CLOSED, 측정진행시 눈감은(CLOSED), 눈뜬(OPEND)상태에 따라 측정된 데이터가 상이함.
  OmnifitBrain.measure(
      onState = { state ->
          when (state) {
              is MeasurementState.Start     -> {  // 측정 시작 
                state.elapsedTime           // 진행시간(초)
                state.electrodeState        // 전극부착상태)
                state.signalStabilityState  // 신호안정상태)
              } 
              is MeasurementState.Measuring -> {  // 측정 중
                state.value                 // 2초 간격의 측정 데이터, DoubleArray[272]
              } 
              is MeasurementState.Stop      -> {  // 측정 종료
                state.result                // 전체 진행시간 동안의 2초 간격 데이터 리스트, List<DoubleArray>
              } 
          }
      }
  )
```
측정 중(2초 간격), 측정 종료시 측정 데이터를 돌려준다. `Data array[272]`

| 인덱스 | 데이터 구분 | 의미 | 범위 |
|---|:---:|:---:|---:|
| [0] | `우뇌쎄타 크기판정값` | 우뇌 쎄타파(4-8Hz미만) 절대파워의 크기판정값 | 0 ~ 10 |
| [1] | `좌뇌쎄타 크기판정값` | 좌뇌 쎄타파(4-8Hz미만) 절대파워의 크기판정값 | 0 ~ 10 |
| [2] | `우뇌알파 크기판정값` | 우뇌 알파(8-12Hz미만) 절대파워의 크기판정값 | 0 ~ 10 |
| [3] | `좌뇌알파 크기판정값` | 좌뇌 알파(8-12HZ미만) 절대파워의 크기판정값 | 0 ~ 10 |
| [4] | `우뇌Low베타 크기판정값` | 우뇌 Low베타파(12-15Hz미만) 절대파워의 크기판정값 | 0 ~ 10 |
| [5] | `좌뇌Low베타 크기판정값` | 좌뇌 Low베타파(12-15Hz미만) 절대파워의 크기판정값 | 0 ~ 10 |
| [6] | `우뇌Middle베타 크기판정값` | 우뇌 Middle베타파(15-20Hz미만) 절대파워의 크기판정값 | 0 ~ 10 |
| [7] | `좌뇌Middle베타 크기판정값` | 좌뇌 Middle베타파(15-20Hz미만) 절대파워의 크기판정값 | 0 ~ 10 |
| [8] | `우뇌High베타 크기판정값` | 우뇌 High베타파(20-30Hz미만) 절대파워의 크기판정값 | 0 ~ 10 |
| [9] | `좌뇌High베타 크기판정값` | 좌뇌 High베타파(20-30Hz미만) 절대파워의 크기판정값 | 0 ~ 10 |
| [10] | `우뇌감마 크기판정값` | 우뇌 감마파(30-40Hz미만) 절대파워의 크기판정값 | 0 ~ 10 |
| [11] | `좌뇌감마 크기판정값` | 좌뇌 감마파(30-40Hz미만) 절대파워의 크기판정값 | 0 ~ 10 |
| [12] | `집중 크기판정값` | 집중 크기판정값 | 0 ~ 10 |
| [13] | `우뇌이완 크기판정값` | 우뇌이완 크기판정값 | 0 ~ 10 |
| [14] | `좌뇌이완 크기판정값` | 좌뇌이완 크기판정값 | 0 ~ 10 |
| [15] | `좌우뇌균형 크기판정값` | 좌우뇌균형 크기판정값 | 0 ~ 10 |
| [16] ~ [23] ([16] == [0] 일때, [0] ~ [7]) | `좌뇌델타 파워스펙트럼` | gain factor(100)가 곱해진 값이다. 그러므로 실제 값을 얻으려면 우측 범위의 값에 100을 나누어 준다. | 0 ~ 65535 |
| [24] ~ [31] ([16] == [0] 일때, [8] ~ [15]) | `좌뇌쎄타 파워스펙트럼` | gain factor(100)가 곱해진 값이다. 그러므로 실제 값을 얻으려면 우측 범위의 값에 100을 나누어 준다. | 0 ~ 65535 |
| [32] ~ [39] ([16] == [0] 일때, [16] ~ [23]) | `좌뇌알파 파워스펙트럼` | gain factor(100)가 곱해진 값이다. 그러므로 실제 값을 얻으려면 우측 범위의 값에 100을 나누어 준다. | 0 ~ 65535 |
| [40] ~ [45] ([16] == [0] 일때, [24] ~ [29]) | `좌뇌Low베타 파워스펙트럼` | gain factor(100)가 곱해진 값이다. 그러므로 실제 값을 얻으려면 우측 범위의 값에 100을 나누어 준다. | 0 ~ 65535 |
| [46] ~ [55] ([16] == [0] 일때, [30] ~ [39]) | `좌뇌Middle베타 파워스펙트럼` | gain factor(100)가 곱해진 값이다. 그러므로 실제 값을 얻으려면 우측 범위의 값에 100을 나누어 준다. | 0 ~ 65535 |
| [56] ~ [76] ([16] == [0] 일때, [40] ~ [60]) | `좌뇌High베타 파워스펙트럼` | gain factor(100)가 곱해진 값이다. 그러므로 실제 값을 얻으려면 우측 범위의 값에 100을 나누어 준다. | 0 ~ 65535 |
| [77] ~ [97] ([16] == [0] 일때, [61] ~ [81]) | `좌뇌감마 파워스펙트럼` | gain factor(100)가 곱해진 값이다. 그러므로 실제 값을 얻으려면 우측 범위의 값에 100을 나누어 준다. | 0 ~ 65535 |
| [144] ~ [151] ([144] == [0] 일때, [0] ~ [7]) | `우뇌델타 파워스펙트럼` | gain factor(100)가 곱해진 값이다. 그러므로 실제 값을 얻으려면 우측 범위의 값에 100을 나누어 준다. | 0 ~ 65535 |
| [152] ~ [159] ([144] == [0] 일때, [8] ~ [15]) | `우뇌쎄타 파워스펙트럼` | gain factor(100)가 곱해진 값이다. 그러므로 실제 값을 얻으려면 우측 범위의 값에 100을 나누어 준다. | 0 ~ 65535 |
| [160] ~ [167] ([144] == [0] 일때, [16] ~ [23]) | `우뇌알파 파워스펙트럼` | gain factor(100)가 곱해진 값이다. 그러므로 실제 값을 얻으려면 우측 범위의 값에 100을 나누어 준다. | 0 ~ 65535 |
| [168] ~ [173] ([144] == [0] 일때, [24] ~ [29]) | `우뇌Low베타 파워스펙트럼` | gain factor(100)가 곱해진 값이다. 그러므로 실제 값을 얻으려면 우측 범위의 값에 100을 나누어 준다. | 0 ~ 65535 |
| [174] ~ [183] ([144] == [0] 일때, [30] ~ [39]) | `우뇌Middle베타 파워스펙트럼` | gain factor(100)가 곱해진 값이다. 그러므로 실제 값을 얻으려면 우측 범위의 값에 100을 나누어 준다. | 0 ~ 65535 |
| [184] ~ [204] ([144] == [0] 일때, [40] ~ [60]) | `우뇌High베타 파워스펙트럼` | gain factor(100)가 곱해진 값이다. 그러므로 실제 값을 얻으려면 우측 범위의 값에 100을 나누어 준다. | 0 ~ 65535 |
| [205] ~ [225] ([144] == [0] 일때, [61] ~ [81]) | `우뇌감마 파워스펙트럼` | gain factor(100)가 곱해진 값이다. 그러므로 실제 값을 얻으려면 우측 범위의 값에 100을 나누어 준다. | 0 ~ 65535 |

파워스펙트럼 구간 (1 인덱스당 주파수 증가 단위는 0.488Hz)
| 데이터 구분 | Hz 범위 | 인덱스 구간 | 수량 |
|---|:---:|:---:|---:|
| DELTA  | 0  ~ 4Hz  미만 | (L/RPS_[ 0 ~  7]) |  8 |
| THETA  | 4  ~ 8Hz  미만 | (L/RPS_[ 8 ~ 15]) |  8 |
| ALPHA  | 8  ~ 12Hz 미만 | (L/RPS_[16 ~ 23]) |  8 |
| L-BETA | 12 ~ 15Hz 미만 | (L/RPS_[24 ~ 29]) |  6 |
| M-BETA | 15 ~ 20Hz 미만 | (L/RPS_[30 ~ 39]) | 10 |
| H-BETA | 20 ~ 30Hz 미만 | (L/RPS_[40 ~ 60]) | 21 |
| GAMMA  | 30 ~ 40Hz 이하 | (L/RPS_[61 ~ 81]) | 21 |

### 상태 모니터링
전극 센서 부착 상태
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
배터리 잔량 상태
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

### 라이브러리 참조 설정
> Gradle

프로젝트 레벨
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
앱 레벨
```groovy
  dependencies {
    ...
    implementation "omnifit.sdk:brain-kotlin:0.1.0"
  }
```
## 라이센스

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


  
