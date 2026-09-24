package com.university.booking.service.dialog;

import com.pengrad.telegrambot.request.SendMessage;
import com.university.booking.client.BackendClient;
import com.university.booking.dto.BookingCreateRequest;
import com.university.booking.dto.BookingDto;
import com.university.booking.dto.CategoryDto;
import com.university.booking.state.Context;
import com.university.booking.state.State;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingDialogHandler implements DialogStageHandler {

    private final BackendClient client;
    private final StateService stateService;

    @Override
    public boolean supports(State state) {
        return switch (state) {
            case BOOK_ROOM, BOOK_CATEGORY, BOOK_EVENT_NAME,
                 BOOK_DATE, BOOK_START_TIME, BOOK_END_TIME,
                 BOOK_PARTICIPANTS, BOOK_PHONE -> true;
            default -> false;
        };
    }

    @Override
    public SendMessage handle(long chatId, String text) {
        Context ctx = stateService.get(chatId);
        return switch (ctx.getState()) {
            case BOOK_ROOM        -> handleRoom(chatId, text.trim());
            case BOOK_CATEGORY    -> handleCategory(chatId, text.trim());
            case BOOK_EVENT_NAME  -> handleEventName(chatId, text.trim());
            case BOOK_DATE        -> handleDate(chatId, text.trim());
            case BOOK_START_TIME  -> handleStartTime(chatId, text.trim());
            case BOOK_END_TIME    -> handleEndTime(chatId, text.trim());
            case BOOK_PARTICIPANTS -> handleParticipants(chatId, text.trim());
            case BOOK_PHONE       -> handlePhone(chatId, text.trim(), ctx);
            default -> new SendMessage(chatId, "Неизвестное состояние бронирования.");
        };
    }

    private SendMessage handleRoom(long chatId, String roomId) {
        List<CategoryDto> cats = client.getCategories();
        if (cats.isEmpty()) {
            stateService.reset(chatId);
            return new SendMessage(chatId, "Нет доступных категорий. Обратись к администратору.");
        }
        stateService.updateContext(chatId, ctx -> {
            ctx.setBookRoomId(roomId);
            ctx.setState(State.BOOK_CATEGORY);
        });
        StringBuilder sb = new StringBuilder("Категории:\n\n");
        for (CategoryDto c : cats) {
            sb.append("ID: ").append(c.getId()).append(" — ").append(c.getName());
            if (c.getDescription() != null && !c.getDescription().isBlank()) {
                sb.append(" (").append(c.getDescription()).append(")");
            }
            sb.append("\n");
        }
        sb.append("\nВведи ID категории:");
        return new SendMessage(chatId, sb.toString());
    }

    private SendMessage handleCategory(long chatId, String categoryId) {
        stateService.updateContext(chatId, ctx -> {
            ctx.setBookCategoryId(categoryId);
            ctx.setState(State.BOOK_EVENT_NAME);
        });
        return new SendMessage(chatId, "Введи название мероприятия:");
    }

    private SendMessage handleEventName(long chatId, String name) {
        stateService.updateContext(chatId, ctx -> {
            ctx.setBookEventName(name);
            ctx.setState(State.BOOK_DATE);
        });
        return new SendMessage(chatId, "Введи дату в формате ГГГГ-ММ-ДД:");
    }

    private SendMessage handleDate(long chatId, String text) {
        try {
            LocalDate date = LocalDate.parse(text);
            stateService.updateContext(chatId, ctx -> {
                ctx.setBookDate(date);
                ctx.setState(State.BOOK_START_TIME);
            });
            return new SendMessage(chatId, "Введи время начала в формате ЧЧ:ММ:");
        } catch (DateTimeParseException e) {
            return new SendMessage(chatId, "Неверный формат. Введи дату как ГГГГ-ММ-ДД:");
        }
    }

    private SendMessage handleStartTime(long chatId, String text) {
        try {
            LocalTime time = LocalTime.parse(text);
            stateService.updateContext(chatId, ctx -> {
                ctx.setBookStartTime(time);
                ctx.setState(State.BOOK_END_TIME);
            });
            return new SendMessage(chatId, "Введи время окончания в формате ЧЧ:ММ:");
        } catch (DateTimeParseException e) {
            return new SendMessage(chatId, "Неверный формат. Введи время как ЧЧ:ММ:");
        }
    }

    private SendMessage handleEndTime(long chatId, String text) {
        try {
            LocalTime time = LocalTime.parse(text);
            stateService.updateContext(chatId, ctx -> {
                ctx.setBookEndTime(time);
                ctx.setState(State.BOOK_PARTICIPANTS);
            });
            return new SendMessage(chatId, "Введи количество участников:");
        } catch (DateTimeParseException e) {
            return new SendMessage(chatId, "Неверный формат. Введи время как ЧЧ:ММ:");
        }
    }

    private SendMessage handleParticipants(long chatId, String text) {
        int count;
        try {
            count = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return new SendMessage(chatId, "Введи целое число больше 0:");
        }
        if (count < 1) {
            return new SendMessage(chatId, "Введи целое число больше 0:");
        }
        stateService.updateContext(chatId, ctx -> {
            ctx.setBookParticipants(count);
            ctx.setState(State.BOOK_PHONE);
        });
        return new SendMessage(chatId, "Введи контактный телефон:");
    }

    private SendMessage handlePhone(long chatId, String phone, Context ctx) {
        BookingCreateRequest req = BookingCreateRequest.builder()
                .personId(ctx.getPersonId())
                .roomId(ctx.getBookRoomId())
                .categoryId(ctx.getBookCategoryId())
                .eventName(ctx.getBookEventName())
                .eventDate(ctx.getBookDate())
                .startTime(ctx.getBookStartTime())
                .endTime(ctx.getBookEndTime())
                .participantCount(ctx.getBookParticipants())
                .contactPhone(phone)
                .build();
        BookingDto booking = client.createBooking(req);
        stateService.reset(chatId);
        log.atInfo()
                .addKeyValue("event", "booking_created")
                .addKeyValue("booking_id", booking.getId())
                .addKeyValue("chat_id", chatId)
                .log("booking created successfully");
        return new SendMessage(chatId, "Бронь создана!\nID: " + booking.getId()
                + "\nСтатус: " + booking.getStatus()
                + "\n\nЧтобы отправить на рассмотрение:\n/submit " + booking.getId());
    }
}
