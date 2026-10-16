# Project Plan

BizTrack Côte d'Ivoire - Simple Business Management for Small Businesses in Côte d'Ivoire.

Features:
1. Daily Sales Records: Quick recording of sales transactions with item selection, payment method (cash, Mobile Money like Wave/Orange Money/MTN), and receipts.
2. Inventory Management & Alerts: Real-time stock tracking, automatic low-stock notifications and alerts for small retail/service businesses.
3. Profit & Loss Calculations: Automated daily, weekly, and monthly profit/loss calculations taking into account cost prices and operating expenses.
4. Expense Tracking: Categorized expense recording (rent, utilities, supplier payments, wages) with receipt photo attachments.
5. Simple Reports & Analytics: Clear visual charts and reports for sales summary, top-selling products, cash flow, and financial health formatted in FCFA (XOF).
6. Paid Monthly Subscription & Account Access: Subscription status checker, business profile, and role-based access control (Owner vs. Staff/Employee).
7. Tailored for Côte d'Ivoire Local Market: FCFA (XOF) currency, offline-first capability with cloud backup/sync, and multi-language support (French default).

## Project Brief

# Project Brief: BizTrack Côte d'Ivoire

BizTrack Côte d'Ivoire is a simple, offline-first business management Android application designed specifically for small retail and service businesses in Côte d'Ivoire.

## Features

1. **Daily Sales & Mobile Money Integration**: Rapid recording of daily transactions supporting local payment methods (Cash, Wave, Orange Money, MTN Mobile Money) and generating receipts formatted in FCFA (XOF).
2. **Inventory Management & Low-Stock Alerts**: Real-time stock tracking with automated alerts when inventory levels fall below defined thresholds.
3. **Expense Tracking & Photo Receipts**: Categorized expense recording (rent, utilities, wages, supplier costs) with receipt photo capture capability.
4. **Profit & Loss Analytics**: Automated daily, weekly, and monthly financial tracking and visual reporting in French with FCFA currency formatting.
5. **Offline-First Sync & Role Access**: Robust offline data collection with cloud backup/sync when online, and role-based access control (Owner vs. Staff).

## High-Level Tech Stack

* **Programming Language**: Kotlin
* **UI Framework**: Jetpack Compose (Material 3)
* **Navigation & Adaptive Strategy**: Jetpack Navigation 3 (state-driven navigation) and Compose Material Adaptive library (for responsive layouts across phones and tablets)
* **Architecture & Concurrency**: Android Architecture Components (ViewModel, StateFlow), Kotlin Coroutines & Flow
* **Offline Persistence & Local Storage**: Room Database (fulfilling offline-first requirements) and DataStore
* **Dependency Injection**: Hilt (Dagger Hilt)

## Implementation Steps
**Total Duration:** 47m 35s

### Task_1_DataAndDomainSetup: Setup Room database entities, DAOs, repository layer, Hilt dependency injection, and DataStore for BizTrack Cote d'Ivoire (Sales, Inventory, Expenses, Analytics, Roles).
- **Status:** COMPLETED
- **Updates:** Configured Room entities, DAOs, Database with prepopulated data, DataStore preferences, Hilt DI modules, BizTrackRepository implementation, Application class, and unit tests. Clean build and unit tests pass.
- **Acceptance Criteria:**
  - Room entities and DAOs created for Sales, Products, Expenses
  - Repository and Hilt DI modules configured
  - Project builds successfully without errors
- **Duration:** 12m 38s

### Task_2_SalesAndInventoryFeature: Implement Sales Recording screen with local payment options (Cash, Wave, Orange Money, MTN) in FCFA XOF, and Inventory Management screen with stock tracking and low-stock alerts.
- **Status:** COMPLETED
- **Updates:** Implemented Sales Recording UI and ViewModel with FCFA formatting, local payment options (Cash, Wave, Orange Money, MTN), digital receipt dialog, stock validation, and Inventory UI with low-stock alerts and product/stock add/edit dialogs. Unit tests pass and build successful.
- **Acceptance Criteria:**
  - Sales UI records transactions in FCFA with Wave, Orange Money, MTN, Cash options
  - Inventory UI lists items and shows low-stock warning indicators
  - State flows properly updated from Room repository
- **Duration:** 8m 50s

### Task_3_ExpensesAndAnalyticsFeature: Implement Expense Tracking with categories and photo receipt capture, plus Profit & Loss analytics screen displaying daily/weekly/monthly summary in FCFA.
- **Status:** COMPLETED
- **Updates:** Implemented Expense Tracking screen with photo receipt attachment, Profit & Loss Analytics dashboard with time filters and payment breakdown, Profile & Subscription manager with role-based permissions, and Main Bottom Navigation. All 22 unit tests pass and debug assemble succeeds.
- **Acceptance Criteria:**
  - Expense recording supports category selection and image URI attachment
  - Analytics screen presents profit and loss calculations formatted in FCFA
  - Jetpack Navigation set up to seamlessly connect all screens
- **Duration:** 11m 4s

### Task_4_RunAndVerify: Run and verify application, ensure offline capability, instruct critic_agent to verify application stability (no crashes), confirm alignment with user requirements, and report critical UI issues.
- **Status:** COMPLETED
- **Updates:** critic_agent verified build stability, zero crashes, sales recording in FCFA with local payment options (Wave, Orange Money, MTN, Cash), inventory tracking with low-stock alert badges, expense tracking with receipt attachment, Profit & Loss analytics, subscription status banner, and role-based access control. All requirements met.
- **Acceptance Criteria:**
  - build pass
  - app does not crash
  - make sure all existing tests pass
  - critic_agent verifies application stability and requirement alignment
- **Duration:** 15m 3s

