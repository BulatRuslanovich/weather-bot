package com.getname.service.impl;

import com.getname.config.OpenWeatherProperties;
import com.getname.dto.WeatherResponse;
import com.getname.service.UpdateService;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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

    private final OpenWeatherProperties openWeatherProperties;

    private final RestTemplate restTemplate;

    @Override
    public String process(String input) {
        input = normalizeInput(input);
        WeatherResponse weatherResponse;

        try {
            weatherResponse = fetchJsonFromApi(input);
        } catch (RestClientException e) {
            log.error("Rest error ", e);
            return "Такого города у openWeather не нашлось...";
        }

        return createWeatherInformationResponse(weatherResponse);
    }

    private String normalizeInput(String input) {
        var separator = input.contains("-") ? "-" : " ";
        return Arrays.stream(input.split(separator))
                .map(String::toLowerCase)
                .map(StringUtils::capitalize)
                .collect(Collectors.joining(separator));
    }

    private String createWeatherInformationResponse(WeatherResponse weatherResponse) {
        var city = weatherResponse.getName();
        var currentWeather = weatherResponse.getMain().getTemp();
        var weatherDescription = weatherResponse.getWeather()[0].getMain();
        var wd = codeToSmileMap.getOrDefault(weatherDescription, "Лучше не выходить");
        var humidity = weatherResponse.getMain().getHumidity();
        var pressure = weatherResponse.getMain().getPressure();
        var wind = weatherResponse.getWind().getSpeed();
        var sunriseTimestamp = LocalDateTime.ofEpochSecond(weatherResponse.getSys().getSunrise(),
                0, ZoneOffset.of("+03:00"));
        var sunsetTimestamp = LocalDateTime.ofEpochSecond(weatherResponse.getSys().getSunset(),
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

    private WeatherResponse fetchJsonFromApi(String cityName) throws RestClientException {
        return restTemplate.getForObject(createUri(cityName), WeatherResponse.class);
    }

    private String createUri(String cityName) {
        return UriComponentsBuilder.fromHttpUrl(openWeatherProperties.getUri())
                .queryParam("q", cityName)
                .queryParam("appid", openWeatherProperties.getToken())
                .queryParam("units", "metric")
                .toUriString();
    }
}
