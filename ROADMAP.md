# Migration Roadmap: NITCWiki App → NITC Wiki App

This document outlines the plan to migrate the official NITCWiki Android app to target
**wiki.fosscell.org** (the NIT Calicut Wiki, "WIKI FOSSCELL NITC"). The goal is to
preserve upstream compatibility so that Wikimedia engineering fixes flow in with minimal
conflict while we maintain a fork that addresses NITC-specific needs.

---

## 1. Current State Assessment

### Source repo
- **Repo**: `apps-android-wikipedia` (official NITCWiki Android app)
- **Package**: `org.wikipedia`
- **App name**: NITCWiki
- **Target**: NITCWiki sites (`*.wikipedia.org`) plus Wikimedia infrastructure
  (Commons, Wikidata, Meta-Wiki, LiftWing, EventGate, RESTBase)

### Target wiki (wiki.fosscell.org)
| Property | Value |
|---|---|
| Sitename | WIKI FOSSCELL NITC |
| MediaWiki version | 1.45.3 |
| API endpoint | `https://wiki.fosscell.org/api.php` |
| Script path | `/` (root — **not** `/w/`) |
| Article path | `/$1` (e.g. `/Main_Page`) |
| REST API (`rest.php`) | **Not available** (404) |
| RESTBase (`/api/rest_v1`) | **Not available** |
| Wikimedia Commons | Not available |
| Wikidata | Not available |
| LiftWing ML | Not available |
| EventGate analytics | Not available |
| Language | English only (single-language wiki) |
| Cargo | Installed (v3.9.1) |
| Semantic MediaWiki | Installed (v7.0.0-alpha) |
| CirrusSearch | Installed (Elasticsearch) |
| DiscussionTools | Installed |

---

## 2. Architecture Strategy

### Guiding principles
1. **Keep upstream diff small** — rename things via build flavors / resource overlays
   rather than deep package renames, so Wikimedia's patches apply cleanly.
2. **Feature-gate, don't delete** — wrap WMF-only features behind flags so they can be
   re-enabled when the upstream repo is pulled.
3. **MW API first** — where RESTBase was used, prefer the equivalent `api.php` query.
   Only drop a feature when no MW API equivalent exists.
4. **No hard fork branding** — use build flavors (`custom` + resources) for naming so
   the core code stays close to upstream.

### Layer diagram
```
┌─────────────────────────────────────────────┐
│  UI (Compose / Views)                        │
│  ┌─────────────────────────────────────────┐ │
│  │  Feature flags (which features enabled)  │ │
│  ├──────────┬──────────┬───────────────────┤ │
│  │ Reading   │ Editing  │ Feed / Discovery  │ │
│  │ (WebView) │ (MW API) │ (MW API reimpl)   │ │
│  ├──────────┴──────────┴───────────────────┤ │
│  │  Service layer                           │ │
│  │  ┌──────────┐ ┌────────┐ ┌──────────┐   │ │
│  │  │ MW API   │ │CoreREST│ │ RESTBase │   │ │
│  │  │/api.php  │ │(stub)  │ │ (stub)   │   │ │
│  │  └──────────┘ └────────┘ └──────────┘   │ │
│  └─────────────────────────────────────────┘ │
│  WikiSite → wiki.fosscell.org                 │
└─────────────────────────────────────────────┘
```

---

## 3. Phased Migration Plan

### Phase 0: Reconnaissance & tooling (Week 1)
- [x] Register the wiki with the MediaWiki MCP server
- [ ] Map all `api.php` capabilities on the target wiki
- [ ] Confirm whether `rest.php` can be enabled on the server
- [ ] Audit the upstream git log to identify the last clean merge point
- [ ] Set up a `nitcwiki` branch strategy (e.g. `main` tracks upstream, `nitcwiki/main`
      contains our diff)

### Phase 1: Minimal viable app — articles work (Week 2)

#### 1a. WikiSite configuration
| File | Change | Reason |
|---|---|---|
| `WikiSite.kt` line 137 | `BASE_DOMAIN = "fosscell.org"` | The target domain |
| `WikiSite.kt` lines 49–54 | Remove English-subdomain special-casing | FOSSCELL is a single-domain wiki |
| `WikiSite.kt` lines 76–80 | Remove Commons language-code special-case | No Commons |
| `WikiSite.kt` line 83 | Remove BASE_DOMAIN subdomain logic | No `en.wikipedia.org` pattern |
| `WikiSite.kt` lines 126–133 | Simplify `dbName()` | No wikidata/commons |
| `WikiSite.kt` lines 148–154 | `forLanguageCode()` — always return base | Single-language wiki |
| `WikiSite.kt` lines 170–172 | `preview()` — use `wiki.fosscell.org` | Previews |
| `WikiSite.kt` lines 178–188 | `authorityToLanguageCode()` — always `""` | Single-language |

