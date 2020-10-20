package hello;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.*;

public class HelloTest {
    @Test
    public void testHello() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        Hello.print();
        assertTrue(out.toString().toLowerCase().contains("hello world"));
    }
}