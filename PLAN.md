# KONSEP & SPESIFIKASI TEKNIS — SKYPRIVILEGE MOBILE KMP
# Android Tablet Station (Kotlin Multiplatform / Compose Multiplatform)

**Status:** ACTIVE SPECIFICATION (sinkron dengan Master Plan §2, §3.1–3.6, §4.5–4.6, §6 per 3 Oktober 2026)  
**Tanggal:** 4 Oktober 2026  
**Target Device:** Android 10+ (API 29+), HP staf 2 GB s/d tablet 4 GB, Dual-SIM wajib  
**Master Plan Referensi:** `/Users/ichigo/Documents/repo/helmi/sky_privilege/PLAN.md`  
**Aturan presedensi:** Jika dokumen ini bertentangan dengan Master Plan, Master Plan menang. Setiap perubahan Master §2 wajib diikuti patch di sini pada hari yang sama.

---

## 1. EXECUTIVE & ARSITEKTUR MOBILE

Aplikasi mobile SkyPrivilege adalah stasiun operasional kasir di counter baggage-wrapping bandara. Kasir hanya menjalankan dua aksi utama: **Clock-in Presensi** dan **Scan Tiket / Transaksi Diskon**.

### 1.1 Spesifikasi Hardware & Kebijakan Kiosk (Master §2.1)
| Aspek | Spesifikasi |
|---|---|
| OS | Android 10+ (API 29+), enrolled Android Enterprise Device Owner (COSU) |
| RAM / Layar | 2 GB (HP staf) s/d 4 GB (tablet 8–10 inci) |
| Kamera | Belakang 8 MP autofocus + flash (force ON saat capture); depan wajib (selfie/face login, fitur masa depan) |
| Jaringan | Dual-SIM Nano **wajib**; failover Wi-Fi → SIM 1 → SIM 2 (`androidMain/network/DualSimFailover.kt`); field `network_iface` ∈ `wifi \| cellular_sim1 \| cellular_sim2` dikirim di setiap request |
| Kiosk | Lock-Task Mode; Home/Recents off; gallery picker off; USB `charge-only` (`persist.sys.usb.config=charging`); Bluetooth hanya printer terdaftar; SIM tray terkunci; update via Managed Google Play privat; `KioskGuardService` verifikasi tiap 5 menit, pelanggaran → event `KIOSK_POLICY_VIOLATION` |
| Izin saat launch | `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `CAMERA` via `PermissionGatedApp`; ditolak → layar "Izin Operasional Wajib Diaktifkan", aplikasi terblokir |
| Manifest | `READ_EXTERNAL_STORAGE` / `READ_MEDIA_IMAGES` dilarang mutlak (Zero-Gallery); `android:usesCleartextTraffic="false"`; `network_security_config.xml` cleartext hanya `10.0.2.2` emulator |
| Transport | TLS 1.3 + certificate pinning di Ktor |

---

## 2. MODUL OPERASIONAL COUNTER

### 2.1 Modul 1: Presensi FSM 1 Tombol (Master §2.2)
Radio "Masuk/Pulang" dihapus. Satu tombol aksi dengan tiga status harian:

| Status hari ini | `type` | Label tombol | Hasil |
|---|---|---|---|
| 0 — Start `--:--` | `check_in` | "Catat Jam Masuk" | Isi Start Time, shift aktif |
| 1 — Start terisi, End `--:--` | `check_out` | "Catat Jam Pulang" | Dialog merah "Peringatan Akhiri Shift" → End Time terisi, status `SELESAI`, transaksi terkunci |
| 2 — Start & End terisi | `check_out` | "Perbarui Jam Pulang" | Ganti End Time dengan jam aktual |

- Endpoint: `POST /api/v1/attendances/record_time` (`type`, `latitude`, `longitude`, `accuracy`, timestamp, `network_iface`, telemetri Wi-Fi/Cell, flag mock-location). Server membuka/menutup record `shifts` atomik dalam satu transaksi.
- Roster: `POST /api/v1/shifts/open` memvalidasi `shift_schedules` aktif (30 menit sebelum s/d 60 menit setelah `start_time`); di luar jadwal atau outlet lain → HTTP 403 "Kasir tidak memiliki jadwal shift aktif di outlet ini".
- Pasca check-out: tombol SCAN memanggil `onAttemptScan()` → modal "Shift Hari Ini Telah Selesai"; backend menolak klaim (HTTP 422).
- Dialog `GpsAttendanceDialog.kt`: radar geofence, pill akurasi ("Akurasi: 15 meter"), jam real-time, IP perangkat, device ID, satu tombol aksi + Batal. Badge `GPS READY` dan `BELUM ABSEN` tidak ada.
- Koreksi: `POST /api/v1/attendance_corrections` (`target_date`, `correction_type` ∈ `check_in \| check_out \| both`, jam diajukan, `reason` wajib) → `pending` → portal.
- Status lain: `GET /api/v1/attendances/today`, `GET /api/v1/shifts/current`.
- Penilaian lokasi oleh backend `AntiFraud::MultiSignalScorer` (GPS 0,35 · Wi-Fi 0,25 · IP 0,15 · Cell 0,15 · Attestation 0,10). Tablet tidak menghitung skor dan tidak menyimpan ambang. Telemetri kosong = skor nol.
- **PIN.** 6 digit, diverifikasi server (bcrypt cost 12). Tablet tidak menyimpan hash PIN. `auth_method_opened` dikirim eksplisit `pin`. Jalur PIN tidak memicu notifikasi supervisor maupun penanda risiko.
- **Selfie, wajah, liveness: BELUM DIIMPLEMENTASIKAN (status 3 Okt 2026).** Tidak ada capture selfie di alur presensi. Fitur ini hanya boleh dibangun setelah DPIA `docs/compliance/DPIA_BIOMETRIK_KASIR.md` disetujui DPO, teks persetujuan versi-ber-hash ditandatangani di tablet (`consent_texts`, `signature_method = pin`), dan alternatif PIN tanpa konsekuensi disipliner tercantum di Peraturan Perusahaan. Nilai `face` hanya ditulis server oleh `POST /api/v1/face_logins`. Embedding wajah tidak pernah disimpan atau dikembalikan ke tablet.

### 2.2 Modul 2: Scan Tiket Strict-Online (Master §2.3–2.5)
Urutan wajib, tidak dapat dilewati:
1. **Gate checklist keaslian** `AuthenticityChecklistModal` dari cache SQLDelight (0 ms, offline). Kategori: `physical_print`, `digital_eticket`, `flight_date`, `barcode`. Tombol "Saya Konfirmasi Tiket Asli (Yes)" disabled 2 detik pertama.
2. Yes → kumpulkan telemetri (Wi-Fi SSID/BSSID/RSSI, Cell MCC/MNC/LAC/CID/RAT, GPS lat/lng/accuracy, `network_iface`, `hardware_uuid`, `shift_id`, `guideline_version`, signature ECDSA) → `POST /api/v1/acknowledgments` fire-and-forward; gagal → antre lokal `PENDING_ACK`. Server mengabaikan `client_ip` di body.
3. **Cold Viewfinder.** `CameraX.bindToLifecycle()` hanya setelah ack. Selama viewfinder aktif: tidak ada GPS, tidak ada HTTP. ML Kit analyzer `STRATEGY_KEEP_ONLY_LATEST`; `FLASH_MODE_ON` + `CAPTURE_MODE_MAXIMIZE_QUALITY` (flash wajib untuk deteksi moire/kertas asli di server).
4. Barcode terdeteksi (PDF417 / Aztec / QR) atau shutter manual → `cameraProvider.unbindAll()` seketika → single-shot GPS `fusedClient.getCurrentLocation` + Wi-Fi RSSI scan.
5. Pilih `bag_size` ∈ `S \| M \| L \| XL` (default `M`), `wrap_type` ∈ `standard \| premium \| bubble` (pilih satu, bukan add-on bertumpuk), `bag_count` > 0, `payment_method` ∈ `cash \| qris \| card`. Harga di layar hanya pratinjau dari katalog server (`SystemSetting wrap_prices`; seed S 60.000, M 75.000, L 90.000, XL 120.000; surcharge premium 15.000, bubble 10.000). Tablet tidak menghitung diskon dan tidak punya input nominal rupiah. Server: `gross = harga(bag_size, wrap_type) × bag_count`, `discount = rule.calculate_discount(gross)`, `net = gross − discount`.
6. Foto downscale 1 MP **di memori** (zero-disk, tidak pernah ke storage), terkompresi ≤ 250 KB, payload JSON < 5 KB, `idempotency_key` UUIDv7. Kirim `POST /api/v1/redemptions` (verifikasi PNR, dedup lintas outlet, harga, antrean injeksi Moka dalam satu transaksi server). Respons HTTP 201 `{redemption_id, claim_token, gross, discount, net}`; target round-trip < 1 detik.
   > Catatan kontrak: kode saat ini memanggil `POST /api/v1/tickets/verify` lalu `POST /api/v1/redemptions/claim`. Master §2.3 langkah 5 menetapkan satu request atomik. Konsolidasi endpoint diputuskan di Master; dokumen ini mengikuti.
7. **Strict online.** Timeout 8 detik. Offline / timeout / 5xx → modal blocking "Koneksi ke backend gagal. Coba lagi." Tidak ada approve lokal, tidak ada antrean transaksi. HTTP 409 → "Order ini sudah menggunakan diskon SkyPrivilege". HTTP 422 → pesan server (promo nonaktif / shift selesai).
8. Layar hasil menampilkan PII masked via `PiiMasker` (`B*** S******`, `AB***F`). Plaintext dibuang setelah scan. Respons server hanya memuat `masked_display_name`, `pnr_masked`, `ticket_photo_url` presigned ≤ 300 detik; tablet menolak field `passenger_name` plaintext dan `pnr_canonical_hash` penuh.
9. Injeksi diskon ke MOKA POS dilakukan **server** (`MokaOutboxWorker` → `DiscountInjector`). Tablet tidak memanggil API Moka dan tidak memasang diskon manual.
10. Sukses → struk terbit (printer Bluetooth terdaftar) → kasir stempel fisik "CLAIMED - SKYPRIVILEGE" tinta permanen pada boarding pass.
- `TicketCameraDialog` menginisialisasi `scannedBarcode = ""`; tombol konfirmasi disabled saat kosong. Tombol sampel GA410 hanya di build flavor `trial`.
- Fallback Vision AI (Qwen-VL / Cloud Vision) berjalan di server dari foto yang sudah dikirim; tablet tidak mengirim request terpisah.
- Void: `POST /api/v1/voids` (SLA 15 menit, OTP supervisor 3 menit, klip audio 5 detik). Emergency Offline Voucher: `POST /api/v1/emergency_vouchers` (PIN supervisor harian, plafon Rp 50.000, maks 20/outlet/hari) hanya saat Moka down, bukan saat backend down.

### 2.3 Modul 3: Ringkasan Shift Kasir (Slide 21)
- Menampilkan status shift aktif kasir:
  - Waktu buka shift (`opened_at`) dan kas modal awal (`opening_cash`).
  - Total transaksi klaim diskon yang berhasil dicatat (`total_redemptions_count`).
  - Total rupiah subsidi diskon yang telah diberikan (`total_discount_cents`).
  - Rekap perbandingan tunai fisik vs penjualan non-tunai sebelum tutup shift.
- Prosedur penutupan shift (`POST /api/v1/shifts/close`):
  - Kasir menghitung uang fisik di kasir (`closing_cash`).
  - Supervisor memverifikasi selisih (jika ada).
  - Struk laporan Z-Report dicetak.
- `POST /api/v1/shifts/close` memicu `Moka::Reconciler` menarik X/Z-Report → `shift_reconciliations`; tablet menampilkan hasil, bukan sumber kebenaran.
- `status` ∈ `open \| closed \| force_closed`; satu shift terbuka per kasir (constraint server).

### 2.4 UI Final: Bottom Bar 5 Slot (`MainAppScreen.kt`, Master §2.6)
| Slot | Tab | Isi |
|---|---|---|
| 1 | Home | Header "SkyPrivilege mobile" + dot koneksi + logout; "Selamat datang, [Nama Kasir]"; baris lokasi/shift cyan `#38BDF8` 12sp `FlatGpsPinIcon`; kartu Status Operasional; kartu promo full-bleed maskapai; riwayat 2–3 scan terakhir (PNR masked). Grid menu 8 ikon dihapus. |
| 2 | Transaksi | Metric chips; kartu riwayat (nomor penerbangan, masked PNR, jam WIB, nominal, pill `APPROVED` / `OVERRIDE`); pull-to-refresh `PullRefreshController`; empty state. |
| 3 | SCAN | Tombol 64dp, `offset(y = -18dp)`, royal blue, ikon kamera putih → alur §2.2. |
| 4 | Absen | Kartu GreatDay HR-style, shift terjadwal, Start/End Time + pin GPS, tombol FSM §2.1, "Ajukan Koreksi", tab Riwayat Absensi vs Riwayat Koreksi. |
| 5 | Akun Saya | Profil; `EditProfileDialog` → `PATCH /api/v1/profile`; `ChangePasswordDialog` → `POST /api/v1/profile/change_password` (`current_pin`, PIN baru); info perangkat (Hardware UUID, status whitelist). |

