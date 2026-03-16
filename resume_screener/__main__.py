"""Entry point for the Resume Screening application."""

import sys

from PyQt6.QtWidgets import QApplication

from .ui import MainWindow


def main():
    app = QApplication(sys.argv)
    app.setApplicationName("이력서 스크리닝 도구")
    window = MainWindow()
    window.show()
    sys.exit(app.exec())


if __name__ == "__main__":
    main()
