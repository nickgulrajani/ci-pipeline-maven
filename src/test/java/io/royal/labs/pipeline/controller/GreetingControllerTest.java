package io.royal.labs.pipeline.controller;

import io.royal.labs.pipeline.model.GreetingResponse;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GreetingControllerTest {

    private static GreetingController controller;

    @BeforeClass
    public static void intiClass() {

        controller = new GreetingController();
    }

    @Test
    public void testController() {

        GreetingResponse response = controller.greeting("World");

        Assert.assertEquals("Hello, World!", response.getContent());
        Assert.assertTrue(response.getId() > 0);
    }
}
