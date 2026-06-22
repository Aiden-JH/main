"""Portfolio risk and action evaluation logic."""

from __future__ import annotations

from typing import Any

import pandas as pd

VALID_ACTIONS = {"HOLD", "DO_NOT_ADD", "TRIM_CANDIDATE", "BUY_CANDIDATE", "WATCH"}


def evaluate_portfolio(portfolio: pd.DataFrame, rules: dict[str, Any], events: dict[str, Any]) -> tuple[pd.DataFrame, list[str], pd.DataFrame]:
    """Return position-level actions, warnings, and account summary."""
    evaluated = portfolio.copy()
    total_value = float(evaluated["market_value"].sum())
    evaluated["portfolio_weight_pct"] = evaluated["market_value"] / total_value * 100 if total_value else 0

    account_summary = _account_summary(evaluated, rules)
    warnings = _build_warnings(evaluated, account_summary, rules, events)
    evaluated["action"] = evaluated.apply(lambda row: _classify_position(row, evaluated, rules, warnings), axis=1)
    return evaluated, warnings, account_summary


def _account_summary(portfolio: pd.DataFrame, rules: dict[str, Any]) -> pd.DataFrame:
    summaries: list[dict[str, Any]] = []
    account_rules = rules.get("accounts", {})
    for account, group in portfolio.groupby("account", sort=True):
        account_value = float(group["market_value"].sum())
        cash_value = float(group.loc[group["category"].str.lower() == "cash", "market_value"].sum())
        overseas_value = float(group.loc[group["market"].str.lower().isin({"us", "overseas"}), "market_value"].sum())
        semiconductor_value = float(group.loc[group["category"].str.lower() == "semiconductor", "market_value"].sum())
        target_cash_pct = float(account_rules.get(account, {}).get("cash_target_pct", 0))
        summaries.append(
            {
                "account": account,
                "market_value": account_value,
                "cash_pct": _pct(cash_value, account_value),
                "cash_target_pct": target_cash_pct,
                "overseas_pct": _pct(overseas_value, account_value),
                "semiconductor_pct": _pct(semiconductor_value, account_value),
            }
        )
    return pd.DataFrame(summaries)


def _build_warnings(portfolio: pd.DataFrame, account_summary: pd.DataFrame, rules: dict[str, Any], events: dict[str, Any]) -> list[str]:
    warnings: list[str] = []
    exposure_rules = rules.get("exposure_limits", {})
    semiconductor_limit = float(exposure_rules.get("semiconductor_max_pct", 0))
    semiconductor_pct = _pct(
        float(portfolio.loc[portfolio["category"].str.lower() == "semiconductor", "market_value"].sum()),
        float(portfolio["market_value"].sum()),
    )
    if semiconductor_limit and semiconductor_pct > semiconductor_limit:
        warnings.append(f"반도체 노출이 {semiconductor_pct:.1f}%로 한도 {semiconductor_limit:.1f}%를 초과했습니다.")

    ria = account_summary.loc[account_summary["account"] == "ria"]
    if not ria.empty and float(ria.iloc[0]["cash_pct"]) < float(ria.iloc[0]["cash_target_pct"]):
        warnings.append(
            f"RIA 현금 비중이 {float(ria.iloc[0]['cash_pct']):.1f}%로 목표 {float(ria.iloc[0]['cash_target_pct']):.1f}%보다 낮습니다."
        )

    overseas_net_buying = events.get("overseas_net_buying", {})
    if overseas_net_buying.get("ria_outside_risk", False):
        detail = overseas_net_buying.get("note", "해외 순매수 쏠림이 RIA 계좌 외부 리스크를 키울 수 있습니다.")
        warnings.append(f"RIA 외부 해외 순매수 리스크: {detail}")

    spacex_rules = rules.get("spacex_price_zones", {})
    spacex = portfolio.loc[portfolio["ticker"].str.upper() == "SPACEX"]
    if not spacex.empty and spacex_rules:
        price = float(spacex.iloc[0]["price"])
        zone = _spacex_zone(price, spacex_rules)
        warnings.append(f"SpaceX 가격 구간: 현재 {price:,.0f}, 판단 구간은 '{zone}'입니다.")

    weak_rules = rules.get("weak_small_positions", {})
    max_weight = float(weak_rules.get("max_weight_pct", 0))
    weak_names = {str(name).lower() for name in weak_rules.get("names", [])}
    total_value = float(portfolio["market_value"].sum())
    for _, row in portfolio.iterrows():
        weight = _pct(float(row["market_value"]), total_value)
        if str(row["name"]).lower() in weak_names and weight <= max_weight:
            warnings.append(f"약한 소형 포지션 점검: {row['name']} 비중 {weight:.1f}%는 추가 매수보다 thesis 재확인이 우선입니다.")
    return warnings


def _classify_position(row: pd.Series, portfolio: pd.DataFrame, rules: dict[str, Any], warnings: list[str]) -> str:
    category = str(row["category"]).lower()
    name = str(row["name"]).lower()
    ticker = str(row["ticker"]).upper()
    weight = float(row.get("portfolio_weight_pct", 0))
    pnl_pct = float(row.get("pnl_pct", 0))
    action_rules = rules.get("actions", {})

    if category == "cash":
        return "HOLD"
    if category == "semiconductor" and any("반도체 노출" in warning for warning in warnings):
        return "DO_NOT_ADD"
    if ticker == "SPACEX":
        return _spacex_action(float(row["price"]), rules.get("spacex_price_zones", {}))
    if name in {str(item).lower() for item in rules.get("weak_small_positions", {}).get("names", [])}:
        return "WATCH"
    if weight > float(action_rules.get("trim_weight_pct", 25)) or pnl_pct > float(action_rules.get("trim_gain_pct", 80)):
        return "TRIM_CANDIDATE"
    if pnl_pct < -float(action_rules.get("watch_loss_pct", 15)):
        return "WATCH"
    if weight < float(action_rules.get("buy_candidate_max_weight_pct", 5)) and pnl_pct >= -5:
        return "BUY_CANDIDATE"
    return "HOLD"


def _spacex_zone(price: float, zones: dict[str, Any]) -> str:
    if not zones:
        return "rule_missing"
    if price <= float(zones.get("accumulate_below", -1)):
        return "accumulate_below"
    if price <= float(zones.get("hold_below", price)):
        return "hold_below"
    if price >= float(zones.get("trim_above", float("inf"))):
        return "trim_above"
    return "watch"


def _spacex_action(price: float, zones: dict[str, Any]) -> str:
    zone = _spacex_zone(price, zones)
    return {
        "accumulate_below": "BUY_CANDIDATE",
        "hold_below": "HOLD",
        "trim_above": "TRIM_CANDIDATE",
        "watch": "WATCH",
    }.get(zone, "WATCH")


def _pct(numerator: float, denominator: float) -> float:
    if denominator == 0:
        return 0.0
    return numerator / denominator * 100
