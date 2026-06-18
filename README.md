# java-lab4-eremeev

Настольное Java Swing приложение по мотивам игры «Кто хочет стать миллионером?».

## Что реализовано

- 15 уровней вопросов.
- 4 варианта ответа, один правильный.
- Выбор имени игрока и несгораемой суммы перед началом игры.
- 5 подсказок: 50/50, помощь зала, звонок другу, право на ошибку, замена вопроса.
- Ограничение: можно использовать только 4 подсказки из 5.
- Хранение вопросов в SQLite.
- Автоматическое создание базы данных при первом запуске.
- Импорт вопросов из `questions.tsv`, если база пустая.
- Таблица рекордов и вывод TOP 10 игроков.
- Генерация нового вопроса через API генеративной модели.

## Состав проекта

- `src/main/java/ru/eremeev/millionaire/Main.java` - запуск программы.
- `src/main/java/ru/eremeev/millionaire/GameFrame.java` - графический интерфейс и логика игры.
- `src/main/java/ru/eremeev/millionaire/DatabaseManager.java` - работа с SQLite.
- `src/main/java/ru/eremeev/millionaire/Question.java` - модель вопроса.
- `src/main/java/ru/eremeev/millionaire/Record.java` - модель результата игрока.
- `src/main/java/ru/eremeev/millionaire/AIGenerator.java` - генерация вопросов через API.
- `src/main/resources/questions.tsv` - вопросы для первичного импорта.
- `src/main/resources/picture.jpg` - изображение на форме.
- `WhoWantsToBeAMillionaire.db` - готовая база данных SQLite.
- `pom.xml` - настройки Maven и зависимости.
- `run.sh` - запуск на macOS/Linux.
- `run.bat` - запуск на Windows.
- `.gitignore` - список файлов, которые не нужно загружать на GitHub.

## Что нужно установить

Для запуска нужны:

1. JDK 17 или новее.
2. Apache NetBeans.
3. Интернет при первом запуске проекта, чтобы Maven скачал зависимость `sqlite-jdbc`.

Проверка Java:

```bash
java -version
```

Если Java установлена правильно, терминал покажет версию JDK.

## Запуск на macOS через NetBeans

1. Распаковать архив проекта.
2. Открыть Terminal.
3. Перейти в папку, где лежит проект. Например, если проект на рабочем столе:

```bash
cd ~/Desktop/java-lab4-eremeev
```

4. Запустить NetBeans. Если NetBeans лежит в `~/Applications/NetBeans-30`, команда такая:

```bash
~/Applications/NetBeans-30/bin/netbeans
```

5. В NetBeans открыть проект:

```text
File - Open Project - java-lab4-eremeev - Open Project
```

6. Дождаться загрузки Maven-зависимостей.
7. Нажать правой кнопкой по проекту и выбрать:

```text
Run
```

Если при запуске NetBeans в терминале появляются строки `WARNING: package ... not in java.desktop`, это не является ошибкой. Если окно NetBeans открылось, можно продолжать работу.

## Запуск на macOS через терминал

Перейти в папку проекта:

```bash
cd ~/Desktop/java-lab4-eremeev
```

Выдать право запуска файлу `run.sh`:

```bash
chmod +x run.sh
```

Запустить программу:

```bash
./run.sh
```

Или запустить напрямую через Maven:

```bash
mvn clean compile exec:java
```

## Запуск на Windows через NetBeans

1. Распаковать архив проекта.
2. Открыть Apache NetBeans.
3. Выбрать:

```text
File - Open Project
```

4. Выбрать папку `java-lab4-eremeev`.
5. Нажать `Open Project`.
6. Дождаться загрузки Maven-зависимостей.
7. Нажать правой кнопкой мыши по проекту и выбрать:

```text
Run
```

## Запуск на Windows через командную строку

Открыть `cmd`, перейти в папку проекта. Пример, если папка на рабочем столе:

```bat
cd %USERPROFILE%\Desktop\java-lab4-eremeev
```

Запустить программу:

```bat
run.bat
```

Или запустить напрямую через Maven:

```bat
mvn clean compile exec:java
```

## Сборка jar-файла

Для сборки выполнить:

```bash
mvn clean package
```

После сборки jar-файл будет находиться в папке:

```text
target/java-lab4-eremeev-1.0.jar
```

Запуск jar-файла:

```bash
java -jar target/java-lab4-eremeev-1.0.jar
```

## Настройка ИИ-вопросов

Кнопка `ИИ вопрос` работает только после настройки переменных окружения.

Программа использует три переменные:

```text
MILLIONAIRE_AI_URL
MILLIONAIRE_AI_KEY
MILLIONAIRE_AI_MODEL
```

Для OpenAI можно использовать такие значения:

```text
MILLIONAIRE_AI_URL=https://api.openai.com/v1/chat/completions
MILLIONAIRE_AI_KEY=ваш_api_ключ
MILLIONAIRE_AI_MODEL=gpt-4o-mini
```

API-ключ нельзя вставлять в код программы и нельзя отправлять другим людям. Его нужно хранить в переменных окружения.

## Постоянная настройка AI-ключа на macOS

Открыть Terminal и выполнить:

```bash
nano ~/.zshrc
```

В самый конец файла добавить строки:

```bash
export MILLIONAIRE_AI_URL="https://api.openai.com/v1/chat/completions"
export MILLIONAIRE_AI_KEY="ваш_api_ключ"
export MILLIONAIRE_AI_MODEL="gpt-4o-mini"
```

Вместо `ваш_api_ключ` нужно вставить свой ключ.

Сохранить файл:

```text
Ctrl + O - Enter - Ctrl + X
```

Применить настройки:

```bash
source ~/.zshrc
```

Проверить, что переменные появились:

```bash
echo $MILLIONAIRE_AI_URL
echo $MILLIONAIRE_AI_MODEL
```

Ключ через `echo` лучше не выводить полностью.

После настройки запускать NetBeans лучше из этого же Terminal:

```bash
~/Applications/NetBeans-30/bin/netbeans
```

## Постоянная настройка AI-ключа на Windows

Открыть `cmd` от имени обычного пользователя и выполнить:

```bat
setx MILLIONAIRE_AI_URL "https://api.openai.com/v1/chat/completions"
setx MILLIONAIRE_AI_KEY "ваш_api_ключ"
setx MILLIONAIRE_AI_MODEL "gpt-4o-mini"
```

После этого нужно закрыть командную строку и открыть ее заново.

Проверка:

```bat
echo %MILLIONAIRE_AI_URL%
echo %MILLIONAIRE_AI_MODEL%
```

NetBeans тоже лучше закрыть и открыть заново, чтобы он увидел новые переменные окружения.