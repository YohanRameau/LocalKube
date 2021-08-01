package localkube.app.start;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.DEFINED_PORT)
public class TestingWebApplicationTests{
    private final ObjectMapper objectMapper = new ObjectMapper();
    @LocalServerPort
    int port = 8080;

    @Test
    public void givenDataIsJson_whenDataIsPostedByPostForObject_thenResponseBodyContainGoodInfos() throws JsonProcessingException {
        var startAppUrl = "http://localhost:8080/app/start";

        RestTemplate restTemplate = new RestTemplate();
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var appJsonObject = new JSONObject();
        appJsonObject.put("name", "helloworld");
        appJsonObject.put("port", 911);

        HttpEntity<String> request = new HttpEntity<String>(appJsonObject.toString(), headers);

        String appResultAsJsonStr = restTemplate.postForObject(startAppUrl, request, String.class);
        JsonNode root = objectMapper.readTree(appResultAsJsonStr);

        assertNotNull(appResultAsJsonStr);
        assertNotNull(root);
        assertNotNull(root.path("name").asText());
        assertEquals(true, appResultAsJsonStr.contains("helloworld"));
        assertEquals(true, appResultAsJsonStr.contains("911"));
    }

    @Test
    public void givenWrongAppNameDataIsJson_whenDataIsPostedByPostForObject_thenResponseBodyContainErrorDisplay() throws JsonProcessingException {
        var startAppUrl = "http://localhost:8080/app/start";

        RestTemplate restTemplate = new RestTemplate();
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var appJsonObject = new JSONObject();
        appJsonObject.put("name", "openjdk:18");
        appJsonObject.put("port", 8086);

        HttpEntity<String> request = new HttpEntity<String>(appJsonObject.toString(), headers);

        String appResultAsJsonStr = restTemplate.postForObject(startAppUrl, request, String.class);
        JsonNode root = objectMapper.readTree(appResultAsJsonStr);

        assertNotNull(appResultAsJsonStr);
        assertNotNull(root);
        assertNotNull(root.path("name").asText());
        assertEquals(true, appResultAsJsonStr.contains("Error"));
    }

    @Test
    public void givenBadPort_whenDataIsPostedByPostForObject_thenResponseBodyContainErrorDisplay() throws JsonProcessingException {
        var startAppUrl = "http://localhost:8080/app/start";

        RestTemplate restTemplate = new RestTemplate();
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var appJsonObject = new JSONObject();
        appJsonObject.put("name", "helloworld");
        appJsonObject.put("port", 99999);

        HttpEntity<String> request = new HttpEntity<String>(appJsonObject.toString(), headers);

        assertThrows(HttpServerErrorException.class, () -> { String appResultAsJsonStr = restTemplate.postForObject(startAppUrl, request, String.class); });
    }

    @Test
    public void whenGettingTheAppList_thenResponseBodyIsEmpty() throws JsonProcessingException {
        var listAppUrl = "http://localhost:8080/app/list";

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> listResultAsJsonStr = restTemplate.getForEntity(listAppUrl, String.class);

        assertEquals(HttpStatus.OK, listResultAsJsonStr.getStatusCode());
        assertEquals(true, listResultAsJsonStr.getBody().equals("[]"));
    }

    @Test
    public void givenDataIsJson_whenDataIsPostedByPostForObject_thenResponseBodyContainErrorDisplay() throws JsonProcessingException {
        var startAppUrl = "http://localhost:8080/app/stop";

        RestTemplate restTemplate = new RestTemplate();
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var appJsonObject = new JSONObject();
        appJsonObject.put("id", 50);

        HttpEntity<String> request = new HttpEntity<String>(appJsonObject.toString(), headers);

        String appResultAsJsonStr = restTemplate.postForObject(startAppUrl, request, String.class);
        JsonNode root = objectMapper.readTree(appResultAsJsonStr);

        assertNotNull(appResultAsJsonStr);
        assertNotNull(root);
        assertNotNull(root.path("name").asText());
        assertEquals(true, appResultAsJsonStr.contains("Error"));
    }

    //@Test
    //public void givenDataIsJson_whenDataIsPostedByPostForObject_AndApplicationIsExited_thenAppListIsEmpty() throws JsonProcessingException, InterruptedException {
    //    var startAppUrl = "http://localhost:8080/app/start";

        //RestTemplate restTemplate = new RestTemplate();
        //var headers = new HttpHeaders();
        //headers.setContentType(MediaType.APPLICATION_JSON);
        //var appJsonObject = new JSONObject();
        //appJsonObject.put("name", "helloworld");
        //appJsonObject.put("port", 911);

        //HttpEntity<String> request = new HttpEntity<String>(appJsonObject.toString(), headers);

        //String appResultAsJsonStr = restTemplate.postForObject(startAppUrl, request, String.class);
        //JsonNode root = objectMapper.readTree(appResultAsJsonStr);

        //assertNotNull(appResultAsJsonStr);
        //assertNotNull(root);
        //assertNotNull(root.path("name").asText());
        //assertEquals(true, appResultAsJsonStr.contains("helloworld"));
        //assertEquals(true, appResultAsJsonStr.contains("911"));

        //Thread.sleep(3000);

        //var listAppUrl = "http://localhost:8080/app/list";

        //restTemplate = new RestTemplate();
        //ResponseEntity<String> listResultAsJsonStr = restTemplate.getForEntity(listAppUrl, String.class);

        //assertEquals(HttpStatus.OK, listResultAsJsonStr.getStatusCode());
        //assertEquals(true, listResultAsJsonStr.getBody().equals("[]"));
    //}

}