#### 1b. API endpoint paths
| File | Change | Reason |
|---|---|---|
| `Service.kt` line 765 | `WIKIPEDIA_URL → WIKI_URL = "https://wiki.fosscell.org/"` | Target URL |
| `Service.kt` line 766–770 | Remove `WIKIMEDIA_URL`, `WIKIDATA_URL`, `COMMONS_URL` | Not available |
| `Service.kt` line 772 | `MW_API_PREFIX = "api.php?format=json&formatversion=2&errorformat=html&errorsuselocal=1&"` | Script path is `/`, not `/w/` |
| `Service.kt` lines 778–783 | `isWikimediaAuthority()` — remove / narrow | Target domain only |
| `CoreRestService.kt` line 44 | `CORE_REST_API_PREFIX = "rest.php/"` (no `w/`) | Script path at root |
| `ServiceFactory.kt` lines 71–78 | `getRestBasePath()` — disable or point to MW API | No RESTBase available |

#### 1c. Build configuration
| File | Change | Reason |
|---|---|---|
| `app/build.gradle` line 39 | `applicationId 'org.nitcwiki'` | New identity |
| `app/build.gradle` line 51 | Remove `DEFAULT_RESTBASE_URI_FORMAT` | No RESTBase |
| `app/build.gradle` lines 52–54, 116–118 | Remove `META_WIKI_BASE_URI`, `EVENTGATE_*` | WMF-specific |
| `app/build.gradle` lines 224–249 | Review closed-source deps (Firebase, ML Kit, GPay) | May keep Firebase for push |
| Add a `nitcwiki` product flavor | Similar to `custom` flavor | Branded builds |

#### 1d. Deep links
| File | Change | Reason |
|---|---|---|
| `AndroidManifest.xml` lines 108–109 | Change `wikipedia` scheme → `nitcwiki` or `wiki-fosscell` | Custom scheme |
| `AndroidManifest.xml` line 155 | Change `*.wikipedia.org` → `*.fosscell.org` and `wiki.fosscell.org` | Target host |
| `AndroidManifest.xml` line 69 | App label → `@string/app_name_nitcwiki` | Branding |

#### 1e. Resource branding
| File | Changes needed |
|---|---|
| `strings.xml` | `app_name_prod` → "NITC Wiki"; update search hint, login strings, about text |
| `strings_no_translate.xml` | `account_name` → "NITC Wiki"; `account_type` → `org.nitcwiki` |
| App icon (`mipmap/`) | Replace with NITC/FOSSCell branding |
| `NITCWikiApp.kt` | Class rename → `NITCWikiApp` (or keep via `@Suppress`) |
| `NITCWikiFileProvider.kt` | Rename → `NITCWikiFileProvider` |
| `strings.xml` line 194 | `wp_stylized` → remove or adapt the "WIKIPEDIA" wordmark |

### Phase 2: Feature compatibility (Weeks 3–4)

#### 2a. Features that work with MW API only (with adaptation)

| Feature | MW API equivalent | Effort |
|---|---|---|
| **Page summaries** | `action=query&prop=extracts|pageimages` via `Service.kt` already | Low — uses MW API path |
| **Page content** | WebView loads `https://wiki.fosscell.org/{title}` directly | Low |
| **Search** | `action=opensearch` or `action=query&list=search` (CirrusSearch) | Low |
| **Random page** | `action=query&list=random` | Low |
| **Login / Auth** | `action=login` via `Service.kt` | Low — already uses MW API |
| **Edit** | `action=edit` via `Service.kt` | Low — already uses MW API |
| **Watchlist** | `action=query&list=watchlist` via `Service.kt` | Medium |
| **Talk pages** | `action=parse&page=Talk:{title}` — render in WebView | Medium |
| **Diff viewer** | `action=compare` via MW API, not CoreREST | Medium |
| **File page / gallery** | `action=query&prop=imageinfo` — local files only (no Commons) | Medium |
| **Notifications (Echo)** | `action=query&meta=notifications` + Firebase | Medium |
| **Category browsing** | `action=query&list=categorymembers` | Medium |

