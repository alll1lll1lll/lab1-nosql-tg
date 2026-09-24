package com.university.booking.commands.user;

import com.university.booking.commands.Command;
import com.university.booking.commands.CommandType;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.university.booking.client.BackendClient;
import com.university.booking.dto.BookingDto;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class BookingsCommand extends Command {

    private final BackendClient client;

    public BookingsCommand(BackendClient client) {
        super(CommandType.BOOKINGS);
        this.client = client;
    }

    @Override
    public SendMessage handle(Update update, long chatId, String text) {
        List<BookingDto> bookings = client.getBookings();
        if (bookings.isEmpty()) {
            return new SendMessage(chatId, "Бронирований пока нет.");
        }
        StringBuilder sb = new StringBuilder("Бронирования:\n\n");
        for (BookingDto b : bookings) {
            sb.append("ID: ").append(b.getId()).append("\n")
              .append("  ").append(b.getEventName()).append("\n")
              .append("  ").append(b.getEventDate())
              .append("  ").append(b.getStartTime()).append("–").append(b.getEndTime()).append("\n")
              .append("  Статус: ").append(b.getStatus()).append("\n\n");
        }
        return new SendMessage(chatId, sb.toString().trim());
    }
}
