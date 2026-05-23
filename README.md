<p align="center">
  <img src="https://raw.githubusercontent.com/jegly/Box/main/images/banner-box002.svg" alt="Box Header" width="81%" />
</p>

[![RAG](https://img.shields.io/badge/RAG-Retrieval%20Augmented-CBA6F7.svg)](https://en.wikipedia.org/wiki/Retrieval-augmented_generation)
[![MCP Servers](https://img.shields.io/badge/MCP_Servers-CBA6F7.svg?logo=anthropic&logoColor=white)](https://modelcontextprotocol.io)
[![Kotlin](https://img.shields.io/badge/Kotlin-90.4%25-7F849C.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-16%2B-A6E3A1.svg?logo=android&logoColor=white)](https://developer.android.com)
[![Version](https://img.shields.io/badge/UpstreamVersion-1.0.15-CBA6F7.svg)](https://github.com/jegly/Box/releases)
[![License](https://img.shields.io/badge/License-Apache%202.0-89DCEB.svg)](LICENSE)
[![llama.cpp](https://img.shields.io/badge/llama.cpp-GGUF-FAB387.svg)](https://github.com/ggerganov/llama.cpp)
[![stable-diffusion.cpp](https://img.shields.io/badge/stable--diffusion.cpp-GGUF-FAB387.svg)](https://github.com/leejet/stable-diffusion.cpp)
[![whisper.cpp](https://img.shields.io/badge/whisper.cpp-STT-FAB387.svg)](https://github.com/ggerganov/whisper.cpp)
[![Voice Mode](https://img.shields.io/badge/Voice%20Mode-Speech--to--Speech-A6E3A1.svg)]()
[![LiteRT](https://img.shields.io/badge/LiteRT-NPU-F5C2E7.svg)](https://ai.google.dev/edge/litert)
[![Document Analysis](https://img.shields.io/badge/Document_Analysis-PDF_%2B_TXT-A6E3A1.svg)]()
[![Vision](https://img.shields.io/badge/Vision-Multimodal_LLM-A6E3A1.svg?logo=camera&logoColor=white)]()
[![Snapdragon NPU](https://img.shields.io/badge/Snapdragon-NPU%208Gen2%2F3%2FElite-F5C2E7.svg)](https://www.qualcomm.com/products/mobile/snapdragon)
[![Google Tensor](https://img.shields.io/badge/Google%20Tensor-TPU%20G3%2FG4%2FG5%20(Pixel%208--10)-F5C2E7.svg)](https://store.google.com/gb/category/phones)
[![MediaTek](https://img.shields.io/badge/MediaTek-NPU-F5C2E7.svg)](https://www.mediatek.com/)
[![SQLCipher](https://img.shields.io/badge/SQLCipher-AES--256-F9E2AF.svg?logo=sqlite&logoColor=white)](https://www.zetetic.net/sqlcipher/)
[![Biometric](https://img.shields.io/badge/Biometric-Lock-F38BA8.svg?logo=fingerprint&logoColor=white)]()
[![Offline](https://img.shields.io/badge/Network-Hard%20Offline-F38BA8.svg)]()
[![GGUF Import](https://img.shields.io/badge/GGUF-Import-A6E3A1.svg)]()
[![Hybrid Engine](https://img.shields.io/badge/Engine-LiteRT%20%2B%20llama.cpp-CBA6F7.svg)]()
[![Gemini Nano](https://img.shields.io/badge/Gemini%20Nano-ML%20Kit%20%C2%B7%20NPU-F5C2E7.svg)](https://developers.google.com/ml-kit/language/gemini-nano)
[![Fork](https://img.shields.io/badge/Fork-Google%20AI%20Edge-7F849C.svg)](https://github.com/google-ai-edge/gallery)
![GitHub all releases](https://img.shields.io/github/downloads/jegly/Box/total)                                                              

If this project helped you, please ⭐️ star it to help others find it. 
## 📱 Download

[![Download Box v1.0.10 APK](https://img.shields.io/badge/Download-Latest_APK-brightgreen?style=for-the-badge&logo=android)](https://github.com/jegly/Box/releases/latest)


> **Note:** If you're using a custom ROM (LineageOS, GrapheneOS, CalyxOS), download the `custom-rom-support` APK from the [latest release](https://github.com/jegly/Box/releases/latest) instead.

### Install via Obtainium

1. Open **Obtainium** on your phone
2. Tap the **+** button
3. Paste this repo URL:  
   `https://github.com/jegly/Box`
4. Tap **Add**


*Recommended for most users: **Main version***


      
  ### Which version should I install?                                                                                                                                                                                                         
                  
  | Version | For |
  |---|---|
  | **Main** | Stock Android (Pixel, Samsung, etc.) |
  | **Custom ROM** | GrapheneOS, LineageOS, CalyxOS — no Google services |
      
- The in-app updater is also available in Settings                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     
  ### Setup steps
                                                                                                                                                                                                                                              
  1. Tap the badge for your version above — this opens Obtainium with the repo pre-filled
  2. Under **APK filter regex**, enter one of the following:
     - Main: `Main`
     - Custom ROM: `custom-rom-support`
  3. Tap **Add** — Obtainium will find the latest release and install it                                                                                                                                                                      
  4. Future updates will be detected automatically
                                                                                                                                                                                                                                              
  > **Note:** The version number shown inside the app (1.0.13) reflects the
  > upstream Google AI Edge Gallery build number and is unrelated to the Box
  > release version. Box releases are tracked via GitHub tags (v1.0.5 etc).
  > Use **Settings → Check for updates** to see if a newer Box release is available.

**Box is a security-hardened fork of [Google AI Edge Gallery](https://github.com/google-ai-edge/gallery) — with on-device image generation, voice mode (speech-to-speech AI chat), voice input, document analysis, vision AI, biometric lock, encrypted chat history, llama.cpp support, and GGUF model import.**

> [!IMPORTANT]
>## Disclaimer

Box is an independent community fork of [Google AI Edge Gallery](https://github.com/google-ai-edge/gallery) and is not affiliated with or endorsed by Google LLC. Google branding has been replaced throughout. All credit for the underlying platform goes to Google and the original contributors — this fork simply builds on top of their work.

  
  
  
## Changelog v1.0.7 – v1.0.10   

| Version | Feature | Details |
|---|---|---|
| v1.0.10 | **Gemini Nano hub** | 6 on-device ML Kit features powered by Gemini Nano on Pixel 9+ (via AICore, NPU/TPU-accelerated): Summarize, Proofread, Rewrite, Chat, Describe Image, and Speech-to-Text. First use triggers an automatic background download of Gemini Nano (~1–2 GB via AICore). |
| v1.0.10 | **Nano Chat — multi-session** | Persistent multi-turn chat with Gemini Nano. Sessions are stored in the existing encrypted SQLCipher database, auto-titled from the first message, and fully resumable. Sessions can be renamed or deleted. Long-press any bubble to copy. |
| v1.0.10 | **Document attachment in Nano** | Proofread and Rewrite now accept attached documents (PDF, TXT, MD) — content is read and passed to Gemini Nano as context. |
| v1.0.10 | **Live camera in Describe Image** | Gallery tab + Live Camera tab. Camera tab binds an `ImageCapture` use case — tap Capture to send the current frame to Nano for description. |
| v1.0.10 | **Background Removal** | New tool powered by ML Kit Subject Segmentation (main branch). One tap removes the background from any photo with a transparency-preserving PNG output. Includes a "Trim transparent edges" toggle. Save or share the result. |
| v1.0.10 | **Catppuccin + Dracula themes** | Three-way theme picker in Settings: System (Material You) / Catppuccin (14 accents) / Dracula (7 accents). Accent colour persists across restarts with no first-frame flicker. |
| v1.0.10 | **Tap jacking protection toggle** | New toggle in Settings (on by default) — `filterTouchesWhenObscured` blocks touch events when an overlay is detected, preventing tap-jacking attacks. |
| v1.0.10 | **Accessibility data sensitivity toggle** | New Settings toggle hides app content from untrusted accessibility services. Off by default (note: incompatible with TalkBack). |
| v1.0.10 | **LaTeX in table cells** | Inline math inside markdown table cells no longer wraps across multiple lines. Uses Compose `InlineTextContent` to embed math as a single placeholder inside `Text()`. |
| v1.0.10 | **Import button simplified** | Home screen import button label shortened to just "Import" (removed "GGUF · LiteRT" subtitle). |
| v1.0.10 | **NPE crash fix** | Fixed a null-pointer crash on startup and on Retry caused by a broken fallback comparator in `groupTasksByCategory`. |
| v1.0.9 | **Document Q&A** | New RAG pipeline: import PDFs and ask questions grounded in the document. Uses MiniLM embeddings (on-device, LiteRT) for chunk retrieval — model only sees the relevant passages. Every answer cites the source chunks it used. |
| v1.0.9 | **Model picker in Document Q&A** | Choose which downloaded LLM handles answering — defaults to first available, switchable mid-session. |
| v1.0.9 | **Kokoro TTS (English)** | Single Kokoro model (`csukuangfj/kokoro-en-v0_19`, ~346 MB) replaces broken individual-voice entries. Correct tensor shapes and metadata — works first time. |
| v1.0.9 | **13 Piper voices** | 8 new voices: LibriTTS-R, HFC Female, HFC Male, Arctic (US English); Thorsten (German); UPMC (French); MLS 10246 (Spanish); Huayan (Chinese Mandarin). 13 total across both branches. |
| v1.0.9 | **10 Whisper models** | Expanded from 3 hardcoded to 10: Tiny, Base, Small, Medium, Large-v3-Turbo, and Large-v3 — each in multilingual and English-only variants. Shared across Audio Scribe and Voice Input. |
| v1.0.9 | **Gemma-4-E2B-it (Snapdragon 8 Elite)** | NPU-optimised variant added to the model allowlist — visible only on SM8750 devices. |
| v1.0.9 | **Fix #46 — Audio Scribe OOM crash** | Replaced boxed `List<Float>` (~16 bytes/sample) with a primitive growing `FloatArray` (4 bytes/sample). 30-min audio at 16 kHz no longer causes ~460 MB excess allocation. |
| v1.0.9 | **Fix #47 — TTS silent with non-Amy voice** | Auto-init and GrapheneOS TTS fallback now filter by download status before selecting a voice model (custom-rom-support only). |
| v1.0.8 | **Saved System Prompts** | Save, name, and reuse system prompts from the model settings dialog. Tap to apply, swipe to delete. |
| v1.0.8 | **Restore Defaults** | New button in model settings resets all sliders (temperature, top-K, top-P, max tokens) back to defaults in one tap. |
| v1.0.8 | **System prompt actually applied** | Changing the system prompt mid-session now correctly resets the conversation with the new instruction — previously saved in UI but not passed to the model. |
| v1.0.8 | **Markdown fix in math responses** | Plain-text segments in chat bubbles now render through the Markdown pipeline, fixing broken formatting in responses that mix text and LaTeX math. |
| v1.0.8 | **Randomised inference seed** | Each conversation now uses a unique random seed for more varied outputs on CPU backend. |
| v1.0.8 | **GPU determinism root cause found** | LiteRT LM v0.11.0 hard-caps `max_top_k: 1` on devices without a GPU sampler, forcing greedy decoding. Switch to CPU for varied outputs. Reported upstream as issue #817. |
| v1.0.7 | **Gemma 4 E2B & E4B updated** | Model files refreshed on HuggingFace — new commit hashes, smaller sizes, same multimodal capabilities. |
| v1.0.7 | **Speculative decoding / MTP** | Multi-Token Prediction reads capability from the model file itself. Gemma 4 E2B reaches 66–91 tok/s on Galaxy S26 Ultra (GPU + spec) vs 52 tok/s plain GPU. |
| v1.0.7 | **Sustained Performance Mode** | `setSustainedPerformanceMode(true)` locks clocks during inference — no mid-conversation thermal throttling on long generations. |
| v1.0.7 | **Benchmark spec decoding toggle** | Benchmark screen shows a speculative decoding toggle for supported models. |
| v1.0.7 | **AI Chat app shortcut** | Long-press the Box icon → AI Chat jumps straight into chat, even from a cold start. |
| v1.0.7 | **In-app update checker** | Settings → Check for updates — fetches the latest GitHub release and offers a direct download link for your variant. |
| v1.0.7 | **Model import from list** | Whisper and TTS models can now be imported directly from the model list. |

---


---

**Related** 
 

Built [OfflineLLM](https://github.com/jegly/OfflineLLM) first — a privacy-first Android chat app with a pure llama.cpp backend.

---

## What is Box?  

<img src="https://raw.githubusercontent.com/jegly/Box/main/images/box-banner-minimal-1600x320.svg" alt="Box Header" width="1000" />  



Box is an Android app for running AI entirely on-device — chat, voice mode, image generation, speech-to-text, document analysis, and vision, all without a network connection. It inherits the full feature set of the upstream Google AI Edge Gallery and layers on top: encrypted conversations, biometric lock, hard offline mode, and three additional native inference engines (llama.cpp, stable-diffusion.cpp, whisper.cpp) alongside LiteRT.

# Box: On-Device AI. No Cloud. No Compromise.

**What makes Box unique?** You can sit at your desk, tap two buttons, and have a real flowing voice conversation with an AI — no wake word, no account, no server, no subscription. It listens, thinks, and speaks back sentence by sentence before it's even finished generating. Point the camera at something and ask about it out loud. The AI sees it and answers. All of it runs on the phone in your hand, completely offline, faster than you'd expect. 

> [!TIP]
> **Custom ROM users (GrapheneOS, LineageOS, CalyxOS):** Use the **custom-rom-support** APK, not Main. Third-party ROMs lack AICore and system TTS, which impairs voice mode and NPU acceleration on the Main build. The custom-rom-support branch works around these limitations with built-in Piper TTS and alternative voice input. TPU/NPU acceleration is supported on Tensor devices; Snapdragon NPU remains untested on custom ROMs.

---

## Screenshots
<div align="center">

<table>
  <tr>
    <td align="center"><img src="images/Box_Screenshots/Homescreen_overview.png" width="300"/><br/><sub>Home Overview</sub></td>
    <td align="center"><img src="images/Box_Screenshots/AI_Chat_Example.png" width="300"/><br/><sub>AI Chat</sub></td>
    <td align="center"><img src="images/Box_Screenshots/Chat_overview.png" width="300"/><br/><sub>Chat Overview</sub></td>
  </tr>
  <tr>
    <td align="center"><img src="images/Box_Screenshots/Ask_Image.png" width="300"/><br/><sub>Vision AI</sub></td>
    <td align="center"><img src="images/Box_Screenshots/VoiceInput.png" width="300"/><br/><sub>Voice Input</sub></td>
    <td align="center"><img src="images/Box_Screenshots/Audio_Scribe.png" width="300"/><br/><sub>Audio Scribe</sub></td>
  </tr>
  <tr>
    <td align="center"><img src="images/Box_Screenshots/Whisper.png" width="300"/><br/><sub>Whisper STT</sub></td>
    <td align="center"><img src="images/Box_Screenshots/Voice_overview.png" width="300"/><br/><sub>Voice Mode</sub></td>
    <td align="center"><img src="images/Box_Screenshots/ImgGen_Overview.png" width="300"/><br/><sub>Image Generation</sub></td>
  </tr>
  <tr>
    <td align="center"><img src="images/Box_Screenshots/Agent_Skills.png" width="300"/><br/><sub>Agent Skills</sub></td>
    <td align="center"><img src="images/Box_Screenshots/Prompt_Lab.png" width="300"/><br/><sub>Prompt Lab</sub></td>
    <td align="center"><img src="images/Box_Screenshots/Model_Configuration.png" width="300"/><br/><sub>Model Config</sub></td>
  </tr>
  <tr>
    <td align="center"><img src="images/Box_Screenshots/Model_Select.png" width="300"/><br/><sub>Model Select</sub></td>
    <td align="center"><img src="images/Box_Screenshots/Settings.png" width="300"/><br/><sub>Settings</sub></td>
    <td align="center"><img src="images/Box_Screenshots/Nano_overview.png" width="300"/><br/><sub>Gemini Nano</sub></td>
  </tr>
</table>

</div>

---
> [!NOTE]
>## What Box adds on top of upstream

Box is a fork of [Google AI Edge Gallery](https://github.com/google-ai-edge/gallery). The upstream project is excellent — Box just layers on additional capabilities:

| Area | What Box adds |
|---|---|
| Inference engines | llama.cpp (GGUF LLMs), stable-diffusion.cpp (image gen), whisper.cpp (STT) alongside LiteRT |
| Model import | Import any local GGUF file — not limited to the curated download list |
| NPU / TPU | All Snapdragon / Tensor / MediaTek variants bundled in one APK (upstream ships per-SoC) |
| Voice mode / Vision mode | Free talk (continuous hands-free loop) and Vision talk (live camera + voice) |
| Image generation | On-device Stable Diffusion via GGUF |
| Speech-to-text | On-device Whisper STT |
| Document analysis | Attach text files (`.txt`, `.md`, `.csv`, `.kt`, etc.) directly in chat |
| Document Q&A | RAG pipeline: import PDFs, embed with MiniLM on-device, ask questions grounded in document content — answers cite their source passages |
| Gemini Nano | 6 on-device ML Kit features (Summarize, Proofread, Rewrite, Chat, Describe, Speech) — NPU/TPU-accelerated on Pixel 9+, entirely on-device via AICore (main branch) |
| Background Removal | ML Kit Subject Segmentation — remove backgrounds from photos, output a transparency-preserving PNG (main branch) |
| Chat history | Persisted to a SQLCipher-encrypted Room database, resumable across sessions |
| Security | Biometric app lock, hard offline mode, prompt sanitisation, audit log, tap jacking protection, accessibility data sensitivity |
| Themes | Catppuccin (14 accents) and Dracula (7 accents) alongside Material You — three-way picker in Settings |
| Agent skills | 20 built-in skills (upstream has 9) |
| Math rendering | LaTeX expressions rendered as Unicode in chat, including inside markdown table cells |
| App shortcut | Long-press icon → AI Chat for instant cold-start navigation |
| In-app updates | Settings → Check for updates — compares against latest GitHub release, downloads correct variant |

---

## Core Features

### Local Chat
Multi-turn conversations with on-device LLMs. Import any GGUF model or download LiteRT models from the built-in list. Supports Thinking Mode on compatible models. Full markdown rendering with LaTeX math support — Greek letters, operators, fractions, and notation are rendered as Unicode symbols. Conversations are persisted and resumable.

> **Recommended models:** We highly recommend **Gemma 4 E2B** or **Gemma 4 E4B** (LiteRT) as your primary models — best-tested, support vision, voice, and documents, and run efficiently with GPU/NPU acceleration. Available to download directly in the app.

With **Gemma 4 E2B / E4B** selected, the chat input expands to a full multimodal interface:
- 📎 Attach documents (`.txt`, `.md`, `.csv`, `.json`, `.py`, `.kt`, and more) — content is injected into context automatically
- 🎙 Record an audio clip or pick a WAV file to speak your question
- 📷 Take a photo or pick from album for visual Q&A

### Local Diffusion
On-device image generation powered by [stable-diffusion.cpp](https://github.com/leejet/stable-diffusion.cpp). Runs Stable Diffusion 1.5 in GGUF format fully offline — no API key, no cloud. Configurable steps, CFG scale, seed, and image size presets. Save generated images directly to your gallery. Import your own GGUF diffusion models.

### Voice Input
On-device speech-to-text using [whisper.cpp](https://github.com/ggerganov/whisper.cpp). Tap to record, tap to transcribe. Copy or clear results. Supports Whisper Tiny through Small models in multiple languages. Audio never leaves the device.

### Free Talk — Real-Time Voice Conversation

Tap the mic and the speaker. That's it. Box listens to you, sends your words to the AI, and speaks the reply back — then immediately starts listening again. No tapping between turns. No waiting for a full response before it starts speaking. Just sit there and talk to it like a person.

On Gemma 4 E2B it keeps up in real time. The first sentence of the reply is already being spoken while the model is still generating the rest.

- *"Explain quantum entanglement like I'm five"* → speaks the answer, listens for your follow-up
- *"Actually, go deeper on that last point"* → multi-turn, completely hands-free  
- *"Help me think through a problem I'm having at work"* → back and forth, no typing ever
- *"What should I cook for dinner tonight? I've got chicken and not much else"* → practical daily use

It feels like having an AI sitting across from you. Entirely offline. Nothing leaves the device.

Three toggles in AI Chat control it:
- **🎤 Mic** — tap once to enter free talk mode, tap again to stop
- **🔊 Speaker** — AI replies spoken aloud, sentence by sentence as they generate
- **📹 Camera** — live vision mode (see below)

Enable **Real-time voice reply** in Settings for sentence-by-sentence speech as the model generates. Works out of the box with Android's built-in speech and TTS — load a Whisper or Piper model for higher quality.

> **De-Googled ROMs (GrapheneOS, CalyxOS, LineageOS without GApps):** Use the **custom-rom-support** APK — it includes Piper TTS (Amy) as a built-in download in the Voice tab, so no third-party TTS app is needed. If you're on the Main build, install a TTS engine from F-Droid (e.g. [RHVoice](https://f-droid.org/packages/com.github.olga_yakovleva.rhvoice.android/) or [eSpeak NG](https://f-droid.org/packages/com.reecedunn.espeak/)) and set it as default in **Android Settings → Accessibility → Text-to-speech**.

---

### Vision Talk — Live Camera + Voice AI

Tap the camera toggle to stream your back camera directly to the AI. Point it at anything and ask — the AI sees the current frame alongside your question and speaks its answer back. All offline, no cloud.

**Things you can do:**

- Point at a plant → *"What species is this and how do I care for it?"*
- Point at food in your fridge → *"What can I cook with what's here?"*
- Point at a label or sign in another language → *"What does this say?"*
- Point at a circuit board → *"What component is this and what does it do?"*
- Point at your code on a laptop screen → *"What's wrong with this function?"*
- Point at a meal → *"Roughly how many calories is this?"*
- Point at a maths problem → *"Walk me through how to solve this"*

Combine with mic + speaker for a fully hands-free vision conversation — speak your question, AI sees the scene, speaks the answer, listens for the next question. Requires a vision-capable model (Gemma 4 E2B or E4B).

When mic is off, camera mode sends a frame every 3 seconds automatically with "What do you see?" — useful for passive scene description.

### Vision AI
Ask questions about images using on-device vision models. Powered by LiteRT with Gemma 4 E2B / E4B — GPU-accelerated, up to 32K context.

### Biometric App Lock
Enable an optional biometric lock from Settings. The app re-locks automatically every time it is backgrounded. Unlock via fingerprint or face authentication before any content is shown.

### Encrypted Chat History
All conversations are stored in a SQLCipher-encrypted Room database. History persists across sessions and is resumable from the Chat History screen. Swipe to delete individual conversations, or wipe all at once.

### NPU / TPU Acceleration
All Qualcomm Hexagon NPU variants (Snapdragon 8 Gen 2 / 8 Gen 3 / 8 Elite / newer), Google Tensor TPU (Pixel 8–10), and MediaTek NPU are bundled in a single APK — no separate builds per device. Select **NPU/TPU** in the model's accelerator dropdown; Box auto-detects the chip and loads the right runtime.

> **Note:** NPU acceleration currently falls back to GPU/CPU for most models. The NPU path (via AICore on Tensor, QNN on Snapdragon) requires model-side AUX metadata that current litert-community models don't yet include. GPU is the recommended accelerator and performs excellently on all supported chips.

Supported hardware:
- **Snapdragon 8 Gen 2** (SM8550, Hexagon V69)
- **Snapdragon 8 Gen 3** (SM8650, Hexagon V73)
- **Snapdragon 8 Elite** (SM8750, Hexagon V75)
- **Snapdragon 8 Elite for Galaxy** (SM8850, Hexagon V79)
- **Snapdragon next-gen** (Hexagon V81)
- **Google Tensor G3 / G4 / G5** (Pixel 8 / 9 / 10)
- **MediaTek Dimensity** (MT6989, MT6991)

### GGUF Model Import
Import any GGUF model file from local storage. At import time set the display name and choose the accelerator (CPU, GPU via OpenCL/Vulkan, or NPU via QNN delegate). Stable Diffusion GGUF models can also be imported for image generation.

### Hard Offline Mode
A toggle in Settings forces the app into a fully airgapped state — all download attempts throw an exception and no network calls are made.

---

## Getting Started

### Requirements

- Android 16+
- ~4 GB of free storage for a typical quantised LLM
- `6 GB of Ram

### Build from source

```bash
git clone --recurse-submodules https://github.com/jegly/box
cd box/Android
./gradlew :app:assembleDebug
```

The `--recurse-submodules` flag is required to pull llama.cpp, stable-diffusion.cpp, and whisper.cpp submodules. The first build compiles all three native libraries from source — expect 15–25 minutes. 

Open `Android/` in Android Studio and run on a physical device for best performance.

### Loading a LiteRT/GGUF model 

1. Copy a `.litertlm/GGUF` file to your device (Downloads, USB, etc.)
2. Open the app → **Model Manager** in the drawer
3. Tap **Import** and pick your file
4. Set a display name and choose CPU / GPU / NPU
5. The model appears in AI Chat

---

## Security Architecture

| Mechanism | Details |
|---|---|
| Database encryption | SQLCipher via `androidx.room` — AES-256 at rest |
| Biometric gate | `BiometricPrompt` API, re-prompts on each foreground |
| Offline mode | `OfflineMode` singleton blocks `DownloadWorker` and network calls |
| Prompt sanitisation | `SecurityUtils.sanitizePrompt()` strips control characters before inference and persistence |
| Tap jacking protection | `filterTouchesWhenObscured` on the window — user-configurable in Settings (on by default) |
| Accessibility data sensitivity | `ViewCompat.setAccessibilityDataSensitive()` hides content from untrusted accessibility services — user-configurable in Settings |
| Screenshot protection | `FLAG_SECURE` blocks screen capture and Recent Apps thumbnails — user-configurable in Settings |
| Audit log | `SecurityAuditLog` writes security events to a local append-only log |

---

## Technology Stack

- **Kotlin + Jetpack Compose** — UI
- **Hilt** — dependency injection
- **Room + SQLCipher** — encrypted persistence
- **LiteRT-LM** — LiteRT inference runtime for LLMs (GPU + NPU/TPU)
- **Qualcomm QNN / QAIRT 2.41** — Hexagon NPU runtime (V69–V81, bundled)
- **LiteRT NPU dispatch** — auto-selects Qualcomm / Google Tensor / MediaTek at runtime
- **llama.cpp** — GGUF LLM inference (git submodule)
- **stable-diffusion.cpp** — GGUF image generation (git submodule)
- **whisper.cpp** — on-device speech-to-text (git submodule)
- **Sherpa-ONNX (k2-fsa)** — Piper TTS engine for on-device voice synthesis (custom-rom-support branch)


---

## Acknowledgements

Box would not exist without the work of the teams and individuals behind the projects it builds on.

**[Google AI Edge Gallery](https://github.com/google-ai-edge/gallery)** — the upstream project this fork is based on. The Google AI Edge team built an exceptionally well-structured, open-source Android app and made it available under the Apache 2.0 licence. Everything in Box starts from their foundation. Upstream changes are periodically merged and any improvements we make that are appropriate to contribute back will be.

**[llama.cpp](https://github.com/ggerganov/llama.cpp)** — Georgi Gerganov and the llama.cpp contributors for making high-performance on-device LLM inference accessible to everyone.

**[stable-diffusion.cpp](https://github.com/leejet/stable-diffusion.cpp)** — leejet and contributors for the C++ Stable Diffusion implementation that powers on-device image generation.

**[whisper.cpp](https://github.com/ggerganov/whisper.cpp)** — Georgi Gerganov and contributors for the Whisper speech-to-text port.

**[LiteRT / TensorFlow Lite](https://ai.google.dev/edge/litert)** — the Google teams behind LiteRT (formerly TFLite) and the NPU/GPU delegate infrastructure.

**[Sherpa-ONNX / k2-fsa](https://github.com/k2-fsa/sherpa-onnx)** — the k2-fsa team for Sherpa-ONNX, which powers the Piper TTS engine (Amy and other voices) in the custom-rom-support branch.

**[off-grid-mobile-ai](https://github.com/alichherawalla/off-grid-mobile-ai)** — Mohammed Ali Chherawalla for the on-device Stable Diffusion Android implementation, which was instrumental in getting efficient on-device image generation working and influenced parts of Box’s pipeline.

 **[PocketSage](https://github.com/umerarif11/pocketsage)** — Umer Arif for the clean, fully offline RAG-on-Android
  reference implementation that the Document Q&A feature in Box is based on.


Thanks to **aryoda** and all the contributors for consistently reporting valid bugs. Appreciate the reports !



Thank you to everyone who has opened issues, tested builds, or contributed to any of these projects. On-device AI is a community effort.

---

## License
<img src="https://github.com/jegly/Box/blob/main/images/Apache_Software_Foundation.png?raw=true" alt="Apache Software Foundation Logo" width="120">
Licensed under the Apache License, Version 2.0

---

## Links

- [Box repository](https://github.com/jegly/box)
- [Upstream: google-ai-edge/gallery](https://github.com/google-ai-edge/gallery)
- [llama.cpp](https://github.com/ggml-org/llama.cpp)
- [stable-diffusion.cpp](https://github.com/leejet/stable-diffusion.cpp)
- [whisper.cpp](https://github.com/ggerganov/whisper.cpp)
- [LiteRT-LM](https://github.com/google-ai-edge/LiteRT-LM)
- [LiteRT NPU docs](https://ai.google.dev/edge/litert/next/litert_lm_npu)
- [Qualcomm QAIRT SDK](https://softwarecenter.qualcomm.com)
- [Hugging Face LiteRT Community](https://huggingface.co/litert-community)

---

  ## Checksums

  | Variant | SHA-256 |
  |---|---|
  | main | `d6406193d0857cc60d99d169738058cb99c40ed84bf4adc82733480bc5ad0f49` |
  | custom-rom-support | `160eb2ed07997412ecfb29416fa79d2d6b708ef093544ac54526071e6509c04f` |

---

<p align="center">
  <img src="https://raw.githubusercontent.com/jegly/Box/main/images/box-linux-catppuccin-latte.svg" alt="Box for Linux" width="81%" />
</p>

[![Python](https://img.shields.io/badge/Python-3.12%2B-BD93F9.svg?logo=python&logoColor=white)](https://www.python.org)
[![GTK4](https://img.shields.io/badge/GTK4-libadwaita-8BE9FD.svg)](https://www.gtk.org)
[![LiteRT-LM](https://img.shields.io/badge/LiteRT--LM-Local%20Inference-FF79C6.svg)](https://github.com/google-ai-edge/LiteRT-LM)
[![Ubuntu](https://img.shields.io/badge/Ubuntu-amd64-FFB86C.svg?logo=ubuntu&logoColor=white)](https://ubuntu.com)
[![Voice Mode](https://img.shields.io/badge/Voice%20Mode-Speech--to--Speech-50FA7B.svg)]()
[![Vision](https://img.shields.io/badge/Vision-Live%20Camera-50FA7B.svg?logo=camera&logoColor=white)]()
[![Knowledge Base](https://img.shields.io/badge/Knowledge%20Base-RAG%20%2B%20Notebooks-50FA7B.svg)]()
[![Tools](https://img.shields.io/badge/Tools-Web%20%2B%20Filesystem-50FA7B.svg)]()
[![On-Device](https://img.shields.io/badge/Network-On--Device%20Only-FF5555.svg)]()
[![License](https://img.shields.io/badge/License-Closed%20Source-FF5555.svg)]()
[![Package](https://img.shields.io/badge/Package-.deb-6272A4.svg)]()

## Box for Linux (Desktop)

**Box for Linux** is a native GTK4 / libadwaita desktop app that brings the Box
experience to the Linux desktop — fully offline local chat, real-time voice
conversation, live camera vision, document Q&A, and web/file tools, all running
on your own machine. Built on Google's LiteRT-LM runtime, it shares the
philosophy of the Android app — on-device, offline-first AI, no account, no
telemetry — in an app designed from the ground up for the Linux desktop.

> [!IMPORTANT]
> Box for Linux is a **separate application, written from scratch** — it is
> **not** a port, build, or fork of the Android app. The two share a name, a
> philosophy, and many similar features, but they are independent codebases.
> The Android app is open source (Apache-2.0); **Box for Linux is distributed
> as a closed-source binary** — the `.deb` ships compiled code and its source
> is not currently published. It does not include the Android app's Stable
> Diffusion image generation, Whisper STT, SQLCipher encryption, or biometric
> lock.

## What is Box for Linux?

A local-first chat app where the language model, the retrieval embedder, the
image captioner, and the text-to-speech all run on your own hardware. The
interface is native — not Electron, not a browser shell — so it starts in under
a second, uses sane amounts of memory, and sits properly inside your GTK
desktop.

It's built to be the daily-driver assistant on a Linux laptop: fast enough for
quick back-and-forth questions, capable enough to ground its answers in your
local documents, your webcam, and the open web — without ever sending your
conversation to someone else's server.

---

## Screenshots

<div align="center">

<table>
  <tr>
    <td align="center"><img src="images/box_linux_chatoverview.png" width="300"/><br/><sub>Local Chat</sub></td>
    <td align="center"><img src="images/box-linux_chatoverview_knowledgebase.png" width="300"/><br/><sub>Knowledge Base</sub></td>
    <td align="center"><img src="images/box-linux_pref_internet_permission_Example.png" width="300"/><br/><sub>Permission Prompts</sub></td>
  </tr>
  <tr>
    <td align="center"><img src="images/box-linux_pref_tools.png" width="300"/><br/><sub>Web &amp; File Tools</sub></td>
    <td align="center"><img src="images/box-linux_pref_tools2.png" width="300"/><br/><sub>Agent Mode</sub></td>
    <td align="center"><img src="images/box-linux_pref_memory.png" width="300"/><br/><sub>Persistent Memory</sub></td>
  </tr>
  <tr>
    <td align="center"><img src="images/box-linux_multimodal.png" width="300"/><br/><sub>Vision &amp; Camera</sub></td>
    <td align="center"><img src="images/box-linux_multimodal2.png" width="300"/><br/><sub>Voice &amp; TTS</sub></td>
    <td align="center"><img src="images/box-linux_pref_knowledge.png" width="300"/><br/><sub>RAG Settings</sub></td>
  </tr>
  <tr>
    <td align="center"><img src="images/box-linux-pref_models.png" width="300"/><br/><sub>Model Settings</sub></td>
    <td align="center"><img src="images/box-linux_pref_behaviour.png" width="300"/><br/><sub>Behaviour</sub></td>
    <td align="center"><img src="images/box-linux_pref_appearance.png" width="300"/><br/><sub>Themes &amp; Appearance</sub></td>
  </tr>
</table>

</div>

---

## Core Features

### Local Chat
Multi-turn conversations with on-device LLMs in `.litertlm` format — Gemma 4 E2B
and E4B are the recommended daily drivers, both supported up to 128K context.
Tokens stream in as they generate, and a snappy Stop button interrupts
mid-token. Full Markdown rendering with LaTeX math — inline expressions render
as Unicode, display equations as images. Attach text, PDF, image, or audio
files directly in the composer. Conversations are saved and resumable, with a
searchable, resizable, hideable sidebar, a live token-usage bar, an adjustable
context window, and a CPU or GPU backend.

### Voice & Conversation
Box listens, thinks, and speaks back. With the audio backend enabled the model
reasons about spoken or attached audio directly — not just transcription.
Record a voice message, play it back inline, and optionally auto-send it. Or
enter **voice conversation mode**: a hands-free, voice-activity-driven loop —
speak, the model replies aloud sentence by sentence as it generates, then it
listens again, no tapping between turns. An optional **push-to-talk** button
covers noisy rooms. Replies are spoken with Piper, an offline neural TTS, in
any of six voices, with adjustable volume.

### Live Camera Vision
Point a webcam at something and ask about it. The 📷 button in the composer
opens a live preview — capture a frame and the model sees it on send.
**Vision Mode** keeps the camera on and auto-captures a frame each turn for a
continuous live-vision conversation. Capture runs through GStreamer + PipeWire
(with a V4L2 fallback), so it integrates cleanly with the Linux camera
permission portal — and the camera light goes off deterministically when you're
done. Images can also be added to your knowledge base, where the model captions
them and makes them searchable.

### Knowledge Base — Document Q&A
Attach a PDF, Markdown file, source file, or plain text and Box chunks, embeds,
and indexes it for retrieval — every answer is grounded in your documents, and
a card on each reply shows exactly which passages the model used.
**Notebooks** are named, reusable collections of documents that live
independently of any chat: index a body of knowledge once and attach it to as
many chats as you like, with an optional auto-attach for collections you always
want. Retrieval unions a chat's private sources with every attached notebook.

### Tools & Agent Mode
Box can search the web (DuckDuckGo, HTTPS-only, no API key, no signup) and read
or write files in a workspace folder you choose. **Agent mode** chains multiple
tool calls to handle multi-step tasks — research and report, compare, plan —
with a configurable per-message cap on tool calls and a live progress pill.
Every tool invocation renders as a collapsible card in the reply, showing the
exact arguments and result.

### Persistent Memory
Save a fact once and Box recalls the relevant ones across all of your chats,
from a long-term store kept separate from per-chat documents. Capture is always
explicit — nothing is remembered without you asking — and a memory inspector
lets you view, search, and delete what Box knows.

### Themes
Six themes — Catppuccin Mocha, Latte, Frappé, and Macchiato, plus Dracula and
Dracula Pro — each with 14 accent colours, five iMessage-style bubble palettes,
a bubble-opacity slider, custom fonts, and macOS-style traffic-light window
controls.

---

> [!NOTE]
> ## You control everything

Every capability in Box for Linux is a separate switch, and **everything is OFF
by default** — vision, audio, TTS, knowledge base, web search, filesystem,
agent mode, and memory are each opt-in.

| Control | What it means |
|---|---|
| Granular toggles | Each capability is its own switch — nothing runs unless you turn it on |
| Permission prompts | Any tool that touches your machine asks first — Allow once / Allow for this chat / Always / Deny |
| Writes always ask | File writes and deletes can never be set to "trust always" — they prompt every time |
| Per-chat overrides | Flip any tool on or off for a single conversation, independent of the global setting |
| HTTPS-only | Every network boundary rejects non-HTTPS URLs — model downloads, web search results, everything |
| Fully on-device | No account, no telemetry, no phoning home; models download once, then run offline |

---

## Install

Download the latest `.deb` from the [Releases](https://github.com/jegly/Box/releases) page:

```bash
sudo apt install ./box_<version>_amd64.deb
```

The package pulls its system dependencies automatically. Then launch **Box**
from your application menu, or run `box` from a terminal. On first run, Box
offers to download a model (Gemma 4 E2B, ~2.59 GB) — models are downloaded
once and then used entirely offline.

### Requirements

- Ubuntu (amd64) with a GTK4 / libadwaita desktop session
- ~3–4 GB of free storage for a model
- A webcam is optional (live vision mode)
- CPU-only works fine; GPU acceleration is faster but not required. NPU/GPU
  paths are included but not all hardware is tested.

---

## Source & License

The Android app is open source (Apache-2.0). **The Linux desktop app is
distributed as a closed-source binary** — the `.deb` ships compiled code and
its source is not currently published. © Jegly. All rights reserved.

---

## Downloads

| Platform | Download | Source |
|----------|----------|--------|
| Android | APK (Releases) / Obtainium | Open (Apache-2.0) |
| Linux (Ubuntu, amd64) | `.deb` (Releases) | Closed (binary only) |