Ikon vektor flat via `Canvas` di `FlatIcons.kt`; tanpa emoji sistem. Tema navy `#102E5C`. `NetworkConnectivityManager` terikat lifecycle activity.

### 2.5 Guideline Cache Offline-First (Master §2.7)
SQLDelight `guideline_bundle` (`version`, `etag`, `fetched_at`, `payload_json`) dan `guideline_item` (`id`, `bundle_version`, `category`, `title`, `description`, `icon_key`, `display_order`). Fetch hanya saat boot pertama (kosong → blok masuk), open-shift, dan WorkManager 30 menit. `GET /api/v1/guidelines` dengan `If-None-Match` → `304 Not Modified` atau `200` + `ETag` + `X-Guideline-Version`. Modal checklist §2.2 langkah 1 selalu dirender dari cache.

---

## 3. RESILIENSI OFFLINE & KEAMANAN PERANGKAT

### 3.1 Antrean Lokal: Hanya Acknowledgment & Audit (Master §2.3.6)
- Transaksi redemption **tidak pernah** diantrekan offline. Tidak ada approve lokal.
- Yang boleh diantrekan di SQLDelight (SQLCipher AES-256): `authenticity_acknowledgments` (`PENDING_ACK`) dan event audit lokal (`PENDING_SYNC`). Setiap entri ditandatangani ECDSA + timestamp perangkat sehingga sinkronisasi tertunda tetap non-repudiable.
- Retry WorkManager exponential backoff hanya untuk dua jenis entri di atas.
- Idempotensi redemption memakai `idempotency_key` UUIDv7 per attempt yang gagal karena timeout, dikirim ulang hanya oleh kasir yang menekan "Coba lagi" saat masih online.
- SQLDelight hanya menyimpan `pnr_canonical_hash`, `masked_display_name`, `masked_pnr`, dan cache guideline. Tidak ada foto, nama, atau PNR plaintext di disk.

