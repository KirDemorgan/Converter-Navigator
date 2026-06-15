# Converter Navigator — план разработки плагина

Плагин для JetBrains IDE: поиск, подсветка и навигация по классам-конвертерам
(мапперам) в Java и Kotlin проектах. Локальный, без внешних запросов.

## 1. Цели (из требований)

1. **Список конвертеров** — получить все конвертеры проекта/модуля.
2. **Навигация тип → конвертер** — стоя на DTO/Entity, прыгнуть к конвертерам этого типа.
3. **Подсветка в gutter** — иконка рядом с каждым классом-конвертером.

Признак «конвертера» — ТРИ сигнала, все настраиваемые:
- по имени: regex, напр. `^(\w+)To(\w+)(Converter|Mapper)$` — пара From/To из имени класса.
- по интерфейсу: список FQN, напр. `com.acme.Converter<F, T>` — пара From/To из дженериков.
- по аннотации: список FQN, напр. `org.mapstruct.Mapper` — пары From/To из сигнатур
  методов маппера (`Target method(Source s)`). Один маппер = несколько конверсий.

Навигация двунаправленная: на типе `T` показываем ВСЕ конверсии, где `T` участвует
как источник ИЛИ как цель. Если конверсий/конвертеров несколько (ручной + mapstruct
для той же пары — обычная ситуация при миграции) — IDE показывает popup со всеми
целями. Цель навигации = `PsiElement`: класс-конвертер ИЛИ конкретный метод маппера.

## 2. Ограничения

- Корпоративная среда: только официальный IntelliJ Platform SDK (Apache 2.0).
- Ноль сетевых запросов, всё работает на локальном PSI/UAST индексе.
- Java + Kotlin одним кодом через UAST (`UClass`, `UMethod`).

## 3. Стек

- Язык плагина: **Kotlin**, JDK 17.
- Сборка: **IntelliJ Platform Gradle Plugin 2.x** (`org.jetbrains.intellij.platform`).
- Платформа сборки: IntelliJ IDEA Community 2024.1.
- `sinceBuild = 233` (2023.3+), `untilBuild` открытый — широкая совместимость,
  т.к. целевые версии в команде разные.
- Базовый пакет: **`xyz.demorgan`**.
- Распространение: **локальная .zip** (Settings | Plugins | Install from disk).
- Зависимости модулей платформы: `com.intellij.modules.platform`,
  `com.intellij.java`, `org.jetbrains.kotlin`, плюс UAST (`com.intellij.modules.lang`).

## 4. Структура проекта

```
E:\ConverterNavigator\
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── .gitignore
├── PLAN.md
└── src/main/
    ├── kotlin/xyz/demorgan/
    │   ├── detector/
    │   │   ├── Conversion.kt               // (target: PsiElement, fromType, toType, источник)
    │   │   ├── ConverterDetector.kt        // применяет правила к UClass/UMethod
    │   │   └── DetectionRule.kt            // NameRule | InterfaceRule | AnnotationRule
    │   ├── index/
    │   │   └── ConverterIndex.kt           // FileBasedIndex: typeFqn -> converterFqn
    │   ├── markers/
    │   │   └── ConverterLineMarkerProvider.kt  // UAST, gutter + навигация
    │   ├── search/
    │   │   └── ConverterSymbolContributor.kt    // Search Everywhere
    │   ├── toolwindow/
    │   │   ├── ConverterToolWindowFactory.kt
    │   │   └── ConverterListPanel.kt
    │   └── settings/
    │       ├── ConverterSettings.kt        // PersistentStateComponent
    │       └── ConverterSettingsConfigurable.kt
    └── resources/
        ├── META-INF/plugin.xml
        └── icons/converter.svg
```

## 5. Точки расширения (plugin.xml)

| Фича                       | Extension point                          |
|----------------------------|------------------------------------------|
| Индекс тип→конвертер        | `com.intellij.fileBasedIndex`            |
| Gutter + навигация          | `com.intellij.codeInsight.lineMarkerProvider` (UAST) |
| Search Everywhere           | `com.intellij.gotoSymbolContributor`     |
| Панель со списком           | `com.intellij.toolWindow`                |
| Настройки                   | `com.intellij.applicationConfigurable` + `applicationService` |

## 6. Ключевые компоненты

### 6.1 ConverterDetector (ядро)
Вход: `UClass` (и его `UMethod` для mapstruct). Применяет правила из настроек,
возвращает список `Conversion` (на класс может приходиться несколько — у mapstruct):
- NameRule: матчит имя класса regex'ом, достаёт From/To из групп. Цель = класс.
- InterfaceRule: реализует ли класс один из FQN-интерфейсов; From/To из дженериков
  супертипа (UAST/PSI generics). Цель = класс. Соглашение: `Converter<S, T>` → S=from,
  T=to (метод `T convert(S)`); порядок дженериков настраиваемый на случай инверсии.
  Матчинг по полному FQN (не короткому имени), чтобы не нахватать чужих `Converter`.
