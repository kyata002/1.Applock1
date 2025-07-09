# 🔐 AppLock Android (Java)

Một ứng dụng khóa ứng dụng ** đơn giản nhưng hiệu quả cho Android **, được xây dựng với Java.
Ứng dụng này cung cấp một cơ chế khóa cơ bản để ngăn chặn truy cập trái phép vào các ứng dụng cụ thể, sử dụng mã PIN để xác thực.
> 🧠 ** Lưu ý: ** Dự án này là giáo dục và trình bày cách sử dụng dịch vụ tiếp cận để phát hiện các ứng dụng tiền cảnh và khóa chúng.
---

## 🚀 Features

- 🔐 Khóa bất kỳ ứng dụng nào bằng pin 4 chữ số
- 👀 Phát hiện và chặn quyền truy cập vào các ứng dụng đã chọn bằng Dịch vụ Truy cập
- Cài đặt khóa liên tục bằng cách sử dụng `SharedPreferences`
- 🧠 UI tùy chỉnh vào đầu vào và xác nhận mã pin
- 📱 Danh sách các ứng dụng đã cài đặt và chọn khóa nào để khóa

---

## 📸 Screenshots

> *(Add your screenshots here to show the main UI and lock screen)*

---

## 🛠️ Tech Stack

- **Language:** Java
- **UI:** Android XML
- **Services:** AccessibilityService
- **Storage:** SharedPreferences
- **Min SDK:** API 21 (Android 5.0)

---

## 🏗️ Project Structure (High-Level)

```
📦 com.applock
│
├── 📁 activities
│   ├── LockActivity.java          # The lock screen interface
│   └── MainActivity.java          # App list and settings
│
├── 📁 services
│   └── AppLockAccessibilityService.java  # Monitors running apps
│
├── 📁 utils
│   └── AppUtils.java              # App info & package utilities
│
├── 📁 models
│   └── AppModel.java              # Holds app info data
│
└── 📁 preferences
    └── PreferenceManager.java     # Save/retrieve locked app list & PIN
```

---

## ✅ How to Use

### 🔧 Installation

1. Clone this repository:

```bash
git clone https://github.com/kyata002/1.Applock1.git
```

2. Open the project in **Android Studio**

3. Build and run on a real device (Accessibility services may not work on emulators)

### ⚙️ Enable App Lock

1. Set your 4-digit PIN
2. Choose the apps you want to lock
3. Grant **Accessibility Permission** when prompted
4. Try opening a locked app ➜ AppLock will intervene and ask for PIN

---

## 🧩 Permissions Required

- `android.permission.PACKAGE_USAGE_STATS` – to detect foreground app
- `android.permission.SYSTEM_ALERT_WINDOW` – to show lock screen over other apps
- Accessibility Service – for real-time app monitoring

---

## ⚠️ Disclaimer

Ứng dụng này sử dụng các tính năng truy cập Android, nên được sử dụng ** về mặt đạo đức và minh bạch **. 
Đảm bảo bạn thông báo cho người dùng về việc thu thập dữ liệu nếu mở rộng dự án này.
---

## 🤝 Contributing

Feel free to fork this repo, open issues or pull requests if you'd like to improve or add new features. Contributions are welcome!

---

## 📜 License

This project is licensed under the MIT License. See [`LICENSE`](./LICENSE) for details.

---

## 👤 Author

- **Kyata Dev**  
  [GitHub](https://github.com/kyata002)

---
