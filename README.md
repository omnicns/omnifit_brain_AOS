# OmnifitBrain

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
  OmnifitBrain.measure(
          state -> {
              if (state instanceof MeasurementState.Start) {}           // 측정 시작
              else if (state instanceof MeasurementState.Measuring) {}  // 측정 중
              else if (state instanceof MeasurementState.Stop) {}       // 측정 종료
              return Unit.INSTANCE;
          }
  );
```
> Kotlin
```kotlin
  OmnifitBrain.measure(
      onState = { state ->
          when (state) {
              is MeasurementState.Start     -> {} // 측정 시작
              is MeasurementState.Measuring -> {} // 측정 중
              is MeasurementState.Stop      -> {} // 측정 종료
          }
      }
  )
```
측정 중(2초 간격), 측정 종료시 측정 데이터를 돌려준다.

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


  
