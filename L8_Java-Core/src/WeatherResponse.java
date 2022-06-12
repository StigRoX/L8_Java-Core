
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.util.StdDateFormat;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class WeatherResponse {
    private String city;
    private String weather;
    private Double temperature;
    private Date date;

    @JsonProperty(value = "Temperature")
    private void unpackNestedTemp(Map<String, Map<String, Object>> main) {
        this.temperature = (Double)main.get("Maximum").get("Value");
    }

    @JsonProperty(value = "Day")
    private void unpackNestedWeather(Map<String, Object> weather) {
        this.weather = (String)weather.get("IconPhrase");
    }

    @JsonProperty(value = "Date")
    private void convertDate(String date) throws ParseException {
        this.date = new StdDateFormat().parse(date);
    }

    // Создаем геттеры и сеттеры для работы ObjectMapper'а
    public void setWeather(String weather){
        this.weather = weather;
    }
    public void setTemperature(Double temperature){
        this.temperature = temperature;
    }
    public void setDate(Date date){
        this.date = date;
    }
    public void setCity(String city){ this.city = city; }

    public String getWeather(){
        return this.weather;
    }
    public Double getTemperature(){
        return this.temperature;
    }
    public Date getDate(){
        return this.date;
    }
    public String getCity(){ return this.city; }

    public String toString() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(this.date);
        String dateString = calendar.get(Calendar.DAY_OF_MONTH) + "." + calendar.get(Calendar.MONTH) + "." + calendar.get(Calendar.YEAR);
        return "В городе " + this.city + " на дату " + dateString + " Ожидается " + this.weather + ", температура - " + this.temperature ;
    }

    public WeatherResponse(){
    }
}
