# 일일 포트폴리오 브리핑 (2026-06-22)

> 이 리포트는 점검용 브리핑이며 자동매매를 실행하지 않습니다.

## 핵심 경고
- ⚠️ 반도체 노출이 90.8%로 한도 35.0%를 초과했습니다.
- ⚠️ RIA 현금 비중이 8.3%로 목표 12.0%보다 낮습니다.
- ⚠️ RIA 외부 해외 순매수 리스크: 최근 해외 성장주 순매수가 RIA 밖 taxable 계좌에 집중되어 세금과 환율 변동 리스크를 점검해야 합니다.
- ⚠️ SpaceX 가격 구간: 현재 240, 판단 구간은 'watch'입니다.
- ⚠️ 약한 소형 포지션 점검: Alteogen 비중 4.9%는 추가 매수보다 thesis 재확인이 우선입니다.

## 계좌별 리스크 요약

| 계좌 | 평가금액 | 현금비중 | 목표현금 | 해외비중 | 반도체비중 |
|---|---:|---:|---:|---:|---:|
| basic | 11,720,000 | 4.5% | 5.0% | 0.0% | 90.8% |
| overseas | 2,310 | 0.0% | 3.0% | 100.0% | 0.0% |
| ria | 3,850 | 6.5% | 12.0% | 100.0% | 36.4% |

## 포지션별 액션

| 계좌 | 종목 | 분류 | 시장 | 비중 | 손익률 | 액션 |
|---|---|---|---|---:|---:|---|
| basic | Samsung Electronics (005930) | semiconductor | KR | 53.2% | 20.0% | **DO_NOT_ADD** |
| basic | SK Hynix (000660) | semiconductor | KR | 37.5% | 46.7% | **DO_NOT_ADD** |
| basic | Alteogen (196170) | biotech | KR | 4.9% | -12.1% | **WATCH** |
| basic | Basic Cash (CASHKRW) | cash | KR | 4.3% | 0.0% | **HOLD** |
| overseas | SpaceX (SPACEX) | private | US | 0.0% | 33.3% | **WATCH** |
| overseas | Tesla (TSLA) | auto | US | 0.0% | -11.9% | **BUY_CANDIDATE** |
| ria | Nvidia (NVDA) | semiconductor | US | 0.0% | 64.7% | **DO_NOT_ADD** |
| ria | S&P 500 ETF (SPY) | index | US | 0.0% | 14.6% | **BUY_CANDIDATE** |
| ria | RIA Cash (CASHUSD) | cash | US | 0.0% | 0.0% | **HOLD** |

## 오늘 확인할 이벤트
- 2026-06-22: 미국 기술주 수급 점검 — 반도체와 AI 인프라 종목의 집중도를 확인합니다.
- 2026-06-25: SpaceX 비상장 가격 업데이트 확인 — 사설 거래 가격이 rules.yaml 가격 구간을 벗어나는지 확인합니다.

## 운용 원칙
- 모든 판단 규칙은 `config/rules.yaml`에서 수정합니다.
- 자동 주문, 자동 리밸런싱, 브로커 API 연동은 포함하지 않습니다.
- BUY_CANDIDATE는 검토 후보일 뿐이며 매수 지시가 아닙니다.
