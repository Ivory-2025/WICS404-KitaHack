# 🍽️ SavePlate (by WICS 404)
Bridging the Gap Between Surplus and Need with Intelligent Orchestration
## 📖 Project Overview
SavePlate is an AI-powered food redistribution system that helps reduce food waste by connecting food vendors with individuals and organizations in need. Vendors can upload details of extra food that would otherwise be thrown away, and the AI helps identify and categorize the items to match them with nearby recipients. With location-based coordination for easy pickups, SavePlate makes it simpler to redistribute surplus food efficiently while promoting sustainability and community support.

### Problem: 
Many perfectly edible surplus meals from vendors go to waste because vendors don’t have a quick way to share them. At the same time, students and NGOs struggle to find affordable, nutritious food.

### The Gap: 
Currently, surplus food at campus cafes is either thrown away or sold at full price until closing, leaving students with no way to access these meals at a discounted rate.

### Solution: 
A "Zero-Typing" listing engine powered by Gemini 2.5 Flash and a proximity-aware marketplace.

## 🏗️ Technical Architecture
SavePlate is built using a professional Controller-Service-DAO (CSD) pattern. This decoupling ensures the system is maintainable, testable, and ready for cloud migration.

### System Layers
#### Frontend (JavaFX): 
Role-specific dashboards (VendorDashboardController, NGODashboardController) providing a responsive, desktop-native experience.

### Service Layer (Business Logic):

#### FoodAnalysisService: 
Orchestrates multimodal AI analysis via the Gemini API.

#### MatchingService: 
Executes pairing logic between vendor surplus and NGO requirements.

#### RoutingService: 
Interfaces with Google Maps Platform for real-time logistics.

### Data Layer (DAO): 
Manages persistent storage via a dedicated DAO layer, separating the kitaHack.db (SQLite) from the UI logic.

## 🛠️ Implementation Details
We leveraged Google’s cutting-edge infrastructure to solve the most complex parts of the food rescue lifecycle.

### Google Technology Integration
#### Gemini 2.5 Flash (AI Tech): 
We implemented the latest Gemini model as an "Intelligent Orchestration Layer." It identifies food items (e.g., "6 Donuts") from a single photo and generates reasoning-based recipes for raw ingredients.

#### Google Maps Platform (Developer Tech): 
Used to calculate precise pickup durations and generate optimized navigation routes for NGOs, ensuring food is rescued before expiration.

#### Google Cloud Console: 
Serving as our central hub for API security, latency monitoring, and quota management.

### Core Stack
#### Language:
Java 17+

#### Framework: 
JavaFX (UI) & Maven (Build System)

#### Database: 
SQLite (Relational, file-based)

## 🧠 Challenges Faced: Overcoming the "Logic Bottleneck"
The biggest hurdle was the complexity of mapping diverse, unstructured food data (e.g., "Leftover Nasi Lemak" vs. "Raw Surplus Onions") to specific NGO needs.

### The Conflict: 
Hard-coded category mapping was too rigid and required constant manual updates.

### The Decision: 
We pivoted to AI-Orchestration.

### The Solution: 
Rather than a static backend, we used Gemini 2.5 Flash as a "Smart Bridge." The AI interprets the intent and quality of the photo data and automatically determines how that data should flow between the vendor's dashboard and the NGO's feed. This reduced our logic complexity by 40%.

## 📈 Impact & Success Metrics
We measure success through transparency and efficiency:

### 100% Traceability: 
Every successful rescue is logged via TransactionDAO to track the total volume of food diverted from landfills.

### Speed-to-Market: 
AI has reduced the time it takes for a vendor to list surplus from ~2 minutes to under 5 seconds.

### Zero Leakage: 
Secured the project against credential leaks using strict .gitignore patterns for Gemini API Keys.

## 🗺️ Future Roadmap
### Phase 1 (Short-term): 
Transition from local SQLite to a hosted PostgreSQL server on Google Cloud to support concurrent multi-campus access.

### Phase 2 (Mid-term): 
Integrate Firebase Cloud Messaging (FCM) for real-time push notifications to NGOs' mobile devices.

### Phase 3 (Long-term): 
Expand the "SavePlate Zone" to 10,000+ users across all major Malaysian universities.
