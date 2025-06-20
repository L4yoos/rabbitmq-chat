# RABBITMQ-CHAT

**Seamless Conversations, Powered by Reliable Messaging Innovation**

[![Last Commit](https://img.shields.io/github/last-commit/L4yoos/rabbitmq-chat)](https://github.com/L4yoos/rabbitmq-chat/commits/main)  
[![Top Language](https://img.shields.io/github/languages/top/L4yoos/rabbitmq-chat)](https://github.com/L4yoos/rabbitmq-chat)  
[![Language Count](https://img.shields.io/github/languages/count/L4yoos/rabbitmq-chat)](https://github.com/L4yoos/rabbitmq-chat)

---

## 🚀 Overview

**rabbitmq-chat** is a developer-focused toolkit for building scalable, real-time chat applications. It combines containerized deployment, reliable messaging, and persistent storage to streamline your development process.

### 🔥 Why rabbitmq-chat?

This project provides a solid foundation for real-time communication systems. The core features include:

- 🐳 **Docker Compose Integration**: Simplifies setup with a ready-to-use containerized environment.
- 🐇 **RabbitMQ Messaging**: Ensures dependable, asynchronous message delivery.
- 🗄️ **PostgreSQL Persistence**: Maintains chat history and user data reliably.
- 🧪 **Extensive Testing Suite**: Validates core functionalities for stability.
- 🧩 **Modular Core Classes**: Facilitates message exchange, user management, and database interactions.

---

## 📦 Built With

- **RabbitMQ**
- **Docker**
- **PostgreSQL**
- **Java** + **Maven** + **JUnit**

---

## 📚 Table of Contents

- [Overview](#-overview)
- [Getting Started](#-getting-started)
- [Prerequisites](#-prerequisites)
- [Installation](#-installation)
- [Usage](#-usage)
- [Testing](#-testing)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🚀 Getting Started

### ✅ Prerequisites

Make sure you have the following installed on your machine:

- **Java** (JDK 11 or higher recommended)
- **Maven** (for building the project)
- **Docker** (to run RabbitMQ and PostgreSQL containers)

---

## 🛠️ Installation

1. **Clone the repository**

```bash
git clone https://github.com/L4yoos/rabbitmq-chat.git
cd rabbitmq-chat
```

2. **Install the dependencies:**

```bash
docker build -t L4yoos/rabbitmq-chat .
```

```bash
mvn install
```

---

## 🚀 Usage

```bash
docker run -it L4yoos/rabbitmq-chat
mvn exec:java -Dexec.args=<nick>
```

---

## 🧪 Testing
rabbitmq-chat uses the JUnit test framework. Run the test suite with:

```bash
docker run L4yoos/rabbitmq-chat mvn test
mvn test
```