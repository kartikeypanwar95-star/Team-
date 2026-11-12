# LUCIFER MUSIC

A starter Android music player with an AMOLED-black and purple interface.

## Included
- Kotlin + Jetpack Compose UI
- Local audio file picker (Android document picker)
- Playback using AndroidX Media3 ExoPlayer
- 10-band equalizer interface
- Device Equalizer integration where supported
- Direct audio URL playback for a personal server
- GitHub Actions debug APK build

## Build on your phone
1. Create a GitHub repository named `LUCIFER-MUSIC`.
2. Upload all files and folders from this project to the repository root.
3. Open the **Actions** tab.
4. Select **Build Android APK** and tap **Run workflow**.
5. When the run finishes, open it and download the `LUCIFER-MUSIC-debug-apk` artifact.
6. Extract the ZIP and install the APK. Android may ask you to allow installs from your browser or file manager.

## Important limitations of this starter version
- Personal server playback supports direct playable audio URLs. It does not yet include server accounts, browsing, or playlist synchronization.
- Android audio equalizer capabilities vary by device. The UI has ten frequency controls; Android's system Equalizer may map several controls to the same available hardware band.
- This is a first build. Test playback and EQ on your phone before relying on it as a daily player.
- Debug APK is for testing. A release build should be signed with a private signing key before distribution.

## Package
`com.lucifer.music`
