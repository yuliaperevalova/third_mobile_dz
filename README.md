# Studio Ghibli Films App

## Автор
**ФИО:** Перевалова Юлия Владимировна
**Группа:** Б9123-09.03.03пикд7

---

## Используемый API

**Studio Ghibli API** — `https://ghibliapi.vercel.app/`

Бесплатный REST API без аутентификации. Используемые эндпоинты:
- `GET /films` — список всех фильмов
- `GET /films/{id}` — детали конкретного фильма

---

## Что хранится в Room

**Таблица:** `favourite_films`

**Сценарий:** Избранное (Favourites)

Когда пользователь добавляет фильм в избранное, приложение запрашивает детали фильма через API и сохраняет их в локальную БД. Экран «Избранное» загружает данные напрямую из Room — без обращения к сети.

**Поля таблицы:** `id`, `title`, `original_title`, `original_title_romanised`, `description`, `director`, `producer`, `release_date`, `running_time`, `rt_score`, `url`

---

## Как проверить

1. Открыть приложение → дождаться загрузки списка фильмов
2. Нажать на иконку сердечка у любого фильма — он добавится в избранное
3. Перейти на экран «Избранное» — фильм отображается
4. **Полностью закрыть приложение** (убить процесс)
5. Открыть приложение снова → перейти в «Избранное»
6. Фильм по-прежнему там — данные сохранились в Room

---

## Тесты

### Юнит-тесты — 9 штук

**FilmsListViewModelTest (6 тестов):**
- `initialState_isLoading` — начальное состояние экрана Loading
- `loadFilms_success_emitsSuccessState` — успешная загрузка данных
- `loadFilms_emptyList_emitsEmptyState` — пустой результат даёт Empty, а не Success(emptyList())
- `loadFilms_error_emitsErrorState` — ошибка загрузки
- `loadFilms_emitsLoadingThenSuccess` — полная последовательность эмиссий Loading → Success (Turbine)
- `retry_afterError_callsApiAgain` — retry после ошибки инициирует новый запрос к API

**FavouritesViewModelTest (2 теста):**
- `initialState_withEmptyDb_emitsEmpty` — пустая БД при запуске даёт Empty
- `retry_triggersNewFlatMapLatestSubscription` — retry создаёт новую подписку через flatMapLatest

**GhibliFilmsRepositoryTest (1 тест):**
- `getAllFilms_cachesResult_doesNotCallApiSecondTime` — второй вызов не идёт в сеть

### Дополнительные юнит-тесты data-слоя — 1 штука

**GhibliFilmsRepositoryTest:**
- `film_toFavouriteFilmEntity_mapsAllFieldsCorrectly` — маппинг Film → FavouriteFilmEntity без потерь

### Интеграционные тесты — 4 штуки

**FavouriteFilmDaoTest (Room In-Memory, 3 теста):**
- `insert_andGetAll_returnsInsertedFilm` — вставка и чтение из базы
- `insert_duplicate_doesNotCreateDuplicate` — повторная вставка одного id не создаёт дубль
- `delete_removesFilmFromFavourites` — удаление из избранного

**FilmsListScreenTest (Compose UI, 1 тест):**
- `errorState_retryButton_clickLeadsToSuccess` — Error state → клик Retry → отображается Success

### Flow-тесты — 2 штуки

1. **Полная последовательность эмиссий** (`loadFilms_emitsLoadingThenSuccess`):
   Через Turbine проверяется точная цепочка: `Loading` → `Success`. Тест перехватывает каждый элемент через `awaitItem()`, а не только финальный `state.value`.

2. **Нетривиальное потоковое поведение** (`retry_triggersNewFlatMapLatestSubscription`):
   Проверяется, что emit в `retryTrigger` (MutableSharedFlow) действительно создаёт новую подписку через `flatMapLatest` — `getAllFavourites()` вызывается ровно дважды. Тест проверяет контракт поведения оператора, а не финальное значение состояния.

---

## Скриншоты

### Список фильмов (List)
![Список фильмов](/screenshots/list.jpg)

### Детали фильма (Detail)
![Детали фильма](/screenshots/details.jpg)

### Избранное (Favourites)
![Избранное](/screenshots/favourite.jpg)
