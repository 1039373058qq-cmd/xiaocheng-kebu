# 开发工作流与审核记录

## 参考资源审查

对用户提供的参考 APK 进行了只读检查：确认原包为 `com.example.deviceasset`、版本 `0.4.2`，并包含宣纸背景、水墨卡片纹理、印章/书本图标和设备插图。新实现仅复用视觉资源，不复用旧设备资产业务逻辑。

## 实施阶段

1. 环境、Gradle、Android SDK 和 Git 状态审查。
2. 建立 Compose/Material 3、Navigation、Room、DataStore 和 Repository 骨架。
3. 完成 `TeachingRecord`、`Semester`、日期范围统计和当前学期约束。
4. 完成周一开周的自定义月历、日录入 Bottom Sheet、快速预设、备注和删除确认。
5. 完成月/学期/年度/全部统计及按月历史记录。
6. 完成学期管理、日期选择器、SAF JSON/CSV 导入导出和覆盖事务。
7. 完成宣纸/水墨视觉、无障碍描述、空状态与错误提示。

## 独立化清理

- 正式 `namespace/applicationId` 统一为 `com.xingchen.xiaochengkebu`，源码和测试 package 已迁移到同名目录。
- 删除旧 DeviceAsset 的数据库、Repository、领域模型、设备列表/编辑/详情/使用助手页面及对应测试；只保留课簿的日历、统计、设置和数据层。
- 删除全部 `device_*.png`；保留并压缩课簿所需的启动图标、宣纸背景和水墨卡片纹理。
- 将旧 `cardiology-*` knowledge pack 移出 `app/src/main/assets`，因此 Android source set 和 APK 均不再包含 `knowledge-packs` 或 Ultrasound Workbench 资源。
- Room schema 只保留 `com.xingchen.xiaochengkebu.course.data.local.TeachingDatabase/1.json`。

## 数据安全边界

- 每日记录以 `dateEpochDay` 为主键，Upsert 保证一天一条。
- 统计值从原始记录动态计算，不写入冗余总数。
- 导入文件先解析和校验，覆盖模式经过二次确认并使用 Room 事务。
- 不请求 INTERNET、联系人、定位、相机或传统外部存储权限。

## 最终验证

```text
./gradlew clean build                   ✅
Debug/Release 编译                     ✅
Debug/Release 单元测试                 ✅
Lint                                    ✅
aapt dump badging                      ✅ versionName=1.0.1 / label=小尘课簿 / applicationId=com.xingchen.xiaochengkebu
```

Debug APK 清理前为 81,365,316 bytes（约 81.1 MB），清理后为 58,863,717 bytes（约 58.9 MB）；Release unsigned APK 为 44,539,399 bytes（约 44.5 MB）。APK 内容扫描确认不存在 `device_*.png`、旧 Device 类、`device_asset.db`、`assets/knowledge-packs`、`cardiology-*` 或 `ultrasound-*`。

当前开发环境没有连接 Android 真机或模拟器，因此没有执行启动截图级验证；构建出的 Debug APK 可直接安装。
