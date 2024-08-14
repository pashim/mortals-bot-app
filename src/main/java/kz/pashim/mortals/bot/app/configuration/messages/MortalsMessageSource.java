package kz.pashim.mortals.bot.app.configuration.messages;

import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class MortalsMessageSource extends ResourceBundleMessageSource {

    public String getMessage(String path, Object... args) {
        return super.getMessage(path, args, Locale.getDefault());
    }
}
