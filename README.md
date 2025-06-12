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
- **H2** - Development database (file-based)
- **PostgreSQL** - Production database dengan sinkronisasi server

### Additional Libraries
- **Retrofit2** - REST API client
- **ZXing** - QR Code generation dan scanning
- **Apache PDFBox** - PDF generation untuk sertifikat
- **BCrypt** - Password encryption
- **JWT** - Token-based authentication
- **Flyway** - Database migration tool

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
│   ├── db/migration/        # Database migration scripts
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

### Database Access

#### H2 Console (Development)
1. Start the application
2. Open browser and navigate to: `http://localhost:8888/h2-console`
3. Use these credentials:
   - JDBC URL: `jdbc:h2:file:./corevent_db`
   - Username: `sa`
   - Password: `password`

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

### API Endpoints

#### Events
- `GET /api/events` - Get all events
- `GET /api/events/{id}` - Get event by ID
- `POST /api/events` - Create new event
- `PUT /api/events/{id}` - Update event
- `DELETE /api/events/{id}` - Delete event

#### Participants
- `GET /api/events/{id}/participants` - Get event participants
- `POST /api/events/{id}/participants` - Add participant
- `DELETE /api/events/{id}/participants/{participantId}` - Remove participant

#### Attendance
- `POST /api/events/{id}/check-in` - Check-in participant
- `GET /api/events/{id}/attendance` - Get attendance list

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
spring.datasource.url=jdbc:h2:file:./corevent_db
spring.jpa.hibernate.ddl-auto=validate

# API Configuration
api.base-url=
api.timeout=30

# Offline sync
offline.sync-interval=300000
```

## 🔍 Troubleshooting

### Common Issues

1. **Database Connection Issues**
   - Check if H2 database files exist
   - Verify database credentials
   - Check if port 8888 is available

2. **Application Won't Start**
   - Check Java version (must be 17+)
   - Verify all dependencies are installed
   - Check logs in `logs/corevent.log`

3. **UI Issues**
   - Clear JavaFX cache
   - Check if JavaFX modules are properly loaded
   - Verify FXML files are in correct location

### Logs
- Application logs are stored in `logs/corevent.log`
- Log level can be configured in `application.properties`
- Use `logging.level.com.corevent=DEBUG` for detailed logs

## 📄 License

This project is proprietary software. All rights reserved.

## 👥 Team

👥 Team
- Project Owner: Livia Arumsari
- Lead Developer: Billy Samuel Setiawan (18222039)
- UI/UX Designer: Dzulfaqor Ali Dipanegara (18222017)
- Backend Developer: Benedicta Eryka Santosa (18222031)
- Frontend Developer (JavaFX): Kezia Caren Cahyadi (18222041)
- Database & API Integration Developer: Ananda Farhan Raihandra (18222084)
- QA Engineer & DevOps: Dahayu Ramaniya Aurasindu (18222099)