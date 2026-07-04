# Chicken On Fire

Android app for **Chicken On Fire**, a restaurant chain, letting customers browse branches, order food for delivery or pickup, and pay online.

## Features

- Branch/restaurant discovery via map and list, with open/closed status and delivery area lookup
- Menu browsing with categories, items, and customizable add-ons
- Basket, checkout, and order history with reorder support
- Address management with Google Places autocomplete and saved locations
- Sign in / account creation via email or Facebook
- Online payments via Stripe and MyFatoorah, including KNET web checkout
- Order reviews and post-delivery feedback (food quality, packaging, delivery time)
- Push notifications via Firebase Cloud Messaging
- Vouchers/promo codes
- Arabic and English localization

## Tech stack

- **Language:** Java
- **Build system:** Gradle (Android Gradle Plugin 3.5.4)
- **Min/target/compile SDK:** 21 / 30 / 30
- **Key libraries:** Firebase (Analytics, Messaging), Google Maps & Places, Facebook SDK, Stripe, MyFatoorah, Retrofit, Volley, OkHttp, Glide, Gson, ACRA

## Getting started

### Prerequisites

- Android Studio (with Android SDK 30 and build tools 29.0.3 installed)
- JDK 8

### Setup

1. Clone the repository.
2. Open the project in Android Studio and let Gradle sync.
3. This project requires a Firebase project configuration (`app/google-services.json`) and valid API keys for Google Maps/Places, Facebook, Stripe, and MyFatoorah — these are not included and must be supplied before the app will build and run correctly.
4. Build and run:
   ```
   ./gradlew assembleDebug
   ```
   or use the **Run** button in Android Studio.

## Project structure

```
app/src/main/java/com/orderzzteam/chickenonfire/
├── *.java              # Activities, adapters, and models (account, basket, checkout, menu, orders, reviews, etc.)
└── tools/               # Shared UI helpers, dialogs, feedback widgets, and Firebase messaging service
```

## Signing

Release builds are signed with `keystore.jks` (see `app/build.gradle` / Android Studio signing config). Keep signing credentials out of version control in new work going forward.
