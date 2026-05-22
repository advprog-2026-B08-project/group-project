# 📦 JSON — Jastip Online Nasional

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.x-brightgreen)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/technologies/downloads/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)](https://www.postgresql.org/)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-Minikube-blueviolet)](https://kubernetes.io/)

JSON (Jastip Online Nasional) adalah aplikasi web berbasis Spring Boot untuk mengelola layanan Jasa Titip (Jastip) berskala nasional. Proyek ini dibangun untuk mendukung transaksi jual-beli barang titipan dari berbagai penjual (Jastiper) kepada pembeli (Titipers) secara aman, berkinerja tinggi, dan tahan terhadap kondisi beban transaksi tinggi (*concurrency/war*).

---

## 🔗 Tautan Deployment Publik

* **Production Deployment:** [json-prod.herokuapp.com](https://json-prod-0561d3767929.herokuapp.com/login)
* **Staging Deployment:** [json-staging.herokuapp.com](https://json-staging-cdf3ecf67eca.herokuapp.com/login)

---

## 🏗️ Arsitektur & Pola Desain (Design Patterns)

Aplikasi ini dirancang dengan prinsip pemrograman bersih dan struktur arsitektur modular:
1. **Hexagonal Architecture (Ports & Adapters):** Diterapkan khusus pada modul **Order** untuk memisahkan logika bisnis inti (*domain*) dari infrastruktur luar (Database, Web Controller, API Eksternal).
2. **Design Patterns yang Diterapkan:**
   * **Factory Pattern:** Untuk instansiasi berbagai jenis produk pada modul *Catalog*.
   * **Command Pattern:** Mengatur aksi pencarian dan penyaringan dinamis di modul *Catalog*.
   * **Builder Pattern:** Digunakan pada pembuatan objek mutasi `WalletTransaction` demi kode yang bersih (*clean code*) dan terstruktur.
3. **Pessimistic Locking & Concurrency Control:** Penanganan transaksi saldo dompet (*wallet*) dan pengurangan stok barang (*catalog*) menggunakan kombinasi *pessimistic write locking* di tingkat JPA Repository dan Query SQL Atomik untuk mencegah *double spending* dan *overselling* saat terjadi beban pembelian puncak (*war*).

---

## 💻 Panduan Menjalankan Aplikasi Secara Lokal

### Prasyarat:
* **Java Development Kit (JDK) 21**
* **Docker & Docker Desktop** (untuk PostgreSQL dan monitoring)
* **Minikube** (opsional, untuk simulasi Kubernetes HPA)
* **k6** (opsional, untuk load testing)

### Langkah-langkah:

1. **Clone Repositori:**
   ```bash
   git clone https://github.com/advprog-2026-B08-project/group-project.git
   cd group-project
   ```

2. **Setup Environment Variables:**
   Buat file `.env` di direktori utama proyek, lalu isi variabel berikut:
   ```env
   ADMIN_EMAIL=admin@gmail.com
   ADMIN_PASSWORD=admin
   JASTIPER_EMAIL=jastiper@gmail.com
   JASTIPER_PASSWORD=jastiper
   TITIPER_EMAIL=titiper@gmail.com
   TITIPER_PASSWORD=titiper
   CLOUDINARY_NAME=your_cloudinary_name
   CLOUDINARY_KEY=your_cloudinary_key
   CLOUDINARY_SECRET=your_cloudinary_secret
   ```

3. **Jalankan Aplikasi:**
   * **Windows:**
     ```powershell
     ./gradlew.bat bootRun
     ```
   * **Linux/macOS:**
     ```bash
     ./gradlew bootRun
     ```

Aplikasi dapat diakses di [http://localhost:8080](http://localhost:8080).

---

## 🧪 Panduan Menjalankan Pengujian (Testing)

### 1. Unit & Integration Testing
Untuk memverifikasi kebenaran logika bisnis aplikasi:
```bash
# Windows
./gradlew.bat test

# Linux/macOS
./gradlew test
```

### 2. Laporan Kualitas Kode & Coverage (JaCoCo)
Setelah pengujian selesai dijalankan, Anda dapat melihat laporan cakupan kode (*code coverage*) yang dihasilkan oleh JaCoCo di:
`build/reports/jacoco/test/html/index.html` (Buka file ini di peramban web/browser).

### 3. Linter (Checkstyle)
Untuk memverifikasi kesesuaian kode dengan standar gaya penulisan Java:
```bash
# Windows
./gradlew.bat checkstyleMain

# Linux/macOS
./gradlew checkstyleMain
```

---

## 📊 Setup Monitoring Lokal (Observability Stack)

Proyek ini telah dilengkapi dengan metrik observabilitas (Spring Actuator + Micrometer Prometheus) yang divisualisasikan melalui Prometheus dan Grafana.

1. **Jalankan Container Monitoring:**
   Pastikan Docker Desktop aktif, lalu jalankan:
   ```bash
   docker compose up -d
   ```
   Ini akan mengaktifkan container **Prometheus** dan **Grafana**.

2. **Akses Dashboard Grafana:**
   * Buka [http://localhost:3000](http://localhost:3000) di browser Anda.
   * Masuk menggunakan kredensial default: **Username:** `admin`, **Password:** `admin`.
   * Dashboard pemantauan JVM, Database HikariCP, dan HTTP Request sudah otomatis terkonfigurasi (*provisioned*).

---

## ☸️ Orkestrasi Kubernetes Lokal & Uji Beban (Minikube + k6)

Untuk mendemonstrasikan ketahanan sistem serta fitur **Horizontal Pod Autoscaling (HPA)** secara lokal:

1. **Aktifkan Minikube dan Metrik Server:**
   ```bash
   minikube start
   minikube addons enable metrics-server
   ```

2. **Buat Secret Lokal di Kubernetes:**
   Salin berkas template secret:
   ```bash
   cp k8s/secrets-local.yaml.template k8s/secrets-local.yaml
   ```
   Buka `k8s/secrets-local.yaml` dan masukkan nilai API Key Cloudinary Anda yang sudah di-encode ke Base64.
   Daftarkan secret ke Minikube:
   ```bash
   kubectl apply -f k8s/secrets-local.yaml
   ```

3. **Build dan Load Docker Image ke Minikube:**
   ```bash
   # Build jar aplikasi
   ./gradlew bootJar

   # Build Docker Image lokal
   docker build -t group-project-app:latest .

   # Load image ke Minikube daemon
   minikube image load group-project-app:latest
   ```

4. **Terapkan Manifest Kubernetes:**
   ```bash
   kubectl apply -f k8s/deployment-local.yaml
   kubectl apply -f k8s/hpa-local.yaml
   ```

5. **Lakukan Port-Forwarding:**
   ```bash
   kubectl port-forward deployment/json-app-local-deployment 8080:8080
   ```

6. **Jalankan Uji Beban (Stress Test) Menggunakan k6:**
   Buka terminal baru, lalu jalankan perintah pengujian untuk memicu autoscaling otomatis (replika pod akan bertambah dari 2 menjadi 3 pod secara dinamis):
   ```bash
   k6 run k6-stress-test.js
   ```
   Anda dapat memantau status pod secara *realtime* dengan perintah:
   ```bash
   kubectl get hpa -w
   ```