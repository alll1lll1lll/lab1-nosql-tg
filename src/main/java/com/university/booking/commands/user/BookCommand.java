package com.university.booking.commands.user;

import com.university.booking.commands.Command;
import com.university.booking.commands.CommandType;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.university.booking.client.BackendClient;
import com.university.booking.dto.RoomDto;
import com.university.booking.exception.NotRegisteredException;
import com.university.booking.service.dialog.StateService;
import com.university.booking.state.State;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class BookCommand extends Command {

    private final BackendClient client;
    private final StateService stateService;

    public BookCommand(BackendClient client, StateService stateService) {
        super(CommandType.BOOK);
        this.client = client;
        this.stateService = stateService;
    }

    @Override
    public SendMessage handle(Update update, long chatId, String text) {
        if (stateService.get(chatId).getPersonId() == null) throw new NotRegisteredException();

        List<RoomDto> rooms = client.getRooms();
        if (rooms.isEmpty()) {
            return new SendMessage(chatId, "Нет доступных комнат.");
        }
        StringBuilder sb = new StringBuilder("Доступные комнаты:\n\n");
        for (RoomDto r : rooms) {
            sb.append("ID: ").append(r.getId()).append(" — ").append(r.getName())
              .append(" [").append(r.getType()).append(", до ").append(r.getCapacity()).append(" чел]\n");
        }
        sb.append("\nВведи ID комнаты:");
        stateService.updateContext(chatId, ctx -> ctx.setState(State.BOOK_ROOM));
        return new SendMessage(chatId, sb.toString());
    }
}
