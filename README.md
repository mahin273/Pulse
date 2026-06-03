# ⚡ Pulse Optimizer

Pulse is a modern, high-performance system and kernel tuner for Android. Designed for power users who want ultimate control over their hardware, Pulse brings the best optimization tools from classic managers like **EX Kernel Manager (EXKM)**, **Scene**, and **Franco Kernel Manager (FKM)** into a single, cohesive Jetpack Compose interface.

Whether you want to squeeze out extra frames in games, maximize battery life, route power directly to prevent heat, or block systemless ads, Pulse provides low-level kernel variables control with standard-setting safety features.

---

##  Screenshot
**Coming Soon** 

---

##  Core Features & Tuning Nodes

### 1. CPU Scheduler & Governor Tuner
* **Hardware Cores Hotplugging**: Dynamically toggle symmetric LITTLE (C0-C3) and BIG (C4-C7) cores online/offline to optimize power consumption.
* **Dynamic Clocks Config**: View real-time frequencies and lock minimum/maximum frequency limits per cluster.
* **Collapsible Governor Tunables**: Edit kernel scaling governor parameters (e.g., `rate_limit_us`, `hispeed_load`, `up_threshold`) directly through a clean collapsible parameter card.
* **Process CPU Affinity Masking**: Taskset processes (like `surfaceflinger` or custom PIDs) to dedicate specific cores, preventing scheduling jitter.
* **IRQ routing**: Migrate heavy hardware interrupts (like Wi-Fi `wlan0` or GPU `kgsl-3d0`) to specific clusters.

### 2. RAM & Virtual Memory Tuner
* **Virtual Memory Latency**: Adjust VM variables like `swappiness` and `vfs_cache_pressure` dynamically.
* **zRAM Swap Disk**: Set zRAM disk size and choose hardware compression algorithms (e.g., `zstd`, `lz4`).
* **LMK Profiles**: Quick-toggle Low Memory Killer presets (Aggressive, Balanced, Conservative) to modify background app garbage collection limits.
* **OOM Weights Adjuster**: Calibrate Out-Of-Memory weight adjustments to prevent system-critical processes from being closed under heavy load.

### 3. Application Profile Automator
* **Foreground Activity Polling**: A background daemon monitors the active app and automatically applies hardware governors, screen refresh rates, and core configurations.
* **ART compilation targets**: Optimize package compilation modes (e.g., `speed`, `speed-profile`, `quicken`, `interpret-only`) directly using dex2oat.

### 4. Advanced Hardware Tweaks
* **KCAL Color Calibrator**: Calibrate screen Red, Green, Blue multiplier channels, contrast, saturation, and hue matrices. Includes horizontal scrollable profile chips (**sRGB, DCI-P3, AMOLED Vibrant, Warm Cinema, Cold Tech**).
* **Sound Digital Gain Controls**: Directly adjust headphone, speaker, and microphone gain registers.
* **Dynamic Wakelocks Blocker**: A wake-lock suspend list with a block filter action next to active sleep-blocking nodes.
* **Battery Cycles Diagnostics**: Estimates battery health degradation curves and charge cycle transitions using persistent telemetry databases.
* **Smart Charging & Bypass Charging**:
  * **Smart Charging**: Automatically cuts charging current when a capacity limit threshold is reached to prevent overcharging.
  * **Bypass Charging**: Directly powers the board from the charger, completely bypassing the battery to eliminate charging heat during gaming or intensive workloads.
* **SELinux security status**: Switch betweenstrict Enforcing security sandboxes and Permissive debugging modes.
* **Systemless Ad-Blocker**: Easily write hosts loopback file maps to block ad networks.
* **Self-Correcting Boot Flasher**: Backup active boot images or flash custom kernels safely.

---

##  Premium Safety Protocols

To ensure that low-level optimizations don't compromise device stability or brick partitions, Pulse integrates two crucial safety mechanisms:

### 1. 15-Second Safe Revert Countdown
When applying high-performance or extreme eco modes, the app starts a **15-second safety timer**. If the device locks up, overheats, or panics, the kernel variables automatically roll back to their balanced defaults. To keep the tweaks running permanently, you simply click the **Dismiss** prompt in the overlay indicator once you verify the system is stable.

### 2. Self-Correcting Block Partition Finder
Raw block writing is highly platform-dependent. Pulse handles this safely using a multi-layered discovery algorithm during boot backups and flashes:
1. Searches the active boot slot suffix (`ro.boot.slot_suffix`) to check slot-aware paths (e.g., `boot_a` or `boot_b` for modern A/B layout devices).
2. Probes alternate SoC structures (like `/dev/block/bootdevice/by-name/`).
3. Falls back to a recursive `find /dev/block` command under root as a last resort.
If no block is found or root writes are blocked by SELinux, the app intercepts the failure, reports the error details, and gracefully falls back to a sandbox simulation mode.

---

##  Sandbox & Simulation Mode
Pulse runs beautifully even on **non-rooted** devices! When the app initializes, the [RootController](app/src/main/java/com/example/root/RootController.kt) tests for generic root access using a background thread. If root is missing:
* The app automatically enables **Sandbox Mode**.
* System command outputs and direct `/sys` paths writes are emulated using simulated state buffers.
* UI sliders, metrics, overlays, and consoles remain fully active and responsive, making it an excellent platform for testing layouts and configurations without risk.

---

##  Tech Stack & Architecture
* **Language**: Kotlin 1.9+
* **UI toolkit**: Jetpack Compose (Declarative UI, Material 3)
* **Local Database**: Room DB (SQLite) for profiles, scripts, and battery state history.
* **Asynchronous Streams**: Coroutines + StateFlow / SharedFlow for responsive real-time metrics.
* **State Management**: Android Architecture Components (ViewModel).

---

##  Building and Verifying

### Run Unit Tests
```powershell
.\gradlew test
```

### Compile Debug APK
```powershell
.\gradlew assembleDebug
```
