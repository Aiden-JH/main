"""PyQt6-based UI for the Resume Screening application."""

import os
import traceback

from PyQt6.QtCore import Qt, QThread, pyqtSignal, QMimeData
from PyQt6.QtGui import QFont, QDragEnterEvent, QDropEvent
from PyQt6.QtWidgets import (
    QApplication,
    QFileDialog,
    QHBoxLayout,
    QLabel,
    QLineEdit,
    QMainWindow,
    QMessageBox,
    QPlainTextEdit,
    QProgressBar,
    QPushButton,
    QSplitter,
    QTabWidget,
    QTextEdit,
    QVBoxLayout,
    QWidget,
)

from .parser import extract_text_from_file, extract_text_from_url
from .screener import screen_resume


# ---------------------------------------------------------------------------
# Worker thread for API calls
# ---------------------------------------------------------------------------

class ScreeningWorker(QThread):
    """Runs the Claude API screening call in a background thread."""

    finished = pyqtSignal(str)
    error = pyqtSignal(str)

    def __init__(self, api_key: str, jd: str, resume: str, notes: str):
        super().__init__()
        self.api_key = api_key
        self.jd = jd
        self.resume = resume
        self.notes = notes

    def run(self):
        try:
            result = screen_resume(self.api_key, self.jd, self.resume, self.notes)
            self.finished.emit(result)
        except Exception as exc:
            self.error.emit(f"API 호출 중 오류가 발생했습니다:\n{exc}\n\n{traceback.format_exc()}")


# ---------------------------------------------------------------------------
# Drag-and-drop file area
# ---------------------------------------------------------------------------

class FileDropArea(QWidget):
    """A widget that accepts file drops and shows the selected file path."""

    file_selected = pyqtSignal(str)

    def __init__(self):
        super().__init__()
        self.setAcceptDrops(True)
        self.setMinimumHeight(80)
        self.setStyleSheet(
            "QWidget { border: 2px dashed #aaa; border-radius: 8px; "
            "background-color: #fafafa; }"
        )

        layout = QVBoxLayout(self)
        self.label = QLabel("PDF 또는 DOCX 파일을 여기에 드래그하거나\n아래 버튼으로 파일을 선택하세요")
        self.label.setAlignment(Qt.AlignmentFlag.AlignCenter)
        self.label.setStyleSheet("border: none; color: #888; font-size: 13px;")
        layout.addWidget(self.label)

        self.btn = QPushButton("파일 선택")
        self.btn.setFixedWidth(120)
        self.btn.setStyleSheet("border: 1px solid #ccc; border-radius: 4px; padding: 4px;")
        self.btn.clicked.connect(self._open_file_dialog)
        layout.addWidget(self.btn, alignment=Qt.AlignmentFlag.AlignCenter)

        self.file_path: str | None = None

    def _open_file_dialog(self):
        path, _ = QFileDialog.getOpenFileName(
            self, "이력서 파일 선택", "", "Documents (*.pdf *.docx)"
        )
        if path:
            self._set_file(path)

    def _set_file(self, path: str):
        self.file_path = path
        name = os.path.basename(path)
        self.label.setText(f"선택된 파일: {name}")
        self.label.setStyleSheet("border: none; color: #333; font-size: 13px;")
        self.file_selected.emit(path)

    # -- Drag & Drop --
    def dragEnterEvent(self, event: QDragEnterEvent):
        if event.mimeData().hasUrls():
            event.acceptProposedAction()

    def dropEvent(self, event: QDropEvent):
        urls = event.mimeData().urls()
        if urls:
            path = urls[0].toLocalFile()
            if path.lower().endswith((".pdf", ".docx")):
                self._set_file(path)
            else:
                QMessageBox.warning(self, "지원하지 않는 형식", "PDF 또는 DOCX 파일만 지원합니다.")

    def clear(self):
        self.file_path = None
        self.label.setText("PDF 또는 DOCX 파일을 여기에 드래그하거나\n아래 버튼으로 파일을 선택하세요")
        self.label.setStyleSheet("border: none; color: #888; font-size: 13px;")


# ---------------------------------------------------------------------------
# Main Window
# ---------------------------------------------------------------------------

class MainWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("이력서 스크리닝 도구")
        self.setMinimumSize(1100, 750)
        self.worker: ScreeningWorker | None = None

        self._build_ui()

    # ---- UI Construction ----

    def _build_ui(self):
        central = QWidget()
        self.setCentralWidget(central)
        root_layout = QVBoxLayout(central)
        root_layout.setContentsMargins(12, 12, 12, 12)

        # API key row
        api_row = QHBoxLayout()
        api_label = QLabel("Anthropic API Key:")
        api_label.setFixedWidth(140)
        self.api_key_input = QLineEdit()
        self.api_key_input.setEchoMode(QLineEdit.EchoMode.Password)
        self.api_key_input.setPlaceholderText("sk-ant-... (환경변수 ANTHROPIC_API_KEY 로도 설정 가능)")
        # Pre-fill from env
        env_key = os.environ.get("ANTHROPIC_API_KEY", "")
        if env_key:
            self.api_key_input.setText(env_key)
        api_row.addWidget(api_label)
        api_row.addWidget(self.api_key_input)
        root_layout.addLayout(api_row)

        # Splitter: left / right panels
        splitter = QSplitter(Qt.Orientation.Horizontal)
        root_layout.addWidget(splitter, stretch=1)

        # ---- Left Panel ----
        left_widget = QWidget()
        left_layout = QVBoxLayout(left_widget)
        left_layout.setContentsMargins(0, 0, 8, 0)

        # 1. JD Input
        left_layout.addWidget(self._section_label("1. 채용공고 (JD)"))

        # JD URL row
        jd_url_row = QHBoxLayout()
        self.jd_url_input = QLineEdit()
        self.jd_url_input.setPlaceholderText("JD 링크 URL (선택사항) — 입력 후 '가져오기' 클릭")
        jd_fetch_btn = QPushButton("가져오기")
        jd_fetch_btn.setFixedWidth(80)
        jd_fetch_btn.clicked.connect(self._fetch_jd_from_url)
        jd_url_row.addWidget(self.jd_url_input)
        jd_url_row.addWidget(jd_fetch_btn)
        left_layout.addLayout(jd_url_row)

        self.jd_text_input = QPlainTextEdit()
        self.jd_text_input.setPlaceholderText("채용공고(JD) 내용을 여기에 붙여넣으세요...")
        self.jd_text_input.setMinimumHeight(120)
        left_layout.addWidget(self.jd_text_input)

        # 2. Notes
        left_layout.addWidget(self._section_label("2. 검토 참고사항"))
        self.notes_input = QPlainTextEdit()
        self.notes_input.setPlaceholderText(
            "예: 이 포지션은 뷰티 업종 경험 필수 / 포트폴리오 퀄리티 중점 확인 / "
            "5년 이상 경력자 우대 등"
        )
        self.notes_input.setMaximumHeight(80)
        left_layout.addWidget(self.notes_input)

        # 3. Resume input (tabs: file / text)
        left_layout.addWidget(self._section_label("3. 지원자 이력서"))

        self.resume_tabs = QTabWidget()
        # Tab 1: file upload
        file_tab = QWidget()
        file_tab_layout = QVBoxLayout(file_tab)
        self.file_drop = FileDropArea()
        file_tab_layout.addWidget(self.file_drop)
        self.resume_tabs.addTab(file_tab, "파일 첨부")

        # Tab 2: text paste
        text_tab = QWidget()
        text_tab_layout = QVBoxLayout(text_tab)
        self.resume_text_input = QPlainTextEdit()
        self.resume_text_input.setPlaceholderText("이력서 내용을 여기에 직접 붙여넣으세요...")
        text_tab_layout.addWidget(self.resume_text_input)
        self.resume_tabs.addTab(text_tab, "텍스트 붙여넣기")

        left_layout.addWidget(self.resume_tabs)

        # Reference link
        ref_row = QHBoxLayout()
        ref_label = QLabel("참고 링크:")
        ref_label.setFixedWidth(70)
        self.ref_link_input = QLineEdit()
        self.ref_link_input.setPlaceholderText("나인하이어 등 ATS 지원자 페이지 URL (메모용)")
        ref_row.addWidget(ref_label)
        ref_row.addWidget(self.ref_link_input)
        left_layout.addLayout(ref_row)

        # 4. Start button
        self.start_btn = QPushButton("검토 시작")
        self.start_btn.setFixedHeight(40)
        self.start_btn.setStyleSheet(
            "QPushButton { background-color: #4A90D9; color: white; font-size: 15px; "
            "font-weight: bold; border-radius: 6px; } "
            "QPushButton:hover { background-color: #357ABD; } "
            "QPushButton:disabled { background-color: #ccc; }"
        )
        self.start_btn.clicked.connect(self._on_start)
        left_layout.addWidget(self.start_btn)

        # Progress bar
        self.progress = QProgressBar()
        self.progress.setRange(0, 0)  # indeterminate
        self.progress.setVisible(False)
        self.progress.setFixedHeight(6)
        left_layout.addWidget(self.progress)

        splitter.addWidget(left_widget)

        # ---- Right Panel ----
        right_widget = QWidget()
        right_layout = QVBoxLayout(right_widget)
        right_layout.setContentsMargins(8, 0, 0, 0)

        right_layout.addWidget(self._section_label("검토 결과"))

        self.result_display = QTextEdit()
        self.result_display.setReadOnly(True)
        self.result_display.setPlaceholderText("검토 결과가 여기에 표시됩니다...")
        self.result_display.setStyleSheet("font-size: 14px; line-height: 1.6;")
        right_layout.addWidget(self.result_display)

        # Copy & clear buttons
        btn_row = QHBoxLayout()
        copy_btn = QPushButton("결과 복사")
        copy_btn.clicked.connect(self._copy_result)
        clear_btn = QPushButton("초기화")
        clear_btn.clicked.connect(self._clear_all)
        btn_row.addWidget(copy_btn)
        btn_row.addWidget(clear_btn)
        right_layout.addLayout(btn_row)

        splitter.addWidget(right_widget)
        splitter.setSizes([480, 620])

    # ---- Helpers ----

    @staticmethod
    def _section_label(text: str) -> QLabel:
        label = QLabel(text)
        label.setFont(QFont("", 12, QFont.Weight.Bold))
        label.setStyleSheet("margin-top: 6px; margin-bottom: 2px;")
        return label

    # ---- Actions ----

    def _fetch_jd_from_url(self):
        url = self.jd_url_input.text().strip()
        if not url:
            QMessageBox.warning(self, "URL 없음", "JD 링크 URL을 입력해주세요.")
            return
        try:
            text = extract_text_from_url(url)
            if text:
                self.jd_text_input.setPlainText(text)
            else:
                QMessageBox.warning(self, "텍스트 추출 실패", "페이지에서 텍스트를 추출하지 못했습니다.\n직접 붙여넣기를 이용해주세요.")
        except Exception as exc:
            QMessageBox.warning(
                self, "URL 가져오기 실패",
                f"JD를 가져오지 못했습니다. 직접 붙여넣기를 이용해주세요.\n\n오류: {exc}",
            )

    def _get_api_key(self) -> str | None:
        key = self.api_key_input.text().strip()
        if not key:
            key = os.environ.get("ANTHROPIC_API_KEY", "").strip()
        if not key:
            QMessageBox.warning(
                self, "API Key 필요",
                "Anthropic API Key를 입력하거나 환경변수 ANTHROPIC_API_KEY를 설정해주세요.",
            )
            return None
        return key

    def _get_resume_text(self) -> str | None:
        current_tab = self.resume_tabs.currentIndex()
        if current_tab == 0:  # file tab
            path = self.file_drop.file_path
            if not path:
                QMessageBox.warning(self, "이력서 필요", "이력서 파일을 선택해주세요.")
                return None
            try:
                return extract_text_from_file(path)
            except Exception as exc:
                QMessageBox.warning(self, "파일 읽기 실패", f"이력서 파일을 읽지 못했습니다:\n{exc}")
                return None
        else:  # text tab
            text = self.resume_text_input.toPlainText().strip()
            if not text:
                QMessageBox.warning(self, "이력서 필요", "이력서 내용을 입력해주세요.")
                return None
            return text

    def _on_start(self):
        api_key = self._get_api_key()
        if not api_key:
            return

        jd = self.jd_text_input.toPlainText().strip()
        if not jd:
            QMessageBox.warning(self, "JD 필요", "채용공고(JD) 내용을 입력해주세요.")
            return

        resume = self._get_resume_text()
        if not resume:
            return

        notes = self.notes_input.toPlainText().strip()

        # Disable button, show progress
        self.start_btn.setEnabled(False)
        self.progress.setVisible(True)
        self.result_display.setPlainText("검토 중입니다... 잠시만 기다려주세요.")

        self.worker = ScreeningWorker(api_key, jd, resume, notes)
        self.worker.finished.connect(self._on_result)
        self.worker.error.connect(self._on_error)
        self.worker.start()

    def _on_result(self, text: str):
        self.result_display.setMarkdown(text)
        self._finish_loading()

    def _on_error(self, msg: str):
        self.result_display.setPlainText(msg)
        self._finish_loading()

    def _finish_loading(self):
        self.start_btn.setEnabled(True)
        self.progress.setVisible(False)

    def _copy_result(self):
        text = self.result_display.toPlainText()
        if text:
            clipboard = QApplication.clipboard()
            if clipboard:
                clipboard.setText(text)

    def _clear_all(self):
        self.jd_url_input.clear()
        self.jd_text_input.clear()
        self.notes_input.clear()
        self.resume_text_input.clear()
        self.ref_link_input.clear()
        self.file_drop.clear()
        self.result_display.clear()
