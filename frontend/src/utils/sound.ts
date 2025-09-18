// UI sound helper (custom files only; no fallback tones)

type SoundKind = 'confirm' | 'success' | 'error';

// Optional custom sound URLs (e.g., '/sounds/success.mp3').
// Call setSoundUrls(...) at app startup to override.
let CUSTOM_URLS: Partial<Record<SoundKind, string>> = {};
let DEFAULT_VOLUME = 0.3;

export function setSoundUrls(urls: Partial<Record<SoundKind, string>>, volume?: number) {
  CUSTOM_URLS = { ...CUSTOM_URLS, ...urls };
  if (typeof volume === 'number') DEFAULT_VOLUME = Math.min(Math.max(volume, 0), 1);
}

function playFromFile(url: string) {
  try {
    const audio = new Audio(url);
    audio.volume = DEFAULT_VOLUME;
    audio.play().catch(() => {});
  } catch {
    // ignore
  }
}

function hasExtension(url: string) {
  return /\.[a-z0-9]+($|\?)/i.test(url);
}

export function playSound(kind: SoundKind) {
  try {
    if (typeof window === 'undefined') return;
    // Play only if custom file is configured
    const base = CUSTOM_URLS[kind];
    if (!base) return;
    if (hasExtension(base)) {
      playFromFile(base);
      return;
    }
    // Try common extensions if extension not provided
    const candidates = [`${base}.mp3`, `${base}.wav`, `${base}.aac`, `${base}.m4a`, `${base}.ogg`];
    for (const url of candidates) {
      try {
        playFromFile(url);
        return;
      } catch {
        // try next
      }
    }
  } catch {
    // noop
  }
}


