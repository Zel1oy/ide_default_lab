## 🎯 Goals of the Project

* **Visual Parallel Programming** – Створення графічного інтерфейсу для моделювання складних багатопоточних алгоритмів без написання коду на початковому етапі.
* **100-Thread Context Management** – Забезпечення стабільної роботи та перемикання контексту між 100 незалежними потоками виконання.
* **Automated Verification (Model Checking)** – Реалізація аналітичного ядра для пошуку "гонок даних" (race conditions) та помилок синхронізації через повний перебір станів (Interleaving).
* **Safe Code Generation** – Трансляція графічних моделей у валідний, готовий до виконання Java-код з використанням механізмів пам'яті JVM (`volatile`, `synchronized`).
* **State Space Constraint Management** – Впровадження ліміту  кроків для запобігання "вибуху станів" (state explosion) при аналізі циклічних алгоритмів.

---

## Visual Multi-threaded Logic Modeler & Verifier

**Project Title:** Multi-threaded Algorithm Designer with State Space Exploration

**Domain:** Parallel Computing, Formal Verification, Software Engineering

**Technology Stack:** Java 21, JavaFX, Lombok

**Core Features:** Visual Modeling, Model Checking (Interleaving), Code Generation

---

## ✅ Completed Goals

1. ✅ **UI/UX Framework** – Реалізовано динамічне полотно (Canvas) з підтримкою Drag-and-Drop та розумним рендерингом зв'язків.
2. ✅ **Analytical Test Engine** – Впроваджено алгоритм пошуку в ширину (BFS) для дослідження дерева станів системи.
3. ✅ **Thread Manager Subsystem** – Створено механізм збереження та відновлення контексту для 100 потоків у `HashMap` структурі.
4. ✅ **Expression Parser** – Реалізовано валідацію та обробку арифметичних виразів для змінних .
5. ✅ **Asynchronous Task Execution** – Аналіз виконується в окремому фоновому потоці (JavaFX Task), що дозволяє переривати обчислення без блокування інтерфейсу.
6. ✅ **Interleaving Test Engine** – Розроблено ядро верифікації на базі алгоритму BFS (Breadth-First Search).
7. ✅ **Asynchronous Monitoring Dashboard** – Реалізовано асинхронний запуск тестів через `JavaFX Task` з можливістю зупинки в реальному часі.
8. ✅ **Expression Parsing & Validation** – Впроваджено систему обробки арифметичних виразів для змінних .

---

## 🛠️ Work Completed (Project Modules)

### 1. UI/UX Module (Visual Modeling)

Цей модуль відповідає за взаємодію користувача з архітектурою алгоритму.

* **Thread Selector:** Компонент для миттєвого перемикання між 100 робочими областями.
* **Dynamic Canvas:** Використання Property Binding для автоматичного перерахунку координат стрілок при русі блоків.
* **Property Editor:** Діалогова система з регулярними виразами (Regex) для валідації вводу користувача.

### 2. Analytical Engine (Model Checker)

Ядро системи, що виконує верифікацію логіки.

* **Interleaving Logic:** Симуляція всіх можливих комбінацій перемикання контексту між потоками.
* **State Snapshotting:** Кожен крок створює новий `ExecutionState` (Immutable pattern), що дозволяє точно відтворити шлях до помилки.
* **Termination Protection:** Жорстке обмеження глибини рекурсії/черги за параметром .

### 3. Code Generation Module

Транслятор, що перетворює граф у текстовий файл `.java`.

* **Visitor Pattern:** Обхід графа блоків для генерації синтаксично правильних конструкцій Java.
* **Concurrency Support:** Автоматичне додавання `volatile` та `synchronized` блоків для забезпечення видимості змінних у реальній JVM.

---

## 📝 Technical Details

* **Concurrency Model:** Моделювання недетермінованості через переплетення операцій (Interleaving).
* **Algorithm Design:** Використання BFS для пошуку найкоротшого шляху до помилки.
* **UI Responsiveness:** Використання `Platform.runLater()` для безпечного оновлення інтерфейсу з фонових потоків аналізу.
* **Data Integrity:** Валідація вводу на етапі проектування за допомогою регулярних виразів (Regex).

