# Zello Message Vault Aggregator

## Overview

The Zello Message Vault Aggregator is a sophisticated Spring Boot web application designed to provide a rich, interactive user interface for browsing the message history of a ZelloWork channel. It fetches data using the Zello API and presents it in a user-friendly, paginated, and filterable format, complete with audio playback and transcription display.

This application moves beyond a simple script by offering a full-featured web front-end, robust authentication, and flexible deployment options, making it suitable for both local use and deployment on a dedicated server.

<!-- A screenshot of the main application interface, showing the message list and calendar sidebar, would go here. -->

## Key Features

-   **Dynamic User Login:** If API credentials are not hard-coded in the properties file, the application presents a secure login screen. Sessions are managed and will time out after a configurable duration.
-   **Full Message History View:** Displays a paginated list of all voice messages from a specified channel, sorted newest to oldest.
-   **Audio Playback & Transcription:**
    -   Listen to any voice message directly in the browser with an integrated audio player.
    -   View the machine-generated transcription text directly below each message, if available.
-   **Advanced Filtering & Navigation:**
    -   **Calendar View:** A full-month calendar is displayed in a sidebar, allowing users to quickly navigate to a specific date.
    -   **Date-Based Filtering:** Clicking on a date in the calendar filters the message list to show only the messages from that specific day, respecting the configured timezone.
    -   **Paginated Browsing:** The full message history can be navigated page by page.
-   **User-Friendly Display:**
    -   Shows the user's "Display Name" (e.g., "Charles Eakins") instead of their system username for better readability.
    -   All timestamps are converted from UTC and displayed in a clean, 24-hour format based on a configurable timezone.
-   **Flexible Deployment:** The application is configured with Maven profiles to be built as either:
    1.  A **standalone executable JAR** with an embedded Tomcat server.
    2.  A **deployable WAR** file for deployment to an external Tomcat server.
-   **Robust Testing Suite:** The project includes a suite of tests written with TestNG and Mockito to ensure the reliability of both the API service layer and the web controller layer.

## Requirements

-   Java 21 (or higher)
-   Apache Maven 3.6+

## Configuration

Before running the application, you must configure the following properties in the `src/main/resources/application.properties` file.

````properties
# =======================================
# Zello API Configuration
# =======================================
# Your Zello network name (e.g., my-company, not my-company.zellowork.com)
zello.api.network=your-network-name

# Leave blank to enable the user login screen.
# If filled, the application will use these credentials automatically.
zello.api.username=
zello.api.password=

# Your Zello API key from the management console.
zello.api.key=your-api-key

# =======================================
# Application Settings
# =======================================
# The specific channel to fetch messages from.
app.target-channel=Emergency Communications

# The number of messages to display on each page in the main view.
app.messages-per-page=50

# The local timezone for displaying timestamps and correctly filtering by date.
# Must be a valid TZ database name (e.g., America/New_York, Europe/London).
app.display-timezone=America/Los_Angeles

# The duration in seconds that a user's login session will remain active.
app.session-timeout-seconds=1800

# =======================================
# Server Configuration
# =======================================
server.port=8080
````
## How to Build and Run

Navigate to the root directory of the project in your terminal.
### Option 1: Running as a Standalone JAR (Default)

1. **Build the JAR:**
````bash
mvn clean package
````
2. **Find the Artifact:** The executable JAR file will be located at target/zello-vault-aggregator.jar
3. **Run the Application:**
````bash
java -jar target/zello-vault-aggregator.jar
````
### Option 2: Building a WAR for Tomcat Deployment

1. **Build the WAR:** Activate the deployable-war profile.
````bash
mvn clean package -P deployable-war
````
2. **Find the Artifact:** The WAR file will be located at target/zello-vault-aggregator.war. 
3. **Deploy:** Copy this zello-vault-aggregator.war file to the webapps/ directory of your external Tomcat server.

Once the application is running, open your web browser and navigate to http://localhost:8080.

## How to Use the Application
1. If you have left the zello.api.username and password properties blank, you will be directed to the login page. Enter your ZelloWork credentials to proceed.
2. The main view will display the most recent messages from the configured channel, with pagination controls at the bottom.
3. Use the calendar on the right to navigate between months.
4. Click on any date in the calendar to view all messages sent on that specific day.
5. When in the single-day view, a "Return to Full History" button will appear, allowing you to go back to the default paginated view.
6. Click the "Logout" link in the footer to end your session.

## Technology Stack
- Backend: Spring Boot 3, Java 21
- Frontend: Thymeleaf, HTML5, CSS3, JavaScript
- Build Tool: Apache Maven
- Testing: TestNG, Mockito