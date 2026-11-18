# NITC Wiki for Android

Browse, search, and edit the [NITC Wiki](https://wiki.fosscell.org) on Android. Save articles in reading lists, adjust the reading theme and text size, and use the article outline to jump between sections.

[Build locally](#build-locally) · [Roadmap](ROADMAP.md) · [Report an issue](https://github.com/Wiki-NITC/nitc-wiki-android/issues)

## Build locally

Install Android Studio with the Android SDK and JDK 17. From the repository root, run the same debug build task used in [CI](.github/workflows/nitcwiki_build.yml).

```bash
./gradlew assembleNitcwikiDebug
```

The debug APK is under `app/build/outputs/apk/`. For release packaging, see the [F-Droid submission guide](FDROID_SUBMISSION_GUIDE.md).

## Reading and editing

- Find articles with search suggestions and browse related pages through internal links.
- Save pages to reading lists for offline reading and return to recent articles from history.
- Choose Light, Dark, Black, or Sepia and adjust the reading font size.
- Sign in to edit wiki pages.

## Upstream

The app builds on Wikimedia’s [Wikipedia Android app](https://github.com/wikimedia/apps-android-wikipedia). See [COPYING](COPYING) for licensing.