---

## 📝 Summary of Technical Implementation

Під час розробки було застосовано декілька ключових паттернів проектування та алгоритмічних рішень:

1. **State Space Exploration:** Замість простого запуску коду, система будує математичну модель станів. Це дозволяє виявляти помилки, які трапляються лише в 1 з 1 000 000 випадків (Heisenbugs).
2. **Context Switching:** Реалізовано через `Map<Integer, List<Block>>`, де стан кожного потоку серіалізується при перемиканні вкладки.
3. **BFS Optimization:** Використання черги (`Queue`) для перебору станів гарантує знаходження найкоротшого шляху до помилки синхронізації.

---

## 📂 Project Structure (As Implemented)

Згідно з поточною архітектурою проекту, систему розділено на три ключові пакети:

```text
src/main/java/com/lab/
├── model/                 # Шар даних та бізнес-логіки
│   ├── visitor/           # Паттерн Visitor для обробки графів
│   │   ├── BlockVisitor   # Інтерфейс для обходу вузлів
│   │   └── JavaCodeGenerator # Генерація Java-коду з моделі
│   ├── AssignmentBlock    # Блок присвоєння (V0 = ...)
│   ├── Block              # Базовий абстрактний клас вузла
│   ├── BlockType          # Перерахування типів блоків
│   ├── ConditionBlock     # Умовний перехід (If/Else)
│   ├── IOBlock            # Ввід/Вивід даних
│   ├── StartBlock/StopBlock # Термінальні вузли
│   └── ThreadFlow         # Контейнер для логіки окремого потоку
├── test/                  # Аналітичне ядро (Model Checker)
│   ├── ExecutionState     # Стан системи (змінні, PC, черга вводу)
│   └── TestEngine         # Алгоритм BFS для перебору Interleaving
├── ui/                    # Графічний інтерфейс (JavaFX)
│   ├── BlockView          # Візуальне представлення вузла
│   ├── ConnectionView     # Рендеринг динамічних зв'язків
│   ├── PropertiesEditor   # Вікно конфігурації параметрів блоку
│   └── TestWindow         # Дашборд моніторингу аналізу
├── Launcher               # Точка входу (Main)
└── MainApp                # Ініціалізація JavaFX сцени

```

---

## 🚧 Challenges & Solutions

1. **State Explosion Problem:** При великій кількості потоків кількість станів росте експоненціально.
* *Solution:* Впроваджено параметр  (max steps) для відсікання занадто глибоких гілок аналізу.


2. **UI Thread Blocking:** Важкі розрахунки тестера "заморожували" інтерфейс.
* *Solution:* Перенесення обчислень у `javafx.concurrent.Task` з використанням `Supplier<Boolean>` для механізму переривання (Cancel).


3. **Array Volatility:** Проблема видимості елементів масиву в Java.
* *Solution:* Використання `volatile` для посилання на масив та проектування архітектури під можливе впровадження `AtomicIntegerArray`.

---

## Demo & Usage

1. Оберіть потік у верхній панелі.
2. Створіть логіку за допомогою блоків (Double-click для налаштування).
3. Натисніть **"Run Test"**, вкажіть ліміт  та очікуваний результат.
4. Отримайте звіт про стабільність алгоритму (Success Rate).

---

### How to Run

1. Виконайте `mvn clean install` для збірки проекту.
2. Запустіть клас `Launcher` як головний клас додатку.
3. У інтерфейсі сконструюйте алгоритм та натисніть кнопку тестування.

---

## 🚀 Future Work and Improvements

* **Slash Commands & Forms:** Додати підтримку складніших логічних виразів та масивів.
* **Liveness Checking:** Реалізація перевірки на Deadlocks (взаємне блокування потоків).
* **Ecosystem Integration:** Експорт графіків у форматі Mermaid.js або JSON для інтеграції з іншими інструментами.
* **Visual Traceback:** Візуальна підсвітка блоків на полотні, які призвели до знайденої помилки (Race Condition).

---

## 🎓 Acknowledgments

Проект розроблено в рамках лабораторної роботи з дисципліни "Конструювання програмного забезпечення". Особлива подяка за консультації щодо моделювання простору станів та Java Memory Model.

---