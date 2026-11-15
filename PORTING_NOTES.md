# Immersive Weathering — порт на Fabric 26.1.2 (заметки)

Неофициальный приватный порт. Оригинал: https://github.com/Silversmith-Mods/Immersive-Weathering
(ветка `1.21.1`, фактически версия 1.20.1-2.0.5). Авторы оригинала: Ordana, MehVahdjukaar,
Keyberries, kayla_the_bee. Лицензия LGPL-3.0 (`LICENSE`), благодарности — `ATTRIBUTIONS`.

Оригинальные исходники лежат в `upstream/` (в git порта не входят, только для справки).

## Окружение
| | |
|---|---|
| Minecraft | 26.1.2 (официальные имена Mojang, без ремапа) |
| Java | 25 |
| Fabric Loader | >= 0.19.3 |
| Fabric API | 0.155.3+26.1.2 |
| Loom / Gradle | net.fabricmc.fabric-loom 1.19.4 / Gradle 9.8.2 |
| Moonlight Lib | 26.1.2-4.2.2 (бинарная зависимость с maven Modrinth, id `Z4zBrgxb`) |

Moonlight используется только через публичный API. Её код не копируется и не портируется
(Supplementaries Team License).

## Структура
- `src/main` — общий код (клиент + выделенный сервер). Клиентские классы здесь недоступны
  на уровне компиляции (`splitEnvironmentSourceSets`).
- `src/client` — только клиент (рендер, частицы, обработчики пакетов).
- Готовый jar: `build/libs/immersive_weathering-<версия>.jar` (не `-sources`).

## Этапы
- [x] Этап 1 — каркас: проект собирается, выделенный сервер стартует с Moonlight.
- [ ] Этап 2 — блоки, предметы, ресурсы и данные под новые форматы.
- [ ] Этап 3 — механики.
- [ ] Этап 4 — генерация мира, частицы, рендер, конфиг, сеть.

## Изменено относительно оригинала
- Убраны Architectury и Forge, один обычный Fabric-проект.
- Интеграции EMI, Quark, Serene Seasons, Trinkets/Curios пока не переносятся
  (решение: вернуть EMI/ModMenu в конце при необходимости).

## Известные баги / замечания
- В логе сервера есть `Registry 'moonlight:world_saved_data_type' was empty after loading` —
  это сообщение самой Moonlight, не ошибка порта.
