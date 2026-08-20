# Publishing to Maven Central

Android Kit publishes from `mamby/android-kit` with the Maven group
`io.github.mamby.androidkit`. Signing in to Central Portal with the `mamby`
GitHub identity makes `io.github.mamby` eligible for automatic namespace
verification; the project-specific subgroup needs no separate namespace.

## One-time setup

1. Create a Central Portal account and verify the `io.github.mamby` namespace.
2. Generate a Central Portal user token.
3. Create a GPG key suitable for signing releases and retain a secure backup.
4. Add these GitHub Actions secrets:

   - `CENTRAL_TOKEN_USERNAME`
   - `CENTRAL_TOKEN_PASSWORD`
   - `MAVEN_SIGNING_KEY` — ASCII-armored private key
   - `MAVEN_SIGNING_PASSWORD`

The official requirements are documented by Sonatype under
[publishing requirements](https://central.sonatype.org/publish/requirements/)
and [namespace registration](https://central.sonatype.org/register/namespace/).

## Local staging inspection

Use a non-snapshot version and in-memory signing properties to stage all five
publications into `staging-repository`:

```powershell
.\gradlew.bat publishAllPublicationsToStagingRepository `
  -PVERSION_NAME=0.1.0 `
  -PsigningInMemoryKey="$env:MAVEN_SIGNING_KEY" `
  -PsigningInMemoryKeyPassword="$env:MAVEN_SIGNING_PASSWORD"
```

Inspect the generated Maven layout before release. Never reuse a published
version: Maven Central components are immutable.

## Release

Push an annotated semantic-version tag such as `v0.1.0`. The release workflow:

1. builds and stages every publication;
2. signs the AARs, POMs, Gradle metadata, sources and Javadoc artifacts;
3. generates checksums and a Maven-layout deployment bundle;
4. uploads it through the official Central Portal Publisher API using automatic
   publication; and
5. waits for the deployment to reach `PUBLISHED` or reports validation errors.

Maven Central does not require an icon, logo or screenshot. Android launcher
icons belong to the demo APK only and are deliberately absent from the AARs.
