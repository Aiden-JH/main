"""Markdown report rendering."""

from __future__ import annotations

from datetime import date
from pathlib import Path
from typing import Any

import pandas as pd

from .io import read_portfolio, read_yaml
from .risk import evaluate_portfolio


def generate_daily_report(portfolio_path: Path, rules_path: Path, events_path: Path, output_path: Path) -> Path:
    """Generate a Korean markdown daily report and return its path."""
    portfolio = read_portfolio(portfolio_path)
    rules = read_yaml(rules_path)
    events = read_yaml(events_path)
    evaluated, warnings, account_summary = evaluate_portfolio(portfolio, rules, events)
    markdown = render_report(evaluated, warnings, account_summary, rules, events)
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(markdown, encoding="utf-8")
    return output_path


def render_report(
    evaluated: pd.DataFrame,
    warnings: list[str],
    account_summary: pd.DataFrame,
    rules: dict[str, Any],
    events: dict[str, Any],
) -> str:
    """Render evaluated portfolio data as Korean markdown."""
    lines: list[str] = [
        f"# 일일 포트폴리오 브리핑 ({date.today().isoformat()})",
        "",
        "> 이 리포트는 점검용 브리핑이며 자동매매를 실행하지 않습니다.",
        "",
        "## 핵심 경고",
    ]
    if warnings:
        lines.extend([f"- ⚠️ {warning}" for warning in warnings])
    else:
        lines.append("- 현재 규칙 기준의 주요 경고가 없습니다.")

    lines.extend(["", "## 계좌별 리스크 요약", "", "| 계좌 | 평가금액 | 현금비중 | 목표현금 | 해외비중 | 반도체비중 |", "|---|---:|---:|---:|---:|---:|"])
    for _, row in account_summary.iterrows():
        lines.append(
            f"| {row['account']} | {_money(row['market_value'])} | {row['cash_pct']:.1f}% | "
            f"{row['cash_target_pct']:.1f}% | {row['overseas_pct']:.1f}% | {row['semiconductor_pct']:.1f}% |"
        )

    lines.extend(["", "## 포지션별 액션", "", "| 계좌 | 종목 | 분류 | 시장 | 비중 | 손익률 | 액션 |", "|---|---|---|---|---:|---:|---|"])
    for _, row in evaluated.sort_values(["account", "market_value"], ascending=[True, False]).iterrows():
        lines.append(
            f"| {row['account']} | {row['name']} ({row['ticker']}) | {row['category']} | {row['market']} | "
            f"{row['portfolio_weight_pct']:.1f}% | {row['pnl_pct']:.1f}% | **{row['action']}** |"
        )

    lines.extend([
        "",
        "## 오늘 확인할 이벤트",
    ])
    for event in events.get("calendar", []):
        lines.append(f"- {event.get('date', '날짜 미정')}: {event.get('title', '제목 없음')} — {event.get('note', '')}")

    lines.extend([
        "",
        "## 운용 원칙",
        "- 모든 판단 규칙은 `config/rules.yaml`에서 수정합니다.",
        "- 자동 주문, 자동 리밸런싱, 브로커 API 연동은 포함하지 않습니다.",
        "- BUY_CANDIDATE는 검토 후보일 뿐이며 매수 지시가 아닙니다.",
    ])
    return "\n".join(lines) + "\n"


def _money(value: float) -> str:
    return f"{value:,.0f}"
