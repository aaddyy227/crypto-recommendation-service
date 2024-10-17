
# Cryptocurrency Recommendation Service

## Overview

The Cryptocurrency Recommendation Service is a Spring Boot application designed to help developers make informed cryptocurrency investment decisions. It reads cryptocurrency price data from CSV files, computes important statistics, and exposes multiple endpoints for data retrieval, comparison, and recommendations. The service dynamically scans a directory for new cryptocurrency data and provides results based on a normalized range for accurate comparisons.

## Features

- **Dynamic Data Scanning**: Automatically reads cryptocurrency data from CSV files and updates the service with new cryptos when new files are added.
- **Statistics Calculation**: Calculates oldest, newest, minimum, and maximum prices for each cryptocurrency.
- **Normalized Range Comparison**: Compares cryptocurrencies based on their normalized range, providing better insight into potential investments.
- **RESTful API Endpoints**: Exposes endpoints for retrieving sorted cryptocurrency statistics, specific crypto stats, and daily top-performer recommendations.
- **Swagger UI**: Available at `/swagger-ui/index.html`.
- **Scalable Design**: Supports adding more cryptocurrencies without changes to the codebase, ensuring scalability as the number of cryptos increases.
- **Rate Limiting**: Configured to limit the number of requests to 100 requests per 30 minutes.

## Requirements

- Java 21 or higher
- Maven
- Docker (optional, for containerization)
- Kubernetes (optional, for container orchestration)

## Installation

### Clone the Repository

```bash
git clone https://github.com/your-username/crypto-recommendation-service.git
cd crypto-recommendation-service
```

### Build the Project

```bash
mvn clean package
```

### Create the Crypto Data Folder

Create a `crypto-data` folder inside the root of your project and add your CSV files there. Ensure it contains the data in the following format:

```
timestamp, symbol, price
1641009600000, BTC, 46813.21
```

### Run the Application

You can run the application using Maven or by executing the JAR file:

```bash
mvn spring-boot:run
```

Or:

```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

## Configuration

In the `application.properties` file, you can configure the following settings:

```properties
spring.application.name=recommendation-service
server.port=8080
logging.level.root=INFO
logging.level.com.example.crypto=DEBUG

# Configurable interval for scanning the crypto directory (in milliseconds)
crypto.scan.interval=60000

# Path to the crypto-data directory
crypto.directory.path=${user.dir}/crypto-data

# Rate limiting configuration
rate.limit.requests=100
rate.limit.duration.minutes=30
```

## API Documentation

- **Get All Cryptos Sorted by Normalized Range**:
  - **Endpoint**: `/api/crypto/normalized`
  - **Method**: `GET`
  - **Response**: `200 OK`

- **Get Statistics for a Specific Crypto**:
  - **Endpoint**: `/api/crypto/{symbol}/statistics`
  - **Method**: `GET`
  - **Response**: `200 OK`, `404 Not Found`, `400 Bad Request`

- **Get Crypto with Highest Normalized Range for a Specific Day**:
  - **Endpoint**: `/api/crypto/highest-normalized`
  - **Method**: `GET`
  - **Response**: `200 OK`, `404 Not Found`, `400 Bad Request`

## Kubernetes and Containerization

### Kubernetes Deployment

For deploying the service in Kubernetes, use the following configurations:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: recommendation-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: recommendation-service
  template:
    metadata:
      labels:
        app: recommendation-service
    spec:
      containers:
        - name: recommendation-service
          image: your-dockerhub-username/recommendation-service:latest
          ports:
            - containerPort: 8080
          resources:
            limits:
              memory: "1024Mi"
              cpu: "500m"
```

### Kubernetes Service

```yaml
apiVersion: v1
kind: Service
metadata:
  name: recommendation-service
spec:
  selector:
    app: recommendation-service
  ports:
    - protocol: TCP
      port: 80
      targetPort: 8080
  type: LoadBalancer
```

## Swagger Documentation

You can access the Swagger UI for API documentation at:

```
http://localhost:8080/swagger-ui/index.html
```

### Deploying to Production
To deploy the application to a production environment, follow these steps:

Docker
If you are using Docker, first build the Docker image:

bash
Copy code
docker build -t crypto-recommendation-service .
Then, run the container:

bash
Copy code
docker run -p 8080:8080 crypto-recommendation-service
Kubernetes
For Kubernetes deployment, use the provided YAML files in the k8s directory:

bash
Copy code
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
