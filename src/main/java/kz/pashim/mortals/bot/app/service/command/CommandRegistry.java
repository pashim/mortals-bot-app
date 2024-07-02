package kz.pashim.mortals.bot.app.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CommandRegistry {

    private final ApplicationContext applicationContext;

    private Map<String, Command> commands = new HashMap<>();

    @PostConstruct
    public void init() {
       commands = applicationContext.getBeansOfType(Command.class).values()
               .stream().collect(Collectors.toMap(Command::command, Function.identity()));
    }

    public Command getCommand(String commandName) {
        return commands.get(commandName);
    }
}