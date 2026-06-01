---
name: Web Research
description: Search the web, parse top pages, and answer with concise evidence.
---

# Web search and page parser

Use this skill when the user asks for latest/current web information, comparisons, references, or source-backed answers.

Execution steps:
1. Call toolName:`searchWeb` with data JSON:
	- `query`: user request rewritten as a focused search query
2. Parse the returned `results_json` array (objects with `title`, `text`, `url`).
3. Rank results by relevance, source quality, freshness, and diversity.
4. Select the best URLs yourself, dont ask user.
5. Call toolName:`parseWebPage` to get information from web data JSON:
	- `url`: chosen URL.
6. If some page fails - try another one from search results, do not repeat same URL. 
7. If there is not enough information - try another URL at step 5.
7. Synthesize a final answer grounded in parsed content.

Output requirements:
- Start with a direct answer or error message.
- Do not fabricate citations or facts.
- If evidence is weak/conflicting, say that explicitly.


