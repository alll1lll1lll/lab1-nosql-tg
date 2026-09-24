package com.university.booking.commands.user;

import com.university.booking.commands.Command;
import com.university.booking.commands.CommandType;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.university.booking.service.dialog.StateService;
import org.springframework.stereotype.Component;

@Component
public class StartCommand extends Command {

    private final StateService stateService;

    public StartCommand(StateService stateService) {
        super(CommandType.START);
        this.stateService = stateService;
    }

    @Override
    public SendMessage handle(Update update, long chatId, String text) {
        if (stateService.get(chatId).getPersonId() != null) {
            return new SendMessage(chatId, "С возвращением! Ты уже зарегистрирован.\nВведи /help для списка команд.");
        }
        return new SendMessage(chatId,
                "Привет! Это бот бронирования учебных помещений.\n\nДля начала зарегистрируйся: /register\nСписок команд: /help");
    }
}
