# Product Management API

REST API do zarządzania produktami z autentykacją JWT i systemem ról (USER/ADMIN).
Aplikacja stworzona w ramach zadania rekrutacyjnego.

## 📋 Spis treści

- [Stack technologiczny](#-stack-technologiczny)
- [Funkcjonalności](#-funkcjonalności)
- [Architektura](#-architektura)
- [Wymagania](#-wymagania)
- [Uruchomienie](#-uruchomienie)
- [Domyślne konta](#-domyślne-konta)
- [Endpointy API](#-endpointy-api)
- [Przykłady użycia](#-przykłady-użycia)
- [Obsługa błędów](#-obsługa-błędów)
- [Zatrzymanie aplikacji](#-zatrzymanie-aplikacji)

## 🛠 Stack technologiczny

- **Java 21** (Amazon Corretto)
- **Spring Boot 3.5** — Web, Data JPA, Security, Validation
- **PostgreSQL 16** (uruchamiana w kontenerze Docker)
- **JWT** (jjwt 0.12) — stateless authentication
- **MapStruct 1.6** — mapowanie DTO ↔ Entity
- **Lombok** — redukcja boilerplate
- **Maven** — zarządzanie zależnościami
- **Docker + Docker Compose** — konteneryzacja bazy danych

## ✨ Funkcjonalności

### Wymagane
- ✅ Dodawanie nowego produktu
- ✅ Pobieranie szczegółów produktu po ID
- ✅ Pobieranie listy produktów (z paginacją)
- ✅ Aktualizacja produktu
- ✅ Usuwanie produktu
- ✅ Wyszukiwanie produktów po kategorii
- ✅ Zabezpieczenie API (JWT + role)
- ✅ Obsługa błędów (`@ControllerAdvice` + ustandaryzowane odpowiedzi)
- ✅ Walidacja danych wejściowych (Bean Validation)

### Dodatkowe (ponad wymagania)
- ✅ Audytowanie encji (`createdAt`, `createdBy`, `updatedAt`, `modifiedBy`)
- ✅ Automatyczna inicjalizacja danych przy starcie (seed userów i produktów)
- ✅ Konteneryzacja bazy danych przez Docker Compose
- ✅ Paginacja listy produktów
- ✅ Strukturyzowane logowanie błędów

## 🏗 Architektura

Projekt używa **package-by-feature** — każdy feature ma własny pakiet zamiast warstwowego podziału (controllers/services/repositories):

```
com.productapi/
├── auth/              # Rejestracja, logowanie, JWT, konfiguracja Spring Security
│   ├── dto/           # Request/Response dla endpointów autentykacji
│   └── jwt/           # Filtr JWT, generowanie/walidacja tokenów
├── audit/             # JPA Auditing — automatyczne wypełnianie pól audytu
├── bootstrap/         # Inicjalizator danych przy starcie aplikacji
├── exception/         # Globalna obsługa błędów
├── product/           # CRUD produktów
│   └── dto/
└── user/              # Encja użytkownika i repozytorium
```
## ⚙️ Wymagania

- **JDK 21** (testowane na Amazon Corretto 21.0.4)
- **Maven 3.9+**
- **Docker Desktop** (do uruchomienia bazy danych)

## 🚀 Uruchomienie

### 1. Sklonuj repozytorium

```bash
git clone <repo-url>
cd product-management-api
```

### 2. Skopiuj plik konfiguracji środowiska

```bash
cp .env.example .env
```

Domyślne wartości w `.env` są gotowe do użycia lokalnego. Możesz je dostosować jeśli potrzeba.

### 3. Uruchom bazę danych w Dockerze

```bash
docker-compose up -d
```

Polecenie uruchomi kontener PostgreSQL 16 w tle. Sprawdź status:

```bash
docker ps
```

Powinieneś zobaczyć kontener `productapi-db` ze statusem `(healthy)`.

### 4. Uruchom aplikację

```bash
mvn spring-boot:run
```

Lub z poziomu IntelliJ IDEA — Run `ProductManagementApiApplication`.

Aplikacja będzie dostępna pod adresem: **http://localhost:8080**

### 5. Sprawdź że działa

Przy pierwszym starcie w logach powinny pojawić się komunikaty:

```
Created default ADMIN user: 'admin'
Created default USER user: 'user'
Seeded 3 default products
```

## 🔐 Domyślne konta

Przy pierwszym starcie (gdy baza jest pusta) tworzone są dwa domyślne konta:

| Username | Password   | Rola   | Uprawnienia |
|----------|------------|--------|-------------|
| `admin`  | `admin123` | ADMIN  | Pełny dostęp do wszystkich endpointów |
| `user`   | `user123`  | USER   | Tylko odczyt produktów |

> 💡 Konta są tworzone tylko gdy tabela jest pusta — po kolejnych restartach aplikacja nie nadpisuje istniejących danych.

## 🔗 Endpointy API

### Autentykacja (publiczne)

| Metoda | Endpoint | Opis |
|--------|----------|------|
| `POST` | `/auth/register` | Rejestracja nowego użytkownika (rola USER) |
| `POST` | `/auth/login` | Logowanie — zwraca JWT |

### Produkty (wymagają autentykacji)

| Metoda | Endpoint | Rola | Opis |
|--------|----------|------|------|
| `GET` | `/products` | USER, ADMIN | Lista produktów z paginacją |
| `GET` | `/products/{id}` | USER, ADMIN | Szczegóły produktu |
| `GET` | `/products/category/{category}` | USER, ADMIN | Filtrowanie po kategorii |
| `POST` | `/products` | ADMIN | Utworzenie produktu |
| `PUT` | `/products/{id}` | ADMIN | Aktualizacja produktu |
| `DELETE` | `/products/{id}` | ADMIN | Usunięcie produktu |

### Parametry paginacji (dla `GET /products`)

| Parametr | Typ | Domyślna wartość | Opis |
|----------|-----|------------------|------|
| `page` | int | `0` | Numer strony (od zera) |
| `size` | int | `20` | Liczba elementów na stronie |

Przykład: `GET /products?page=0&size=10`

### Autoryzacja

Po zalogowaniu, dołącz JWT do każdego żądania w nagłówku:

```
Authorization: Bearer <token>
```

## 💡 Przykłady użycia

### 1. Rejestracja nowego użytkownika

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "jankowalski",
    "password": "mojeHaslo123"
  }'
```

**Odpowiedź (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "jankowalski",
  "role": "USER"
}
```

### 2. Logowanie

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

### 3. Utworzenie produktu (wymaga roli ADMIN)

```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "name": "Laptop Dell XPS 15",
    "description": "Laptop biznesowy z ekranem 4K",
    "price": 4500.00,
    "category": "Elektronika"
  }'
```

**Odpowiedź (201 Created):**
```json
{
  "id": 4,
  "name": "Laptop Dell XPS 15",
  "description": "Laptop biznesowy z ekranem 4K",
  "price": 4500.00,
  "category": "Elektronika",
  "createdAt": "2026-04-24T12:00:00",
  "createdBy": "admin",
  "updatedAt": "2026-04-24T12:00:00",
  "modifiedBy": "admin"
}
```

### 4. Lista produktów z paginacją

```bash
curl "http://localhost:8080/products?page=0&size=5" \
  -H "Authorization: Bearer <TOKEN>"
```

### 5. Wyszukiwanie po kategorii

```bash
curl http://localhost:8080/products/category/Elektronika \
  -H "Authorization: Bearer <TOKEN>"
```

### 6. Aktualizacja produktu (ADMIN)

```bash
curl -X PUT http://localhost:8080/products/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "price": 4200.00
  }'
```

> 💡 Update obsługuje częściową aktualizację — można wysłać tylko te pola, które chcemy zmienić.

### 7. Usunięcie produktu (ADMIN)

```bash
curl -X DELETE http://localhost:8080/products/1 \
  -H "Authorization: Bearer <TOKEN>"
```

## 🚨 Obsługa błędów

API zwraca ustandaryzowane odpowiedzi błędów:

```json
{
  "timestamp": "2026-04-24T12:00:00",
  "status": 404,
  "message": "Product not found with id: 99"
}
```
## 🛑 Zatrzymanie aplikacji

### Zatrzymanie aplikacji Java
Ctrl+C w terminalu z `mvn spring-boot:run`, lub Stop w IntelliJ.

### Zatrzymanie bazy danych

```bash
docker-compose down
```

Dane zostają w wolumenie — po kolejnym `docker-compose up -d` baza wraca w tym samym stanie.

### Wyczyszczenie danych bazy

```bash
docker-compose down -v
```

⚠️ Flaga `-v` usuwa wolumen z danymi. Przy kolejnym starcie baza będzie pusta, a DataInitializer stworzy domyślne konta i produkty od nowa.

## 📝 Uwagi końcowe

- Token JWT wygasa po **24 godzinach** (konfigurowalne przez `jwt.expiration` w `application.yml`)
- Wszystkie zmiany w tabelach są automatycznie audytowane
- Hasła są hashowane algorytmem **BCrypt** (10 rund)
- Baza danych pracuje w wolumenie Docker — dane są persystentne między restartami kontenera
- Aplikacja używa `Hibernate DDL auto-update` — schemat bazy tworzy się automatycznie przy starcie
