package org.broscorp.cryptobot.bot;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.broscorp.cryptobot.dto.CurrencyDTO;
import org.broscorp.cryptobot.model.User;
import org.broscorp.cryptobot.service.CryptoApiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
    private final CryptoApiService cryptoApiService;
    @Getter
    private final Map<Long, User> users = new HashMap<>();

    @Value("${bot.name}")
    private String botName;
    @Value("${bot.token}")
    private String botToken;
    @Value("${bot.max-chats}")
    private int maxChats;

    @PostConstruct
    private void init() {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(this);
        } catch (TelegramApiException e) {
            throw new RuntimeException("Couldn't register the bot: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start":
                    startNewChat(chatId);
                    break;
                default:
                    //TODO: smth
            }
        }
    }

    private void startNewChat(long chatId) {
        if (users.size() < maxChats || users.containsKey(chatId)) {
            sendMessage(chatId, "Hi, welcome to Crypto BroBot!");

            User user = new User();
            Map<String, Double> state = new HashMap<>();
            List<CurrencyDTO> currencies = cryptoApiService.getListFromApi();
            for (CurrencyDTO dto: currencies) {
                state.put(dto.getSymbol(), dto.getPrice());
            }
            user.setInitState(state);
            user.setInitTime(LocalDateTime.now());

            users.put(chatId, user);
        } else {
            sendMessage(chatId, "Sorry, but bot isn't available(.");
        }
    }

    public void sendMessage(Long chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
