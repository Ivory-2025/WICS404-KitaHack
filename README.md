# 🏗️ Technical Architecture
SavePlate follows a robust Controller-Service-DAO (CSD) pattern, ensuring clear separation of concerns and high system maintainability.

High-Level Architectural Diagram
Frontend Layer (JavaFX): Utilizes FXML for visual layout and role-based Controllers (VendorDashboardController, NGODashboardController) to handle asynchronous UI updates and user input.

Logic Layer (Service): The "Intelligence Engine."

FoodAnalysisService: Handles the multimodal integration with Gemini 1.5 Pro.

MatchingService: Executes the pairing algorithms for NGOs and surplus listings.

RoutingService: Interfaces with the Google Maps API for distance and navigation logic.

Data Layer (DAO): An abstraction layer that manages CRUD operations for the local kitaHack.db. This layer decouples the application logic from the SQLite storage, allowing for "plug-and-play" database migrations.

# 🛠️ Implementation Details
The solution leverages a "Cloud-Hybrid" approach, combining local high-performance processing with Google’s global AI infrastructure.

Core Technology Stack
Google Gemini 1.5 Pro (via Google AI Studio):

Use Case: Powering the "Snap & Sell" feature.

Implementation: We pass binary image data to Gemini with a structured prompt to return a JSON-formatted analysis of food type, quantity, and quality (e.g., "Good", "Bruised", "Expires Soon").

Google Maps Platform:

Use Case: Proximity-based alerts.

Implementation: Uses the Distance Matrix API to filter active surplus listings within a specific radius of an NGO’s registered coordinates.

JavaFX & SQLite:

The DAO pattern utilizes Prepared Statements to prevent SQL injection, ensuring that even in a prototype, data integrity and security are prioritized.

# 🧠 Challenges Faced: Bridging the "Logic Bottleneck"
During development, we encountered a significant challenge: The Logic Bottleneck.

The Conflict: Mapping highly diverse, unstructured food types (e.g., "Leftover Nasi Lemak" vs. "Raw Surplus Onions") to specific NGO needs using traditional hard-coded conditional logic was too rigid and error-prone.

The Decision: We decided to pivot from hard-coding to AI-Orchestration.

The Solution: We integrated Gemini not just as a labeler, but as a Smart Bridge. Gemini interprets the intent and utility of the food, automatically determining its flow in the matching logic. This reduced our codebase complexity by 40% and improved matching accuracy for diverse local food items.

# 🗺️ Future Roadmap: Scaling the Impact
Our roadmap transitions SavePlate from a campus-specific tool to a national food-rescue infrastructure.

Phase 1: Real-Time Mobile Expansion (0-6 Months)
Firebase Integration: Moving from a desktop-centric JavaFX model to a mobile-responsive environment using Firebase Cloud Messaging (FCM) for instant "Food Alert" push notifications to rescuers.

Cloud Migration: Swapping the local SQLite DAO for a hosted PostgreSQL instance on Google Cloud to support concurrent multi-campus access.

Phase 2: IoT & Predictive Analytics (6-18 Months)
Smart Freshness Monitoring: Integrating IoT sensors (Temperature/Humidity) that feed data directly into the listing status, allowing Gemini to update "Safety Windows" in real-time.

Predictive Surplus Reporting: Using historical data in the TransactionDAO to generate AI reports for vendors, helping them reduce over-production before it becomes waste.

Phase 3: National "SavePlate Zones" (18+ Months)
Campus-to-City Scaling: Expanding the "SavePlate Zone" model to urban residential areas and shopping malls across Malaysia.
