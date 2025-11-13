package com.example.backend.service;

import com.example.backend.entity.News;
import com.example.backend.entity.Sport;
import com.example.backend.repository.NewsRepository;
import com.example.backend.repository.SportRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor

public class NewsCrawlerService {

    private final NewsRepository newsRepository;
    private final SportRepository sportRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${naver.api.client-id}")
    private String clientId;

    @Value("${naver.api.client-secret}")
    private String clientSecret;

    // 종목별 검색 키워드
    private static final Map<String, String> SPORT_KEYWORD_MAP = new HashMap<>() {{
        put("FOOTBALL", "축구");
        put("BASKETBALL", "농구");
        put("BASEBALL", "야구");
        put("LOL", "리그오브레전드");
        put("MMA", "UFC");
    }};

    /**
     * 30분마다 자동 실행
     */
    @Scheduled(fixedRate = 1800000) // 30분
    @Transactional
    public void crawlAllSportsNews() {
        log.info("=== 네이버 뉴스 API 수집 시작 ===");

        for (Map.Entry<String, String> entry : SPORT_KEYWORD_MAP.entrySet()) {
            String sportName = entry.getKey();
            String keyword = entry.getValue();

            try {
                fetchNewsFromNaverAPI(sportName, keyword);
                Thread.sleep(1000); // 1초 대기
            } catch (Exception e) {
                log.error("뉴스 수집 실패: {} - {}", sportName, e.getMessage());
            }
        }

        log.info("=== 네이버 뉴스 API 수집 완료 ===");
    }

    /**
     * 수동 크롤링
     */
    @Transactional
    public void crawlNewsManually() {
        crawlAllSportsNews();
    }

    /**
     * 네이버 뉴스 검색 API 호출
     */
    private void fetchNewsFromNaverAPI(String sportName, String keyword) {
        try {
            log.info("뉴스 수집 시작: {} - {}", sportName, keyword);

            // Sport 엔티티 조회
            Optional<Sport> sportOpt = sportRepository.findBySportName(sportName);
            if (sportOpt.isEmpty()) {
                log.warn("Sport not found: {}", sportName);
                return;
            }
            Sport sport = sportOpt.get();

            // 네이버 뉴스 검색 API 호출
            String text = URLEncoder.encode(keyword + " 스포츠", "UTF-8");
            String apiURL = "https://openapi.naver.com/v1/search/news.json?query=" + text
                    + "&display=10&sort=date"; // 최신순 10개

            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-Naver-Client-Id", clientId);
            con.setRequestProperty("X-Naver-Client-Secret", clientSecret);

            int responseCode = con.getResponseCode();
            BufferedReader br;

            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream(), "UTF-8"));
                log.error("API 호출 실패: {}", responseCode);
                return;
            }

            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();

            // JSON 파싱
            JsonNode root = objectMapper.readTree(response.toString());
            JsonNode items = root.path("items");

            int count = 0;
            for (JsonNode item : items) {
                try {
                    String title = cleanHtmlTags(item.path("title").asText());
                    String link = item.path("link").asText();
                    log.info("==== 크롤링 데이터 ====");
                    log.info("제목: {}", title);
                    log.info("원본 링크: {}", link);
                    log.info("링크 길이: {}", link.length());
                    String description = cleanHtmlTags(item.path("description").asText());
                    String pubDate = item.path("pubDate").asText();

                    // 중복 체크
                    if (newsRepository.existsBySourceUrl(link)) {
                        continue;
                    }

                    // 썸네일 추출
                    String thumbnailUrl = extractThumbnail(link);

                    // 뉴스 저장
                    News news = new News();
                    news.setSport(sport);
                    news.setTitle(title);
                    news.setContent(description);
                    news.setSourceUrl(link);
                    news.setSourceName("네이버 뉴스");
                    news.setThumbnailUrl(thumbnailUrl);
                    news.setPublishedAt(parseNaverDate(pubDate));
                    news.setViewCount(0);

                    newsRepository.save(news);
                    count++;

                    log.info("뉴스 저장: {}", title);

                } catch (Exception e) {
                    log.error("뉴스 파싱 실패: {}", e.getMessage());
                }
            }

            log.info("{} 뉴스 {}개 저장 완료", sportName, count);

        } catch (Exception e) {
            log.error("API 호출 오류: {} - {}", sportName, e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 뉴스 기사에서 썸네일 이미지 추출
     */
    private String extractThumbnail(String newsUrl) {
        try {
            Document doc = Jsoup.connect(newsUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(5000)
                    .get();

            // OpenGraph 이미지 태그 찾기
            Element ogImage = doc.selectFirst("meta[property=og:image]");
            if (ogImage != null) {
                String imageUrl = ogImage.attr("content");
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    log.info("썸네일 추출 성공: {}", imageUrl);
                    return imageUrl;
                }
            }

            // og:image가 없으면 첫 번째 img 태그 찾기
            Element firstImg = doc.selectFirst("article img, .article img, #articleBodyContents img");
            if (firstImg != null) {
                String imageUrl = firstImg.attr("src");
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    return imageUrl;
                }
            }

        } catch (Exception e) {
            log.warn("썸네일 추출 실패: {} - {}", newsUrl, e.getMessage());
        }
        return null;
    }

    /**
     * HTML 태그 제거
     */
    private String cleanHtmlTags(String text) {
        return text.replaceAll("<[^>]*>", "")
                .replaceAll("&quot;", "\"")
                .replaceAll("&amp;", "&")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&nbsp;", " ");
    }

    /**
     * 네이버 날짜 형식 파싱
     */
    private LocalDateTime parseNaverDate(String pubDate) {
        try {
            // 예: "Tue, 05 Nov 2024 14:30:00 +0900"
            DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(pubDate, formatter);
            return zonedDateTime.toLocalDateTime();
        } catch (Exception e) {
            log.error("날짜 파싱 실패: {}", pubDate);
            return LocalDateTime.now();
        }
    }
}
