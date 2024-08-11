package kz.pashim.mortals.bot.app.util;

import org.springframework.util.StringUtils;

public class BotMessageUtils {

    public static String extractCommand(String message) {
        if (!StringUtils.hasText(message)) {
            return null;
        }
        if (message.startsWith("/")) {
            return message.split(" ")[0];
        }
        return null;
    }

    public static String extractFirstArgument(String message) {
        if (message.startsWith("/")) {
            // Разбиваем команду и аргументы по пробелам
            String[] parts = message.split(" ");

            if (parts.length <= 1) {
                return null;
            }

            // Остальные части - это аргументы
            String[] arguments = new String[parts.length - 1];
            System.arraycopy(parts, 1, arguments, 0, arguments.length);

            return arguments[0];
        }
        return null;
    }

    public static String[] extractArguments(String message) {
        if (message.startsWith("/")) {
            // Разбиваем команду и аргументы по пробелам
            String[] parts = message.split(" ");

            if (parts.length <= 1) {
                return null;
            }

            // Остальные части - это аргументы
            String[] arguments = new String[parts.length - 1];
            System.arraycopy(parts, 1, arguments, 0, arguments.length);

            return arguments;
        }
        return null;
    }
}
