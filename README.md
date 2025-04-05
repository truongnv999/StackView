# 📦 StackLayoutCustom

A sleek and customizable Android layout that stacks multiple `CardView`s vertically and allows **infinite upward swipe** with smooth animations.

![GitHub CI](https://img.shields.io/github/actions/workflow/status/truongnv999/StackView/android.yml?label=CI&logo=github&style=flat-square)
[![](https://jitpack.io/v/truongnv999/StackView.svg)](https://jitpack.io/#truongnv999/StackView)

---

## 🚀 Features

- 🔁 Infinite swipe loop for stacked `CardView`s
- ✨ Smooth animation using `OvershootInterpolator`
- 📐 Auto-measured stacked layout with decreasing size
- 🪶 Lightweight and customizable
- 🧩 Header `TextView` support
- 🧱 Easy to integrate into any Android project

---

## 📦 Installation

This library is available via [JitPack.io](https://jitpack.io).

### Step 1: Add JitPack repository

In your **project-level** `build.gradle`:

```gradle
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
