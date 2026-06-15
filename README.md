# Converter Navigator

IntelliJ-плагин для навигации по классам-конвертерам и мапперам в Java и Kotlin.
Помогает при рефакторинге быстро находить конвертеры, которые иначе видны только
через DI-зависимости.

## Возможности

- **Gutter-иконка на конвертере** — переход к типам, которые он связывает.
- **Gutter-иконка на типе (DTO/Entity)** — переход ко всем конвертерам этого типа,
  в обе стороны (тип как источник и как результат). Если конвертеров несколько
  (например ручной + MapStruct) — IDE показывает popup со всеми.
- **Search Everywhere / Navigate → Symbol** — конвертеры доступны по имени.
- **Tool Window «Converters»** — список всех конвертеров проекта, двойной клик = переход.

## Как определяется конвертер

Три настраиваемых правила (Settings | Tools | Converter Navigator):

1. **По имени** — regex с двумя группами, по умолчанию `^(\w+)To(\w+)(Converter|Mapper)$`.
2. **По интерфейсу** — реализация `Converter<S, T>`; по умолчанию
   `org.springframework.core.convert.converter.Converter` (S — источник, T — результат).
3. **По аннотации** — класс помечен аннотацией; по умолчанию `org.mapstruct.Mapper`,
   пары типов берутся из сигнатур методов маппера.

## Сборка

```bash
./gradlew buildPlugin
```

Готовый плагин: `build/distributions/converter-navigator-*.zip`.

## Установка

IDE → Settings | Plugins | ⚙ | Install Plugin from Disk… → выбрать собранный `.zip`.

## Разработка

```bash
./gradlew test        # юнит- и фикстурные тесты
./gradlew runIde      # запустить sandbox-IDE с плагином
./gradlew verifyPlugin # проверка совместимости (IC 2022.2)
```

Требования: JDK 17+. Целевая платформа — IntelliJ IDEA **2022.2+** (Community/Ultimate).
Код плагина ограничен Kotlin 1.7 API ради совместимости со встроенным в 2022.2 Kotlin.
