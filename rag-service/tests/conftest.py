import os

# app.config builds Settings at import time and GOOGLE_API_KEY is required.
# Tests never call Gemini, so any value will do.
os.environ.setdefault("GOOGLE_API_KEY", "test-key")
