package zerobase.weather.service;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import zerobase.weather.domain.DateWeather;
import zerobase.weather.domain.Diary;
import zerobase.weather.error.InvalidDate;
import zerobase.weather.repository.DateWeatherRepository;
import zerobase.weather.repository.DiaryRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service // 어노테이션을 붙여주지 않으면 스프링 부트가 서비스 클래스임을 인식하지 못하고 빈으로 등록도 안됨
@Transactional(readOnly = true)
public class DiaryService {

    @Value("${openweathermap.key}")
    // 실무의 다양한 환경들이 같은 DB를 바라보지 않아서 profile을 사용하기 위함
    private String apiKey;

    private final DiaryRepository diaryRepository;

    private final DateWeatherRepository dateWeatherRepository;

    @Autowired
    public DiaryService(DiaryRepository diaryRepository, DateWeatherRepository dateWeatherRepository) {
        this.diaryRepository = diaryRepository;
        this.dateWeatherRepository = dateWeatherRepository;
    }

    @Transactional // 디비 작업이므로 트랜잭셔널
    @Scheduled(cron = "* * 1 * * *") // 스케쥴 된 작업을 진행하는 함수임
    public void saveWeatherDate(){
        log.info("오늘도 날씨 데이터 잘 가져옴~!");
        dateWeatherRepository.save(getWeatherFromApi());
    }

    // 코드의 재사용성과 가독성을 위해 각각의 기능들을 함수로 분리
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void createDiary(LocalDate date, String text) {
        log.info("started to create diary");
        // 날씨 데이터 가져오기 1)api or 2)db
        DateWeather dateWeather = getDateWeather(date);

        // 파싱된 데이터 + 일기 값 db에 넣기
        Diary nowDiary = new Diary();
        nowDiary.setDateWeather(dateWeather);
        nowDiary.setDate(date);
        nowDiary.setText(text);

        diaryRepository.save(nowDiary);
        log.info("end to create diary");
    }

    private DateWeather getDateWeather(LocalDate date){
        List<DateWeather>  dateWeatherListFromDb = dateWeatherRepository.findAllByDate(date);
        if(dateWeatherListFromDb.size() == 0){
            // 새로 api에서 날씨 정보를 가져와야 한다
            // 정책상 현재 날씨를 가져오거나 날씨 없이 일기를 쓰도록
            return getWeatherFromApi();
        }
        // 디비에서 가져온다
        return dateWeatherListFromDb.get(0);
    }

    private DateWeather getWeatherFromApi(){
        // open weather map 에서 날씨 데이터 받아오기
        String weatherData = getWeatherString();
        // 받아온 날씨 json 파싱하기
        Map<String, Object> parsedWeather = parseWeather(weatherData);
        // 파싱된 데이터를 dateweather 엔티티에 넣어주기
        DateWeather dateWeather = new DateWeather();
        dateWeather.setDate(LocalDate.now()); // 날씨를 가져오는 시점의 날짜
        dateWeather.setWeather(parsedWeather.get("main").toString());
        dateWeather.setIcon(parsedWeather.get("icon").toString());
        dateWeather.setTemperature((Double) parsedWeather.get("temp"));

        return dateWeather;
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

    @Transactional(readOnly = true)
    public List<Diary> readDiary(LocalDate date) {
        log.debug("read diary");
        if(date.isAfter(LocalDate.ofYearDay(3050,1))){
            // throw new InvalidDate();
        }
        return diaryRepository.findAllByDate(date);
    }

    @Transactional(readOnly = true)
    public List<Diary> readDiaries(LocalDate startDate, LocalDate endDate) {
        return diaryRepository.findAllByDateBetween(startDate, endDate);
    }

    @Transactional
    public void updateDiary(LocalDate date, String text) {
        Diary nowDiary = diaryRepository.getFirstByDate(date);
        nowDiary.setText(text);
        diaryRepository.save(nowDiary); // 아이디 값은 그대로 둔 채로 텍스트만 바꿨기 때문에 업데이트
    }

    @Transactional
    public void deleteDiary(LocalDate date) {
        diaryRepository.deleteAllByDate(date);
    }
}
