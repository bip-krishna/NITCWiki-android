# P2 Task Plan — Android Build System & Flavors (Phase 1c)

> **Reference**: `ROADMAP.md` (Section 1c, Section 6, Section 4 file inventory #1–3)
> **Deliverable**: A working `nitcwiki` product flavor that builds cleanly, is isolated
> from NITCWiki/Wikimedia infrastructure, and coexists with upstream flavors.
> **Build target to verify**: `./gradlew assembleNitcwikiDebug`

---

## Overview of Changes

| Step | File | Action |
|------|------|--------|
| 1 | `app/build.gradle` | Add `nitcwiki` flavor block |
| 2 | `app/build.gradle` | Add `nitcwiki` to `sourceSets` extra sources |
| 3 | `app/build.gradle` | Add flavor-scoped Google deps for `nitcwiki` |
| 4 | `app/build.gradle` | Conditionally apply `google-services` plugin |
| 5 | `app/build.gradle` | Remove/nullify WMF-only `defaultConfig` BuildConfig fields |
| 6 | `app/src/nitcwiki/AndroidManifest.xml` | Create — set channel, strip Google Pay/Firebase entries |
| 7 | `app/src/nitcwiki/res/values/strings.xml` | Create — stub file (content filled by P3) |
| 8 | `app/src/nitcwiki/java/…/donate/GooglePayComponent.kt` | Copy stub from `fdroid/` |
| 9 | `app/src/nitcwiki/java/…/push/NITCWikiFirebaseMessagingService.kt` | Copy stub from `fdroid/` |
| 10 | `app/src/nitcwiki/java/…/mlkit/MlKitLanguageDetector.kt` | Copy stub from `fdroid/` |
| 11 | `app/src/nitcwiki/java/…/installreferrer/InstallReferrerListener.kt` | Copy stub from `fdroid/` |

---

## Step-by-Step Instructions

### Step 1 — Add the `nitcwiki` flavor block in `app/build.gradle`

Inside the `productFlavors { }` block (after the `custom { }` entry), add:

```groovy
nitcwiki {
    applicationId 'org.nitcwiki'
    versionName computeVersionName(defaultConfig.versionCode, 'nitcwiki')
    buildConfigField "boolean", "IS_NITC_WIKI", "true"
    buildConfigField "String",  "WIKI_BASE_URL",  '"https://wiki.fosscell.org"'
    buildConfigField "String",  "WIKI_API_URL",   '"https://wiki.fosscell.org/api.php"'
    buildConfigField "boolean", "HAS_RESTBASE",   "false"
    buildConfigField "boolean", "HAS_CORE_REST",  "false"
    buildConfigField "boolean", "HAS_COMMONS",    "false"
    buildConfigField "boolean", "HAS_WIKIDATA",   "false"
    buildConfigField "boolean", "HAS_EVENTGATE",  "false"
    // No signingConfig here yet — debug signing is fine until release setup
}
```

> **Note for agent**: Do NOT set `signingConfig` unless a `nitcwiki` signing key is
> already configured in `~/.sign/signing.properties`. Leave it absent for now.

---

### Step 2 — Add `nitcwiki` to the `sourceSets` extra sources loop

In `app/build.gradle`, find this line:

```groovy
['prod', 'beta', 'alpha', 'dev', 'custom'].forEach { name ->
```

Change it to:

```groovy
['prod', 'beta', 'alpha', 'dev', 'custom', 'nitcwiki'].forEach { name ->
```

> **Why**: The `extra/` source set contains the real Firebase, Google Pay, ML Kit, and
> InstallReferrer implementations. Adding `nitcwiki` here is what allows the flavor-
> specific stub files (Step 8–11) to override those real implementations. Without this,
> the build will include the real Google implementations even though we have no stubs.
>
> **Wait** — actually the opposite is what we want. `nitcwiki` should NOT be in this
> list, because being in the list pulls in `extra/` (the real Google code). The flavor-
> level stubs in `app/src/nitcwiki/java/` override `main/`, not `extra/`. So:
> **Leave `nitcwiki` OUT of the `sourceSets` loop.** The stub files in
> `app/src/nitcwiki/java/` will automatically take precedence for that flavor.

**Do nothing to the sourceSets block.**

---

### Step 3 — Add flavor-scoped dependency lines for `nitcwiki`

In `app/build.gradle`, find the block of flavor-prefixed dependency lines. They look like:

```groovy
prodImplementation libs.com.google.mlkit.language.id
betaImplementation libs.com.google.mlkit.language.id
// ... etc
```

**Decision per dependency:**

| Dependency | Decision | Reason |
|---|---|---|
| `com.google.mlkit.language.id` | **Omit** for `nitcwiki` | Stubbed out; no real implementation needed |
| `com.google.firebase.firebase.messaging.ktx3` | **Omit** for `nitcwiki` | Stubbed out |
| `com.google.android.gms.play.services.wallet2` | **Omit** for `nitcwiki` | Donations removed |
| `installreferrer` | **Omit** for `nitcwiki` | Stubbed out |

So: add **no** `nitcwikiImplementation` lines for any of those four dependencies.
The stubs in `app/src/nitcwiki/java/` provide the no-op implementations at compile time
without the real libraries being on the classpath.

---

### Step 4 — Conditionally apply the `google-services` Gradle plugin

**Problem**: The line `id 'com.google.gms.google-services'` in the `plugins {}` block
at the top of `app/build.gradle` runs unconditionally and demands a valid
`google-services.json` at build time — even for the `nitcwiki` flavor which doesn't use
Firebase. The `fdroid` flavor has the same problem and deals with it by keeping
`google-services.json` present (it's already in the repo at `app/google-services.json`).

**Resolution**: `google-services.json` already exists in the repo, so the plugin will
not crash the `nitcwiki` build even though Firebase is stubbed out in the flavor.
**No change needed here.** The plugin runs but Firebase is never initialised at runtime
because the stub `NITCWikiFirebaseMessagingService` is what gets compiled in.

---

### Step 5 — Remove/nullify WMF-only `defaultConfig` BuildConfig fields

In `app/build.gradle`, find the `defaultConfig { }` block. Remove these four lines:

```groovy
buildConfigField "String", "DEFAULT_RESTBASE_URI_FORMAT", '"%1$s://%2$s/api/rest_v1/"'
buildConfigField "String", "META_WIKI_BASE_URI", '"https://meta.wikimedia.org"'
buildConfigField "String", "EVENTGATE_ANALYTICS_EXTERNAL_BASE_URI", '"https://intake-analytics.wikimedia.org"'
buildConfigField "String", "EVENTGATE_LOGGING_EXTERNAL_BASE_URI", '"https://intake-logging.wikimedia.org"'
```

> **Important**: Before deleting, grep the codebase for each constant name to check if
> any source file references it directly via `BuildConfig.*`. If references exist,
> **coordinate with P1** before removing — they may need to guard those call sites with
> `if (BuildConfig.IS_NITC_WIKI)` first.
>
> Also note: the `dev` flavor re-declares `META_WIKI_BASE_URI` and `EVENTGATE_*` with
> beta lab URLs. Those overrides in the `dev { }` block should also be removed once the
> `defaultConfig` entries are gone, or they will cause a Gradle error referencing a
> field that no longer has a base declaration.

**Grep command to run before deleting:**
```bash
grep -r "DEFAULT_RESTBASE_URI_FORMAT\|META_WIKI_BASE_URI\|EVENTGATE_ANALYTICS\|EVENTGATE_LOGGING" \
  app/src/main/java --include="*.kt" --include="*.java" -l
```

If that returns any files, flag them to P1 before proceeding.

---

### Step 6 — Create `app/src/nitcwiki/AndroidManifest.xml`

Create the file with this content:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <application>

        <!-- Set the build channel metadata to "nitcwiki" -->
        <meta-data
            android:name="@string/preference_key_app_channel"
            android:value="nitcwiki"
            tools:replace="android:value" />

        <!-- Remove Google Pay wallet metadata — not used -->
        <meta-data
            android:name="com.google.android.gms.wallet.api.enabled"
            tools:node="remove" />

        <!-- Remove Firebase messaging service — stubbed out -->
        <service
            android:name=".push.NITCWikiFirebaseMessagingService"
            tools:node="remove" />

        <!-- Remove Google Pay donate activity — not applicable -->
        <activity
            android:name=".donate.GooglePayActivity"
            tools:node="remove" />

    </application>

</manifest>
```

---

### Step 7 — Create `app/src/nitcwiki/res/values/strings.xml`

Create a stub strings file. P3 will populate the actual values; this file just needs to
exist so P3 can add to it without creating it themselves:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Branding overrides for the nitcwiki flavor. Content owned by P3. -->
    <!-- P3: add app_name, search hints, login strings, account_name, etc. here -->
</resources>
```

---

### Steps 8–11 — Copy stub source files from `fdroid/` into `nitcwiki/`

The `fdroid/` flavor already has exactly the stubs needed. Copy them verbatim; only the
directory path changes.

**Create the directory:**
```bash
mkdir -p app/src/nitcwiki/java/org/wikipedia/donate
mkdir -p app/src/nitcwiki/java/org/wikipedia/push
mkdir -p app/src/nitcwiki/java/org/wikipedia/mlkit
mkdir -p app/src/nitcwiki/java/org/wikipedia/installreferrer
```

**Copy the four stub files:**
```bash
cp app/src/fdroid/java/org/wikipedia/donate/GooglePayComponent.kt \
   app/src/nitcwiki/java/org/wikipedia/donate/GooglePayComponent.kt

cp app/src/fdroid/java/org/wikipedia/push/NITCWikiFirebaseMessagingService.kt \
   app/src/nitcwiki/java/org/wikipedia/push/NITCWikiFirebaseMessagingService.kt

cp app/src/fdroid/java/org/wikipedia/mlkit/MlKitLanguageDetector.kt \
   app/src/nitcwiki/java/org/wikipedia/mlkit/MlKitLanguageDetector.kt

cp app/src/fdroid/java/org/wikipedia/installreferrer/InstallReferrerListener.kt \
   app/src/nitcwiki/java/org/wikipedia/installreferrer/InstallReferrerListener.kt
```

No edits needed inside those files — the package declarations and stubs are identical
to what `nitcwiki` needs.

---

## Verification

After all steps, run:

```bash
# Primary check — must succeed with no errors
./gradlew assembleNitcwikiDebug

# Secondary check — upstream flavors must still build cleanly
./gradlew assembleProdDebug
./gradlew assembleFdroidDebug
```

**Expected outcomes:**
- `assembleNitcwikiDebug` succeeds and produces an APK at
  `app/build/outputs/apk/nitcwiki/debug/`
- The APK has package name `org.nitcwiki` (verify via `aapt dump badging <apk> | grep package`)
- `assembleProdDebug` and `assembleFdroidDebug` are unaffected

---

## What Is NOT Your Responsibility

These are in scope for other team members. Do not touch:

| File | Owner | Why |
|---|---|---|
| `dataclient/WikiSite.kt` | P1 | API domain logic |
| `dataclient/Service.kt` | P1 | URL constants and MW_API_PREFIX |
| `dataclient/ServiceFactory.kt` | P1 | RESTBase path disabling |
| `dataclient/CoreRestService.kt` | P1 | REST prefix path |
| `app/src/main/AndroidManifest.xml` | P4 | Deep link hosts and URI scheme |
| `res/values/strings.xml` content | P3 | Actual branding text |
| App icons (`mipmap/`) | P3 | Icon assets |
| `NITCWikiApp.kt` | P3 | Class rename |

---

## Handoff Notes for Other Members

**→ P1**: Once you've removed `META_WIKI_BASE_URI` and `EVENTGATE_*` from `defaultConfig`,
tell P1 so they can remove any `BuildConfig.META_WIKI_BASE_URI` call sites in the
service layer.

**→ P3**: The file `app/src/nitcwiki/res/values/strings.xml` is created and waiting.
Add your branding strings there — do not edit `app/src/main/res/values/strings.xml`
directly (that keeps upstream diffs clean).

**→ P4**: The `nitcwiki/AndroidManifest.xml` created here only handles channel metadata
and Google service removal. The deep link host changes (`*.fosscell.org`) and URI scheme
(`nitcwiki://`) go in `app/src/main/AndroidManifest.xml` — that's your file.
