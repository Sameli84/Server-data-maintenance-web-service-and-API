# Server-data-maintenance-web-service-and-API
Summer trainee porject at TAMK

A multipurpose fullstack application developed with Spring Boot
that can for example be used by teachers to define data gathered
from students for course.

Project uses PostgreSQL database and Keycloak for authentication
and authorization management.

Project is dockerized and can be setup easily with
running the Spring boot application and 'docker-compose up'
with .env file containing following information:

API_PORT=4000:4000
SERVER_PORT=4000

POSTGRES_DSN=jdbc:postgresql://db:5432/postgres
KC_POSTGRES_DSN=jdbc:postgresql://db:5432/keycloak

POSTGRES_USER=postgres_user
POSTGRES_PASSWORD=postgres_password

KEYCLOAK_URL=http://keycloak:8080
KC_ADMIN_PASSWORD=keycloak_admin_password

POSTGRES_DSN=jdbc:postgresql://db:5432/postgres

APP_DOMAIN=serverdata.localhost
AUTH_SUBDOMAIN=auth

KC_ADMIN_USERNAME=admin
KC_POSTGRES_DSN=jdbc:postgresql://db:5432/keycloak
KC_REALM=springbootkeycloak
KC_CLIENT=serverdata

Image from running project and example from datafield creation view:
![project](https://user-images.githubusercontent.com/47208771/214525649-0bf60a15-bf6c-4b26-8788-ba4f26e8763b.jpeg)

