# BME / osu! 파일 구조 정리

bme2osu 프로젝트에서 참고하기 위한 두 파일 형식의 구조 정리입니다.
각 항목의 출처는 문서 끝의 [출처](#출처) 번호로 표시했습니다. 출처로 확인하지 못한 내용은 **(미검증)** 으로 표시했습니다.

---

## 1. BME 파일 구조

### 1-1. 기본 규칙

- 일반 텍스트 파일이며 `#`으로 시작하는 줄만 명령으로 취급하고, 나머지 줄은 모두 무시(주석)한다. 줄 순서는 자유롭다. [B1]
- 명령은 대소문자를 구분하지 않는다. [B1]
- 인코딩은 규격에 정해져 있지 않으며, 일본에서 만든 BMS 대부분은 Shift_JIS로 쓰여 있다. [B2]
  - 구현 메모: Java에서는 Shift_JIS의 Windows 확장판인 `MS932`로 읽는 것이 안전하다. 최근 차트 중에는 UTF-8도 있으므로 판별이 필요하다.
- 현대 BMS 플레이어는 `#PLAYER`보다 확장자와 실제 사용된 채널로 플레이 방식을 판단한다. [B3]
  - .bme는 7키 차트에 관례적으로 쓰는 확장자다.
- 줄은 두 종류다.
  - 헤더 줄: `#명령 값` (예: `#TITLE 곡명`)
  - 채널 줄: `#MMMCC:데이터` (예: `#00111:01000200`) [B3]

### 1-2. 헤더

#### 메타데이터·설정

| 헤더 | 의미 | 출처 |
|---|---|---|
| `#PLAYER` | 1 = SP, 2 = Couple, 3 = DP (일부 플레이어는 4 = Battle) | [B1][B4] |
| `#TITLE` / `#SUBTITLE` | 곡명 / 부제 | [B3] |
| `#ARTIST` / `#SUBARTIST` | 아티스트 / 보조 아티스트 | [B3] |
| `#GENRE` | 장르 | [B1] |
| `#BPM` | 시작 BPM. 규격상 기본값은 130이지만 플레이어마다 120, 150 등으로 다르다 | [B1][B2] |
| `#PLAYLEVEL` | 표시용 난이도 레벨 (난이도표 레벨과는 별개) | [B3] |
| `#DIFFICULTY` | 1 BEGINNER, 2 NORMAL, 3 HYPER, 4 ANOTHER, 5 INSANE | [B2][B3] |
| `#RANK` | 판정 폭: 0 VERY HARD, 1 HARD, 2 NORMAL, 3 EASY (4 VERY EASY는 일부 확장). 기본값은 정해져 있지 않다 | [B2][B3] |
| `#TOTAL` | 노트를 칠 때 게이지 회복량 | [B3] |
| `#STAGEFILE` | 곡 선택·로딩 시 표시되는 이미지 (osu 배경 후보) | [B3] |
| `#BANNER` | 곡 선택 시 배너 이미지 | [B3] |
| `#BACKBMP` | 배경 이미지 (**의미 미검증**) | – |
| `#PREVIEW` | 미리듣기 음원. 최근 플레이어가 지원하는 확장 헤더 (**도입한 플레이어 미검증**) | [B6] |
| `#VOLWAV` | 확장 헤더. 전체 볼륨(%)으로 알려져 있음 (**의미 미검증**) | [B6] |
| `#LNTYPE` / `#LNOBJ` | 롱노트 방식 (1-4절) | [B2] |

#### 정의형 헤더 (`xx` = 01~ZZ, 2자리 36진수)

| 헤더 | 의미 | 출처 |
|---|---|---|
| `#WAVxx 파일명` | 키음 파일 정의. `#WAV00`은 지뢰 폭발음 | [B3] |
| `#BMPxx 파일명` | BGA 이미지·영상 (사용 안 함) | [B3] |
| `#BPMxx 값` | 확장 BPM. 255를 넘는 값이나 소수 BPM용 | [B2] |
| `#STOPxx 값` | 정지 길이. 1 = 4/4박자 한 마디의 1/192 | [B2] |

#### 제어문

- `#RANDOM n`은 1~n 중 하나를 무작위로 고른다. 이 값을 `#IF` / `#ENDIF`와 함께 써서 분기한다. [B4]
- 블록 구조를 이루는 명령은 `#RANDOM`(또는 `#SETRANDOM`), `#IF`, `#ELSEIF`, `#ELSE`, `#ENDIF`, `#ENDRANDOM`이다. [B2]
- 블록은 중첩할 수 있다. [B7]

### 1-3. 채널 줄

`#MMMCC:데이터`

- `MMM`: 마디 번호 (000~999)
- `CC`: 채널 번호 (2글자)
- 데이터: 2글자씩 끊은 개수만큼 마디를 똑같이 나눈다. `00`은 빈칸이다. [B3]

예: `#00111:01000200`
→ 마디 1을 4칸으로 나눈 것이다. 0번째 칸(마디 시작)에 `01`, 2번째 칸(마디 절반 지점)에 `02`가 있다.

같은 마디·같은 채널에 줄이 여러 개 있으면:
- 일반 채널은 두 줄을 합친다. [B2]
- BGM(01)은 여러 줄을 각각 별개의 BGM 줄로 해석해야 하며 합치면 안 된다. [B2]
- 같은 칸끼리 겹칠 때의 처리는 **(미검증)**.

#### 주요 채널

| 채널 | 의미 | 데이터 해석 | 출처 |
|---|---|---|---|
| 01 | BGM (자동 재생) | WAV ID | [B4] |
| 02 | 마디 길이 배율. **해당 마디에만 적용**된다 | 10진 실수 (예: `0.75`) | [B2][B4] |
| 03 | BPM 변경 | **16진수** | [B4] |
| 04, 06, 07, 0A | BGA | 사용 안 함 | [B4][B5] |
| 08 | 확장 BPM 변경 | `#BPMxx` 참조 | [B4] |
| 09 | STOP | `#STOPxx` 참조 | [B4] |
| 11–15, 18, 19 | 1P 7키 노트 | WAV ID | [B2][B8] |
| 16 | 1P 스크래치 | WAV ID | [B4] |
| 17 | 1P 프리존 | 무시 | [B4] |
| 31–36, 38–39 | 1P 숨김 노트. 표시되지 않고, 판정과 점수에도 포함되지 않는다 | 버림 | [B2] |
| 51–59 | 1P 롱노트 (`#LNTYPE 1`) | WAV ID | [B2] |
| D1–D9 | 1P 지뢰 | 버림 | [B2] |
| 21–29, 41–49, 61–69, E1–E9 | 2P 채널 | SP 7키에서는 없음 | [B2][B4] |

#### 1P 7키 배치 [B2][B8]

| | 스크래치 | 1 | 2 | 3 | 4 | 5 | 6 | 7 |
|---|---|---|---|---|---|---|---|---|
| 일반 | 16 | 11 | 12 | 13 | 14 | 15 | **18** | **19** |
| 롱노트 | 56 | 51 | 52 | 53 | 54 | 55 | **58** | **59** |

6번과 7번 키는 16, 17이 아니라 18, 19 채널이다. 이 프로젝트에서 스크래치(16, 56)와 프리존(17)은 버린다.

### 1-4. 롱노트 방식

- **`#LNTYPE 1` (RDM 방식)**
  - 5x 채널에서 `00`이 아닌 값이 나오면 롱노트 시작점이 되고, 그다음 `00`이 아닌 값이 끝점이 된다. [B2]
  - 생략하면 이 방식이 기본으로 적용된다. [B2]
  - **끝점의 키음은 항상 무시된다.** [B2]
- **`#LNTYPE 2` (MGQ 방식)**: MGQ 방식 롱노트를 쓸 때만 필요하다. [B2]
- **`#LNOBJ xx`**
  - 일반 채널에서 값이 `xx`인 오브젝트가 롱노트 끝점이 되고, 바로 앞 오브젝트가 시작점이 된다. [B2]
  - 규격상 끝점 키음은 BGM처럼 재생되지만, **대부분의 구현은 재생하지 않는다.** [B2]

→ 음원을 합칠 때 롱노트 끝점 키음은 넣지 않는다.

### 1-5. 시간 계산 규칙

- 1마디 = 4박 × 마디 길이 배율
- 1박 = 60000 / BPM (ms)
- 마디 안 위치 f(0~1)를 박으로 바꾸면 = 4 × 배율 × f
- 곡 처음부터 BPM 변경과 STOP 이벤트를 차례로 지나가며 `구간 박 수 × 60000 / 현재 BPM`을 더해 간다.
- **STOP**: `#STOPxx`의 1단위는 4/4박자 한 마디의 1/192, 즉 1/48박이다. [B2][B9]
  - 값이 v일 때 정지 시간 = v/48 × 60000/BPM = **v × 1250 / BPM** (ms)
- **같은 위치 처리**
  - BPM 변경과 STOP이 같은 위치에 있으면 BPM이 먼저 바뀌고, 그다음 정지한다. [B2]
  - STOP과 노트(키음)가 같은 위치에 있으면 소리가 먼저 나고, 그다음 정지한다. [B2]
- 계산 예: BPM 120, 마디 0 길이 1.0일 때, 마디 1의 절반 지점 = (4 + 2)박 × 500ms = **3000ms**

### 1-6. 키음

- 노트의 값은 `#WAV` ID이며, 연주 채널이나 BGM 채널에 그 ID가 나오면 해당 파일이 재생된다. [B3]
- 하나의 음원으로 합칠 때 넣는 소리: BGM + 보이는 노트 + 롱노트 시작음. 숨김 노트, 지뢰, 롱노트 끝점은 뺀다.
- **확장자 대체**: `#WAV`에 적힌 파일이 없으면, 확장자만 다른 같은 이름의 파일(예: .wav 대신 .ogg)을 찾는다. [B2]
- **같은 WAV ID 재생**
  - `#WAV` 인덱스 하나는 동시에 하나의 소리만 낸다. 같은 ID가 다시 재생되면 이전 소리는 끊긴다. [B2]
  - 소리를 겹쳐 재생하고 싶은 제작자는 같은 파일을 여러 ID에 정의해 쓴다. [B2]
- 파일명 대소문자가 실제 파일과 다른 경우 (**미검증**, 실제 차트로 확인 필요)

### 1-7. 차트 식별 (난이도표 기능 관련)

- BMS 난이도표 데이터는 차트를 md5 또는 sha256 해시로 식별한다. [B10]
- 해시를 BMS 파일 바이트 전체로 계산하는지는 **(미검증)**. 2단계에서 실제 표 데이터로 확인한다.

---

## 2. osu! 파일 구조

### 2-1. 기본 규칙

- 첫 줄은 파일 형식 버전이며, `osu file format v14`가 최신이다. [O1]
- `[섹션]` 아래에 `키: 값` 형태나 쉼표로 구분한 줄이 들어간다. [O1]
- 문자 인코딩은 공식 문서에 명시되어 있지 않다. UTF-8로 저장하는 것이 일반적 (**미검증**, 실제 파일로 확인 필요).
- 파일명 관례 `Artist - Title (Creator) [Version].osu` (**미검증**)
- **.osz**는 비트맵 아카이브이며 실체는 zip 파일이다. 확장자만 .zip에서 .osz로 바꾸면 되고, 열면 osu!가 Songs 폴더에 압축을 푼다. [O2]

### 2-2. 섹션별 내용

#### [General] [O1]

| 키 | 의미 | 우리 값 |
|---|---|---|
| AudioFilename | 음원 파일 경로 (현재 폴더 기준) | 합친 음원 |
| AudioLeadIn | 음원 시작 전 무음 시간 (ms) | 0 |
| PreviewTime | 미리듣기 시작 시간 (ms), 기본값 -1 | 계산값 또는 -1 |
| Countdown | 0 없음, 1 보통, 2 절반 속도, 3 두 배 속도 | 0 |
| SampleSet | 타이밍 포인트에서 지정하지 않을 때 쓰는 기본 샘플 세트 (Normal/Soft/Drum) | 미정 |
| Mode | 0 osu!, 1 taiko, 2 catch, **3 mania** | 3 |
| SpecialStyle | osu!mania의 "N+1" 키 배치 사용 여부 (0/1) | 0 |

#### [Editor]
편집기 설정이며 변환에는 필요 없다.

#### [Metadata] [O1]

| 키 | 의미 |
|---|---|
| Title / TitleUnicode | 로마자 표기 제목 / 원어 제목 |
| Artist / ArtistUnicode | 로마자 표기 아티스트 / 원어 아티스트 |
| Creator | 비트맵 제작자 |
| Version | 난이도 이름. 난이도표 레벨을 표기할 후보 위치 |
| Source | 곡의 원작 매체 |
| Tags | 공백으로 구분한 검색어 |
| BeatmapID / BeatmapSetID | 난이도 ID / 비트맵셋 ID |

#### [Difficulty]

| 키 | 의미 | 출처 |
|---|---|---|
| HPDrainRate | HP 설정 (0~10) | [O1] |
| **CircleSize** | **osu!mania에서는 키(열) 수 → 7** | [O1][O3][O4] |
| OverallDifficulty | OD (판정 엄격도, 0~10) | [O1] |
| ApproachRate | AR (mania에서는 사용하지 않음, **미검증**) | [O1] |
| SliderMultiplier / SliderTickRate | 슬라이더 관련 값 | [O1] |

#### [Events] [O1]

- 배경: `0,0,"파일명",xOffset,yOffset` (따옴표는 보통 쓰지만 생략 가능)
- 휴식 구간: `2,시작ms,끝ms` (`2` 대신 `Break`라고 써도 된다)

#### [TimingPoints] [O1]

`time,beatLength,meter,sampleSet,sampleIndex,volume,uninherited,effects`

- **빨간선** (uninherited = 1): BPM 변경. `beatLength` = 1박의 길이(ms) = `60000 / BPM`
- **초록선** (uninherited = 0): 배속 변경. `beatLength` = 음수로 쓴 배속의 역수(%).
  - 예: -50은 2배속이므로, 일반식은 `beatLength = -100 / 배속`이다.
- 시간순으로 정렬해야 한다.

#### [HitObjects] [O1]

`x,y,time,type,hitSound,objectParams,hitSample`

- 일반 노트: `x,192,time,1,0,0:0:0:0:`
- 롱노트: `x,y,time,type,hitSound,endTime:hitSample` → `x,192,time,128,0,endTime:0:0:0:0:`
- **type 비트**

  | 비트 | 값 | 의미 |
  |---|---|---|
  | 0 | 1 | 원 (mania 일반 노트) |
  | 1 | 2 | 슬라이더 |
  | 2 | 4 | 새 콤보 |
  | 3 | 8 | 스피너 |
  | 4–6 | – | 콤보 색 건너뛰기 |
  | 7 | 128 | **mania 롱노트** |

- **hitSound 비트**: 0(1) normal, 1(2) whistle, 2(4) finish, 3(8) clap. 아무 비트도 없으면 normal을 쓴다.
- **hitSample**: `normalSet:additionSet:index:volume:filename`
  - 생략하면 `0:0:0:0:`이다.
  - filename을 지정하면 비트맵 폴더의 그 파일을 커스텀 샘플로 재생한다 (이 프로젝트에서는 사용하지 않음).
- **x → 레인**: `레인 = floor(x × 키수 / 512)` (0 ~ 키수-1로 제한)
  - 쓸 때는 각 레인의 가운데 값 `x = floor((레인 + 0.5) × 512 / 키수)`를 쓴다.
  - 7K: **36, 109, 182, 256, 329, 402, 475**
- **y**: mania에서는 쓰지 않는다 (기본값 192).

### 2-3. osu!mania 스크롤 속도와 STOP

- osu!stable에는 두 가지 스크롤 방식이 있다. [O5][O6]
  - BPM 스케일: 현재 BPM에 따라 스크롤 속도가 달라지는 방식
  - 고정 속도: 옵션 `Scale osu!mania scroll speed with BPM`을 끄면 적용된다
- 고정 속도도 곡의 대표 BPM 하나를 기준으로 할 뿐이라, 곡 안의 SV·BPM 변경은 그대로 속도에 반영된다. [O6]
  - 즉 곡 안에서 BPM이 바뀌면 노트 속도도 바뀐다.
- 랭크 비트맵은 초록선으로 SV를 조절해 가장 많이 쓰인 BPM 구간에 맞춰 속도를 통일한다. [O7]
  - 이 프로젝트에서는 "속도 통일"을 옵션 후보로 둔다.
- osu!에는 STOP이 없다.
  - 시간만 뒤로 밀면 판정은 맞지만, 정지 구간에도 노트는 계속 내려온다.
  - 정지 구간에 아주 낮은 배속의 초록선을 넣어 흉내 내는 방법을 검토한다 (**아이디어, 미검증**).

### 2-4. 참고: osu! 자체 BMS 가져오기 기능

osu!(stable) 편집기의 File 메뉴 → Import from... 에 .bms/.bme 파일을 osu!mania 난이도로 가져오는 기능이 있다. [O8]
→ 이 프로젝트와 비교·차별점을 정리할 때 참고한다.

---

## 3. BMS → osu! 대응표

| BMS | osu! | 비고 |
|---|---|---|
| `#TITLE` (+`#SUBTITLE`) | Title / TitleUnicode | Title은 로마자 표기가 원칙이라 일본어 제목 처리 방법을 정해야 함 |
| `#ARTIST` | Artist / ArtistUnicode | 위와 같음 |
| `#SUBARTIST` | Creator 또는 Tags | 미정 |
| `#GENRE` | Tags | |
| `#PLAYLEVEL`, `#DIFFICULTY`, 난이도표 레벨 | Version | 예: `[ANOTHER] ★12 / sl3` |
| `#RANK` | OverallDifficulty | 숫자 대응 규칙 미정 |
| `#TOTAL` | HPDrainRate | 숫자 대응 규칙 미정 |
| `#STAGEFILE` / `#BACKBMP` | [Events] 배경 | |
| BGM + 키음 | 합친 음원 → AudioFilename | |
| BPM / STOP / 마디 길이 | TimingPoints + 노트 시간 | |
| 11–15, 18, 19 | 레인 0~6 (x = 36, 109, 182, 256, 329, 402, 475) | |
| 51–55, 58, 59 / `#LNOBJ` | type 128 롱노트 | |
| 16, 17, 56, 31–39, D1–D9, BGA | 버림 | |

---

## 출처

### BMS
- **[B1]** Urao Yane, *BMS Format Specification* (원본 규격, bms-rs 저장소 사본): https://rust-digger.code-maven.com/src/bms-rs-0.4.4/SPECIFICATION.md
- **[B2]** hitkey, *BMS command memo*: https://hitkey.nekokan.dyndns.info/cmds.htm
- **[B3]** Wikipedia, *Be-Music Source*: https://en.wikipedia.org/wiki/Be-Music_Source
- **[B4]** Wikipedia(일본어) *BMS (音楽ゲーム)* (weblio 사본): https://www.weblio.jp/content/BM98
- **[B5]** bms-rs 소스 `channel.rs`: https://docs.rs/bms-rs/0.10.1/src/bms_rs/bms/command/channel.rs.html
- **[B6]** be-music (BMS 확장 헤더 목록): https://identichosting.duckdns.org/nulltask/be-music
- **[B7]** Bemuse, *BMS support*: https://bemuse.ninja/project/docs/bms-support
- **[B8]** *BMS Conversions* (BME 채널 ↔ 키 대응): https://thenathannator.github.io/GuitarGame_ChartFormats/Chart-File-Formats/chart-format/Miscellaneous/BMS-Conversions
- **[B9]** bms-rs 소스 `process/bms.rs` (STOP 단위 변환): https://docs.rs/crate/bms-rs/1.0.0/source/src/chart/process/bms.rs
- **[B10]** bms-table (난이도표 파서): https://docs.rs/crate/bms-table/0.10.3

### osu!
- **[O1]** osu! wiki, *.osu (file format)*: https://osu.ppy.sh/wiki/osu!_File_Formats/Osu_(file_format)
- **[O2]** osu! wiki, *File formats*: https://osu.ppy.sh/wiki/en/Client/File_formats
- **[O3]** pcircle (Go 라이브러리) 문서: https://pkg.go.dev/github.com/polkisss/pcircle
- **[O4]** osu!(lazer) 소스 `CheckKeyCount.cs`: https://cocalc.com/github/ppy/osu/blob/master/osu.Game.Rulesets.Mania/Edit/Checks/CheckKeyCount.cs
- **[O5]** osu! wiki (구판), *osu!mania*: https://old.ppy.sh/wiki/Osu!mania
- **[O6]** osu! 포럼, 스크롤 속도 변경 공지: https://osu.ppy.sh/forum/p/3465881
- **[O7]** osu! wiki, *Ranking criteria/osu!mania*: https://osu.ppy.sh/wiki/sr/Ranking_criteria/osu!mania
- **[O8]** osu! wiki, *Beatmap Editor/Menu*: https://osu.ppy.sh/wiki/Beatmap_Editor/Menu
