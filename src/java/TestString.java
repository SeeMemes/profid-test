import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TestString {
    @Test
    public void test() {
        String lowerItemName = "AWP | Crakow! (Battle-Scarred)"
                .toLowerCase()
                .replaceAll("[^a-zA-Z0-9 ★]", "") // Убираем лишние символы, кроме ★
                .trim();

        List<String> list = List.of("awp  crakow"
                .toLowerCase()
                .replaceAll("[^a-zA-Z0-9 ★]", "") // Убираем лишние символы, кроме ★
                .trim());

        Assertions.assertFalse(list.stream()
                .noneMatch(lowerItemName::contains));
    }
}