#### 2b. Features that need reimplementation

| Feature | Current approach (WMF) | NITC approach | Effort |
|---|---|---|---|
| **Explore Feed** | RESTBase `feed/featured/`, `feed/onthisday/`, `feed/announcements` | Build from MW API: `action=query&list=recentchanges`, random articles, on-this-day via Cargo/SMW | **High** |
| **On This Day** | RESTBase `feed/onthisday/events/{mm}/{dd}` | Build from Cargo or Semantic MediaWiki queries, or use MW API page history | **High** |
| **User contributions** | RESTBase `metrics/edits/...` | MW API `action=query&list=usercontribs` | Medium |
| **Reading list sync** | RESTBase `data/lists/...` | Local-only (Room DB) or custom Cargo-based sync | **High** |
| **Suggested edits** | RESTBase `growthexperiments/...` + LiftWing | Disable or reimplement via MW API | Medium |
| **Wiktionary definitions** | RESTBase `page/definition/` | Disable (no Wiktionary on FOSSCELL) | Low |
| **Activity Tab** | RESTBase metric queries | Use MW API usercontribs instead | Medium |

#### 2c. Features to disable (WMF-specific)

| Feature | Reason |
|---|---|
| Donations (Google Pay) | Not applicable to FOSSCELL — remove or stub |
| Year in Review | Depends on WMF-specific yearly stats data |
| Wikidata descriptions | No Wikidata instance |
| LiftWing ML suggestions | Not available |
| Event platform analytics | WMF-specific EventGate pipeline |
| Feed configuration (remote) | Remote config on Meta-Wiki |
| Reading challenge widgets | WMF-specific campaign data |
| Announements | RESTBase `feed/announcements` — WMF fundraising etc. |

#### 2d. Features to enhance (NITC-specific)

| Feature | Potential |
|---|---|
| **Cargo-powered browsing** | Use Cargo API to browse NITC-specific structured data |
| **Semantic MediaWiki queries** | Surface SMW property data in article cards |
| **Campus map integration** | Link to NITC Campus Map page or GeoJSON |
| **Event / Club / Hostel browsing** | Custom feed sections via Cargo API |
| **Blog / Magazine reading** | BlogPage extension content via MW API |

### Phase 3: Testing & polish (Week 5)

- [ ] Build with `./gradlew assembleNitcwikiDebug` succeeds
- [ ] Article reading (WebView loads wiki.fosscell.org pages)
- [ ] Search works (returns FOSSCELL results)
- [ ] Login and editing work
- [ ] Watch list works
- [ ] Deep links open the correct pages
- [ ] Offline reading (saved pages) works
- [ ] All NITCWiki references in UI are replaced
- [ ] Version bumps and release process documented

### Phase 4: Upstream sync strategy (Ongoing)

#### How to keep upstream changes flowing in

1. **Maintain 3 branches:**
   - `upstream/main` — tracks `https://github.com/wikimedia/apps-android-wikipedia.git`
   - `nitcwiki/main` — our fork's development branch
   - `nitcwiki/release` — tagged releases

2. **Merge cadence:**
   - Evaluate upstream changes weekly
   - `git merge upstream/main` into `nitcwiki/main`
   - Resolve conflicts (they should be minimal if we kept our changes focused)

3. **Conflict minimizers:**
   - Use resource overlays (`src/nitcwiki/res/`) for branding strings instead of
     editing `values/strings.xml` directly
   - Keep `WikiSite.kt` changes behind a build-config flag where possible
   - Add `@Suppress("unused")` on stubbed-out WMF features rather than deleting code
   - Prefer `if (BuildConfig.IS_NITC_WIKI)` checks over structural changes

