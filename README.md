# Taxi Star (v1) 🚕

**Taxi Star** is an Android application developed in **Kotlin** as a digital alternative to the traditional taxi meter. It calculates ride costs in real-time based on driver location, distance, time, and dynamic day/night pricing.

---

## Features

- **Real-time fare calculation**:
  - Base fare (day/night)
  - Price per kilometer
  - Price per minute (stops over 1 minute included)
- **Driver location tracking** via Google Maps
- Automatic detection of **day/night mode**
- Support for **multiple countries and currencies** (Morocco, France, USA, Canada, Japan, etc.)
- **Driver profile** customization (name, age, license type, etc.)
- **QR Code generation and scanning** to display driver information
- **End-of-ride notifications** including:
  - Total distance
  - Duration
  - Final fare
- Multi-language support: **French, English, Arabic**

---

## Technical Details

- **Precise location access** (`ACCESS_FINE_LOCATION`)
- **Real-time GPS tracking** using `FusedLocationProviderClient`
- **Runtime permissions** managed with `EasyPermissions`
- Dynamic calculation of **distance and time**
- **Android Notifications**
- Standard UI components: `TextView`, `Button`, `ImageView`
- Clear project structure: main parameters + driver profile screen

---

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/m-bensalah-dev/TaxiStar.git
