# Sadar Uang

**Sadar Uang** adalah aplikasi Android untuk membantu pengguna mencatat, mengelola, dan menganalisis pengeluaran pribadi serta memantau anggaran bulanan.

Aplikasi ini dibuat untuk memberikan cara yang lebih terstruktur dalam mengelola transaksi keuangan sehari-hari. Pengguna dapat mencatat pengeluaran, melihat riwayat transaksi, memantau penggunaan budget, melihat analisis pengeluaran, serta mengelola beberapa pengaturan aplikasi.

> **Catatan:** Screenshot pada repository ini ditempatkan di folder `docs/screenshots/`. Jika repository dipublikasikan, pastikan data akun, email, nama, atau informasi pribadi pada screenshot sudah disamarkan bila diperlukan.

---

## 1. Daftar Isi

- [Tentang Aplikasi](#2-tentang-aplikasi)
- [Tujuan Pengembangan](#3-tujuan-pengembangan)
- [Fitur Utama](#4-fitur-utama)
- [Teknologi yang Digunakan](#5-teknologi-yang-digunakan)
- [Arsitektur Aplikasi](#6-arsitektur-aplikasi)
- [Struktur Project](#7-struktur-project)
- [Alur Penggunaan](#8-alur-penggunaan)
- [Tampilan Aplikasi](#9-tampilan-aplikasi)
- [Penjelasan Setiap Screenshot](#10-penjelasan-setiap-screenshot)
- [Cara Menambahkan Screenshot ke GitHub](#11-cara-menambahkan-screenshot-ke-github)
- [Cara Menjalankan Project](#12-cara-menjalankan-project)
- [Contoh Penggunaan](#13-contoh-penggunaan)
- [Database dan Data](#14-database-dan-data)
- [Keamanan](#15-keamanan)
- [Pengembangan Selanjutnya](#16-pengembangan-selanjutnya)
- [Lisensi](#17-lisensi)

---

## 2. Tentang Aplikasi

Mengelola keuangan pribadi dapat menjadi sulit apabila transaksi dicatat secara manual atau tersebar di beberapa aplikasi. **Sadar Uang** dikembangkan sebagai aplikasi pencatatan keuangan pribadi yang menggabungkan pencatatan transaksi, pengelolaan budget, riwayat pengeluaran, dan analisis keuangan dalam satu aplikasi.

Aplikasi memiliki konsep antarmuka yang sederhana dengan tampilan transparan/translucent dan dukungan mode gelap. Informasi penting seperti total pengeluaran, jumlah transaksi, rata-rata pengeluaran, budget, dan sisa budget ditampilkan pada halaman utama sehingga pengguna dapat melihat kondisi keuangan dengan cepat.

---

## 3. Tujuan Pengembangan

Tujuan utama pengembangan Sadar Uang adalah:

1. Membantu pengguna mencatat transaksi pengeluaran secara terstruktur.
2. Mempermudah pengguna melihat riwayat pengeluaran.
3. Membantu pengguna menetapkan dan memantau budget bulanan.
4. Menyediakan ringkasan pengeluaran bulanan dan tahunan.
5. Menyediakan analisis pengeluaran berdasarkan kategori.
6. Membantu pengguna memahami pola pengeluaran melalui grafik.
7. Menyediakan penyimpanan bukti transaksi berupa gambar struk.
8. Menyediakan fitur ekspor dan impor data untuk kebutuhan backup atau pemindahan data.
9. Menyediakan pengaturan personalisasi aplikasi seperti wallpaper dan mode tampilan.

---

## 4. Fitur Utama

### 4.1 Autentikasi Pengguna

Aplikasi menyediakan halaman masuk yang memungkinkan pengguna:

- Login menggunakan email dan password.
- Menampilkan atau menyembunyikan password.
- Melanjutkan login menggunakan Google.
- Membuat akun baru melalui halaman registrasi.
- Menggunakan aplikasi tanpa login melalui mode offline.

### 4.2 Dashboard Keuangan

Dashboard menampilkan informasi utama seperti:

- Total pengeluaran tahun berjalan.
- Jumlah transaksi.
- Total pengeluaran bulan yang sedang dipilih.
- Persentase penggunaan budget.
- Nominal budget bulanan.
- Sisa budget.
- Jumlah transaksi bulan tersebut.
- Rata-rata pengeluaran.

Contoh data pada screenshot:

- Total tahun 2026: **Rp1.730.244**
- Total transaksi: **77**
- Total bulan Juli 2026: **Rp741.400**
- Budget: **Rp1.000.000**
- Progress budget: **74%**
- Sisa budget: **Rp258.600**

### 4.3 Pencatatan Pengeluaran

Pengguna dapat menambahkan transaksi melalui form **Tambah Pengeluaran**.

Data yang dapat dimasukkan meliputi:

- Keterangan transaksi.
- Kategori.
- Nominal.
- Tanggal transaksi.
- Bukti struk berupa gambar.

Contoh kategori:

- Makanan
- Investasi
- Transportasi
- Belanja
- Hiburan
- Lainnya

### 4.4 Riwayat Pengeluaran

Riwayat transaksi menampilkan:

- Nama/keterangan transaksi.
- Kategori.
- Tanggal transaksi.
- Nominal.
- Tombol hapus transaksi.

Contoh transaksi pada screenshot:

| Keterangan | Kategori | Tanggal | Nominal |
|---|---|---|---:|
| chunky chicks | Makanan | 28/07/2026 | Rp20.000 |
| permen milo | Makanan | 28/07/2026 | Rp13.900 |
| reksadana | Investasi | 17/07/2026 | Rp5.800 |
| cheat time | Makanan | 17/07/2026 | Rp29.000 |
| minum | Makanan | 17/07/2026 | Rp5.000 |
| maklor | Makanan | 17/07/2026 | Rp5.000 |

### 4.5 Analytics

Halaman analytics menyediakan visualisasi untuk membantu pengguna memahami pola pengeluaran.

Visualisasi yang tersedia pada implementasi saat ini antara lain:

- Diagram donat berdasarkan kategori.
- Grafik tren pengeluaran harian.

Contoh distribusi kategori pada screenshot:

- Makanan: 55
- Transportasi: 15
- Hiburan: 10
- Belanja: 10
- Lainnya: 9
- Investasi: ditampilkan pada grafik sesuai data transaksi.

### 4.6 Budget Bulanan

Pengguna dapat menentukan budget per bulan.

Pilihan budget cepat yang tersedia:

- Rp1.000.000
- Rp2.000.000
- Rp3.000.000
- Rp5.000.000
- Rp10.000.000

Tersedia juga opsi **Notifikasi Budget 80%** untuk memberikan pengingat ketika penggunaan budget mendekati batas yang ditentukan.

### 4.7 Notifikasi Budget

Aplikasi menyediakan pengaturan notifikasi budget.

Pada implementasi yang ditampilkan:

> Notifikasi akan muncul setiap minggu jika pengeluaran mendekati batas budget.

Pengguna dapat mengaktifkan atau menonaktifkan notifikasi mingguan.

### 4.8 Wallpaper / Personalisasi

Pengguna dapat mengganti wallpaper aplikasi.

Pilihan yang tersedia pada tampilan:

- Wallpaper gelap.
- Wallpaper terang.
- Upload gambar dari galeri.
- Reset wallpaper.

### 4.9 Manajemen Akun dan Data

Halaman akun menampilkan:

- Status akun.
- Nama pengguna.
- Email pengguna.
- Tombol keluar/ganti akun.
- Export data.
- Import data.

Fitur export/import dapat digunakan untuk melakukan backup atau memindahkan data ketika menggunakan mode offline.

### 4.10 Mode Gelap

Aplikasi mendukung tampilan gelap untuk memberikan alternatif tampilan yang lebih nyaman digunakan pada kondisi minim cahaya.

---

## 5. Teknologi yang Digunakan

| Teknologi | Penggunaan |
|---|---|
| Kotlin | Bahasa pemrograman utama |
| Jetpack Compose | Membangun antarmuka pengguna |
| Android Studio | IDE pengembangan |
| Room Database | Penyimpanan data lokal |
| Material Design | Komponen dan prinsip desain UI |
| Gradle | Build system dan manajemen dependency |

---

## 6. Arsitektur Aplikasi

Aplikasi menggunakan pemisahan komponen agar antarmuka, pengelolaan data, dan logika aplikasi tidak bercampur dalam satu bagian.

Secara umum:

```text
User
 │
 ▼
UI Layer
Jetpack Compose
 │
 ▼
Application Logic
 │
 ├── Expense Management
 ├── Budget Management
 ├── Analytics
 ├── Authentication
 └── Settings
 │
 ▼
Data Layer
 │
 └── Room Database
```

### UI Layer

Bertanggung jawab terhadap:

- Menampilkan dashboard.
- Menampilkan form transaksi.
- Menampilkan daftar transaksi.
- Menampilkan pengaturan.
- Menampilkan grafik.
- Menerima input pengguna.

### Application Logic

Bertanggung jawab terhadap:

- Perhitungan total pengeluaran.
- Perhitungan progress budget.
- Perhitungan sisa budget.
- Pengelompokan transaksi berdasarkan kategori.
- Pengolahan data untuk grafik.
- Validasi input.

### Data Layer

Bertanggung jawab terhadap:

- Penyimpanan transaksi.
- Pengambilan data transaksi.
- Penyimpanan pengaturan.
- Pengelolaan data lokal menggunakan Room Database.
- Export dan import data.

---

## 7. Struktur Project

Struktur utama project:

```text
sadar-uang/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── ...
│   │       ├── res/
│   │       │   └── ...
│   │       └── AndroidManifest.xml
│   │
│   ├── build.gradle.kts
│   └── proguard-rules.pro
│
├── docs/
│   └── screenshots/
│       ├── 01-dashboard.jpg
│       ├── 02-login-password-hidden.jpg
│       ├── 03-login-password-visible.jpg
│       ├── 04-expense-list.jpg
│       ├── 05-add-expense.jpg
│       ├── 06-analytics.jpg
│       ├── 07-settings-budget.jpg
│       ├── 08-settings-notification.jpg
│       ├── 09-settings-wallpaper.jpg
│       └── 10-settings-account-data.jpg
│
├── gradle/
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
└── README.md
```

> Folder `docs/screenshots/` digunakan khusus untuk dokumentasi tampilan aplikasi. Folder ini tidak harus berada di dalam `app/src/main/res/` karena screenshot README bukan resource aplikasi.

---

## 8. Alur Penggunaan

Alur dasar aplikasi:

```text
Buka Aplikasi
     │
     ▼
Halaman Login
     │
     ├── Login Email
     │
     ├── Login Google
     │
     └── Mode Offline
             │
             ▼
          Dashboard
             │
      ┌──────┼────────┐
      ▼      ▼        ▼
  Tambah   Analytics  Pengaturan
 Pengeluaran
      │
      ▼
Simpan Transaksi
      │
      ▼
Daftar Pengeluaran
      │
      ▼
Perhitungan Budget
      │
      ▼
Ringkasan & Grafik
```

---

# 9. Tampilan Aplikasi

## 9.1 Dashboard

Halaman utama menampilkan ringkasan kondisi keuangan pengguna.

![Dashboard Sadar Uang](docs/screenshots/01-dashboard.jpg)

**Keterangan:**

- Header aplikasi.
- Tombol Analytics.
- Tombol Excel.
- Tombol import.
- Pemilih bulan.
- Total pengeluaran tahunan.
- Total transaksi.
- Total pengeluaran bulan berjalan.
- Progress penggunaan budget.
- Sisa budget.
- Daftar pengeluaran.
- Tombol tambah transaksi.

---

## 9.2 Login dengan Password Tersembunyi

![Login password tersembunyi](docs/screenshots/02-login-password-hidden.jpg)

Tampilan login menyediakan input:

- Email.
- Password.
- Tombol tampil/sembunyikan password.
- Tombol masuk.
- Registrasi akun.
- Login menggunakan Google.
- Mode offline.

---

## 9.3 Login dengan Password Ditampilkan

![Login password ditampilkan](docs/screenshots/03-login-password-visible.jpg)

Ketika ikon mata ditekan, password dapat ditampilkan untuk membantu pengguna memastikan input password sudah benar.

---

## 9.4 Daftar Pengeluaran

![Daftar pengeluaran](docs/screenshots/04-expense-list.jpg)

Halaman ini menampilkan transaksi yang telah disimpan.

Setiap item menampilkan:

1. Ikon kategori.
2. Keterangan transaksi.
3. Kategori.
4. Tanggal.
5. Nominal.
6. Tombol hapus.

---

## 9.5 Tambah Pengeluaran

![Tambah pengeluaran](docs/screenshots/05-add-expense.jpg)

Form tambah pengeluaran terdiri dari:

1. **Keterangan** — nama transaksi.
2. **Kategori** — kategori pengeluaran.
3. **Nominal** — jumlah uang yang dikeluarkan.
4. **Tanggal** — tanggal transaksi.
5. **Upload Bukti Struk** — gambar bukti transaksi.
6. **Simpan Pengeluaran** — menyimpan transaksi.

---

## 9.6 Analytics

![Analytics](docs/screenshots/06-analytics.jpg)

Halaman analytics digunakan untuk melihat pola pengeluaran.

Terdapat:

- Diagram donat berdasarkan kategori.
- Grafik tren pengeluaran harian.
- Nilai pengeluaran pada masing-masing data.

---

## 9.7 Pengaturan Budget

![Pengaturan budget](docs/screenshots/07-settings-budget.jpg)

Pengguna dapat:

- Menentukan budget bulanan.
- Memilih nominal budget cepat.
- Mengaktifkan notifikasi budget 80%.
- Menyimpan konfigurasi budget.

---

## 9.8 Pengaturan Notifikasi

![Pengaturan notifikasi](docs/screenshots/08-settings-notification.jpg)

Pengguna dapat mengaktifkan **Notifikasi Budget** dan mengatur notifikasi mingguan untuk memantau penggunaan budget.

---

## 9.9 Pengaturan Wallpaper

![Pengaturan wallpaper](docs/screenshots/09-settings-wallpaper.jpg)

Fitur wallpaper menyediakan:

- Preview wallpaper.
- Pilihan wallpaper bawaan.
- Upload wallpaper dari galeri.
- Reset wallpaper.

---

## 9.10 Akun dan Manajemen Data

![Akun dan manajemen data](docs/screenshots/10-settings-account-data.jpg)

Halaman ini menyediakan:

- Informasi akun.
- Status mode aplikasi.
- Nama pengguna.
- Email.
- Keluar/ganti akun.
- Export data.
- Import data.

---

# 10. Penjelasan Setiap Screenshot

Urutan screenshot yang digunakan dalam README:

| No. | File | Tampilan | Fungsi |
|---:|---|---|---|
| 1 | `01-dashboard.jpg` | Dashboard | Ringkasan keuangan |
| 2 | `02-login-password-hidden.jpg` | Login | Password disembunyikan |
| 3 | `03-login-password-visible.jpg` | Login | Password ditampilkan |
| 4 | `04-expense-list.jpg` | Daftar Pengeluaran | Riwayat transaksi |
| 5 | `05-add-expense.jpg` | Tambah Pengeluaran | Input transaksi |
| 6 | `06-analytics.jpg` | Analytics | Grafik pengeluaran |
| 7 | `07-settings-budget.jpg` | Budget | Pengaturan budget |
| 8 | `08-settings-notification.jpg` | Notifikasi | Pengingat budget |
| 9 | `09-settings-wallpaper.jpg` | Wallpaper | Personalisasi tampilan |
| 10 | `10-settings-account-data.jpg` | Akun | Akun dan export/import |

---

# 11. Cara Menambahkan Screenshot ke GitHub

## Tahap 1 — Buat folder screenshot

Di folder utama project, buat:

```text
docs/
└── screenshots/
```

Sehingga struktur sederhananya menjadi:

```text
sadar-uang/
├── app/
├── gradle/
├── docs/
│   └── screenshots/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Tahap 2 — Masukkan screenshot

Masukkan screenshot ke:

```text
docs/screenshots/
```

Sebaiknya gunakan nama file sederhana tanpa spasi.

Contoh:

```text
01-dashboard.jpg
02-login-password-hidden.jpg
03-login-password-visible.jpg
04-expense-list.jpg
05-add-expense.jpg
06-analytics.jpg
07-settings-budget.jpg
08-settings-notification.jpg
09-settings-wallpaper.jpg
10-settings-account-data.jpg
```

Menggunakan nama seperti ini lebih aman daripada nama:

```text
Screenshot 2026-07-29 22.18.07.jpg
```

karena nama file dengan spasi dan karakter khusus lebih mudah menyebabkan kesalahan path.

## Tahap 3 — Upload ke repository

Jika menggunakan GitHub melalui browser:

1. Buka repository Sadar Uang.
2. Pilih **Add file**.
3. Pilih **Upload files**.
4. Masukkan folder `docs/screenshots` beserta semua screenshot.
5. Pastikan file berada di lokasi:

```text
docs/screenshots/nama-file.jpg
```

6. Commit perubahan.

## Tahap 4 — Hubungkan screenshot dengan README

Di `README.md`, gunakan format:

```markdown
![Dashboard Sadar Uang](docs/screenshots/01-dashboard.jpg)
```

GitHub akan membaca path tersebut sebagai gambar.

Contoh:

```markdown
## Dashboard

Halaman utama menampilkan ringkasan kondisi keuangan pengguna.

![Dashboard Sadar Uang](docs/screenshots/01-dashboard.jpg)
```

## Tahap 5 — Menambahkan keterangan di bawah gambar

Agar README lebih profesional, jangan hanya menampilkan gambar. Tambahkan penjelasan.

Contoh:

```markdown
![Tambah Pengeluaran](docs/screenshots/05-add-expense.jpg)

**Keterangan:** Form ini digunakan untuk mencatat keterangan,
kategori, nominal, tanggal, dan bukti transaksi.
```

Dengan cara tersebut pembaca GitHub dapat mengetahui fungsi setiap screenshot.

---

# 12. Cara Menjalankan Project

## Persyaratan

Sebelum menjalankan aplikasi, pastikan perangkat telah memiliki:

- Android Studio.
- Android SDK.
- JDK yang sesuai dengan versi project.
- Android Emulator atau perangkat Android fisik.
- Koneksi internet untuk proses pertama kali mengunduh dependency Gradle.

## Langkah menjalankan

1. Clone repository.

```bash
git clone <URL-REPOSITORY>
```

2. Buka project menggunakan Android Studio.

3. Tunggu proses **Gradle Sync** selesai.

4. Pastikan tidak terdapat error pada dependency.

5. Hubungkan smartphone Android melalui USB atau gunakan Android Emulator.

6. Aktifkan **USB Debugging** apabila menggunakan smartphone fisik.

7. Pilih device pada Android Studio.

8. Tekan **Run**.

9. Aplikasi akan di-install dan dijalankan pada perangkat.

---

# 13. Contoh Penggunaan

## Mencatat Pengeluaran

1. Buka aplikasi.
2. Login atau pilih mode offline.
3. Pada dashboard tekan tombol `+`.
4. Masukkan keterangan transaksi.
5. Pilih kategori.
6. Masukkan nominal.
7. Tentukan tanggal.
8. Jika diperlukan, upload bukti struk.
9. Tekan **Simpan Pengeluaran**.
10. Transaksi akan muncul pada daftar pengeluaran.
11. Nilai total bulan berjalan akan diperbarui.

## Mengatur Budget

1. Buka **Pengaturan**.
2. Pilih bagian budget.
3. Masukkan nominal budget bulanan.
4. Pilih nominal cepat jika diperlukan.
5. Aktifkan **Notifikasi Budget 80%**.
6. Tekan **Simpan Budget**.

## Melihat Analytics

1. Buka dashboard.
2. Tekan **Analytics**.
3. Lihat distribusi pengeluaran berdasarkan kategori.
4. Periksa tren pengeluaran harian.
5. Gunakan hasil analisis untuk mengetahui kategori pengeluaran terbesar.

---

# 14. Database dan Data

Aplikasi menggunakan **Room Database** sebagai penyimpanan data lokal.

Secara konseptual, data transaksi dapat terdiri dari informasi seperti:

```text
Expense
├── id
├── description
├── category
├── amount
├── date
└── receiptImage
```

Data tersebut digunakan untuk:

- Menampilkan daftar transaksi.
- Menghitung total pengeluaran.
- Menghitung jumlah transaksi.
- Menghitung rata-rata pengeluaran.
- Menghitung progress budget.
- Menghasilkan grafik analytics.

> Struktur entity sebenarnya dapat berbeda sesuai implementasi source code project.

---

# 15. Keamanan

Beberapa hal yang perlu diperhatikan sebelum project dipublikasikan:

- Jangan menyimpan password asli di source code.
- Jangan memasukkan API key atau secret key ke repository.
- Jangan memasukkan file konfigurasi rahasia ke GitHub.
- Gunakan `.gitignore` untuk file yang bersifat lokal atau rahasia.
- Gunakan password dummy untuk screenshot dokumentasi.
- Jika screenshot berisi email atau identitas pribadi, lakukan blur/samarkan terlebih dahulu.
- Untuk aplikasi produksi, data sensitif sebaiknya dilindungi menggunakan mekanisme keamanan Android yang sesuai.

---

# 16. Pengembangan Selanjutnya

Beberapa fitur yang dapat dikembangkan:

- Sinkronisasi data antarperangkat.
- Backup otomatis ke cloud.
- Notifikasi pengeluaran harian.
- Notifikasi ketika budget hampir habis.
- Grafik yang lebih interaktif.
- Filter transaksi berdasarkan kategori.
- Filter transaksi berdasarkan tanggal.
- Pencarian transaksi.
- Edit transaksi.
- Export PDF.
- Import Excel yang lebih lengkap.
- Dukungan multi-currency.
- Rekap pemasukan dan pengeluaran.
- Target tabungan.
- Laporan keuangan otomatis.
- Widget Android.
- Biometric login.
- Enkripsi database dan data sensitif.

---

# 17. Lisensi

Project ini dibuat sebagai project pengembangan aplikasi Android dan dapat digunakan sebagai dokumentasi portofolio.

Jika repository akan dipublikasikan secara umum, tambahkan lisensi yang sesuai dengan kebutuhan project, misalnya MIT License.

---

## Screenshot README yang Sudah Disiapkan

Semua screenshot dokumentasi pada project ini berada di:

```text
docs/screenshots/
```

dan sudah menggunakan nama file yang mudah dipanggil dari Markdown.

Struktur akhirnya:

```text
docs/
└── screenshots/
    ├── 01-dashboard.jpg
    ├── 02-login-password-hidden.jpg
    ├── 03-login-password-visible.jpg
    ├── 04-expense-list.jpg
    ├── 05-add-expense.jpg
    ├── 06-analytics.jpg
    ├── 07-settings-budget.jpg
    ├── 08-settings-notification.jpg
    ├── 09-settings-wallpaper.jpg
    └── 10-settings-account-data.jpg
```

**Cara paling mudah:** salin folder `docs` dan file `README.md` dari paket ini ke folder utama project Sadar Uang. Setelah itu upload/commit keduanya ke GitHub.
