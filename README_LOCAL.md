# 🚀 Corevent Local Development Setup

## 📋 Quick Start Guide

### Prerequisites
- **Java 21** ✅ (Sudah terinstall)
- **Git** untuk clone repository
- **Windows 10/11** (Script batch disediakan)

### 🎯 Cara Menjalankan (3 Menit Setup)

#### Option 1: One-Click Run (Paling Mudah)
```bash
# Double-click file ini:
run.bat
```

#### Option 2: Development Mode
```bash
# Double-click untuk development:
dev.bat
```

#### Option 3: Manual Terminal
```bash
# 1. Build project (first time only)
.\mvnw.cmd clean package -DskipTests

# 2. Run aplikasi
java -jar target\Corevent-1.0.0-SNAPSHOT.jar
```

### 🌐 Akses Aplikasi
- **Desktop App**: Window JavaFX akan terbuka otomatis
- **Database Console**: http://localhost:8888/h2-console
- **API Endpoints**: http://localhost:8888/api/

### 🔑 Default Login
- **Username**: `admin`
- **Password**: `admin`
- **Role**: Committee

### 📁 File Konfigurasi Penting
```
Corevent/
├── mvnw.cmd                 # Maven wrapper (no need install Maven)
├── run.bat                  # Production run script
├── dev.bat                  # Development run script
├── .mvn/wrapper/            # Maven wrapper config
├── src/main/resources/
│   └── application.properties  # Database & server config
└── target/                  # Built JAR files
```

## 🔧 Troubleshooting

### ❌ Common Issues & Solutions

#### "mvn tidak dikenal"
✅ **FIXED**: Gunakan `.\mvnw.cmd` instead of `mvn`

#### JavaFX Warning
```
WARNING: Unsupported JavaFX configuration
```
✅ **Normal**: Aplikasi tetap berjalan dengan baik

#### Icon/CSS Not Found
```
Warning: Could not load application icon
Resource "style.css" not found
```
✅ **Normal**: UI tetap berfungsi, hanya styling yang default

#### Port 8888 Already in Use
```bash
# Stop existing Java processes
tasklist | findstr java
taskkill /PID [PID_NUMBER] /F
```

### 🔄 Clean Restart
```bash
# If stuck, clean everything:
.\mvnw.cmd clean
.\mvnw.cmd clean package -DskipTests
java -jar target\Corevent-1.0.0-SNAPSHOT.jar
```

## 🛠️ Development Workflow

### Project Structure
```
src/main/java/com/corevent/
├── entity/           # JPA Entities (Event, User, Ticket)
├── repository/       # Data Access Layer
├── service/          # Business Logic
├── controller/       # JavaFX Controllers
├── config/           # Spring Configuration
└── CoreventApplication.java  # Main class
```

### Database Access
- **URL**: `jdbc:h2:file:./corevent_db`
- **Username**: `sa`
- **Password**: `password`
- **Console**: http://localhost:8888/h2-console

### Hot Reload Development
```bash
# For code changes without restart:
.\mvnw.cmd compile exec:java -Dexec.mainClass="com.corevent.CoreventApplication"
```

## 📱 Features Currently Working
- ✅ Authentication System (Login/Logout)
- ✅ Spring Boot + JavaFX Integration
- ✅ H2 Database with JPA
- ✅ Security Configuration
- ✅ User Management (Committee/Participant)

## 🚧 In Development
- ⏳ Dashboard UI
- ⏳ Event CRUD Operations
- ⏳ QR Code System
- ⏳ Participant Management
- ⏳ Report Generation

## 🆘 Need Help?
1. Check application logs di terminal
2. Verify Java version: `java --version`
3. Check if port 8888 is free
4. Try clean build: `.\mvnw.cmd clean package -DskipTests`

---
**Last Updated**: June 2025 | **Team**: DRPL K1L Corevent 