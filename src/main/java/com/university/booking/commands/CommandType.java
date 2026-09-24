package com.university.booking.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommandType {
    START("/start", "начало работы с ботом", false),
    HELP("/help", "список всех команд", false),
    REGISTER("/register", "зарегистрироваться", false),
    ROOMS("/rooms", "список доступных комнат", false),
    ROOM("/room", "информация о комнате: /room <id>", false),
    BOOKINGS("/bookings", "список всех бронирований", false),
    BOOKING("/booking", "детали бронирования: /booking <id>", false),
    BOOK("/book", "создать бронирование (пошаговый диалог)", false),
    SUBMIT("/submit", "отправить черновик на рассмотрение: /submit <id>", false),
    CANCEL("/cancel", "отменить действие или удалить бронь: /cancel <id>", false),
    APPROVE("/approve", "одобрить бронирование: /approve <id>", true),
    REJECT("/reject", "отклонить бронирование: /reject <id>", true),
    PERSONS("/persons", "список пользователей", true),
    ADDPERSON("/addperson", "добавить пользователя: /addperson <имя> <роль>", true);

    private final String commandName;
    private final String description;
    private final boolean adminOnly;
}
