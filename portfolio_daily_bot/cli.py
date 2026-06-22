"""CLI for generating a daily Korean portfolio briefing."""

from __future__ import annotations

import argparse
from pathlib import Path

from .report import generate_daily_report


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description="Generate a Korean daily portfolio briefing from CSV and YAML rules."
    )
    parser.add_argument("--portfolio", default="data/portfolio.csv", help="Portfolio CSV path")
    parser.add_argument("--rules", default="config/rules.yaml", help="Editable rules YAML path")
    parser.add_argument("--events", default="config/events.yaml", help="Events YAML path")
    parser.add_argument("--output", default="reports/daily_report.md", help="Markdown output path")
    return parser


def main() -> None:
    args = build_parser().parse_args()
    output_path = generate_daily_report(
        portfolio_path=Path(args.portfolio),
        rules_path=Path(args.rules),
        events_path=Path(args.events),
        output_path=Path(args.output),
    )
    print(f"Daily report generated: {output_path}")