- AnnotationRule: помечен ли класс одной из FQN-аннотаций (напр. `org.mapstruct.Mapper`);
  для каждого подходящего метода `Target m(Source s)` достаём пару From/To. Цель = метод.
Модель `Conversion(target: PsiElement, fromType: String, toType: String, source: RuleKind)`.

### 6.2 ConverterIndex (производительность, двунаправленный)
`FileBasedIndex<String, List<ConversionRef>>`: ключ — FQN типа. Каждую конверсию
индексируем по ОБОИМ концам (и From, и To), чтобы навигация работала в обе стороны.
Значение указывает на цель (класс или метод) — FQN класса + опц. сигнатура метода.
Инкрементально пересобирается платформой. Lookup «конверсии для типа X» = O(1),
без обхода проекта. Источник данных — тот же ConverterDetector на уровне PSI файла.

### 6.3 ConverterLineMarkerProvider
`RelatedItemLineMarkerProvider`, зарегистрирован на UAST (Java+Kotlin).
- На классе/методе-конвертере: иконка «это конвертер» (фича 3).
- На DTO/Entity: иконка «есть конверсии этого типа» (фича 2) — цели берём из
  ConverterIndex по обоим концам; если целей несколько (ручной + mapstruct) —
  `NavigationGutterIconBuilder.setTargets(...)` сам даёт popup со списком.

### 6.4 ConverterSymbolContributor
`ChooseByNameContributorEx` — конвертеры попадают в Navigate | Symbol и
Search Everywhere. Имена берём из ConverterIndex (быстро, без сканирования).

### 6.5 ConverterToolWindow
ToolWindow со списком/деревом всех конвертеров проекта (фича 1),
двойной клик — переход. Источник — ConverterIndex.

### 6.6 Settings
`PersistentStateComponent`: regex-правила имени + список FQN интерфейсов.
UI-страница в Settings | Tools | Converter Navigator.
Дефолты: имя `^(\w+)To(\w+)(Converter|Mapper)$`, интерфейсы — пусто (заполняет команда),
аннотации — `org.mapstruct.Mapper` (включено по умолчанию).

## 7. Критерии проверки (как поймём, что готово)

- [ ] Плагин собирается: `./gradlew buildPlugin` без ошибок.
- [ ] `./gradlew runIde` поднимает sandbox-IDE с плагином.
- [ ] На тестовом проекте (Java + Kotlin) класс `FooToBarConverter` помечен иконкой.
- [ ] Класс, реализующий настроенный интерфейс, тоже помечен.
- [ ] MapStruct-маппер (`@Mapper`) помечен; пары берутся из методов.
- [ ] На классе `Foo` gutter ведёт к `FooToBarConverter` (в обе стороны: и на `Foo`, и на `Bar`).
- [ ] Когда для пары есть и ручной конвертер, и mapstruct — gutter показывает popup с обоими.
- [ ] Конвертер находится в Search Everywhere.
- [ ] ToolWindow показывает полный список конвертеров.
- [ ] Settings: смена regex/интерфейсов меняет поведение без перекомпиляции.
- [ ] На большом проекте нет фризов (индекс, не обход).

## 8. Порядок реализации

1. Скелет: Gradle + plugin.xml + иконка + git init. Проверка: `runIde` стартует.
   `.gitignore` ПЕРВЫМ делом исключает AI-артефакты (`PLAN.md`, `tmp/`, любые
   заметки/черновики) — они не отслеживаются и не пушатся. Код — без AI-маркеров.
2. Settings (модель + дефолты), без UI — чтобы было откуда брать правила.
3. ConverterDetector + unit-тесты на NameRule/InterfaceRule.
4. ConverterIndex (FileBasedIndex) поверх детектора.
5. LineMarkerProvider: сначала иконка-конвертер, потом навигация тип→конвертер.
6. gotoSymbolContributor.
7. ToolWindow.
8. Settings UI (Configurable).
9. Прогон по критериям раздела 7.

## 9. Открытые вопросы / риски

- Точная сигнатура целевых интерфейсов-конвертеров (дженерики/позиции From,To) —
  уточнить на реальных примерах из кодовой базы перед InterfaceRule.
- MapStruct: подтвердить, что фильтруем по `@org.mapstruct.Mapper` и что пары
  достаём из публичных методов-мапперов (исключая `default`/`@AfterMapping` и пр.).
- Декоратор/`uses`-композиции mapstruct и абстрактные классы-мапперы — учесть позже,
  если такие есть в кодовой базе.
