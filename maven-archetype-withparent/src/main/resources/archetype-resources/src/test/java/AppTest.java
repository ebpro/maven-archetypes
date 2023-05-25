package $package;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test for simple App.
 */
class AppTest {

    @Test
    void addCorrect() {
        assertEquals(5, new App().add(3, 2));
    }

    @Test
    void addCheckException1() {
        App app = new App();        
        assertThrows(NumberFormatException.class, () -> app.add(-3, 2));
    }

    @Test
    void addCheckException2() {
        App app = new App();        
        assertThrows(NumberFormatException.class, () -> app.add(3, -2));
    }

}