### 3.2 Dual-SIM & Jaringan Failover (Master §2.1)
- Aplikasi memantau konektivitas jaringan:
  - Sinyal Wi-Fi Counter Bandara (Utama).
  - Seluler SIM 1 Telkomsel (Failover 1).
  - Seluler SIM 2 Indosat/XL (Failover 2).
- Field `network_iface` ∈ `wifi \| cellular_sim1 \| cellular_sim2` disertakan pada setiap request (body telemetri dan header). Header `X-Network-Interface` tidak dipakai. Backend menilai `S_Cell` memakai nilai ini saat failover.

### 3.3 Zero-Trust Device, Attestation & Kill-Switch (Master §6.3)
- Enrollment di backoffice: Android ID, serial, public key ECDSA P-256 dari StrongBox Keystore (non-exportable) → `devices` dengan `outlet_id` hard-bound dan `fingerprint = SHA256(public_key)`.
- Handshake per request: header `X-Device-Id`, `X-Device-Signature` (ECDSA base64), `X-Timestamp`, `X-Nonce` atas string kanonikal `{HTTP_METHOD}\n{PATH}\n{X-Timestamp}\n{X-Nonce}\n{SHA256(body)}`. Server cek Δt < 60 detik, nonce sekali pakai 300 detik, signature vs `public_key_pem`.
- Penolakan: `403 device_not_authorized`, `403 device_signature_invalid`, `403 device_outlet_mismatch`. Kasir pada request harus sama dengan kasir shift `open` perangkat.
- Play Integrity dikirim saat open-shift dan acknowledgment (bobot `S_Attest` 0,10): root, emulator, APK tidak sah → skor nol.
- Kill-switch: Super Admin → `KillSwitchService.kill!` → Redis `device:kill` → FCM push → tablet hapus session, wipe cache lokal, kunci kiosk; blacklist efektif < 5 detik; request berikutnya 403. `Suspend` untuk servis sementara.
- Step-up OTP `POST /api/v1/auth/verify_otp` untuk void, override, ganti outlet binding, enrollment.

