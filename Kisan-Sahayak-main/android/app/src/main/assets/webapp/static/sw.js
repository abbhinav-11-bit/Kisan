/* Shram Setu service worker.
   - Static assets: cache-first (fast, works offline)
   - Pages (GET navigations): network-first, fall back to cached copy, then /offline
   - POST / API calls are never cached. */
const VERSION = 'v1';
const STATIC_CACHE = `ks-static-${VERSION}`;
const PAGE_CACHE = `ks-pages-${VERSION}`;
const PRECACHE = [
  '/offline',
  '/static/css/style.css',
  '/static/js/script.js',
  '/static/manifest.webmanifest',
  '/static/icons/icon-192.png',
  '/static/icons/icon-512.png'
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(STATIC_CACHE).then((c) => c.addAll(PRECACHE)).then(() => self.skipWaiting())
  );
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys()
      .then((keys) => Promise.all(
        keys.filter((k) => ![STATIC_CACHE, PAGE_CACHE].includes(k)).map((k) => caches.delete(k))
      ))
      .then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', (event) => {
  const req = event.request;
  if (req.method !== 'GET') return;
  const url = new URL(req.url);

  // Fonts / icon CDNs: stale-while-revalidate
  if (url.origin !== location.origin) {
    if (/fonts\.(googleapis|gstatic)\.com|cdnjs\.cloudflare\.com/.test(url.host)) {
      event.respondWith(
        caches.open(STATIC_CACHE).then(async (cache) => {
          const cached = await cache.match(req);
          const fetching = fetch(req).then((res) => { cache.put(req, res.clone()); return res; }).catch(() => cached);
          return cached || fetching;
        })
      );
    }
    return;
  }

  // Never cache live JSON/API or auth-sensitive endpoints
  if (url.pathname.startsWith('/api/') || url.pathname.startsWith('/admin') ||
      url.pathname === '/login' || url.pathname === '/logout') {
    return;
  }

  // Static assets: cache-first
  if (url.pathname.startsWith('/static/')) {
    event.respondWith(
      caches.match(req).then((hit) => hit || fetch(req).then((res) => {
        const copy = res.clone();
        caches.open(STATIC_CACHE).then((c) => c.put(req, copy));
        return res;
      }))
    );
    return;
  }

  // Pages: network-first with offline fallback
  if (req.mode === 'navigate') {
    event.respondWith(
      fetch(req)
        .then((res) => {
          if (res.ok && !res.redirected) {
            const copy = res.clone();
            caches.open(PAGE_CACHE).then((c) => c.put(req, copy));
          }
          return res;
        })
        .catch(async () => (await caches.match(req)) || caches.match('/offline'))
    );
  }
});
