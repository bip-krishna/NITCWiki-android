# F-Droid submission guide for this fork

This document is a practical guide for preparing the current Android app fork for inclusion in F-Droid. It is based on the upstream F-Droid documentation and on the structure already present in this repository.

## 1. What F-Droid expects

Before submitting an app, confirm that it is compatible with the F-Droid inclusion policy:

1. The app must have a public source repository and a clear free/open-source license.
2. The app should be buildable from source without proprietary toolchains or non-free dependencies. If the app can work without Google services or other proprietary components, F-Droid generally expects a separate build flavor or version that removes them.
3. The app author should be notified and should not object to inclusion.
4. The app should ship with proper metadata files such as a short description, full description, screenshots, and release notes.

## 2. What is already present in this repository

This repository already has several pieces that are helpful for F-Droid packaging:

- A dedicated F-Droid build flavor exists in [app/build.gradle](app/build.gradle).
- The F-Droid flavor removes Google Pay and Firebase push integration in [app/src/fdroid/AndroidManifest.xml](app/src/fdroid/AndroidManifest.xml).
- A stub Firebase messaging service exists for the F-Droid build in [app/src/fdroid/java/org/wikipedia/push/NITCWikiFirebaseMessagingService.kt](app/src/fdroid/java/org/wikipedia/push/NITCWikiFirebaseMessagingService.kt).
- Basic Fastlane metadata already exists under [fastlane/metadata/android/en-US](fastlane/metadata/android/en-US).
- A separate custom flavor for the college wiki fork exists in [app/build.gradle](app/build.gradle), with the package name set to `org.nitcwiki`.

These are strong starting points, but they are not automatically sufficient for a real F-Droid submission.

## 3. The most important nuances for this app

### 3.1 The app identity must be explicit

The current default application ID is still `org.wikipedia` in [app/build.gradle](app/build.gradle), while the repository also includes a custom `nitcwiki` flavor that uses `org.nitcwiki`.

For a proper F-Droid submission, the package identity should be deliberate and stable:

- If the F-Droid build is meant to represent the college wiki fork, the F-Droid build should use a unique package name such as `org.nitcwiki` or a domain-based identifier.
- The F-Droid metadata file name should match the final application ID.

This is one of the biggest packaging nuances for this fork.

### 3.2 The versioning should be stable

The current build logic in [app/build.gradle](app/build.gradle) computes the version name dynamically using the current date. That is convenient for local development, but it is not ideal for F-Droid:

- F-Droid relies on version information that is stable and explicit.
- The version name and version code should be easy to parse from source and should not change arbitrarily on each build.
- For F-Droid metadata, the preferred pattern is a conventional version name such as `1.2.3` plus a monotonic version code.

### 3.3 The F-Droid flavor is already partially isolated

The F-Droid flavor already removes some Google-specific components, which is good for compliance. However, the repository still contains:

- Google services configuration in [app/google-services.json](app/google-services.json)
- Google services plugin usage in [app/build.gradle](app/build.gradle)
- Google Play / Firebase-related dependencies in the non-F-Droid flavors

That means the F-Droid build should be verified carefully to ensure it does not accidentally inherit proprietary behavior or non-free dependencies.

### 3.4 Release tags are required by F-Droid

F-Droid expects a source tag for each official release. A common convention is a tag like `v1.2.3` for a release whose version name is `1.2.3`.

This repository should adopt a predictable tagging policy before submission.

## 4. What needs to be implemented before submission

### A. Decide the F-Droid app identity

Choose the package name that will be used for the F-Droid build and make sure it is unique and consistent.

Suggested decision points:

- Use the existing `nitcwiki` flavor identity if the F-Droid app is meant to represent the college wiki fork.
- If the app should be published as a separate app, use a unique domain-based package name.

### B. Make the F-Droid build use stable version information

Replace the dynamic version naming approach with explicit values for the F-Droid build, or otherwise make the version values easy for F-Droid to extract.

### C. Review the dependency surface

Even though the F-Droid flavor is partially isolated, every dependency should be reviewed for compliance:

- Google Play Services
- Firebase / messaging
- Google Pay / wallet integration
- Any analytics or crash-reporting libraries

If any of these are not essential for the F-Droid build, they should be removed from that build path.

### D. Prepare app metadata

The repository already contains basic Fastlane metadata in [fastlane/metadata/android/en-US](fastlane/metadata/android/en-US), but it should be reviewed and expanded:

- Short description under 80 characters, no trailing period
- Full description with the app purpose and relevant details
- App icon
- Screenshots
- Changelog entry for each release

### E. Add release tags and changelog entries

For each release you want F-Droid to track:

- Create a Git tag such as `v1.2.3`
- Add a changelog file under the Fastlane metadata folder for the corresponding version code

### F. Prepare F-Droid build metadata

F-Droid uses a metadata file in the `fdroiddata` repository, usually named after the app package ID, such as `metadata/org.nitcwiki.yml`.

The metadata should include:

- Source repository details
- License information
- Categories and description fields
- A build block for the F-Droid flavor
- Version and update information

## 5. Suggested build metadata structure

A minimal F-Droid metadata file will usually follow this outline:

```yaml
RepoType: git
Repo: https://github.com/<owner>/<repo>.git

Builds:
  - versionName: 1.2.3
    versionCode: 10203
    commit: <release-tag-or-commit>
    subdir: app
    gradle:
      - fdroidRelease
```

The exact build block should be adapted to the actual Gradle task names and output paths of this project.

## 6. Validation checklist

Before submitting, verify the following:

- The source repository is public and reachable.
- The license is clearly visible.
- The F-Droid build can be produced from source.
- The app can build without proprietary dependencies in the F-Droid flavor.
- The package name is unique and stable.
- The version information is explicit and easy for F-Droid to parse.
- Tags exist for releases.
- Metadata and screenshots are present.
- The build metadata file is valid and passes F-Droid linting.

## 7. Recommended order of work

1. Decide final app identity and package name.
2. Make the F-Droid flavor use stable, explicit versioning.
3. Confirm that the F-Droid build does not depend on non-free services.
4. Review and expand Fastlane metadata.
5. Add release tags and changelog entries.
6. Create the F-Droid metadata file for the app.
7. Test the metadata locally or in CI.
8. Submit the app to the F-Droid submission queue.

## 8. Bottom line

The repository already has a useful starting point for F-Droid inclusion, especially because it includes a dedicated F-Droid flavor and some manifest-level removal of Google-specific components. The main work left is to make the packaging identity, versioning, metadata, and release process fully consistent with F-Droid’s expectations.
