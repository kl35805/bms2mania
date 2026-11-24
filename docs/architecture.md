# bms2mania 아키텍처

1차 MVP 기준의 대략적인 아키텍처입니다.
요구사항은 [requirements.md](./requirements.md), 파일 형식은 [format.md](./format.md)를 참고하세요.

---

## 1. 모듈 구성

Gradle 멀티모듈 2개로 구성한다.

```
bms2mania/
├── core/   ← 변환·스캔·난이도표 로직 (순수 Java, JavaFX 의존 없음)
└── app/    ← JavaFX 화면 (core를 호출만 함)
```

나누는 이유:
- **테스트 용이성**: core에 JavaFX 의존이 없어서 JUnit만으로 변환 로직 전체를 테스트할 수 있다.
- **경계 강제**: 화면 코드가 변환 로직에 섞이는 것을 Gradle 의존 관계가 막는다.
- **설계 의도 표현**: 로직과 UI를 분리했다는 점이 레포 구조에서 바로 드러난다.

---

## 2. 데이터 흐름

```
 [BMS 폴더]                         [난이도표 URL]
     │                                   │
     ▼                                   ▼
 library: 스캔·7키 판별·캐시       table: 동기화·캐시·해시 색인
     │                                   │
     └──────────► GUI 검색·선택 ◄────────┘
                       │ (채보 1개)
                       ▼
               convert: 변환 총괄
                       │
   ┌───────────────────┼─────────────────────────┐
   ▼                   ▼                         ▼
 bms: 파싱          chart: 시간 계산·          table: 레벨 조회
 (바이트→해시,       노트 추출                  (Version/Tags)
  인코딩→BmsChart)  (BmsChart→Chart)
                       │
          ┌────────────┴────────────┐
          ▼                         ▼
   osu: .osu 작성             audio: 디코딩→믹싱→mp3
          └────────────┬────────────┘
                       ▼
              osu: .osz 패키징 + 변환 리포트
```

---

## 3. core 모듈 패키지

기준 패키지: `io.github.kl35805.bms2mania`

| 패키지 | 책임 | 주요 클래스 (안) |
|---|---|---|
| `bms` | BMS 파일 읽기: 원본 바이트로 해시 계산, 인코딩 판별, 헤더·채널 파싱 | `BmsFile`(바이트·해시·인코딩), `BmsParser`, `BmsChart`(파일 내용 그대로), `RandomResolver` |
| `chart` | BMS를 형식 중립적인 채보 모델로 해석 | `TimingEngine`(마디 위치 → ms), `NoteExtractor`(레인·롱노트), `Chart`(노트·타이밍·사운드 이벤트) |
| `osu` | osu 출력 | `OsuBeatmap`, `OsuWriter`, `OszPackager`, `FileNameSanitizer` |
| `audio` | 키음을 합쳐 단일 음원 생성 | `FfmpegRunner`, `KeysoundLoader`, `Mixer`(배치·choke·정규화), `Mp3Encoder` |
| `table` | 난이도표 | `TableFetcher`(HTTP·리다이렉트·상대 경로), `TableParser`(JSON), `TableCache`, `TableIndex`(해시 → 레벨), `LevelFormatter`(`st12 / dy7`) |
| `library` | 폴더 스캔·검색 | `LibraryScanner`, `KeyModeDetector`(7키 판별), `LibraryCache`, `SearchService` |
| `convert` | 위 패키지를 순서대로 엮는 변환 총괄 | `ConversionService`, `ConversionOptions`, `ConversionReport` |
| `config` | 설정 저장·로드 | `AppSettings`(BMS 폴더, 출력 폴더, ffmpeg 경로) |
| `common` | 공통 인터페이스 | `ProgressListener`, `Warning` |

---

## 4. app 모듈 (JavaFX)

### 화면 배치 (초안)

```
┌──────────────────────────────────────────────────────────┐
│ [BMS 폴더: ______ ][찾기][다시 스캔]   [표 동기화] 마지막: 16:20 │
├──────────────────────────────────────────────────────────┤
│ 검색: [____________]                                       │
├───────────────────────────────┬──────────────────────────┤
│ 결과 목록 (TableView)          │ 선택한 채보 정보            │
│ 제목 | 아티스트 | 레벨 | 표 레벨 │ 제목/부제/BPM/노트 수       │
│ ...                           │ Version 미리보기: st12 / dy7│
│                               │ 인코딩: [자동 ▼]            │
│                               │ [ 변환 ]                    │
├───────────────────────────────┴──────────────────────────┤
│ 변환 리포트 / 로그                                          │
└──────────────────────────────────────────────────────────┘
+ 설정 창: ffmpeg 경로, 출력 폴더, "채보만 변환" 옵션
```

