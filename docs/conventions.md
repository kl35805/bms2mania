# 개발 규칙

bms2mania의 패키지, 브랜치, 커밋, PR 규칙입니다.

---

## 1. 패키지

- 기준 패키지: `io.github.kl35805.bms2mania`
- `io.github.kl35805`는 GitHub 계정이 보장하는 네임스페이스라 다른 프로젝트와 이름이 겹치지 않는다. 나중에 Maven Central 배포 시에도 그대로 쓸 수 있다.
- 하위 패키지는 [architecture.md](./architecture.md)의 패키지 구성을 따른다.
  - `bms`, `chart`, `osu`, `audio`, `table`, `library`, `convert`, `config`, `common`

---

## 2. 브랜치 전략

```
main ─────────────────────────●────────── 마일스톤 완료 시 병합 + 태그 (v0.1.0)
                              ↑
develop ──●──────●──────●─────●──────────  개발 통합 브랜치 (GitHub 기본 브랜치)
           ↖    ↗ ↖    ↗
    feat/3-gradle-setup  feat/5-bms-parser   이슈 단위 브랜치
```

| 브랜치 | 역할 |
|---|---|
| `main` | 마일스톤이 완료된 안정 버전. develop에서만 병합한다 |
| `develop` | 개발 통합 브랜치. **GitHub 기본 브랜치로 설정**한다 |
| 이슈 브랜치 | 이슈 1개 = 브랜치 1개. develop에서 분기해 develop으로 병합한다 |

### 이슈 브랜치 이름

`타입/이슈번호-짧은-설명` (영문 소문자, 하이픈 구분)

- `feat/5-bms-parser`
- `fix/12-sjis-decode`
- `docs/2-readme`

이슈 번호 앞에 `#`은 붙이지 않는다. 터미널에서 주석 기호로 해석된다.

### 작업 흐름

1. GitHub에 이슈를 만든다. 예: `#5 BMS 파서 구현`
2. develop에서 브랜치를 만든다.
   ```bash
   git switch develop
   git pull
   git switch -c feat/5-bms-parser
   ```
3. 작업하며 커밋하고 push한다.
4. develop을 대상으로 PR을 연다. PR 본문에 `Closes #5`를 적는다.
5. PR을 **Create a merge commit** 방식으로 병합하고, 이슈 브랜치를 삭제한다.
6. 마일스톤이 끝나면 develop → main PR을 병합하고 태그를 붙인다.
   ```bash
   git switch main
   git pull
   git tag v0.1.0
   git push origin v0.1.0
   ```

### 병합 방식

- 모든 병합은 **Create a merge commit**으로 한다.
- Squash 병합은 쓰지 않는다. 개발 과정을 커밋 단위로 남기는 것이 목적이기 때문이다.

---

## 3. 커밋 메시지

### 형식

```
타입(범위): 설명

본문 (선택)

Refs #이슈번호 (선택)
```

- 설명은 한국어로, 마침표 없이 쓴다.
- 범위는 모듈·패키지 이름을 쓴다. 전체에 해당하면 생략한다.

### 타입

| 타입 | 용도 |
|---|---|
| `feat` | 새 기능 |
| `fix` | 버그 수정 |
| `refactor` | 동작 변화 없는 구조 개선 |
| `test` | 테스트 추가·수정 |
| `docs` | 문서 |
| `build` | Gradle 설정, 의존성 변경 |
| `chore` | 그 밖의 잡무 (.gitignore 등) |

### 범위

`bms`, `chart`, `osu`, `audio`, `table`, `library`, `convert`, `config`, `app`

### 예시

```
feat(bms): 채널 데이터 파싱 구현
test(chart): STOP 시간 계산 테스트 추가
fix(bms): Shift_JIS 판별 실패 시 CP949로 재시도
build: JavaFX 플러그인 추가
docs: README에 빌드 방법 추가
chore: .gitignore에 samples 폴더 추가
```

---

## 4. 이슈와 PR

- **이슈 양식**: `.github/ISSUE_TEMPLATE/`의 양식을 사용한다.

  | 양식 | 제목 접두어 | 라벨 | 브랜치 타입 |
  |---|---|---|---|
  | 기능 추가 | `[Feat]` | enhancement | `feat/` |
  | 버그 수정 | `[Fix]` | bug | `fix/` |
  | 기타 작업 | `[Refactor]` `[Test]` `[Docs]` `[Build]` `[Chore]` | 필요 시 | `refactor/` `test/` `docs/` `build/` `chore/` |

- **이슈 제목**: `[접두어] 무엇을 할지 한 줄`로 쓴다. 예: `[Feat] BMS 파서 구현`
- **PR 제목**: 커밋 메시지 형식을 따른다. 예: `feat(bms): BMS 파서 구현`
- **PR 본문**
  - 무엇을 바꿨는지
  - 어떻게 확인했는지 (테스트, 실행 결과)
  - `Closes #이슈번호`

---

## 5. 테스트 데이터

- 레포에는 **직접 만든 작은 BMS와 짧은 비프음 키음**만 올린다.
- 실제 곡 BMS·키음은 저작물이므로 `samples/` 폴더(`.gitignore` 등록)에서만 사용한다.
