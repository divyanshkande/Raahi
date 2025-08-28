!-- Banner -->
<p align="center">
  <img src="https://your-banner-image-link.com/banner.png" alt="AI Tour Planner Banner" width="100%"/>
</p>

<h1 align="center">🌍 Raahi-Your AI Tour Planner</h1>
<p align="center"><b>Plan • Travel • Explore</b> – AI-powered smart travel planner (React + Spring Boot)</p>


---

## 📌 Table of Contents
- [About](#-about)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Repository Structure](#-repository-structure)
- [Screenshots](#-screenshots)
- [Installation](#-installation)
- [Usage (Quick Start)](#-usage-quick-start)
- [Environment Variables](#-environment-variables)
- [Future Scope](#-future-scope)
- [Contributing](#-contributing)
- [License](#-license)

---

## 📖 About
**AI Tour Planner** is a full-stack travel planning application that helps users generate personalized itineraries.  
It uses **AI + Spring Boot backend** to analyze destinations, duration, and interests, then produces smart day-wise plans.

---

## ✨ Features
- 🔐 User authentication & secure login  
- 🧠 AI-powered itinerary generation  
- 📍 Destination, number of days & interest selection  
- 📅 Day-wise trip planning  
- 🎨 Beautiful, responsive UI with animations  
- 📡 React frontend connected with Spring Boot backend  
- ⚡ Real-time data handling with REST APIs  

---

## 🛠 Tech Stack
**Frontend:** React, Tailwind CSS, Axios  
**Backend:** Spring Boot, Java, Spring Security  
**Database:** MySQL  
**Authentication:** JWT & BCrypt Password Encoder  
**Tools:** Postman, Maven, npm, VS Code

---

## 📁 Repository Structure
raahi/
│
├── backend/ # Spring Boot backend source code
│ ├── src/
│ ├── pom.xml
│
├── frontend/ # React frontend source code
│ ├── src/
│ ├── package.json
│
└── README.md

---

## 📸 Screenshots
![Homepage](path-to-screenshot1.png)  
*Homepage with sections for Home, About & Login*

![Itinerary](path-to-screenshot2.png)  
*AI-generated itinerary with day-wise plan*

---

## 🛠 Installation

### 📌 Prerequisites
- **Node.js** (v16+)  
- **Java** (JDK 17+)  
- **Maven**  
- **MySQL Server**  

---

### 📥 Clone the repository
```bash
git clone https://github.com/divyanshkande/Raahi.git

cd raahi
⚡ One-Time Setup for Combined Start
Create a root-level package.json


npm init -y
Install concurrently


npm install concurrently
Edit root package.json and add:


"scripts": {
  "start": "concurrently \"mvn -f backend/pom.xml spring-boot:run\" \"npm start --prefix frontend\""
}
Install frontend dependencies


npm install --prefix frontend
🚀 Usage (Quick Start)
From the root folder:


npm start
This will:

Start the backend at: http://localhost:8080

Start the frontend at: http://localhost:3000

🔐 Environment Variables
Backend (backend/.env)
DB_USERNAME=your_mysql_username
DB_PASSWORD=your_mysql_password
OPENROUTER_API_KEY=your_openrouter_api_key
DB_URL=your_database_url

Update your application.properties:

properties

spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
openrouter.api.key=${OPENROUTER_API_KEY}
spring.datasource.url=${DB_URL}



🔮 Future Scope
🌐 Multi-language & multi-currency support

🌓 Dark mode support

🤖 AI-based trip cost prediction

📲 PWA (offline support)

🤝 Contributing
Fork the repository 🍴

Create a feature branch 🌿

Commit your changes ✅

Open a Pull Request 🚀

📜 License
This project is licensed under the MIT License.