### 동작 원칙
- 스캔·동기화·변환처럼 오래 걸리는 작업은 JavaFX `Task`로 백그라운드에서 실행한다.
- 진행 상황은 core의 `ProgressListener`로 전달받는다. core는 JavaFX에 의존하지 않는다.

---

## 5. 설계 원칙

1. **중간 모델(`Chart`)을 둔다.** BMS 파서와 osu 출력이 서로 직접 알지 않게 해서 단계별로 테스트할 수 있게 한다.
2. **해시는 디코딩 전에 계산한다.** 난이도표 매칭이 원본 파일 바이트 기준이다.
3. **ffmpeg는 인터페이스 뒤에 둔다.** 테스트에서 가짜 구현으로 바꿔 끼울 수 있게 해서, ffmpeg가 없는 환경에서도 테스트가 돌아가게 한다.
4. **경고는 예외 대신 리포트에 모은다.** 키음 하나가 없다고 변환 전체가 실패하지 않게 한다.
5. **core는 상태를 최소화한다.** 입력을 받아 결과를 돌려주는 서비스 형태로 만들고, 캐시와 설정만 파일로 저장한다.

---

## 6. 빌드 구성과 외부 라이브러리

### 빌드 구성

```
bms2mania/
├── settings.gradle.kts           모듈 등록 (core, app)
├── gradle/
│   ├── libs.versions.toml        버전 카탈로그 (라이브러리·플러그인 버전)
│   └── wrapper/                  Gradle Wrapper
├── buildSrc/src/main/kotlin/
│   └── bms2mania.java-conventions.gradle.kts   공통 설정 (JDK 25 toolchain, UTF-8, JUnit)
├── core/build.gradle.kts         java-library + 공통 설정
└── app/build.gradle.kts          application + JavaFX 플러그인 + 공통 설정, core 의존
```

- 공통 설정은 루트 `subprojects {}` 대신 **컨벤션 플러그인**(`buildSrc`)으로 공유한다. Gradle 권장 방식이다.
- 버전은 `gradle/libs.versions.toml`에서만 관리한다.
  - 버전 키가 다른 키의 접두어가 되지 않게 짓는다. 예: `javafx`와 `javafx-plugin`을 같이 쓰면 `libs.versions.javafx`가 값이 아닌 묶음이 된다.
- app의 실행 시작점은 `Application`을 상속하지 않는 `Launcher` 클래스다. IDE 실행이나 단일 jar 실행에서 JavaFX 런타임 오류를 피하기 위해서다.

### 외부 라이브러리

| 용도 | 라이브러리 | 버전 | 상태 |
|---|---|---|---|
| 빌드 | Gradle (Wrapper) | 9.9.1 | 적용 |
| GUI | JavaFX + `org.openjfx.javafxplugin` | 25.0.5.1 / 0.1.0 | 적용 |
| 테스트 | JUnit (BOM) | 6.1.3 | 적용 |
| JSON (난이도표, 캐시, 설정) | Jackson | – | 예정 |
| 인코딩 판별 | juniversalchardet | – | 예정 |
| HTTP | JDK 내장 `HttpClient` | – | 예정 |

- 캐시(스캔 결과, 난이도표)와 설정은 **JSON 파일**로 저장한다. SQLite는 MVP 규모에 필요하지 않다.
- "예정" 항목의 버전은 의존성을 추가하는 시점에 확인한다.

---

## 7. 구현 순서

불확실한 부분을 최대한 빨리 검증하는 순서로 구성했다.

| 순서 | 작업 | 확인하는 것 |
|---|---|---|
| 1 | Gradle 멀티모듈 설정 + 빈 JavaFX 창 | 빌드 환경 |
| 2 | `bms`: 파일 읽기·해시·인코딩·파싱 | 일본어·한국어 제목이 깨지지 않는지 |
| 3 | `chart`: 타이밍 엔진 | 손으로 계산한 ms와 일치하는지 |
| 4 | `chart`: 노트 추출 (레인, 롱노트) | |
| 5 | `osu`: .osu 작성 + .osz 패키징 (채보만) | **osu에서 플레이 가능한지, UTF-8이 정상인지** |
| 6 | `table`: 동기화·캐시·매칭 | 4개 표 URL의 실제 응답 |
| 7 | `library`: 스캔·7키 판별·검색 | |
| 8 | `app`: GUI 연결 | |
| 9 | `audio`: ffmpeg 연동·믹싱·mp3 | 무음 샘플로 기본 타격음이 꺼지는지 |
| 10 | 변환 리포트, 마무리, README | |

5번이 끝나면 음원 없이도 osu에서 채보를 직접 플레이해 볼 수 있다. 타이밍과 파일 형식이라는 가장 큰 위험을 일찍 확인하는 것이 목적이다.
