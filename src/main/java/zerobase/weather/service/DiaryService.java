package zerobase.weather.service;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import zerobase.weather.domain.Diary;
import zerobase.weather.repository.DiaryRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service // 어노테이션을 붙여주지 않으면 스프링 부트가 서비스 클래스임을 인식하지 못하고 빈으로 등록도 안됨
public class DiaryService {

    @Value("${openweathermap.key}")
    // 실무의 다양한 환경들이 같은 DB를 바라보지 않아서 profile을 사용하기 위함
    private String apiKey;

    private final DiaryRepository diaryRepository;

    @Autowired
    public DiaryService(DiaryRepository diaryRepository) {
        this.diaryRepository = diaryRepository;
    }

    // 코드의 재사용성과 가독성을 위해 각각의 기능들을 함수로 분리
    public void createDiary(LocalDate date, String text) {
        // open weather map 에서 날씨 데이터 받아오기
        String weatherData = getWeatherString();
        // 받아온 날씨 json 파싱하기
        Map<String, Object> parsedWeather = parseWeather(weatherData);
        // 파싱된 데이터 + 일기 값 db에 넣기
        Diary nowDiary = new Diary();
        nowDiary.setDate(date);
        nowDiary.setText(text);
        nowDiary.setWeather(parsedWeather.get("main").toString());
        nowDiary.setIcon(parsedWeather.get("icon").toString());
        nowDiary.setTemperature((Double) parsedWeather.get("temp"));

        diaryRepository.save(nowDiary);
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
            JSONArray weatherArray = (JSONArray) jsonObject.get("weather");
            JSONObject weatherData = (JSONObject) weatherArray.get(0);
            resultMap.put("temp", mainData.get("temp"));
            resultMap.put("main", weatherData.get("main"));
            resultMap.put("icon", weatherData.get("icon"));
            return resultMap;

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Diary> readDiry(LocalDate date) {
        return diaryRepository.findAllByDate(date);
    }

    public List<Diary> readDiaries(LocalDate startDate, LocalDate endDate) {
        return diaryRepository.findAllByDateBetween(startDate, endDate);
    }

    public void updateDiary(LocalDate date, String text) {
        Diary nowDiary = diaryRepository.getFirstByDate(date);
        nowDiary.setText(text);
        diaryRepository.save(nowDiary); // 아이디 값은 그대로 둔 채로 텍스트만 바꿨기 때문에 업데이트
    }

    public void deleteDiary(LocalDate date) {
        diaryRepository.deleteAllByDate(date);
    }
}
