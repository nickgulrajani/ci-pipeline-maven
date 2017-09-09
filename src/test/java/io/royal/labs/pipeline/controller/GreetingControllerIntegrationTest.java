package io.royal.labs.pipeline.controller;

import io.royal.labs.pipeline.model.GreetingTestResponse;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GreetingControllerIntegrationTest {

    @Value(value="${local.server.port}")
    private String port;
    private String host = "http://localhost";

    private static RestTemplate restTemplate;

    @BeforeClass
    public static void initClass() {

        RestTemplateBuilder builder = new RestTemplateBuilder();

        restTemplate = builder.build();
    }

    @Test
    public void test() {

        String url = host + ":" + port + "/greeting";

        ResponseEntity<GreetingTestResponse> response = restTemplate.getForEntity(url, GreetingTestResponse.class, "World");

        Assert.assertNotNull(response);
        Assert.assertTrue(response.getStatusCode().equals(HttpStatus.OK));

        GreetingTestResponse body = response.getBody();

        Assert.assertTrue(body.getId() > 0);
        Assert.assertTrue(body.getContent().equals("Hello, World!"));
    }
}
