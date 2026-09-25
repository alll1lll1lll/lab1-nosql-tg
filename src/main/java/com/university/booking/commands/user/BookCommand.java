package com.university.booking.commands.user;

import com.university.booking.commands.Command;
import com.university.booking.commands.CommandType;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.university.booking.client.BackendClient;
import com.university.booking.dto.RoomDto;
import com.university.booking.enums.PersonRole;
import com.university.booking.exception.NotRegisteredException;
import com.university.booking.service.dialog.StateService;
import com.university.booking.state.State;
import java.util.List;
import java.util.stream.Collectors;
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
    public boolean acceptsArguments() {
        return true;
    }

    @Override
    public SendMessage handle(Update update, long chatId, String text) {
        if (stateService.get(chatId).getPersonId() == null) throw new NotRegisteredException();

        PersonRole role = stateService.get(chatId).getPersonRole();
        String args = text.replaceFirst("(?i)^/book\\s*", "").trim().toLowerCase();

        if (args.isEmpty()) {
            return new SendMessage(chatId,
                    "Укажи тип помещения:\n" +
                    "  /book classroom — аудитории\n" +
                    "  /book coworking — коворкинги");
        }

        String[] parts = args.split("\\s+", 2);
        String type = parts[0];
        String building = parts.length > 1 ? parts[1].toUpperCase() : null;

        if (!type.equals("classroom") && !type.equals("coworking")) {
            return new SendMessage(chatId, "Неверный тип. Используй classroom или coworking.");
        }

        String roomType = type.equals("classroom") ? "CLASSROOM" : "COWORKING";
        List<RoomDto> all = client.getRooms().stream()
                .filter(r -> r.getType().equals(roomType))
                .filter(r -> !(r.isTeacherOnly() && role == PersonRole.STUDENT))
                .collect(Collectors.toList());

        if (all.isEmpty()) {
            return new SendMessage(chatId, "Помещений не найдено.");
        }

        if (building == null) {
            String label = type.equals("classroom") ? "Аудитории" : "Коворкинги";
            return new SendMessage(chatId, RoomMenuHelper.buildBuildingMenu(label, "book", type, all));
        }

        String prefix = building + "-";
        List<RoomDto> filtered = all.stream()
                .filter(r -> r.getId().startsWith(prefix))
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            return new SendMessage(chatId, "Помещения не найдены для корпуса: " + building);
        }

        String buildingName = RoomMenuHelper.BUILDING_NAMES.getOrDefault(building, building);
        String label = roomType.equals("CLASSROOM") ? "Аудитории" : "Коворкинги";
        StringBuilder sb = new StringBuilder(label + " (" + buildingName + "):\n\n");
        for (RoomDto r : filtered) {
            sb.append(RoomMenuHelper.formatRoomLine(r)).append("\n");
        }
        sb.append("\nВведи ID комнаты:");

        stateService.updateContext(chatId, ctx -> ctx.setState(State.BOOK_ROOM));
        return new SendMessage(chatId, sb.toString().trim());
    }
}
