# portfolio-daily-bot

`portfolio-daily-bot`은 한국 개인 투자자를 위한 일일 포트폴리오 브리핑 도구입니다. `data/portfolio.csv`, `config/rules.yaml`, `config/events.yaml`을 읽어 계좌별 리스크와 포지션별 액션을 평가한 뒤 한국어 마크다운 리포트를 `reports/daily_report.md`에 생성합니다.

## 기능

- 계좌 구분: `basic`, `ria`, `overseas`
- 포지션 액션 분류: `HOLD`, `DO_NOT_ADD`, `TRIM_CANDIDATE`, `BUY_CANDIDATE`, `WATCH`
- YAML로 편집 가능한 리스크 규칙
- 반도체 집중도, RIA 현금 부족, RIA 외부 해외 순매수, SpaceX 가격 구간, Alteogen 같은 약한 소형 포지션 경고
- 자동매매 미구현: 주문 실행이나 브로커 API 연동을 하지 않습니다.

## 설치

```bash
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

## 사용법

```bash
python -m portfolio_daily_bot
```

기본 입력과 출력 경로는 다음과 같습니다.

- 포트폴리오: `data/portfolio.csv`
- 규칙: `config/rules.yaml`
- 이벤트: `config/events.yaml`
- 리포트: `reports/daily_report.md`

다른 파일을 사용하려면 옵션을 지정합니다.

```bash
python -m portfolio_daily_bot \
  --portfolio data/portfolio.csv \
  --rules config/rules.yaml \
  --events config/events.yaml \
  --output reports/daily_report.md
```

## 포트폴리오 CSV 형식

필수 컬럼은 다음과 같습니다.

- `account`: `basic`, `ria`, `overseas` 등 계좌명
- `ticker`: 종목 코드
- `name`: 종목명
- `category`: `semiconductor`, `cash`, `biotech`, `private` 등 분류
- `market`: `KR`, `US`, `overseas` 등 시장
- `quantity`: 수량
- `price`: 현재가
- `cost_basis`: 평균단가
- `currency`: 통화
- `liquidity`: 유동성 메모

## 규칙 수정

`config/rules.yaml`에서 현금 목표, 반도체 최대 비중, 액션 기준, SpaceX 가격 구간, 약한 소형 포지션 목록을 수정할 수 있습니다.
