# Zello Message Vault Aggregator

## Overview

The Zello Message Vault Aggregator is a sophisticated Spring Boot web application designed to provide a rich, interactive user interface for browsing the message history of a ZelloWork channel. It fetches data using the Zello API and presents it in a user-friendly, paginated, and filterable format, complete with audio playback and transcription display.

A key feature of this application is its **first-run setup wizard**. Instead of requiring manual editing of configuration files, the application will guide you through a web-based setup process the first time it is launched, creating an external `application.properties` file that is decoupled from the application artifact.

## Key Features

-   **First-Run Setup Wizard:** On initial launch, the application detects that it's unconfigured and presents a setup page to create the necessary external `application.properties` file.
-   **Dynamic User Login:** After setup, the application presents a secure login screen. Sessions are managed and will time out after a configurable duration.
-   **Full Message History View:** Displays a paginated list of all voice messages from a specified channel, sorted newest to oldest.
-   **Audio Playback & Transcription:**
    -   Listen to any voice message directly in the browser with an integrated audio player.
    -   View the machine-generated transcription text directly below each message, if available.
-   **Advanced Filtering & Navigation:**
    -   **Calendar View:** A full-month calendar is displayed in a sidebar, allowing users to quickly navigate to a specific date.
    -   **Date-Based Filtering:** Clicking on a date in the calendar filters the message list to show only the messages from that specific day, respecting the configured timezone.
    -   **Live Refresh for Today's View:** When viewing the filtered list for the current date, the application will automatically check for and display new messages.
-   **User-Friendly Display:**
    -   Shows the user's "Display Name" (e.g., "Charles Eakins") instead of their system username for better readability.
    -   All timestamps are converted from UTC and displayed in a clean, 24-hour format based on a configurable timezone.
-   **Flexible Deployment:** The application is configured with Maven profiles to be built as either:
    1.  A **standalone executable JAR** with an embedded Tomcat server.
    2.  A **deployable WAR** file for deployment to an external Tomcat server.
-   **Robust Testing Suite:** The project includes a suite of tests written with TestNG and Mockito to ensure the reliability of the application's core logic.

## Requirements

-   Java 21 (LTS)
-   Apache Maven 3.6+

## Configuration

The application uses an external configuration file located at `config/application.properties` relative to where the application is run. **You do not need to create this file manually.** The application will guide you through creating it on the first launch.

The generated file will contain the following properties:

```properties
# =======================================
# Zello API Configuration
# =======================================
# Your Zello network name (e.g., my-company, not my-company.zellowork.com)
zello.api.network=your-network-name

# These are intentionally left blank to force the user login screen.
zello.api.username=
zello.api.password=

# Your Zello API key from the management console.
zello.api.key=your-zello-api-key

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

# Time in seconds before timeout to show the warning dialog.
app.session-warning-seconds=15

# Interval in seconds to check for new messages on refreshable pages.
app.auto-refresh-seconds=60

# =======================================
# Server Configuration
# =======================================
server.port=8080
```
# How to Build and Run
Navigate to the root directory of the project in your terminal.
## Option 1: Running as a Standalone JAR (Default)
1. **Build the JAR:**
```Bash
mvn clean package
```
2. **Find the Artifact:** The executable JAR file will be located at target/zello-vault-aggregator.jar.
3. **Run the Application:**
```Bash
java -jar target/zello-vault-aggregator.jar
```

## Option 2: Building a WAR for Tomcat Deployment
1. **Build the WAR:** Activate the deployable-war profile.
```Bash
mvn clean package -P deployable-war
```
2. **Find the Artifact:** The WAR file will be located at target/zello-vault-aggregator.war.
3. **Deploy:** Copy this zello-vault-aggregator.war file to the webapps/ directory of your external Tomcat server.

# First-Time Setup Workflow

1. After starting the application for the first time, open your web browser and navigate to http://localhost:8080 (or the appropriate server address).
2. You will be automatically redirected to the **Setup Page**.
3. Fill in the required configuration details (Zello Network Name, API Key, etc.) and click "Save Configuration".
4. You will be redirected to a success page with the message: **"IMPORTANT: You must now restart the application for the new configuration to take effect."**
5. Stop the application (e.g., by pressing Ctrl+C in the terminal where it's running).
6. Start the application again using the same command as before (java -jar ... or by starting Tomcat).
7. The application will now be fully configured.

# How to Use the Application (After Setup)

1. Navigate to the application's URL. You will now be directed to the **Login Page**.
2. Enter your ZelloWork credentials to proceed.
3. The main view will display the most recent messages from the configured channel, with pagination controls at the bottom.
4. Use the calendar on the right to navigate between months.
5. Click on any date in the calendar to view all messages sent on that specific day.
6. When in the single-day view, a "Return to Full History" button will appear, allowing you to go back to the default paginated view.
7. Click the "Logout" link in the footer to end your session.

# Technology Stack

* **Backend**: Spring Boot 3, Java 21
* **Frontend**: Thymeleaf, HTML5, CSS3, JavaScript
* **Build Tool**: Apache Maven
* **Testing**: TestNG, Mockito

**Copyright 2025 Charles L. Eakins**