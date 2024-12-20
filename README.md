# Project Structure

This project is organized into several packages, each responsible for a specific aspect of the application’s functionality. Below is an overview of each package and its primary files.

## Packages Overview

- **`activity`**: Contains the main activity that initializes and manages navigation within the app.
- **`data`**: Holds data models that represent key entities within the application, such as `Asset`, `Portfolio`, and `User`.
- **`ui`**: Contains UI components, grouped into sub-packages:
  - **`pages`**: Organized into `authentication` and `main` sub-packages, which contain the various screen Composables used in the app.
  - **`theme`**: Defines the app’s color scheme, typography, and other theming attributes.
- **`viewmodel`**: Contains `ViewModel` classes that manage the app’s data and handle business logic.
- **`worker`**: Handles background tasks such as checking price alerts when the app is not in use.

## Detailed Package and File Descriptions

### **1. `activity`**
- **MainActivity.kt**: The entry point of the application. This activity manages navigation throughout the app using Jetpack Compose’s navigation components. It initializes the start screen based on the user's login status and provides a single point for handling app-wide navigation quirks.

---

### **2. `data`**
This directory contains all data-related files, including models and repositories.

- **`model`**
  - **`Asset.kt`**: Represents individual assets like stocks or cryptocurrencies.
  - **`Portfolio.kt`**: Defines the structure of a user’s portfolio containing multiple assets.
  - **`PriceAlert.kt`**: Represents a price alert with fields for asset, price, and date.
  - **`SortingCriteria.kt`**: Defines sorting options for filtering and displaying assets.
  - **`User.kt`**: Represents a user in the system, holding details like email and portfolios.

- **`repository`**
  - **`AiRepository.kt`**: Provides functions for AI-related operations, such as predictions and analytics.
  - **`AiRepositoryTestImp.kt`**: A test implementation of the AI repository.
  - **`AssetRepository.kt`**: Manages CRUD operations for assets.
  - **`AssetRepositoryTestImp.kt`**: A test implementation of the asset repository for development.
  - **`PriceAlertRepository.kt`**: Handles operations for adding, removing, and retrieving price alerts.
  - **`PriceAlertRepositoryTestImp.kt`**: A test implementation of the price alert repository.

---

### **3. `ui`**
This directory contains all the user interface components for the application.

#### **`pages`**
- **`authentication`**
  - **AuthenticationUI.kt**: Handles user login and registration screens.
  - **PasswordRecovery.kt**: Allows users to recover their password.
  - **RegistrationSuccess.kt**: Displays a success message after registration.

- **`main`**
  - **AssetDetail.kt**: Displays detailed information about a specific asset.
  - **Feedback.kt**: Allows users to provide feedback.
  - **MainPage.kt**: The primary screen showing asset summaries.
  - **PortfolioBuilder.kt**: A tool for constructing and managing portfolios.
  - **Support.kt**: Provides help and support options for users.

#### **`theme`**
- **Color.kt**: Defines the app’s color scheme.
- **Theme.kt**: Sets up the Material 3 theme used throughout the app.
- **Type.kt**: Specifies typography styles used across the UI.

---

### **4. `viewmodel`**
This directory contains ViewModel classes that manage data and business logic.

- **AIViewModel.kt**: Handles AI-related operations, such as fetching predictions and analysis for assets.
- **AssetPriceViewModel.kt**: Manages asset data, including real-time price updates.
- **PortfolioViewModel.kt**: Handles portfolio management, including adding and removing assets.
- **PriceAlertViewModel.kt**: Manages price alerts and checks if asset prices meet alert thresholds.

---

### **5. `worker`**
- **PriceAlertWorker.kt**: A background worker that checks for price alerts and triggers notifications even when the app is not running.
# android-app-groupproject
