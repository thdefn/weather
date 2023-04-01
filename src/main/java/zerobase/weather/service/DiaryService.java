package zerobase.weather.service;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service // 어노테이션을 붙여주지 않으면 스프링 부트가 서비스 클래스임을 인식하지 못하고 빈으로 등록도 안됨
public class DiaryService {

    @Value("${openweathermap.key}")
    // 실무의 다양한 환경들이 ex> real/test/dev 같은 DB를 바라보지 않아서 profile을 사용하기 위함
    private String apiKey;

    // 코드의 재사용성과 가독성을 위해 각각의 기능들을 함수로 분리
    public void createDiary(LocalDate date, String text) {
        // open weather map 에서 날씨 데이터 받아오기
        String weatherData = getWeatherString();
        // 받아온 날씨 json 파싱하기
        Map<String, Object> parsedWeather = parseWeather(weatherData);
        // 파싱된 데이터 + 일기 값 db에 넣기
    }

    private String getWeatherString() {
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=seoul&appid=" + apiKey;

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(connection.getErrorStream())); // 에러를 가져온다
            }
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();

            return response.toString();

        } catch (Exception e) {
            return "failed to get response";
        }
    }

    // {"weather":[{"id":800,"main":"Clear","description":"clear sky","icon":"01n"}],
    // "main":{"temp":291.88,"feels_like":290.62,"temp_min":283.84,"temp_max":295.81,"pressure":1014,"humidity":31}}
    private Map<String, Object> parseWeather(String jsonString) {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject;

        try {
            jsonObject = (JSONObject) jsonParser.parse(jsonString);
            Map<String, Object> resultMap = new HashMap<>();

            JSONObject mainData = (JSONObject) jsonObject.get("main");
            JSONObject weatherData = (JSONObject) jsonObject.get("weather");
            resultMap.put("temp", mainData.get("temp"));
            resultMap.put("main", weatherData.get("main"));
            resultMap.put("icon", weatherData.get("icon"));
            return resultMap;

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
