
# Tool Rental Management System

## Overview
This project is a comprehensive tool rental management system designed to manage tools, tool charges, and rental agreements. The system leverages modern technologies, including Spring Boot, Gradle, Docker, Liquibase, and Swagger UI, to ensure a robust, scalable, and well-documented application.

## Features
- **Cache Invalidation**: Supports cache invalidation for `Tool` and `ToolCharge` entities to ensure data consistency.
- **Database Management**: Uses Liquibase for schema creation and version control.
- **Docker Integration**: The database can be started with Docker when the application boots up.
- **In-Memory Database Support**: The project is configured to use an in-memory database by default, but this can be modified for a persistent database.
- **Swagger UI**: Provides an easy-to-use interface for exploring and testing the API endpoints.
- **Security Configuration**: Simple authentication setup with username and password both set as `doguscan`. The security configuration can be adjusted as needed.
- **JavaDocs**: Comprehensive JavaDocs have been added for methods for better code understanding and maintenance.
- **Global Exception Handling**: A global exception handler is included to handle errors uniformly across the application.
- **Audit Table**: An audit table has been added to track specific data changes and operations. This can be extended for more comprehensive auditing needs.
- **Environment Profiles**: Currently, the project assumes a single environment. However, environmental profiles can be added and configured as needed.
- **Open API**: Open API can be enabled with some code adjustments and project rebuilding.

## Getting Started

### Prerequisites
- **Java 17 or later**
- **Docker**
- **Gradle**

### Installation and Setup

1. **Clone the Repository**
   ```bash
   git clone https://github.com/your-repo/tool-rental-management.git
   cd tool-rental-management
   ```

2. **Run the Application**
   Ensure Docker is running on your machine, then start the application using:
   ```bash
   ./gradlew bootRun
   ```

   This will automatically spin up the database using Docker.

3. **Database Initialization**
   - **Liquibase** is used for database schema creation and version control. The database schema will be set up automatically when the application starts.

### Configuration

- **Database**: By default, an in-memory database (H2) is used. You can access the database through the H2 console at:
  ```
  http://localhost:8080/h2-console
  ```
  Change the database configurations in `application.properties` or `application.yml` to switch to a persistent database.

- **Security Configuration**: The security setup can be adjusted in `SecurityConfig.java` to fit different authentication and authorization needs.

- **Swagger UI**: 
  Access the API documentation at:
  ```
  http://localhost:8080/swagger-ui.html
  ```

- **Open API**: To use Open API, enable it by modifying the relevant configuration and making necessary code changes. Rebuild the project to apply these changes.

### Authentication
The application is secured with basic authentication. Use the following credentials to access protected resources:
- **Username**: `doguscan`
- **Password**: `doguscan`

### Docker Database Setup
The application is configured to boot up the database through Docker. Ensure that Docker is installed and running on your system.

### Liquibase Version Control
Liquibase tracks database changes and applies version control automatically when the application starts. This helps manage schema changes and ensures consistency.

### JavaDocs
JavaDocs have been added for methods across the project, providing detailed explanations of their purpose and usage.

### Global Exception Handling
A global exception handler has been added to provide a unified error response format and enhance the user experience when handling errors.

### Audit Table
An audit table is included in the database schema to track certain data and operations for auditing purposes. This table can be extended as needed to include more detailed data.

## API Endpoints

### Cache Management
- **Clear Tool Cache**
  ```http
  DELETE /api/cache/invalidate/tools
  ```

- **Clear ToolCharge Cache**
  ```http
  DELETE /api/cache/invalidate/toolCharges
  ```

- **Clear All Caches**
  ```http
  DELETE /api/cache/invalidate/all
  ```

### Example CURL Command
To clear the `ToolCharge` cache:
```bash
curl -X DELETE http://localhost:8080/api/cache/invalidate/toolCharges
```

## Development Notes
- **Logging**: The application is configured with logging to track key operations and cache invalidation activities.
- **Error Handling**: Comprehensive error handling is in place with a global exception handler to ensure informative responses.
- **Environment Profiles**: The application currently assumes a single environment but can be configured to support different profiles (e.g., `dev`, `prod`) in `application.properties` or `application.yml`.

## Future Enhancements
- **Persistent Database Configuration**: Update `application.properties` to switch from an in-memory database to a production-ready persistent database.
- **Enhanced Security**: Integrate advanced security features as needed.

## License
This project is licensed under the MIT License. See the dsozeri file for more details.

## Contributing
Contributions are welcome! Please create a pull request or submit an issue for any improvements or suggestions.

## Contact
For further questions, please contact [doguscan.sozeri@gmail.com].
