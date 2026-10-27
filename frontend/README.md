# Frontend

Angular 21 app (server-side rendered) for the HW Student Assistant portal: content pages, the chat
widget, language switching and the campus map. See the [root README](../README.md) for the overall
architecture.

## Setup

```bash
npm ci
cp src/environments/environment.example.ts src/environments/environment.ts
npm start            # http://localhost:4200
```

`npm start` proxies `/api` to the backend on `http://localhost:8080` (see `proxy.conf.json`), so the
backend must be running. `environment.ts` holds the optional Google Maps key and map ID; the map is
the only feature that needs them.

## Scripts

| Command | What it does |
|---|---|
| `npm start` | Dev server with live reload |
| `npm test -- --watch=false` | Unit tests (Vitest) |
| `npm run lint` | ESLint |
| `npm run build` | Production build with SSR, output in `dist/` |
| `npm run serve:ssr:frontend` | Serve the production SSR build |
| `npm run translate` | Regenerate `src/assets/{es,fr,de}.json` from `en.json` with Google Translate (needs `GOOGLE_TRANSLATE_KEY`) |

## Layout

- `src/app/pages/`: one folder per portal page (landing, registration, academic, wellbeing, ...)
- `src/app/components/`: chat widget and campus map
- `src/app/services/`: API client, language selection, logging
- `src/app/interceptors/`: adds the selected language as `Accept-Language` to every `/api` request, and logs HTTP errors
- `src/assets/*.json`: UI text for each language. Edit `en.json`, then run `npm run translate`.
