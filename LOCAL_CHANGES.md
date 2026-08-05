# Local Changes vs Upstream `Wiki-NITC/nitc-wiki-android`

A detailed inventory of every difference between this working copy and the upstream
[`Wiki-NITC/nitc-wiki-android`](https://github.com/Wiki-NITC/nitc-wiki-android) repository.

## 1. Comparison metadata

| Item | Value |
|---|---|
| Local branch | `main` (working tree clean) |
| Local remote | `origin` → `https://github.com/bip-krishna/NITCWiki-android.git` |
| Upstream ref compared | `Wiki-NITC/nitc-wiki-android` `main` |
| Upstream HEAD compared | `09e4b9d7b84a8c3cb321afd7857d876d3d5ee32f` |
| Merge base | `09e4b9d7b8` (upstream HEAD is an ancestor of local `HEAD`) |
| Local commits ahead of upstream | **4** |
| Files changed | **503** (491 modified + 12 renamed, 0 added, 0 deleted) |
| Insertions / deletions | +5,549 / −5,548 (near-zero net change) |

> The local repository is a fork that sits **exactly 4 commits on top of upstream main**.
> There are no commits in upstream that the local repo is missing, and no commits unique
> to the fork that added or removed any file.

## 2. The 4 local commits

| Commit | Message | Files touched | Net lines | Summary |
|---|---|---|---|---|
| `6f860567cd` | **Port for NITC** | 219 | +915/−915 | The bulk of the port: class renames, `AndroidManifest` deeplink/URI changes, main strings + `nitcwiki` flavor strings, flavor stubs, tests. |
| `a2cfa2531a` | **IDK** | 11 | +74/−74 | Completed the leftover `WikipediaTheme` → `NITCWikiTheme` import/usage rename in Compose components. |
| `50f8dbb1f2` | **Updates** | 1 | +41/−41 | `DonationReminderScreen.kt` — `WikipediaTheme` → `NITCWikiTheme` renames only. |
| `4ed8a572c5` | **Updates** | 292 | +4,522/−4,521 | Localized string resources (`values-*/strings.xml`) and Fastlane store metadata branding across all locales. |

## 3. File inventory by location

| Location | Files changed |
|---|---|
| `app/src/main/` | 406 |
| `fastlane/metadata/` | 62 |
| `app/src/androidTest/` | 12 |
| `app/src/test/` | 8 |
| `app/src/extra/` | 4 |
| `app/src/nitcwiki/` | 3 |
| `app/src/fdroid/` | 2 |
| `app/src/dev/` | 1 |
| Repository root `*.md` | 5 |
| **Total** | **503** |

By file type:

| Extension | Count |
|---|---|
| `.kt` (Kotlin) | 287 |
| `.xml` (resources/manifests) | 145 |
| `.txt` (Fastlane store metadata) | 62 |
| `.md` (docs) | 5 |
| `.json` (test fixtures) | 4 |

## 4. File renames (12) — `Wikipedia*` → `NITCWiki*`

| Old path | New path | Similarity |
|---|---|---|
| `app/src/main/java/org/wikipedia/WikipediaApp.kt` | `app/src/main/java/org/wikipedia/NITCWikiApp.kt` | 96% |
| `app/src/main/java/org/wikipedia/WikipediaFileProvider.kt` | `app/src/main/java/org/wikipedia/NITCWikiFileProvider.kt` | 84% |
| `app/src/main/java/org/wikipedia/compose/theme/WikipediaColor.kt` | `app/src/main/java/org/wikipedia/compose/theme/NITCWikiColor.kt` | 94% |
| `app/src/main/java/org/wikipedia/compose/theme/WikipediaTheme.kt` | `app/src/main/java/org/wikipedia/compose/theme/NITCWikiTheme.kt` | 68% |
| `app/src/main/java/org/wikipedia/compose/components/WikipediaAlertDialog.kt` | `app/src/main/java/org/wikipedia/compose/components/NITCWikiAlertDialog.kt` | 83% |
| `app/src/main/java/org/wikipedia/settings/languages/WikipediaLanguagesActivity.kt` | `app/src/main/java/org/wikipedia/settings/languages/NITCWikiLanguagesActivity.kt` | 73% |
| `app/src/main/java/org/wikipedia/settings/languages/WikipediaLanguagesFragment.kt` | `app/src/main/java/org/wikipedia/settings/languages/NITCWikiLanguagesFragment.kt` | 89% |
| `app/src/main/java/org/wikipedia/settings/languages/WikipediaLanguagesItemView.kt` | `app/src/main/java/org/wikipedia/settings/languages/NITCWikiLanguagesItemView.kt` | 98% |
| `app/src/extra/java/org/wikipedia/push/WikipediaFirebaseMessagingService.kt` | `app/src/extra/java/org/wikipedia/push/NITCWikiFirebaseMessagingService.kt` | 92% |
| `app/src/nitcwiki/java/org/wikipedia/push/WikipediaFirebaseMessagingService.kt` | `app/src/nitcwiki/java/org/wikipedia/push/NITCWikiFirebaseMessagingService.kt` | 90% |
| `app/src/fdroid/java/org/wikipedia/push/WikipediaFirebaseMessagingService.kt` | `app/src/fdroid/java/org/wikipedia/push/NITCWikiFirebaseMessagingService.kt` | 90% |
| `app/src/androidTest/java/org/wikipedia/base/TestWikipediaColors.kt` | `app/src/androidTest/java/org/wikipedia/base/TestNITCWikiColors.kt` | 99% |

## 5. AndroidManifest & deeplink changes

### `app/src/main/AndroidManifest.xml`
- Application class: `android:name=".WikipediaApp"` → `android:name=".NITCWikiApp"`.
- Deep-link URI scheme for both `activity-alias` blocks:
  - `android:scheme="wikipedia"` → `android:scheme="nitcwiki"`
  - `android:host="*.wikipedia.org"` → `android:host="*.fosscell.org"`
- Article `intent-filter` hosts for `/wiki/`, `/zh.*` and `/sr.*` path patterns:
  - `*.wikipedia.org` → `*.fosscell.org` (HTTP/HTTPS scheme unchanged).
- Language settings activity: `WikipediaLanguagesActivity` → `NITCWikiLanguagesActivity`.
- File provider: `WikipediaFileProvider` → `NITCWikiFileProvider` (authority `${applicationId}.fileprovider` unchanged).
- Firebase messaging service: `WikipediaFirebaseMessagingService` → `NITCWikiFirebaseMessagingService`.

### `app/src/fdroid/AndroidManifest.xml` and `app/src/nitcwiki/AndroidManifest.xml`
- Only the `tools:node="remove"` entry for the Firebase messaging service was renamed
  (`WikipediaFirebaseMessagingService` → `NITCWikiFirebaseMessagingService`).

## 6. Application class (`NITCWikiApp.kt`)

`NITCWikiApp` is byte-for-byte identical to the upstream `WikipediaApp` **except** for:

| Upstream | Local |
|---|---|
| `class WikipediaApp : Application()` | `class NITCWikiApp : Application()` |
| import `WikipediaFirebaseMessagingService` | import `NITCWikiFirebaseMessagingService` |
| `WikipediaFirebaseMessagingService.updateSubscription()` | `NITCWikiFirebaseMessagingService.updateSubscription()` |
| `WikipediaFirebaseMessagingService.unsubscribePushToken(...)` | `NITCWikiFirebaseMessagingService.unsubscribePushToken(...)` |
| User-Agent string `"WikipediaApp/%s (Android ...)"` | User-Agent string `"NITCWikiApp/%s (Android ...)"` |
| `lateinit var instance: WikipediaApp` | `lateinit var instance: NITCWikiApp` |

Package, build config, language state, tab handling, theme handling, install-ID, and all
other app logic are unchanged.

## 7. Kotlin source renames (287 `.kt` files)

Almost all Kotlin changes are **identifier swaps** propagated through imports and call sites:

- `org.wikipedia.WikipediaApp` → `org.wikipedia.NITCWikiApp` (e.g. `ServiceFactory.kt`,
  `WikiSite.kt`, `EventPlatformClient.kt`, `AccountUtil.kt`, `BaseActivity.kt`,
  `ActivityLifecycleHandler.kt`, `AppDatabase.kt`, `CoilImageServiceLoader.kt`, …)
- `org.wikipedia.compose.theme.WikipediaTheme` → `NITCWikiTheme`
- `org.wikipedia.compose.theme.WikipediaColor` / `LocalWikipediaColor` → `NITCWikiColor` / `LocalNITCWikiColor`
- `WikipediaAlertDialog` → `NITCWikiAlertDialog`
- `WikipediaLanguages{Activity,Fragment,ItemView}` → `NITCWiki…`
- `WikipediaFileProvider` → `NITCWikiFileProvider`
- `WikipediaFirebaseMessagingService` → `NITCWikiFirebaseMessagingService`
  (push, `NITCWikiApp` background worker, watchlist push token logic)

Theme-colour references inside Composables (`WikipediaTheme.colors.*` → `NITCWikiTheme.colors.*`)
appear across `compose/components/`, `activitytab/`, `feed/`, `page/`, `search/`, `donate/`,
`yearinreview/`, `readinglist/`, `watchlist/`, `talk/`, `settings/`, `widgets/` and more.

### Non-rename Kotlin edits (functional but cosmetic)

Only three files contain changes that are **not** pure name swaps:

| File | Change |
|---|---|
| `analytics/eventplatform/EventWithDt.kt` | Constructor re-formatted onto multiple lines; sealed-class layout changed, no behavior change. |
| `yearinreview/YearInReviewScreenDeck.kt` | `Box(modifier = Modifier.fillMaxSize())` → `Box(Modifier.fillMaxSize())`; local val `bitmap` renamed to `graphicsLayerBitmap`; theme colour renames. |
| `compose/theme/WikipediaTheme.kt` → `NITCWikiTheme.kt` | File renamed; internal code unchanged (theme provider, `BaseTheme`, `RippleConfiguration`). |

## 8. String resource changes (XML)

### `app/src/main/res/values/strings.xml` (base)
High-signal changes beyond the simple word swap:

| Key | Upstream | Local |
|---|---|---|
| `app_name_prod` | Wikipedia | **NITCWiki** |
| `app_name_beta` | Wikipedia Beta | **NITCWiki Beta** |
| `app_name_alpha` | Wikipedia Alpha | **NITCWiki Alpha** |
| `search_hint` | Search Wikipedia | Search NITCWiki |
| `wp_stylized` (logo) | `<big>W</big>IKIPEDI<big>A</big>` | `<big>N</big>ITCWIK<big>I</big>` |
| `about_wikipedia_url` | `https://en.wikipedia.org/wiki/Wikipedia:About` | `https://wiki.fosscell.org/wiki/NITC_Wiki:About` |
| `about_description` | About the Wikipedia app | About the NITCWiki app |
| `about_logo_content_description` | Wikipedia puzzle globe logo | NITCWiki puzzle globe logo |
| `preference_title_language` | Wikipedia languages | NITCWiki languages |
| `langlinks_your_wikipedia_languages` | Your Wikipedia languages | Your NITCWiki languages |
| `nav_item_login` | Log in to Wikipedia | Log in to NITCWiki |
| `login_join_wikipedia` | Join Wikipedia | Join NITCWiki |
| `wikipedia_app_faq` | Wikipedia App FAQ | NITCWiki App FAQ |
| `preference_summary_sync_reading_lists*` | …save them to your Wikipedia account | …your NITCWiki account |
| `account_vanish_request_confirm` | Account deletion on Wikipedia… | …on NITCWiki… |
| `edit_section_captcha_request_an_account_message` | `…/Wikipedia:Request_an_account` | `…/NITCWiki:Request_an_account` |
| `android_app_request_an_account_url` | `https://en.wikipedia.org/wiki/Wikipedia:Request_an_account` | `https://en.wikipedia.org/wiki/NITCWiki:Request_an_account` |
| `explore_feed_potd_subtitle` | Daily images on Wikimedia Commons… | Daily images on **NITC Wiki Commons**… |

All remaining entries in this file are the mechanical `Wikipedia` → `NITCWiki` substitution.

### Localized strings (`values-<locale>/strings.xml`, ~140 files)
Every locale was updated with the same `Wikipedia` → `NITCWiki` swap (app names, search
hint, year-in-review / donation / reading-stat copy, etc.). Translation-workshop strings
(`values-qq/strings.xml`) were also updated.

### `app/src/nitcwiki/res/values/strings.xml` (NITC flavor branding)
This file was rewritten wholesale (350 changed lines). Upstream branded the NITC flavor as
**“WikiNITC”**; the local repo rebrands it to **“NITCWiki”** everywhere:

- `app_name_*`: WikiNITC / WikiNITC Beta / WikiNITC Alpha → NITCWiki / NITCWiki Beta / NITCWiki Alpha
- `about_description`, `about_logo_content_description`, crash-report dialog, abuse-filter
  message, account-vanish text, activity-tab copy, language picker text, donation campaign,
  donation-reminder text, suggested-edits copy, edit success/revert copy — all
  “WikiNITC” → “NITCWiki”.
- The upstream comment `<!-- Branding overrides for the nitcwiki flavor. Replace Wikipedia → WikiNITC. -->`
  was simplified to `<!-- Branding overrides for the nitcwiki flavor. -->`.

### Other resources
- `app/src/main/res/values/strings_no_translate.xml`: multiple URL/string updates:
  - `donate_url`: `wmf_medium=WikipediaApp` → `wmf_medium=NITCWikiApp` (donation link still points to `donate.wikimedia.org`).
  - `survey_privacy_policy_url`: `…/Legal:Wikipedia_Android_App_Onboarding_Survey_Privacy_Statement` → `…/Legal:NITCWiki_Android_App_Onboarding_Survey_Privacy_Statement`.
  - `year_in_review_media_wiki_url` / `_faq_url` / `reading_list_learn_more`: `…/Wikipedia_Year_in_Review…` → `…/NITCWiki_Year_in_Review…` (these MediaWiki paths don’t exist upstream).
  - `year_in_review_hashtag`: `#WikipediaYearInReview` → `#NITCWikiYearInReview`.
  - `short_description_help_url_en`: `Wikipedia:Short_description` → `NITCWiki:Short_description`.
- `app/src/dev/res/values/strings_no_translate.xml`: app name `Wikipedia Dev` → `NITCWiki Dev`.
- `app/src/main/res/values/preference_keys.xml`: preference key value `aboutWikipediaApp` → `aboutNITCWikiApp`.

## 9. Flavor / build-variant changes

| File | Change |
|---|---|
| `app/src/nitcwiki/AndroidManifest.xml` | Firebase service name rename (`tools:node="remove"` entry). |
| `app/src/nitcwiki/java/org/wikipedia/push/NITCWikiFirebaseMessagingService.kt` | Renamed stub (from `WikipediaFirebaseMessagingService.kt`). |
| `app/src/fdroid/AndroidManifest.xml` | Firebase service name rename. |
| `app/src/fdroid/java/org/wikipedia/push/NITCWikiFirebaseMessagingService.kt` | Renamed stub. |
| `app/src/extra/java/org/wikipedia/push/NITCWikiFirebaseMessagingService.kt` | Renamed real Firebase service. |
| `app/src/extra/java/org/wikipedia/donate/GooglePayActivity.kt` | `WikipediaApp` → `NITCWikiApp` references. |
| `app/src/extra/java/org/wikipedia/donate/GooglePayViewModel.kt` | `WikipediaApp` → `NITCWikiApp` references. |
| `app/src/extra/java/org/wikipedia/installreferrer/InstallReferrerListener.kt` | `WikipediaApp` → `NITCWikiApp` references. |

> **No `build.gradle`, `gradle.properties`, `settings.gradle`, Gradle wrapper, icons, or
> package-name changes** were made. Application ID (`org.nitcwiki` flavor / `org.wikipedia`
> main) and the `org.wikipedia` Java package are identical to upstream.

## 10. Tests

| Area | Change |
|---|---|
| `app/src/test/` (8 files) | `WikipediaApp` → `NITCWikiApp` references in event-platform, language, and serialization tests. |
| `app/src/androidTest/` (12 files) | `TestWikipediaColors.kt` → `TestNITCWikiColors.kt` rename; robot constants/text updated e.g. `TODAY_ON_WIKIPEDIA_MAIN_PAGE = "Today on Wikipedia"` → `"Today on NITCWiki"`, `clickAboutWikipediaAppOptionItem()` → `clickAboutNITCWikiAppOptionItem()`. |
| Test fixtures (4 `.json`) | Branding strings inside `announce_2016_11_21.json`, `most_read.json`, `mostread_2016_11_07.json`, `notifications.json`. |

## 11. Fastlane store metadata (62 files)

Every locale under `fastlane/metadata/android/` had its `title.txt`, `short_description.txt`
and `full_description.txt` updated, replacing `Wikipedia` → `NITCWiki` (e.g.
`en-US/full_description.txt`: “The best Wikipedia experience…” → “The best NITCWiki
experience…”).

## 12. Documentation (5 `.md` files)

| File | Change |
|---|---|
| `README.md` | Branding; link label changed to “official NITCWiki Android app” (still pointing at the Wikimedia repo). |
| `AGENTS.md` | Branding across the whole guide; **introduced typo** — “understandable by a general audience” → “understandable by **a other** audience”. |
| `PLAN.md` | Update references to the renamed `NITCWikiFirebaseMessagingService.kt` / `NITCWikiApp.kt` files and branding text. |
| `ROADMAP.md` | Headings and target descriptions updated (“Wikipedia App → NITC Wiki App”). |
| `FDROID_SUBMISSION_GUIDE.md` | File path references to renamed Firebase stub. |

## 13. Files intentionally unchanged

- No Gradle/build files (`app/build.gradle`, root `build.gradle`, `settings.gradle`,
  `gradle.properties`, wrapper).
- No `google-services.json` change.
- No app launcher icons or brandable image assets.
- No Java/Kotlin **package** (namespace) changes.
- No dependency upgrades/additions.
- No files added or deleted anywhere.

## 14. Observations & possible follow-ups

1. **Typo in `AGENTS.md`**: “understandable by a other audience” (should be “a general
   audience” as upstream).
2. **Play Store links not updated**: `AGENTS.md` still links the beta/prod flavors to
   `org.wikipedia.beta` / `org.wikipedia`; F-Droid link still points at `org.wikipedia`.
3. **Mixed URLs**: `about_wikipedia_url` now points to `wiki.fosscell.org`, but
   `android_app_request_an_account_url` still resolves to `en.wikipedia.org`
   (`/wiki/NITCWiki:Request_an_account`), which likely doesn’t exist there.
4. **Fastlane copy still references Wikimedia**: store descriptions retain “made by the
   Wikimedia Foundation”, “Wikipedia articles”, and `m.wikimediafoundation.org` Terms of
   Use links (only the product name was swapped).
5. **No functional code changes**: the entire diff is branding, naming, and deep-link
   configuration; no feature logic, network endpoints (beyond deeplinks), or build
   configuration was altered.