4. **What NOT to change (leave as-is to reduce conflicts):**
   - Package name `org.wikipedia` in source files (use build config for app ID)
   - Class names in shared code (only rename truly conflicting files like
     `NITCWikiApp.kt`)
   - Core Retrofit service interfaces unless absolutely necessary
   - Compose component library (it's generic)

---

## 4. Detailed File Change Inventory

### Core configuration files

| # | File | Change type | Notes |
|---|---|---|---|
| 1 | `app/build.gradle` | Modify | `applicationId`, flavor, BuildConfig fields, deps |
| 2 | `settings.gradle.kts` | Review | Flavor include |
| 3 | `gradle.properties` | Maybe | Custom property for NITC release channel |

### Manifest files

| # | File | Change type | Notes |
|---|---|---|---|
| 4 | `app/src/main/AndroidManifest.xml` | Modify | Deep link hosts, app label, scheme |
| 5 | `app/src/nitcwiki/AndroidManifest.xml` | Create | Flavor-specific manifest (channel) |

### Data client (API layer)

| # | File | Change type | Notes |
|---|---|---|---|
| 6 | `dataclient/WikiSite.kt` | Rewrite key parts | `BASE_DOMAIN`, language logic, `dbName()` |
| 7 | `dataclient/Service.kt` | Modify | URL constants, `MW_API_PREFIX` |
| 8 | `dataclient/CoreRestService.kt` | Modify | `CORE_REST_API_PREFIX` |
| 9 | `dataclient/RestService.kt` | Stub/split | Many endpoints unavailable |
| 10 | `dataclient/ServiceFactory.kt` | Modify | RESTBase path logic |
| 11 | `dataclient/liftwing/LiftWingModelService.kt` | Disable | No LiftWing |

### App entry & branding

| # | File | Change type | Notes |
|---|---|---|---|
| 12 | `NITCWikiApp.kt` | Modify | User-agent string, class rename (optional) |
| 13 | `NITCWikiFileProvider.kt` | Modify | Class rename |
| 14 | `Constants.kt` | Modify | Commons/Wikidata constants |
| 15 | `settings/Prefs.kt` | Review | Preference keys if needed |

### Resources

| # | File | Change type | Notes |
|---|---|---|---|
| 16 | `res/values/strings.xml` | Modify | App name, branding strings |
| 17 | `res/values/strings_no_translate.xml` | Modify | App name, account info |
| 18 | `res/mipmap-*/` | Replace | App icon |
| 19 | `res/xml/network_security_config.xml` | Review | May need updates |

### Code (NITCWiki references)

| # | Pattern | Approx count | Action |
|---|---|---|---|
| 20 | `import org.wikipedia.NITCWikiApp` (in ~400 files) | 400+ | Keep `org.wikipedia` package; rename only if essential |
| 21 | `"en.wikipedia.org"` hardcoded URLs | ~10 files | Replace with configurable or `wiki.fosscell.org` |
| 22 | `wikipedia://` deep link scheme | 4 files | Replace with custom scheme |
| 23 | `.wikipedia.org` string refs | ~15 files | Replace with `.fosscell.org` |

---

## 5. Feature Compatibility Matrix

```
Feature                    Current (WMF)          NITC Wiki          Status
─────────────────────────────────────────────────────────────────────────────
Article reading (WebView)  MW API + RESTBase      MW API only         ✅ Works
Page summaries             RESTBase summary        MW API extracts     ✅ Works
Search                     CirrusSearch            CirrusSearch        ✅ Works
Random article             RESTBase random         MW API random       ✅ Works
Login / Auth               MW API                  MW API              ✅ Works
Editing                    MW API                  MW API              ✅ Works
Watchlist                  MW API                  MW API              ✅ Works
Talk pages                 RESTBase                MW API (WebView)    ⏳ Adapt
Diff viewer                CoreREST                MW API compare      ⏳ Adapt
Gallery / Files            Commons API             Local files only    ⏳ Adapt
Notifications              Echo + Firebase         Echo + Firebase     ⏳ Adapt
User contributions         RESTBase metrics        MW API contribs     ⏳ Adapt
Explore Feed               RESTBase feed           MW API-based        🔧 Rebuild
On This Day                RESTBase                Cargo/SMW           🔧 Rebuild
Reading list sync          RESTBase                Local-only          🔧 Rebuild
Suggested edits            RESTBase + LiftWing     Disable             ❌ Drop
Wiktionary definitions     RESTBase                Disable             ❌ Drop
Year in Review             WMF stats               Disable             ❌ Drop
Donations                  Google Pay              Remove              ❌ Drop
Announcements              RESTBase                Remove              ❌ Drop
Event analytics            EventGate               Remove              ❌ Drop
Reading challenge widgets  WMF campaigns           Remove              ❌ Drop

Key: ✅ Works | ⏳ Needs adaptation | 🔧 Needs rebuild | ❌ Drop
```

---

## 6. Recommended Build Flavor Strategy

Add a `nitcwiki` product flavor alongside existing ones:

```groovy
nitcwiki {
    applicationId 'org.nitcwiki'
    versionName computeVersionName(defaultConfig.versionCode, 'nitcwiki')
    signingConfig = signingConfigs.prod
    buildConfigField "boolean", "IS_NITC_WIKI", "true"
    buildConfigField "String", "WIKI_BASE_URL", '"https://wiki.fosscell.org"'
    buildConfigField "String", "WIKI_API_URL", '"https://wiki.fosscell.org/api.php"'
    buildConfigField "boolean", "HAS_RESTBASE", "false"
    buildConfigField "boolean", "HAS_CORE_REST", "false"
    buildConfigField "boolean", "HAS_COMMONS", "false"
    buildConfigField "boolean", "HAS_WIKIDATA", "false"
    buildConfigField "boolean", "HAS_EVENTGATE", "false"
}
```

This keeps the codebase unified while allowing compile-time feature gating.

---

## 7. Risks & Mitigations

| Risk | Mitigation |
|---|---|
| Upstream makes deep refactors of WikiSite/Service | Keep our changes minimal; rebase frequently |
| RESTBase dependency is too deep to extract cleanly | Accept some features being disabled; contribute upstream abstraction |
| Cargo/SMW queries don't map neatly to expected feed format | Build a simple MW API-based feed as a fallback |
| Single-language assumption breaks when upstream adds lang features | Use `IS_NITC_WIKI` flag to skip multi-lang UI |
| Server-side rest.php gets enabled later | Re-enable CoreREST path with the correct prefix |

---

## 8. Quick Start Checklist

```bash
# 1. Create nitcwiki flavor directory structure
mkdir -p app/src/nitcwiki/java/org/wikipedia
mkdir -p app/src/nitcwiki/res/values

# 2. Add resource overlays
# app/src/nitcwiki/res/values/strings.xml  → override app_name etc.

# 3. Create nitcwiki AndroidManifest.xml
# app/src/nitcwiki/AndroidManifest.xml     → channel metadata

# 4. Modify app/build.gradle
#    - Add nitcwiki flavor
#    - Add BuildConfig fields
#    - Update applicationId

# 5. Modify WikiSite.kt, Service.kt, ServiceFactory.kt

# 6. Replace mipmap icons with NITC branding

# 7. Build & test
./gradlew assembleNitcwikiDebug

# 8. Verify articles, search, login, editing on wiki.fosscell.org
```

---

## 9. Repository Setup

```
nitc-wiki-android/
├── app/
│   ├── src/
│   │   ├── main/              # Upstream-compatible shared code
│   │   ├── nitcwiki/          # NITC flavor overlays (resources, manifest)
│   │   ├── fdroid/            # Upstream
│   │   ├── dev/               # Upstream
│   │   ├── prod/              # Upstream
│   │   └── extra/             # Upstream (closed-source)
│   └── build.gradle
├── ROADMAP.md                 # This file
├── AGENTS.md                  # Updated for NITC Wiki context
└── CLAUDE.md                  # Project-specific agent instructions
```

---

## 10. Appendices

### A. wiki.fosscell.org API verification

To verify any API endpoint during migration, use:

```bash
# MW API query
curl "https://wiki.fosscell.org/api.php?action=query&meta=siteinfo&format=json"

# Search
curl "https://wiki.fosscell.org/api.php?action=query&list=search&srsearch=NITC&format=json"

# Page content
curl "https://wiki.fosscell.org/api.php?action=parse&page=Main_Page&format=json"

# OpenSearch
curl "https://wiki.fosscell.org/api.php?action=opensearch&search=NITC&format=json"
```

### B. Key extension capabilities on target wiki

- **Cargo** — structured data queries via `action=cargoquery`
- **Semantic MediaWiki** — semantic queries via `action=ask`
- **CirrusSearch** — Elasticsearch-backed full-text search via `action=query&list=search`
- **DiscussionTools** — enhanced talk pages (API via `action=discussiontools*`)
- **Page Forms** — form-based editing via `action=pfautoedit`
- **Echo** — notification system via `action=query&meta=notifications`

### C. Related reading

- [MediaWiki API sandbox on wiki.fosscell.org](https://wiki.fosscell.org/Special:ApiSandbox)
- [Upstream repo](https://github.com/wikimedia/apps-android-wikipedia)
- [MediaWiki RESTBase docs](https://www.mediawiki.org/wiki/RESTBase)
- [MediaWiki Core REST API docs](https://www.mediawiki.org/wiki/API:REST_API)
