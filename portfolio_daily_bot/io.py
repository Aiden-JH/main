"""Input helpers for CSV and YAML configuration files."""

from __future__ import annotations

from pathlib import Path
from typing import Any

import pandas as pd
import yaml

REQUIRED_PORTFOLIO_COLUMNS = {
    "account",
    "ticker",
    "name",
    "category",
    "market",
    "quantity",
    "price",
    "cost_basis",
    "currency",
    "liquidity",
}


def read_portfolio(path: Path) -> pd.DataFrame:
    """Read and validate the portfolio CSV."""
    if not path.exists():
        raise FileNotFoundError(f"Portfolio CSV not found: {path}")

    portfolio = pd.read_csv(path)
    missing = REQUIRED_PORTFOLIO_COLUMNS - set(portfolio.columns)
    if missing:
        raise ValueError(f"Portfolio CSV is missing required columns: {sorted(missing)}")

    numeric_columns = ["quantity", "price", "cost_basis"]
    for column in numeric_columns:
        portfolio[column] = pd.to_numeric(portfolio[column], errors="coerce").fillna(0)

    portfolio["market_value"] = portfolio["quantity"] * portfolio["price"]
    portfolio["cost_value"] = portfolio["quantity"] * portfolio["cost_basis"]
    portfolio["pnl_pct"] = portfolio.apply(_pnl_pct, axis=1)
    return portfolio


def read_yaml(path: Path) -> dict[str, Any]:
    """Read a YAML file and return an empty dict for blank files."""
    if not path.exists():
        raise FileNotFoundError(f"YAML file not found: {path}")
    with path.open("r", encoding="utf-8") as file:
        return yaml.safe_load(file) or {}


def _pnl_pct(row: pd.Series) -> float:
    if row["cost_basis"] <= 0:
        return 0.0
    return (row["price"] - row["cost_basis"]) / row["cost_basis"] * 100