### 3.4 UU PDP No. 27/2022 di Tablet (Master §6.1)
- Zero-disk: foto tiket dan (kelak) selfie diproses in-memory, downscale, upload, buang. Tidak ada file lokal, tidak ada gallery.
- Zero plaintext: `PiiMasker` di semua render dan log; DTO tablet tidak memiliki field `passenger_name`; hanya `masked_display_name`, `pnr_masked`, `pnr_canonical_hash`.
- Tidak ada placeholder berbentuk nama orang di kode produksi maupun test; gunakan token non-nama (`PAX_A`, `PNR_SAMPLE_1`).
- Log, crash reporter, dan telemetri melewati `PiiMasker.scrub` (nama, PNR, NIK, alamat, telepon, IP, koordinat, BSSID/SSID); koordinat di log hanya `geohash6`.
- Duplikat lintas outlet ditampilkan sebagai "Outlet Lain (Bandara)" / "Staf Outlet Lain".
- Biometrik: lihat gate DPIA + consent di §2.1. Go-live dengan data penumpang nyata dilarang selama register Master §6.8 belum lengkap.
- Bukti elektronik (UU ITE Ps. 5–6, 11; KUHP 233): tablet tidak menyimpan bukti yang dapat diubah; semua bukti ditandatangani ECDSA dan dicatat di ledger server append-only.

