# TrackEdu - APK Installation & Verification Details

This directory contains the production-ready **Signed Release APK** and the **Debug APK** for TrackEdu.

---

## 1. APK Files Breakdown

| Filename | Type | Size | SHA-256 Checksum |
| :--- | :--- | :--- | :--- |
| **`TrackEdu-v1.0-release.apk`** | Signed Release (Production) | **15.19 MB** | `5E7F87B02BA76862E3A385EBCFABB6C1E9CC4F916B454A926D63E45DF38A26E4` |
| **`app-release.apk`** | Signed Release Mirror | **15.19 MB** | `5E7F87B02BA76862E3A385EBCFABB6C1E9CC4F916B454A926D63E45DF38A26E4` |
| **`TrackEdu-v1.0-debug.apk`** | Debug APK (Development) | **22.12 MB** | Standard Android debug certificate |
| **`app-debug.apk`** | Debug APK Mirror | **22.12 MB** | Standard Android debug certificate |

---

## 2. Release Signature Details

* **Signing Scheme**: APK Signature Scheme v2 (Verified)
* **Certificate DN**: `CN=TrackEdu, OU=Engineering, O=TrackEdu, L=Bengaluru, ST=Karnataka, C=IN`
* **Key Algorithm**: RSA 2048-bit
* **Certificate SHA-256 Digest**: `bded85ac8988fc6d30222847617cc3a6008df685a91ed3a34e41ed12f7894368`
* **Certificate Validity**: 10,000 days (~25 years)

---

## 3. Installation Guide

### Option A: Direct Device Installation (Recommended)
1. Transfer `TrackEdu-v1.0-release.apk` to your Android device via USB, Google Drive, WhatsApp, or email.
2. Open your device's **Files** or **Downloads** app.
3. Tap on `TrackEdu-v1.0-release.apk`.
4. If prompted with *"Install unknown apps"*, toggle **Allow from this source**.
5. Tap **Install** and open **TrackEdu**.

### Option B: Installation via ADB (Android Debug Bridge)
If your phone is connected to your PC with USB Debugging enabled:
```powershell
adb install -r "Deliverables\05_APK_File\TrackEdu-v1.0-release.apk"
```
Or for the debug APK:
```powershell
adb install -r "Deliverables\05_APK_File\TrackEdu-v1.0-debug.apk"
```

---

## 4. First-Time Setup Instructions
1. Launch TrackEdu on your Android phone or emulator.
2. On the initial launch, the app automatically detects that no teacher account exists and routes to **Setup Teacher Account**.
3. Create your teacher username and password.
4. Log in and begin managing attendance, rosters, and special requests!
