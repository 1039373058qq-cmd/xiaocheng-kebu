# 小尘课簿

小尘课簿是一款离线、极简的教师课时记录 Android 应用。打开后进入当前月份日历，点击日期即可记录授课节数、晚辅节数和备注。

## 技术栈

Kotlin、Jetpack Compose、Material 3、Room、Navigation Compose、Coroutines/Flow、ViewModel、DataStore Preferences 和 Storage Access Framework。

## 构建

```bash
export JAVA_HOME=/path/to/jdk-17
export ANDROID_SDK_ROOT=/path/to/android-sdk
./gradlew build
```

Debug APK 输出到 `app/build/outputs/apk/debug/app-debug.apk`。

## 代码入口

- `app/src/main/java/com/example/deviceasset/course/navigation`：一级导航
- `app/src/main/java/com/example/deviceasset/course/ui`：日历、统计、设置页面
- `app/src/main/java/com/example/deviceasset/course/data`：Room、Repository、DataStore、备份
- `docs/ARCHITECTURE.md`：分层和数据边界
- `docs/PRODUCT_SPEC.md`、`docs/DESIGN_SPEC.md`：产品与视觉约束
- `docs/DEVELOPMENT_WORKFLOW.md`：实现阶段和审核记录

课时数据使用独立数据库 `xiaocheng_kebu.db`。源码 namespace 仍为 `com.example.deviceasset` 以减少迁移风险，但应用安装标识已切换为 `com.xingchen.xiaochengkebu`，不会覆盖旧版“小尘记账”（`com.example.deviceasset`）。当前版本为 `1.0.1`。