---

## 4. STRUKTUR DIREKTORI KMP AKTUAL

```text
mobile/composeApp/src/
├── commonMain/kotlin/com/skyprivilege/
│   ├── data/remote/          # KtorClientFactory (timeout, header device, pinning) + dto/
│   ├── data/repository/      # Attendance, Guideline, Profile, Redemption, Shift, Ticket
│   ├── domain/model/         # BaggageWrap, Ticket, ShiftStatus, TicketGuideline, ...
│   ├── domain/repository/    # Interface repository
│   ├── ui/                   # MainAppScreen, GpsAttendanceDialog, TicketCameraDialog, AuthenticityChecklistDialog, ...
│   └── ui/components/        # FlatIcons, SkyPullRefreshBox
├── androidMain/kotlin/com/skyprivilege/
│   ├── camera/               # CameraXManager, BarcodeAnalyzer (ML Kit)
│   ├── location/             # FusedLocationProvider (single-shot)
│   ├── network/              # NetworkConnectivityManager → DualSimFailover
│   ├── security/             # KeystoreSignerProvider (ECDSA P-256)
│   └── wifi/                 # WifiSignalScanner
├── commonMain/sqldelight/    # GuidelineCacheQueries.sq, AckOutboxQueries.sq (direncanakan)
└── commonTest/kotlin/        # Unit + E2E tests
```
