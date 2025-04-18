# Analyze - Log analyzer (Test task VL.RU)

**Описание**:  
Приложение для анализа логов, выполненное в рамках тестового задания компании VL.RU. Это консольное приложение позволяет обрабатывать и анализировать данные из логов.

## Требования

- JDK 21
- Maven 3.6.

## Установка и запуск

1. Клонируйте репозиторий на ваш локальный компьютер:
   ```bash
   git clone https://github.com/your-username/analyze.git
   ```
2. Соберите артефакт
   ```bash
   cd analyze
   mvn clean package
   ```
3. Запустите артефакт, передав необходимые параметры (-h для справки)
   ```bash
   java -jar target/analyze-1.0-SNAPSHOT.jar [параметры] 
   ```
   Параметры запуска:

   - ```-h, --help``` - Справка по утилите
   - ```-v, --version``` - Номер версии
   - ```-t, --time``` - Минимально допустимое время обработки запроса
   - ```-p, --percentage``` - Минимально приемлемый уровень доступности (в процентах)

   Пример:
   ```bash
   cat access.log | java -jar analyze-1.0-SNAPSHOT.jar -p 99.9 -t 45
   ```