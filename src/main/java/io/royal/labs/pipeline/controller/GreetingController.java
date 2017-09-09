package io.royal.labs.pipeline.controller;
// Added a Tag 
import io.royal.labs.pipeline.model.GreetingResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicLong;

@RestController
public class GreetingController {

    private static final String TEMPLATE = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/greeting")
    public GreetingResponse greeting(@RequestParam(value="name", defaultValue="World") String name) {

        return new GreetingResponse(counter.incrementAndGet(), String.format(TEMPLATE, name));
    }
}
