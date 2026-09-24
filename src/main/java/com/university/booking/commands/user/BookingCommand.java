package com.university.booking.commands.user;

import com.university.booking.commands.Command;
import com.university.booking.commands.CommandType;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.university.booking.client.BackendClient;
import com.university.booking.dto.BookingDto;
import com.university.booking.service.routing.CommandParser;
import org.springframework.stereotype.Component;

@Component
public class BookingCommand extends Command {

    private final BackendClient client;
    private final CommandParser parser;

    public BookingCommand(BackendClient client, CommandParser parser) {
        super(CommandType.BOOKING);
        this.client = client;
        this.parser = parser;
    }

    @Override
    public boolean acceptsArguments() {
        return true;
    }

    @Override
    public SendMessage handle(Update update, long chatId, String text) {
        String id = parser.extractArguments(text);
        if (id.isBlank()) {
            return new SendMessage(chatId, "Укажи ID: /booking <id>");
        }
        BookingDto b = client.getBooking(id);
        return new SendMessage(chatId, "Бронирование: " + b.getEventName()
                + "\nID: " + b.getId()
                + "\nДата: " + b.getEventDate()
                + "\nВремя: " + b.getStartTime() + "–" + b.getEndTime()
                + "\nКомната: " + b.getRoomId()
                + "\nКатегория: " + b.getCategoryId()
                + "\nУчастников: " + b.getParticipantCount()
                + "\nТелефон: " + b.getContactPhone()
                + "\nСтатус: " + b.getStatus());
    }
}
