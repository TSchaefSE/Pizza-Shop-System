package pizza_shop_system.utils;

public class StringUtil {
    public String captilizeWord(String word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }
}
