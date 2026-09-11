# 小尘课簿架构（V2）

## 分层

```text
Compose UI
  ├─ CalendarScreen / StatisticsScreen / SettingsScreen
  └─ CourseNavHost（仅日历、统计、设置；历史为统计二级页）
        ↓
ViewModel + StateFlow
  ├─ CalendarViewModel：月份、单日录入、月/学期摘要
  ├─ StatisticsViewModel：本月/学期/年度/全部统计
  └─ SettingsViewModel：学期、预设、导入覆盖/合并
        ↓
TeachingRepository
        ↓
Room TeachingDatabase（每日原始记录 + 学期日期范围）

QuickPresetStore（DataStore Preferences）
BackupManager（SAF JSON / UTF-8 BOM CSV）
```

## 应用边界与升级标识

- `namespace` 为 `com.xingchen.xiaochengkebu`；为兼容 `xiaocheng-kebu-1.0.0`，发布包 `applicationId` 保持为 `com.example.deviceasset`。
- `App` 只创建 `CourseContainer`，其中包含 `TeachingDatabase`、`TeachingRepository`、`QuickPresetStore` 和 `BackupManager`。
- 数据库文件名为 `xiaocheng_kebu.db`；Room schema version 为 2，通过 `MIGRATION_1_2` 从 1.0.0/v1 原地升级，不使用破坏性迁移；不删除可能存在的其它数据库文件。

## 数据边界

- `TeachingRecord` 以 `dateEpochDay` 为主键，一天一条记录，保存授课、晚辅和备注。
- `Semester` 只保存日期范围；记录所属学期由查询时按范围动态判断，不写入记录表。
- 统计值全部由原始记录计算，不保存月/学期/年度冗余计数。
- Repository 拒绝负数课时和反向日期范围；空记录（两个计数为 0 且备注为空）直接删除。

## 导入安全

导入流程先在内存中解析并校验 `schemaVersion`、日期范围、非负计数、重复日期和重复学期，再由设置页选择合并或覆盖。学期以 `name + startEpochDay + endEpochDay` 去重，当前学期写入、合并和覆盖都在 Room `withTransaction` 中完成；解析失败或写入失败时不会留下半套数据。JSON/CSV 文件读写统一运行在 `Dispatchers.IO`。

## 包结构

`course/domain` 放模型，`course/data/local` 放 Room，`course/data/repository` 放数据访问抽象，`course/data/settings` 放 DataStore，`course/data/backup` 放文件交换，`course/ui` 按页面拆分，`course/navigation` 只负责路由。

## 运行约束

应用默认启动日历，完全离线且不申请网络或传统外部存储权限。仅保留课簿所需的宣纸、水墨和印章资源；标题使用衬线字体，正文使用 Sans Serif。版本展示读取 `BuildConfig.VERSION_NAME`，不在 UI 中硬编码。
