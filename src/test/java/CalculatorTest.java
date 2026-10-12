import com.UrlShortner.Calculator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class CalculatorTest {

    @Test
    void ShouldAddTwoNums(){
        Calculator calculator = new Calculator();
       int res= calculator.add(2,3);
       assertEquals(5,res);
    }
    @Test
    void ShouldAddTwoNums1(){
        Calculator calculator = new Calculator();
        int res= calculator.add(2,3);
        assertEquals(5,res);
    }

}
