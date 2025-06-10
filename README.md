# Corevent Desktop Application

## 📋 Overview

Corevent Desktop adalah aplikasi manajemen acara kampus yang terintegrasi, dirancang untuk menyederhanakan seluruh siklus hidup pengelolaan acara dari persiapan hingga pasca-acara. Aplikasi ini dibangun menggunakan Java dengan framework Spring Boot dan JavaFX untuk antarmuka desktop yang modern dan responsif.

## 🚀 Tech Stack

### Core Technologies
- **Java 17** - Bahasa pemrograman utama
- **Spring Boot 3.2** - Framework aplikasi dan dependency injection
- **JavaFX 21** - Framework UI untuk aplikasi desktop
- **Hibernate/JPA** - Object-Relational Mapping

### Database
- **PostgreSQL** - Production database dengan sinkronisasi server

### Additional Libraries
- **Retrofit2** - REST API client
- **ZXing** - QR Code generation dan scanning
- **Apache PDFBox** - PDF generation untuk sertifikat
- **BCrypt** - Password encryption
- **JWT** - Token-based authentication

## 📁 Project Structure

```
corevent-desktop/
├── src/main/java/com/corevent/
│   ├── entity/               # Domain entities (Event, Participant, Ticket, dll)
│   ├── controller/           # Business logic controllers
│   ├── boundary/            # JavaFX UI controllers
│   ├── repository/          # Data access layer
│   ├── service/             # Service layer
│   ├── api/                 # REST API clients
│   ├── security/            # Security utilities
│   ├── config/              # Configuration classes
│   └── util/                # Utility classes
├── src/main/resources/
│   ├── fxml/                # FXML view files
│   ├── css/                 # Styling files
│   ├── images/              # Icons & images
│   └── application.properties
└── src/test/                # Unit & integration tests
```

## 🛠️ Setup & Installation

### Prerequisites
- Java JDK 17 atau lebih tinggi
- Maven 3.8+
- PostgreSQL (untuk production)

### Development Setup

1. Clone repository
```bash
git clone https://github.com/billysams21/IF2050-2025-K1L-Corevent.git
cd Corevent
```

2. Install dependencies
```bash
mvn clean install
```

3. Run application (development mode)
```bash
mvn spring-boot:run
```

atau

```bash
mvn javafx:run
```

### Building for Production

1. Build executable JAR
```bash
mvn clean package -Pproduction
```

2. Run JAR file
```bash
java -jar target/corevent-desktop-1.0.0-SNAPSHOT.jar
```

## 🔑 Key Features (MVP Phase)

### Authentication & Account Management
- ✅ Login dengan role Panitia dan Peserta
- ✅ Offline authentication support
- ✅ Session management
- ✅ Remember me functionality

### Core Features
- ✅ **Event Management (UC01, UC07)**
  - Create new events
  - Update event schedule
  - Manage event details
  
- ✅ **Participant Management (UC08)**
  - View participant list
  - Export participant data (CSV, Excel, PDF)
  - Real-time synchronization
  
- ✅ **Event Check-in (UC04)**
  - QR code scanning
  - Offline validation
  - Attendance tracking
  
- ✅ **Evaluation Results (UC05)**
  - View evaluation statistics
  - Export reports
  - Data visualization

## 🔐 Security Features

- **BCrypt** password hashing
- **AES-256** encryption untuk data sensitif
- **JWT** token-based authentication
- Offline mode dengan cached credentials

## 🌐 API Integration

Aplikasi desktop berkomunikasi dengan backend server melalui REST API:

- Base URL: `https://belum.ada`
- Authentication: Bearer token (JWT)
- Automatic retry dengan exponential backoff
- Offline mode fallback

## 📊 Database Schema

### Main Entities
- **Event** - Informasi acara
- **Participant** - Data peserta
- **Ticket** - Tiket dengan QR code
- **Payment** - Transaksi pembayaran
- **Attendance** - Kehadiran peserta
- **Evaluation** - Evaluasi acara
- **Question/Answer** - Q&A forum

## 🧪 Testing

Run unit tests:
```bash
mvn test
```

Run integration tests:
```bash
mvn verify
```

## 📝 Configuration

### application.properties
```properties
# Profile: development atau production
spring.profiles.active=development

# Database
spring.datasource.url=jdbc:h2:file:./data/corevent_dev
spring.jpa.hibernate.ddl-auto=update

# API Configuration
api.base-url=https://belum.ada
api.timeout=30

# Offline sync
offline.sync-interval=300000
```

## 📄 License

This project is proprietary software. All rights reserved.

## 👥 Team

- Project Owner: Livia
- Lead Developer: Billy
- UI/UX Designer: Dzulfaqor
- 
- 
- 