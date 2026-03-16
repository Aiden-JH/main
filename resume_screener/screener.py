"""Claude API-based resume screening logic."""

import anthropic


SYSTEM_PROMPT = """\
당신은 채용팀의 서류 심사 전문가입니다.
주어진 채용공고(JD)와 지원자의 이력서를 비교 분석하여 적합성을 평가합니다.
객관적이고 구조화된 평가를 제공하며, 한국어로 답변합니다.
"""

USER_PROMPT_TEMPLATE = """\
아래의 채용공고(JD)와 지원자 이력서를 비교 분석하여 서류 적합성을 평가해주세요.

## 채용공고 (JD)
{jd_text}

## 지원자 이력서
{resume_text}

{notes_section}

---

아래 양식에 맞춰 결과를 출력해주세요:

### 1. 적합도 점수
- **종합 점수**: X / 100점
- **경력 적합도**: X / 25점
- **기술 스택 적합도**: X / 25점
- **학력/자격 적합도**: X / 25점
- **기타 (문화 적합성, 성장 가능성 등)**: X / 25점

### 2. 핵심 강점 (JD 부합 항목)
- (JD 요구사항과 매칭되는 지원자의 강점을 구체적으로 나열)

### 3. 우려 사항 / 부족한 부분
- (JD 요구사항 대비 부족하거나 확인이 필요한 부분)

### 4. 추가 확인 필요 사항
- (면접에서 확인하면 좋을 질문이나 포인트)

### 5. 종합 의견
(합격/보류/불합격 추천 및 근거를 2-3문장으로 요약)
"""


def build_prompt(jd_text: str, resume_text: str, notes: str = "") -> str:
    """Build the user prompt for the screening request."""
    notes_section = ""
    if notes.strip():
        notes_section = f"## 검토 참고사항\n{notes.strip()}"

    return USER_PROMPT_TEMPLATE.format(
        jd_text=jd_text,
        resume_text=resume_text,
        notes_section=notes_section,
    )


def screen_resume(
    api_key: str,
    jd_text: str,
    resume_text: str,
    notes: str = "",
    model: str = "claude-sonnet-4-20250514",
) -> str:
    """Send screening request to Claude API and return the result text."""
    client = anthropic.Anthropic(api_key=api_key)

    user_message = build_prompt(jd_text, resume_text, notes)

    message = client.messages.create(
        model=model,
        max_tokens=4096,
        system=SYSTEM_PROMPT,
        messages=[{"role": "user", "content": user_message}],
    )

    return message.content[0].text
