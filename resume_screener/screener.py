"""Claude API-based resume screening logic."""

import json

import anthropic


SYSTEM_PROMPT = """\
당신은 채용 서류 검토 전문가입니다. 주어진 JD(직무기술서)와 지원자의 이력서를 비교 분석하여 적합성을 판단하세요.

[입력]
- JD 내용
- 검토 참고사항 (있을 경우)
- 지원자 이력서 내용

[출력 형식 — 반드시 아래 JSON 형식으로만 응답]
{
  "result": "적합" | "보류" | "부적합",
  "reasons": ["사유1", "사유2", "사유3"]
}

[판단 기준]
- 적합: JD의 핵심 요구사항과 지원자의 경력/역량이 높은 수준으로 부합
- 보류: 일부 부합하나 확인이 필요한 부분이 있거나, 경험의 깊이가 불확실
- 부적합: JD 핵심 요구사항과의 괴리가 크거나, 필수 조건 미충족

reasons는 3~4개, 각 항목은 한국어로 한 문장 이내로 작성하세요.
검토 참고사항이 제공된 경우 반드시 해당 내용을 판단에 반영하세요.
"""

USER_PROMPT_TEMPLATE = """\
아래의 JD(직무기술서)와 지원자 이력서를 비교 분석하여 적합성을 판단해주세요.

## JD (직무기술서)
{jd_text}

## 지원자 이력서
{resume_text}

{notes_section}

반드시 지정된 JSON 형식으로만 응답해주세요.
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


def parse_screening_result(raw_text: str) -> dict:
    """Parse the JSON response from Claude into a dict.

    Returns a dict with keys 'result' and 'reasons'.
    Raises ValueError if parsing fails.
    """
    # Strip markdown code fences if present
    text = raw_text.strip()
    if text.startswith("```"):
        lines = text.splitlines()
        # Remove first and last lines (``` markers)
        lines = [l for l in lines if not l.strip().startswith("```")]
        text = "\n".join(lines).strip()

    try:
        data = json.loads(text)
    except json.JSONDecodeError as exc:
        raise ValueError(f"Claude 응답을 JSON으로 파싱할 수 없습니다:\n{raw_text}") from exc

    if "result" not in data or "reasons" not in data:
        raise ValueError(f"응답에 필수 필드(result, reasons)가 없습니다:\n{raw_text}")

    valid_results = {"적합", "보류", "부적합"}
    if data["result"] not in valid_results:
        raise ValueError(f"result 값이 올바르지 않습니다: {data['result']}")

    return {
        "result": data["result"],
        "reasons": list(data["reasons"]),
    }


def screen_resume(
    api_key: str,
    jd_text: str,
    resume_text: str,
    notes: str = "",
    model: str = "claude-sonnet-4-20250514",
) -> dict:
    """Send screening request to Claude API and return parsed result dict.

    Returns: {"result": "적합|보류|부적합", "reasons": [...]}
    """
    client = anthropic.Anthropic(api_key=api_key)

    user_message = build_prompt(jd_text, resume_text, notes)

    message = client.messages.create(
        model=model,
        max_tokens=1024,
        system=SYSTEM_PROMPT,
        messages=[{"role": "user", "content": user_message}],
    )

    raw_text = message.content[0].text
    return parse_screening_result(raw_text)
