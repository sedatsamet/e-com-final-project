
# E-Commerce Web Application Backend Project

This project serves as the backend for an e-commerce web application. It's developed using several technologies including ```Spring Boot```, ```Spring Data JPA```, ```Spring Security```, ```Maven```, ```MySQL```, ```Spring Cloud Gateway```, ```OpenApi Documentation```, and ```Eureka```.


## Table of Contents

- [User Management](#user-management)
- [Product Management](#product-management)
- [Cart Management](#cart-management)
- [Order Management](#order-management)
- [Spring Cloud Integration](#spring-cloud-integration)
- [Logging and Exception Handling](#logging-and-exception-handling)
- [API Documentation and Testing](#centralized-api-documentation-and-testing)
- [Setup and Running the Project](#setup-and-running-the-project)

## User Management
- Endpoints: Created endpoints for user registration, login, and authorization.
- Security: Implemented ```JWT-based``` authentication using ```Spring Security```.
- Roles: Defined roles as CUSTOMER and ADMIN.
- Admin Capabilities: Admin users can list and update all users.
- Error Handling: Provided appropriate error messages and HTTP status codes for unauthorized user actions.

## Product Management
- Endpoints: Established endpoints for adding, updating, deleting, and viewing products.
- Product Details: Ensured each product has price and stock information.
- Image Upload: Enabled image upload for products.

## Cart Management
- Cart Structure: Each customer has one active cart.
- Product Addition: Customers can add various products and quantities to their carts.
- Quantity Update: Allowed changing quantities of products in the cart.
- Clear Cart: Enabled clearing the cart with a single request.
- Stock Control: Conducted stock checks when adding products to the cart.
- Customer Access: Customers can only view their carts.

## Order Management
- Order Creation: Customers can create orders.
- Order History: Customers can view their order history.
- Admin View: Admin users can view all orders.
- Order Status: Orders start with a "new order" status, and upon admin approval, the status can change.
- Stock Management: Adjusted stock based on order statuses (approved or rejected).

## Spring Cloud Integration
- Microservices Architecture: Utilized Spring Cloud features such as ```Eureka Service Registry```.
- API Gateway: Implemented API Gateway using ```Spring Cloud Gateway``` to handle routing and security.

## Logging and Exception Handling
- Advanced Logging: Integrated logging mechanisms for better error tracking and analysis.
- Exception Handling: Implemented exception handling mechanisms for potential errors.

## Centralized API Documentation and Testing
- Swagger Support: Documented the Centralized API using ```Swagger-UI```,```OpenAPI```.
- Postman Collection: Included a Postman collection to facilitate testing.
- Swagger UI : http://localhost:9000/swagger-ui.html

## Setup and Running the Project
1. Clone the repository.
2. Navigate to the project directory.
3. Run mvn clean install to build the project.
4. Start the necessary services (e.g., database, Eureka server).
5. Run the Spring Boot application.
6. Access the API using the provided endpoints.