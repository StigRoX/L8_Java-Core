import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;


public class Main {
    public static String makeRequest(URL url) throws IOException { // Метод получения строки из тела запроса
        HttpURLConnection hpCon = (HttpURLConnection) url.openConnection(); // Открываем соединение по указанному URL
        BufferedReader reader = new BufferedReader(new InputStreamReader(hpCon.getInputStream())); // Создаем объект, читающий тело ответа как поток
        StringBuilder result = new StringBuilder(); // Создаем конструктор строки
        String line;
        while((line = reader.readLine()) != null) { // Пока в ответе есть строки для чтения, читаем их в переменную
            result.append(line); // И добавляем к конструктору
        }
        reader.close();
        return result.toString();
    }

    public static void main(String[] args) throws Exception{
        String API_KEY = "JgAMAHrLiHprtKzycOrrDmd6WGq5uR0o"; // API ключ для запросов

        Scanner cityScan = new Scanner(System.in); // Сканнер консоли
        ObjectMapper mapper = new ObjectMapper(); // Маппер для JSON, которым будем десериализовать данные
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // Отключаем падение на неизвестных параметрах
        URL baseUrl = new URL("https://dataservice.accuweather.com"); // Базовый адрес сервера

        DbHandler handler = DbHandler.getInstance(); // Экземпляр класса для работы с БД
        while (true) {
            System.out.println("Введите название города, для которого хотите получить погоду");
            System.out.println("Введите 1, чтобы прочитать все записанные запросы погоды");
            System.out.println("Введите 0, чтобы выйти из программы");
            System.out.print("Ваша команда: ");
            String cityLine = cityScan.nextLine();
            if (cityLine.startsWith("0") || cityLine.toLowerCase().startsWith("выход")) { // Если получили "0" или "выход" в любом регистре, выходим из цикла
                System.out.println("Программа завершена");
                handler.close();
                break;
            }
            if (cityLine.startsWith("1")) {
                List<WeatherResponse> weatherList = handler.getWeatherList();
                for(WeatherResponse weather: weatherList) {
                    System.out.println(weather); // Выводим погоду в консоль.
                }
                continue;
            }
            // Кодируем кириллическое название города в пригодный для URL формат
            String cityEncoded = URLEncoder.encode(cityLine, StandardCharsets.UTF_8.toString());
            // URL для запроса ключа локации (требуется для запроса погоды)
            URL cityUrl = new URL(baseUrl, "locations/v1/cities/autocomplete?apikey="+API_KEY+"&language=ru-ru&q=" + cityEncoded);

            // Делаем запрос на поиск городов по указанному имени
            String cityResult = makeRequest(cityUrl);
            JsonNode cityList = mapper.readTree(cityResult); // Читаем JSON
            if (cityList.isEmpty()) { // Если ответ пустой - идем в начало и просим у пользователя другой город
                System.out.println("Не могу найти город. Попробуйте другое название.");
                continue;
            }
            JsonNode city = cityList.get(0); // Берем первый город из списка
            String cityKey = city.get("Key").textValue(); // Получаем ключ для запроса
            String cityName = city.get("LocalizedName").textValue(); // А также полное название, если пользователь ввел его не полностью

            // Создаем запрос на погоду
            URL weatherUrl = new URL(baseUrl, "forecasts/v1/daily/5day/"+cityKey+"?apikey="+API_KEY+"&language=ru-ru&metric=true");
            String result = makeRequest(weatherUrl);
            JsonNode list = mapper.readTree(result.toString()).at("/DailyForecasts"); // Читаем данные относящиеся к ежедневному прогнозу
            System.out.println(list);
            WeatherResponse[] weatherDays = mapper.readValue(list.toString(), WeatherResponse[].class); // Создаем список ответов с температурой и погодой
            for(WeatherResponse day: weatherDays) {
                day.setCity(cityName);
                handler.addWeather(day);
                System.out.println(day); // Выводим погоду в консоль.
            }
        }

    }
}
