/**
 * Bitopi MDM – QR Provisioning Code Generator
 *
 * Generates a Device Owner provisioning QR code for Android 7.0+.
 *
 * Usage:
 *   node generate-qr.js
 *   node generate-qr.js --apk-url https://your-server.com/bitopi-mdm.apk
 *
 * The device must be factory-fresh (just wiped).
 * During Android setup wizard, tap the screen 6 times on the "Welcome" page
 * to enter QR provisioning mode, then scan this QR code.
 */

const QRCode = require('qrcode');
const path   = require('path');
const args   = process.argv.slice(2);

// ─── Configuration ─────────────────────────────────────────────────────────

const COMPONENT_NAME = 'com.bitopi.mdm/.DeviceAdminReceiver';

// SHA-256 of the signing certificate encoded as Base64url (no padding).
// Extracted from app/release/app-release.apk via apksigner.
const SIGNATURE_CHECKSUM = '-JOgn4LulmhMgyXG-J061aymAzUkdQk5ucPslGGWp34';

// Optional: URL where the release APK is hosted (required if the app is not
// pre-installed on the device before scanning the QR).
// Pass via --apk-url <url> or set the env var APK_DOWNLOAD_URL.
let apkUrl = process.env.APK_DOWNLOAD_URL || null;
const urlFlag = args.indexOf('--apk-url');
if (urlFlag !== -1 && args[urlFlag + 1]) {
  apkUrl = args[urlFlag + 1];
}

// ─── Build Payload ──────────────────────────────────────────────────────────

const payload = {
  'android.app.extra.PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME': COMPONENT_NAME,
  'android.app.extra.PROVISIONING_DEVICE_ADMIN_SIGNATURE_CHECKSUM': SIGNATURE_CHECKSUM,
  'android.app.extra.PROVISIONING_SKIP_ENCRYPTION': false,
  'android.app.extra.PROVISIONING_LEAVE_ALL_SYSTEM_APPS_ENABLED': false,
};

if (apkUrl) {
  payload['android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION'] = apkUrl;
  console.log(`APK download URL: ${apkUrl}`);
} else {
  console.log('⚠  No --apk-url provided.');
  console.log('   The app must already be sideloaded on the device before scanning,');
  console.log('   OR pass --apk-url https://your-server/bitopi-mdm.apk');
}

const jsonPayload = JSON.stringify(payload);
console.log('\nQR payload:\n', JSON.stringify(payload, null, 2));

// ─── Generate QR ────────────────────────────────────────────────────────────

const outputFile = path.join(__dirname, 'mdm-provisioning-qr.png');

QRCode.toFile(outputFile, jsonPayload, { errorCorrectionLevel: 'M', width: 512 }, (err) => {
  if (err) {
    console.error('QR generation failed:', err);
    process.exit(1);
  }
  console.log(`\nQR code saved → ${outputFile}`);
  console.log('\nProvisioning steps:');
  console.log('  1. Factory reset the target device (or use -wipe-data in emulator).');
  console.log('  2. On the "Welcome" / language screen, tap 6 times quickly.');
  console.log('  3. When prompted, scan this QR code.');
  console.log('  4. Android will install the APK and set it as Device Owner.');
});
