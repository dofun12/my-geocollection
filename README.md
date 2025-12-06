# My Geo Collection 🗺️

A full-stack application for managing Points of Interest (POI) on an interactive map. Built with Spring Boot, React, TypeScript, and PostgreSQL.

## ✨ Features

- 🗺️ **Interactive Map**: OpenStreetMap integration with Leaflet
- 📍 **POI Management**: Full CRUD operations for Points of Interest
- 🖱️ **Click-to-Add**: Add POIs by clicking directly on the map
- 🔍 **Address Search**: Autocomplete address search with Nominatim API
- 🖼️ **Image Upload**: Support for POI images (JPEG, PNG, GIF up to 5MB)
- 💾 **State Persistence**: Map position and zoom level saved to localStorage
- 🎨 **Modern UI**: Beautiful, responsive design with TailwindCSS
- 🐳 **Docker Ready**: Complete containerization with Docker Compose
- ✅ **Comprehensive Tests**: High coverage with JUnit 5 and Mockito

## 🛠️ Tech Stack

### Backend
- **Java 21**
- **Spring Boot 3.2.1**
  - Spring Web
  - Spring Data JPA
  - Spring Validation
  - Spring Actuator
- **PostgreSQL 16**
- **Lombok**
- **JUnit 5 + Mockito**

### Frontend
- **React 18**
- **TypeScript**
- **Vite**
- **TailwindCSS**
- **Leaflet / React-Leaflet**
- **Axios**
- **Lucide React** (icons)

### DevOps
- **Docker**
- **Docker Compose**
- **Maven**

## 🚀 Quick Start

### Prerequisites

- Docker and Docker Compose
- OR:
  - Java 21 JDK
  - Node.js 18+ and npm
  - PostgreSQL 16

### Option 1: Docker (Recommended)

1. **Clone the repository**
```bash
git clone <repository-url>
cd my-geocollection
```

2. **Start all services**
```bash
docker-compose up --build
```

3. **Access the application**
- Backend API: http://localhost:8080
- Backend Health: http://localhost:8080/actuator/health
- Frontend: Start separately (see Development Setup)

The database will be automatically created and the backend will wait for PostgreSQL to be healthy before starting.

### Option 2: Local Development

#### Backend Setup

1. **Start PostgreSQL**
```bash
docker run --name geocollection-postgres \
  -e POSTGRES_DB=geocollection \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:16-alpine
```

2. **Run the Spring Boot application**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The backend will be available at http://localhost:8080.

#### Frontend Setup

1. **Navigate to frontend directory**
```bash
cd frontend
```

2. **Install dependencies**
```bash
npm install
```

3. **Start development server**
```bash
npm run dev
```

The frontend will be available at http://localhost:3000.

## 📚 API Documentation

### Endpoints

#### Get All POIs
```http
GET /api/pois
```

#### Get POI by ID
```http
GET /api/pois/{id}
```

#### Create POI
```http
POST /api/pois
Content-Type: application/json

{
  "title": "POI Title",
  "description": "Optional description",
  "latitude": -23.5505,
  "longitude": -46.6333,
  "imageBase64": "data:image/png;base64,..."
}
```

#### Update POI
```http
PUT /api/pois/{id}
Content-Type: application/json

{
  "title": "Updated Title",
  "description": "Updated description",
  "latitude": -23.5505,
  "longitude": -46.6333,
  "imageBase64": "data:image/png;base64,..."
}
```

#### Delete POI
```http
DELETE /api/pois/{id}
```

### Validation Rules

- **Title**: Required, max 200 characters
- **Description**: Optional, max 5000 characters
- **Latitude**: Required, -90 to 90
- **Longitude**: Required, -180 to 180
- **Image**: Optional, max 5MB, formats: JPEG, PNG, GIF

## 🧪 Running Tests

### Backend Tests
```bash
mvn test
```

### Generate Coverage Report
```bash
mvn jacoco:report
```

The coverage report will be available at `target/site/jacoco/index.html`.

### Coverage Requirements
- Minimum line coverage: 80%

## 🏗️ Architecture

```
my-geocollection/
├── src/main/java/com/geocollection/
│   ├── MyGeoCollectionApplication.java    # Main application
│   ├── config/                            # Configuration classes
│   ├── controller/                        # REST controllers
│   ├── dto/                               # Data Transfer Objects
│   ├── entity/                            # JPA entities
│   ├── exception/                         # Exception handling
│   ├── repository/                        # JPA repositories
│   └── service/                           # Business logic
├── src/test/java/                         # Unit & integration tests
├── frontend/
│   └── src/
│       ├── components/                    # React components
│       ├── services/                      # API services
│       ├── types/                         # TypeScript types
│       ├── App.tsx                        # Main component
│       └── main.tsx                       # Entry point
├── Dockerfile                             # Backend Docker image
├── docker-compose.yml                     # Multi-container setup
└── pom.xml                                # Maven configuration
```

## 🎯 Usage Guide

### Adding a POI via Map Click

1. Click anywhere on the map
2. A modal will open with the coordinates pre-filled
3. Fill in the title and optional description
4. Optionally upload an image
5. Click "Create POI"

### Adding a POI via Address Search

1. Click the "Add New POI" button in the header
2. Use the address search field
3. Select an address from the autocomplete dropdown
4. The coordinates will be automatically filled
5. Fill in remaining details and submit

### Editing a POI

1. Click on a POI marker on the map
2. In the popup, click the "Edit" button
3. Update the information
4. Click "Update POI"

### Deleting a POI

1. Click on a POI marker
2. Click the "Delete" button
3. Confirm the deletion

### Map State Persistence

The map automatically saves your current position and zoom level to localStorage. When you reload the page, the map will return to your last viewed position.

## 🔧 Configuration

### Backend Environment Variables

- `DB_HOST`: Database host (default: localhost)
- `DB_PORT`: Database port (default: 5432)
- `DB_NAME`: Database name (default: geocollection)
- `DB_USERNAME`: Database username (default: postgres)
- `DB_PASSWORD`: Database password (default: postgres)
- `CORS_ORIGINS`: Allowed CORS origins

### Frontend Configuration

The frontend is configured via `vite.config.ts`:
- Development server port: 3000
- API proxy: Forwards `/api/*` to `http://localhost:8080`

## 📝 License

This project is open source and available under the MIT License.

## 👥 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 🐛 Known Issues

- Image storage uses Base64 encoding in the database. For production, consider using external file storage (S3, cloud storage, etc.)
- Nominatim API has usage limits. For production, consider implementing caching or using a commercial geocoding service

## 🔮 Future Enhancements

- [ ] User authentication and authorization
- [ ] POI categories and filtering
- [ ] Export POIs to various formats (JSON, CSV, KML)
- [ ] Offline map support with Progressive Web App (PWA)
- [ ] Multi-language support (i18n)
- [ ] Advanced search and filtering
- [ ] POI sharing and collaboration features

## 📞 Support

For questions or issues, please open an issue on GitHub.

---

Made with ❤️ using Spring Boot, React, and Leaflet
