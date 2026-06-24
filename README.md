# ServiceLink

ServiceLink is a full-stack service marketplace project with a Spring Boot API and a React/Vite frontend. It supports user registration, role-based login, service listings, bookings, admin users, and service categories.

## Tech Stack

- Java 17 and Spring Boot
- MongoDB by default, with a MySQL profile available
- React, Vite, React Router, React Query, and Axios
- Docker Compose for local MongoDB/MySQL services

## Project Structure

```text
backend/   Spring Boot REST API
frontend/  React Vite frontend
```

## Backend Setup

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

The default profile is `mongo`. You can switch profiles with:

```powershell
$env:SPRING_PROFILES_ACTIVE="mysql"
```

Useful environment variables:

```text
JWT_SECRET=replace-with-a-long-secret
MONGODB_URI=mongodb://localhost:27017/servicelink
MYSQL_URL=jdbc:mysql://localhost:3306/servicelink?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
MYSQL_USER=sluser
MYSQL_PASSWORD=slpass
DEMO_USER_PASSWORD=password
```

## Frontend Setup

```powershell
cd frontend
npm install
npm run dev
```

Set the API URL if the backend is not running on `http://localhost:8080`:

```powershell
$env:VITE_API_BASE_URL="http://localhost:8080"
```

## Local Data

The backend seeds demo accounts for local development. The default demo password is `password`, or the value of `DEMO_USER_PASSWORD` if set.

## Notes

- `docker-compose.yml` is for local development only.
- Use a strong `JWT_SECRET` outside local demos.
- The frontend now includes a minimal app shell for the existing pages.

