package com.university.booking.commands.user;

import com.university.booking.commands.Command;
import com.university.booking.commands.CommandType;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.university.booking.client.BackendClient;
import com.university.booking.dto.RoomDto;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class RoomsCommand extends Command {

    private static final Map<String, String> BUILDING_NAMES = new LinkedHashMap<>();

    static {
        BUILDING_NAMES.put("K49", "Кронверкский пр., 49");
        BUILDING_NAMES.put("L9",  "ул. Ломоносова, 9");
        BUILDING_NAMES.put("GR",  "ул. Гривцова, 1/2");
        BUILDING_NAMES.put("G12", "ул. Гастелло, 12");
        BUILDING_NAMES.put("B14", "Биржевая линия, 14");
        BUILDING_NAMES.put("TB",  "Пространство T-Bank");
    }

    private final BackendClient client;

    public RoomsCommand(BackendClient client) {
        super(CommandType.ROOMS);
        this.client = client;
    }

    @Override
    public boolean acceptsArguments() {
        return true;
    }

    @Override
    public SendMessage handle(Update update, long chatId, String text) {
        String args = text.replaceFirst("(?i)^/rooms\\s*", "").trim().toLowerCase();

        if (args.isEmpty()) {
            return new SendMessage(chatId,
                    "Укажи тип помещения:\n" +
                    "  /rooms classroom — аудитории\n" +
                    "  /rooms coworking — коворкинги");
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
                .collect(Collectors.toList());

        if (all.isEmpty()) {
            return new SendMessage(chatId, "Помещений не найдено.");
        }

        if (building == null) {
            return buildBuildingMenu(chatId, type, all);
        }

        String prefix = building + "-";
        List<RoomDto> filtered = all.stream()
                .filter(r -> r.getId().startsWith(prefix))
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            return new SendMessage(chatId, "Помещения не найдены для корпуса: " + building);
        }

        String buildingName = BUILDING_NAMES.getOrDefault(building, building);
        String label = roomType.equals("CLASSROOM") ? "Аудитории" : "Коворкинги";
        StringBuilder sb = new StringBuilder(label + " (" + buildingName + "):\n\n");
        for (RoomDto r : filtered) {
            String floor = r.getLocation().replaceAll(".*,\\s*", "");
            sb.append(r.getId()).append(" — ").append(r.getName())
              .append(" | вмест. ").append(r.getCapacity())
              .append(" | ").append(floor).append("\n");
        }
        return new SendMessage(chatId, sb.toString().trim());
    }

    private SendMessage buildBuildingMenu(long chatId, String type, List<RoomDto> rooms) {
        String label = type.equals("classroom") ? "Аудитории" : "Коворкинги";
        Map<String, Long> counts = rooms.stream()
                .collect(Collectors.groupingBy(r -> r.getId().split("-")[0], Collectors.counting()));

        StringBuilder sb = new StringBuilder(label + " по корпусам:\n\n");
        for (Map.Entry<String, String> entry : BUILDING_NAMES.entrySet()) {
            String prefix = entry.getKey();
            if (!counts.containsKey(prefix)) continue;
            sb.append("• ").append(entry.getValue())
              .append(" (").append(counts.get(prefix)).append(" шт.)")
              .append(" → /rooms ").append(type).append(" ").append(prefix.toLowerCase()).append("\n");
        }
        return new SendMessage(chatId, sb.toString().trim());
    }
}
