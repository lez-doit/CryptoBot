package org.broscorp.cryptobot.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Getter
@Setter
public class User {
    private Map<String, Double> initState;

    private LocalDateTime initTime;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String getInitTime() {
        return initTime.format(formatter);
    }
}
