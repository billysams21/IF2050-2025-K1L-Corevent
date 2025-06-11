# 📝 Git Commit Message Guide untuk Tim DRPL

## 🎯 Format Commit Message
```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

## 📋 Types yang Digunakan

### Core Development
- **feat**: Feature baru
- **fix**: Bug fix
- **refactor**: Refactoring code tanpa mengubah functionality
- **style**: Formatting, missing semicolons, etc (no code change)
- **docs**: Documentation changes
- **test**: Adding tests atau fixing existing tests

### Setup & Configuration
- **build**: Build system atau dependency changes
- **ci**: CI configuration files dan scripts
- **chore**: Maintenance tasks

## 🏷️ Scope Examples
- **auth**: Authentication system
- **ui**: User interface
- **db**: Database
- **api**: API endpoints
- **config**: Configuration
- **entity**: JPA entities
- **service**: Business logic

## ✅ Good Commit Messages untuk Project Ini

### Setup & Configuration
```bash
chore(setup): add Maven wrapper and local development scripts

- Add mvnw.cmd for cross-platform Maven execution
- Create run.bat and dev.bat for easy local development
- Configure Maven wrapper properties for automatic download
- Add README_LOCAL.md for team onboarding

Fixes issue with team members not having Maven installed locally.
```

### Feature Development
```bash
feat(auth): implement login system with Spring Security

- Add User entity with Committee and Participant roles
- Configure BCrypt password encoding
- Implement AuthService with JWT token support
- Add remember-me functionality with token expiry
- Create login FXML interface

Co-authored-by: TeamMember <email@example.com>
```

### Bug Fixes
```bash
fix(ui): resolve CSS stylesheet loading issue

- Update CSS resource path in CoreventApplication
- Add fallback styling for missing stylesheets
- Fix application icon loading from JAR

Resolves #123
```

### Documentation
```bash
docs(setup): add comprehensive local development guide

- Create step-by-step setup instructions
- Add troubleshooting section for common issues
- Document default login credentials
- Include project structure overview

Closes #45
```

## 🚀 Commit Message untuk Kondisi Saat Ini

Berdasarkan progress project sekarang, ini commit message yang cocok:

```bash
chore(setup): configure local development environment and Maven wrapper

- Add Maven wrapper (mvnw.cmd) to eliminate Maven installation requirement
- Create run.bat and dev.bat scripts for easy application execution
- Configure Maven wrapper properties with Maven 3.9.5
- Add comprehensive README_LOCAL.md for team onboarding
- Fix application startup issues related to classpath with spaces

This enables all team members to run the project locally without 
Maven installation and provides clear setup instructions for DRPL coursework.

Tested on: Windows 11 with Java 21
```

## 📌 Pro Tips untuk Tim

### 1. Conventional Commits
Gunakan format yang konsisten agar mudah di-track:
- Commit messages dalam bahasa Inggris
- Use imperative mood: "add" not "added" or "adding"
- Capitalize first letter
- No period at the end

### 2. Reference Issues
```bash
feat(event): implement event creation form

Implements requirements from #UC01
Closes #42
```

### 3. Co-authoring untuk Pair Programming
```bash
feat(qr): add QR code generation service

Co-authored-by: Nama-Tim-Member <email@student.itb.ac.id>
```

### 4. Breaking Changes
```bash
feat(api)!: change authentication endpoint structure

BREAKING CHANGE: auth endpoints moved from /auth to /api/auth
```

## 🔄 Workflow untuk Branch Main

### Before Commit
```bash
# 1. Test aplikasi jalan
.\mvnw.cmd clean package -DskipTests
java -jar target\Corevent-1.0.0-SNAPSHOT.jar

# 2. Check status
git status

# 3. Add changes
git add .

# 4. Commit dengan message yang proper
git commit -m "chore(setup): configure local development environment"

# 5. Push ke main (hati-hati!)
git push origin main
```

---
**Tim DRPL K1L - Corevent Project** 