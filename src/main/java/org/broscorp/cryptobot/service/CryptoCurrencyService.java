package org.broscorp.cryptobot.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.broscorp.cryptobot.bot.TelegramBot;
import org.broscorp.cryptobot.dto.CurrencyDTO;
import org.broscorp.cryptobot.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CryptoCurrencyService {
    private final CryptoApiService cryptoApiService;

    private final TelegramBot telegramBot;
    private List<CurrencyDTO> currentCurrencies;

    @Value("${currency.percent}")
    private double minChangePercent;

    @PostConstruct
    private void init() {
        //load current currencies
        currentCurrencies = cryptoApiService.getListFromApi();
        log.info("Loaded {} currencies from api.", currentCurrencies.size());

        //start the bot
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(telegramBot);
        } catch (TelegramApiException e) {
            throw new RuntimeException("Couldn't register the bot: " + e.getMessage());
        }
    }

    @Scheduled(initialDelayString = "${api.refresh-rate}000", fixedRateString = "${api.refresh-rate}000")
    private void refreshData() {
        log.info("Refreshing data...");
        currentCurrencies = cryptoApiService.getListFromApi();

        Map<Long, User> users = telegramBot.getUsers();
        for (Long chatId : users.keySet()) {
            User user = users.get(chatId);
            for (CurrencyDTO currency : currentCurrencies) {
                if (user.getInitState().containsKey(currency.getSymbol())) {
                    double oldPrice = user.getInitState().get(currency.getSymbol());

                    double diffPercent = Math.abs(oldPrice - currency.getPrice())
                            / oldPrice * 100;

                    if (diffPercent > minChangePercent) {
                        telegramBot.sendMessage(chatId,
                                String.format("Since %s currency %s has changed:\n %s -> %s",
                                        user.getInitTime(),
                                        currency.getSymbol(),
                                        BigDecimal.valueOf(oldPrice).toPlainString(),
                                        BigDecimal.valueOf(currency.getPrice()).toPlainString()));
                    }
                } else {
                    telegramBot.sendMessage(chatId,
                            String.format("New currency appeared: %s", currency.getSymbol()));
                }
            }
        }
    }
}
