package com.university.booking.commands.user;

import com.university.booking.dto.RoomDto;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class RoomMenuHelper {

    static final Map<String, String> BUILDING_NAMES = new LinkedHashMap<>();

    static {
        BUILDING_NAMES.put("K49", "Кронверкский пр., 49");
        BUILDING_NAMES.put("L9",  "ул. Ломоносова, 9");
        BUILDING_NAMES.put("GR",  "ул. Гривцова, 1/2");
        BUILDING_NAMES.put("G12", "ул. Гастелло, 12");
        BUILDING_NAMES.put("B14", "Биржевая линия, 14");
        BUILDING_NAMES.put("TB",  "Пространство T-Bank");
    }

    private RoomMenuHelper() {}

    static String buildBuildingMenu(String label, String command, String type, List<RoomDto> rooms) {
        Map<String, Long> counts = rooms.stream()
                .collect(Collectors.groupingBy(r -> r.getId().split("-")[0], Collectors.counting()));

        StringBuilder sb = new StringBuilder(label + " по корпусам:\n\n");
        for (Map.Entry<String, String> entry : BUILDING_NAMES.entrySet()) {
            String prefix = entry.getKey();
            if (!counts.containsKey(prefix)) continue;
            sb.append("• ").append(entry.getValue())
              .append(" (").append(counts.get(prefix)).append(" шт.)")
              .append(" → /").append(command).append(" ").append(type).append(" ").append(prefix.toLowerCase()).append("\n");
        }
        return sb.toString().trim();
    }

    static String formatRoomLine(RoomDto r) {
        String floor = r.getLocation().replaceAll(".*,\\s*", "");
        return r.getId() + " — " + r.getName()
                + " | вмест. " + r.getCapacity()
                + " | " + floor;
    }
}
