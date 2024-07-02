package kz.pashim.mortals.bot.app.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class TelegramBotListener extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return "MortalsBot";
    }

    @Override
    public String getBotToken() {
        return "7209512559:AAEfcF46jI4U0sEExxbMxKNeZMR5_s67JWI";
    }

    @Override
    public void onUpdateReceived(Update update) {
        var msg = update.getMessage();
        var user = msg.getFrom();

        log.info(update.toString());
        sendText(user.getId(), msg.getText());
    }

    public void sendText(Long who, String what){
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString()) //Who are we sending a message to
                .text("hello " + what).build();    //Message content
        try {
            execute(sm);                        //Actually sending the message
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);      //Any error will be printed here
        }
    }
}
