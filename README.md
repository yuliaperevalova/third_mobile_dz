# Ghibli Companion

**Автор:** Перевалова Юлия Владимировна
**Группа:** Б9123-09.03.03пикд2

---

## API

**Studio Ghibli API** — `https://ghibliapi.vercel.app/`

Используемые эндпоинты: `GET /films`, `/films/{id}`, `/people`, `/locations`, `/species`, `/vehicles` (все 5).

---

## Что добавлено

Приложение превращено из витрины 22 фильмов с одним «избранным» в персональный offline-first companion:

- **Records** — статус, оценка (0–10), текстовая заметка на каждый фильм
- **Collections** — пользовательские коллекции фильмов (M–N, создание/переименование/удаление/добавление/удаление фильмов)
- **Ghibli Universe** — персонажи, локации, расы, транспорт (4 справочника, M–N cross-ref с фильмами)
- **Pinned** — универсальные закреплённые сущности любого типа с заметкой
- **History** — автосохранение просмотров с дедупом за 5 мин, лимит из настроек, swipe-to-delete
- **Settings** — тема, TTL кэша, Wi-Fi only, лимит истории, sort order, очистка данных, обновление вселенной, экспорт/импорт бэкапа
- **Offline-first** — полная предзагрузка каталога и справочников, чтение только из Room, флаги isOffline/isStale
- **WorkManager** — initial preload (one-time, requiresNetwork) + periodic backup (раз в неделю, requiresStorageNotLow)
- **Bottom Nav** — Films / Collections / Universe / History / Settings
- **Backup** — JSON-экспорт всех пользовательских данных (records + collections + pins + history) с ротацией 4 файлов

---

## Room: таблицы (9 entity)

| Таблица | Назначение |
|---|---|
| `film_cache` | Кэш каталога фильмов (с `lastFetchedAt` для TTL) |
| `user_film_record` | Статус, оценка, заметка по фильму |
| `collection` | Коллекция (id, name, color, createdAt) |
| `collection_film_cross_ref` | M–N коллекция↔фильм |
| `person`, `location`, `species`, `vehicle` | Справочники вселенной |
| `film_person/location/species/vehicle_cross_ref` | M–N API-side связи |
| `pinned_entity` | Закреплённые сущности (composite PK) |
| `recent_view` | История просмотров |

DataStore — только настройки.

---

## Сценарии (4 шт)

1. **Records** (substantial) — простановка статуса/оценки/заметки на FilmDetailScreen, фильтр по статусу на FilmsListScreen
2. **Collections** (substantial) — создание/переименование/удаление коллекций, добавление/удаление фильмов, просмотр списка и деталей
3. **Ghibli Universe + Pinned** (substantial) — 4 справочника, детальные страницы персонажей/локаций, закрепление любого типа сущности
4. **History** (базовый) — автосохранение, rail на главном экране, экран истории с очисткой и swipe-to-delete

---

## Offline-first

- UI читает только из Room
- Сеть — WorkManager (InitialPreloadWorker грузит все 5 эндпоинтов) + pull-to-refresh
- `NetworkMonitor` → `Flow<Boolean>` (ConnectivityManager)
- `IsCatalogueStaleUseCase` — проверка TTL (`lastFetchedAt` + настройка пользователя)
- `OfflineBanner` / `StaleBanner` на FilmsListScreen

## Фоновая обработка

- `InitialPreloadWorker` — однократный при первом запуске, грузит все эндпоинты
- `BackupUserDataWorker` — периодический раз в неделю, экспорт JSON
- Ротация: хранится до 4 backup-файлов

---

## Тесты

### Unit-тесты (21 шт)

- `FilmsListViewModelTest` (7) — состояния, offline-флаг, search, filter
- `SettingsViewModelTest` (1) — двусторонняя связка DataStore
- `FilmDetailViewModelTest` (1) — combine(film, record) через Turbine
- `HistoryViewModelTest` (4) — загрузка, clearAll, deleteItem, реактивность
- `CollectionDetailViewModelTest` (1) — Turbine
- `GhibliFilmsRepositoryTest` (2) — refreshFilms, getFilmById fallback
- `IsCatalogueStaleUseCaseTest` (3) — expired, within TTL, never fetched
- `AddFilmToCollectionUseCaseTest` (1) — идемпотентность
- `RecordOpenUseCaseTest` (1) — дедуп в окне

### Android-тесты (36 шт)

- `Migration2to3Test` — перенос favourite_films → user_film_record
- `UserFilmRecordDaoTest` — upsert/delete/observe
- `CollectionDaoTest` — M–N add/remove, CASCADE
- `CollectionDaoTest` (cross-ref) — FK constraint
- `UniverseDaoTest` — батч insert, cross-ref CASCADE
- `PinnedEntityDaoTest` — pin/unpin, observe
- `RecentViewDaoTest` — дедуп за 5 мин, лимит
- `BackupRepositoryTest` — round-trip export→import, ротация
- `InitialPreloadWorkerTest` — networkFailure, success, maxRetries
- `BackupUserDataWorkerTest` — export, ротация
- `FavouriteFilmDaoTest` (3) — legacy (удалён в Phase 43+44)
- `FavouritesRepositoryTest` (3) — legacy (удалён)
- `FilmsListScreenTest` (5) — error, success, filmClick, collections, empty
- `NavGraphIntegrationTest` (1) — collectionsScreen
- `Migration2to3Test` — favourite_films → WATCHED
- `SettingsDataStoreTest` (1) — основной

---
