import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestMath {
    public static double calculateBExponential(double days, double coefficient) {
        return -Math.log(coefficient) / (Math.sqrt(days));
    }

    public static double calculateYExponential(double x, double b) {
        return Math.exp(-b * Math.sqrt(x));
    }

    public static double calculateBLinear(double days, double coefficient) {
        return (1 - coefficient) / days;
    }

    public static double calculateYLinear(double x, double b) {
        return 1 - b * x;
    }

    public static double calculateBLogarithmic(double days, double coefficient) {
        return (1 / coefficient - 1) / (days * days * days);
    }

    public static double calculateYLogarithmic(double x, double b) {
        return 1 / (1 + b * x * x * x);
    }

    @Test
    public void calculateFuncions(){
        double days = 8;
        double coefficient = 0.98;

        double bExponential = calculateBExponential(days, coefficient);
        double bLinear = calculateBLinear(days, coefficient);
        double bLogarithmic = calculateBLogarithmic(days, coefficient);

        System.out.println("Коэффициент b для экспоненциальной функции: " + bExponential);
        System.out.println("Коэффициент b для линейной функции: " + bLinear);
        System.out.println("Коэффициент b для логарифмической функции: " + bLogarithmic);

        int day = 8;
        double yExponential = calculateYExponential(day, bExponential);
        double yLinear = calculateYLinear(day, bLinear);
        double yLogarithmic = calculateYLogarithmic(day, bLogarithmic);

        System.out.println("Значение y для экспоненциальной функции при x = " + day + ": " + yExponential);
        System.out.println("Значение y для линейной функции при x = " + day + ": " + yLinear);
        System.out.println("Значение y для логарифмической функции при x = " + day + ": " + yLogarithmic);

        Assertions.assertEquals(yExponential, yLinear, 1e-5);
        Assertions.assertEquals(yLogarithmic, yLinear, 1e-5);
    }
}
