package com.getname.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.getname.service.UpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateServiceImpl implements UpdateService {
    private final Map<String, String> codeToSmileMap = Map.of(
            "Clear", "Ясно ☀️",
            "Clouds", "Облачно ☁️",
            "Rain", "Дождь \uD83C\uDF27️",
            "Drizzle", "Моросит \uD83C\uDF27️",
            "Thunderstorm", "Гроза \uD83C\uDF29️",
            "Snow", "Снег \uD83C\uDF28️",
            "Mist", "Туман \uD83C\uDF2B️"
    );

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    private final HttpEntity<String> httpEntity;

    @Value("${open_weather.token}")
    private String openWeatherToken;

    @Value("${open_weather.pattern}")
    private String uriPattern;

    @Override
    public String process(String input) {
        String jsonString;
        JsonNode jsonNode;

        try {
            jsonString = fetchJsonFromApi(input);
        } catch (RestClientException e) {
            log.error("Rest error ", e);
            return "Такого города у openWeather не нашлось...";
        }

        try {
            jsonNode = objectMapper.readTree(jsonString);
        } catch (JsonProcessingException e) {
            log.error("jsonString reading", e);
            return "openWeather отправил нам точно не json 0_o";
        }

        return createWeatherInformationResponse(jsonNode);
    }

    private String createWeatherInformationResponse(JsonNode jsonNode) {
        var city = jsonNode.get("name").asText();
        var currentWeather = jsonNode.get("main").get("temp").asText();
        var weatherDescription = jsonNode.get("weather").get(0).get("main").asText();
        var wd = codeToSmileMap.getOrDefault(weatherDescription, "Лучше не выходить");
        var humidity = jsonNode.get("main").get("humidity").asText();
        var pressure = jsonNode.get("main").get("pressure").asText();
        var wind = jsonNode.get("wind").get("speed").asText();
        var sunriseTimestamp = LocalDateTime.ofEpochSecond(jsonNode.get("sys").get("sunrise").asLong(),
                0, ZoneOffset.of("+03:00"));
        var sunsetTimestamp = LocalDateTime.ofEpochSecond(jsonNode.get("sys").get("sunset").asLong(),
                0, ZoneOffset.of("+03:00"));
        var lengthOfDay = Duration.between(sunriseTimestamp, sunsetTimestamp);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        return  String.join("\n",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("---dd MMMM yyyy, HH:mm---", new Locale("ru"))),
                String.format("<i>Погода в городе <b>%s</b></i>", city),
                String.format("<b>Температура:</b> %s°C %s", currentWeather, wd),
                String.format("<b>Влажность:</b> %s %%", humidity),
                String.format("<b>Давление:</b> %s мм.рт.ст", pressure),
                String.format("<b>Ветер:</b> %s м/с", wind),
                String.format("<b>Восход солнца:</b> %s", sunriseTimestamp.format(formatter)),
                String.format("<b>Закат солнца:</b> %s", sunsetTimestamp.format(formatter)),
                String.format("<b>Продолжительность дня:</b> %s ч. %s м.", lengthOfDay.getSeconds() / 3600, lengthOfDay.getSeconds() / 60 % 60));
    }

    private String fetchJsonFromApi(String cityName) throws RestClientException {
        var uriString = uriPattern.formatted(cityName, openWeatherToken);
        return restTemplate.exchange(uriString, HttpMethod.GET, httpEntity, String.class)
                .getBody();
    }

}
