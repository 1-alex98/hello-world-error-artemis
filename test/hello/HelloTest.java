package hello;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;

import static org.junit.Assert.*;

public class HelloTest {
    @Test
    public void testHello() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        Hello.print();
        String output = out.toString();
        assertTrue(MessageFormat.format("The output ''{}'' did contain 'hello world'",output), output.toLowerCase().contains("hello world"));
    }
}