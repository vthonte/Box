---
name: web-research
description: Search the web, parse top pages, and answer with concise evidence.
---

Use this skill when the user asks for latest/current web information, comparisons, references, or source-backed answers.

Execution steps:
1. Call `searchWeb` with:
   - `query`: user request rewritten as a focused search query.
   - `maxResults`: `"8"`.
2. Parse the returned `results_json` array (objects with `title`, `text`, `url`).
3. Rank results by relevance, source quality, freshness, and diversity.
4. Select the best 3 URLs.
5. Call `parseWebPage` for each selected URL with:
   - `url`: chosen result URL.
   - `maxChars`: `"7000"` unless user requested deeper detail.
6. Synthesize a final answer grounded in parsed content.

Output requirements:
- Start with a direct answer.
- Then provide short bullet evidence with source URLs.
- Do not ask user which links to parse.
- Do not fabricate citations or facts.
- If evidence is weak/conflicting, say that explicitly.
