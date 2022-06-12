import org.sqlite.JDBC;

import java.sql.*;
import java.util.*;

public class DbHandler {
    private static final String CONNECTION = "jdbc:sqlite:weather.db"; // Строка для подключения к базе

    private static DbHandler instance = null; // Ссылка на экземпляр обработчика
    public static DbHandler getInstance() throws SQLException { // Реализуем шаблон Синглтона, чтобы у нас всегда был только один экземпляр обработчика в программе
        if (instance == null) {
            instance = new DbHandler();
        }
        return instance;
    }
    private Connection connection; // Объект соединения

    private DbHandler() throws SQLException {
        DriverManager.registerDriver(new JDBC());
        this.connection = DriverManager.getConnection(CONNECTION);
        try(Statement statement = this.connection.createStatement()) {
            // Создаем таблицу, если её нет
            statement.execute("CREATE TABLE IF NOT EXISTS Weather (city VARCHAR(50), date DATETIME, temperature DOUBLE, weatherText VARCHAR(20))");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() throws SQLException { // Метод закрытия соединения
        connection.close();
    }

    public List<WeatherResponse> getWeatherList() { // Метод получения всех записанных запросов погоды
        try(Statement statement = this.connection.createStatement()) {
            List<WeatherResponse> weatherList =  new ArrayList<>(); // Загатавливаем список погодных запросов
            ResultSet result = statement.executeQuery("SELECT * FROM Weather WHERE 1"); // Получаем все запросы
            while(result.next()) {
                // Создаем на каждую строку ответа новый объект погоды и заполняем его
                WeatherResponse weather = new WeatherResponse();
                weather.setCity(result.getString("city"));
                weather.setDate(result.getDate("date"));
                weather.setTemperature(result.getDouble("temperature"));
                weather.setWeather(result.getString("weatherText"));
                weatherList.add(weather); // Добавляем в список
            }
            return weatherList;
        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public void addWeather(WeatherResponse weather) { // Добавление погоды в таблицу
        try (PreparedStatement statement = this.connection.prepareStatement(
                "INSERT INTO Weather (`city`, `date`, `temperature`, `weatherText`) VALUES(?, ?, ?, ?)"
        )) {
            // Заполняем строку запроса данными
            statement.setObject(1, weather.getCity());
            statement.setObject(2, weather.getDate());
            statement.setObject(3, weather.getTemperature());
            statement.setObject(4, weather.getWeather());
            statement.execute(); // Выполняем его
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
