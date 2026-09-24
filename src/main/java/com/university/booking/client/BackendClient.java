package com.university.booking.client;

import com.university.booking.dto.BookingCreateRequest;
import com.university.booking.dto.BookingDto;
import com.university.booking.dto.CategoryDto;
import com.university.booking.dto.PersonCreateRequest;
import com.university.booking.dto.PersonDto;
import com.university.booking.dto.RoomDto;
import com.university.booking.exception.BackendException;
import com.university.booking.exception.NotRegisteredException;
import com.university.booking.exception.ResourceNotFoundException;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class BackendClient {

    private final RestTemplate restTemplate;

    @Value("${backend.url}")
    private String baseUrl;

    public List<RoomDto> getRooms() {
        try {
            RoomDto[] rooms = restTemplate.getForObject(baseUrl + "/api/rooms", RoomDto[].class);
            return rooms != null ? Arrays.asList(rooms) : List.of();
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new NotRegisteredException();
        } catch (RestClientException e) {
            log.atError().addKeyValue("endpoint", "GET /api/rooms").setCause(e).log("backend request failed");
            throw new BackendException();
        }
    }

    public RoomDto getRoom(String id) {
        try {
            RoomDto room = restTemplate.getForObject(baseUrl + "/api/rooms/" + id, RoomDto.class);
            if (room == null) throw new ResourceNotFoundException(id);
            return room;
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new NotRegisteredException();
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException(id);
        } catch (RestClientException e) {
            log.atError().addKeyValue("endpoint", "GET /api/rooms/" + id).setCause(e).log("backend request failed");
            throw new BackendException();
        }
    }

    public List<BookingDto> getBookings() {
        try {
            BookingDto[] list = restTemplate.getForObject(baseUrl + "/api/bookings", BookingDto[].class);
            return list != null ? Arrays.asList(list) : List.of();
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new NotRegisteredException();
        } catch (RestClientException e) {
            log.atError().addKeyValue("endpoint", "GET /api/bookings").setCause(e).log("backend request failed");
            throw new BackendException();
        }
    }

    public BookingDto getBooking(String id) {
        try {
            BookingDto booking = restTemplate.getForObject(baseUrl + "/api/bookings/" + id, BookingDto.class);
            if (booking == null) throw new ResourceNotFoundException(id);
            return booking;
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new NotRegisteredException();
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException(id);
        } catch (RestClientException e) {
            log.atError().addKeyValue("endpoint", "GET /api/bookings/" + id).setCause(e).log("backend request failed");
            throw new BackendException();
        }
    }

    public BookingDto createBooking(BookingCreateRequest request) {
        try {
            BookingDto booking = restTemplate.postForObject(baseUrl + "/api/bookings", request, BookingDto.class);
            if (booking == null) throw new BackendException();
            return booking;
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new NotRegisteredException();
        } catch (RestClientException e) {
            log.atError().addKeyValue("endpoint", "POST /api/bookings").setCause(e).log("backend request failed");
            throw new BackendException();
        }
    }

    public BookingDto submitBooking(String id) {
        try {
            BookingDto booking = restTemplate.exchange(
                    baseUrl + "/api/bookings/" + id + "/submit",
                    HttpMethod.PUT, null, BookingDto.class).getBody();
            if (booking == null) throw new BackendException();
            return booking;
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new NotRegisteredException();
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException(id);
        } catch (RestClientException e) {
            log.atError().addKeyValue("endpoint", "PUT /api/bookings/" + id + "/submit").setCause(e).log("backend request failed");
            throw new BackendException();
        }
    }

    public BookingDto approveBooking(String id, String adminId) {
        try {
            BookingDto booking = restTemplate.exchange(
                    baseUrl + "/api/bookings/" + id + "/approve?adminId=" + adminId,
                    HttpMethod.PUT, null, BookingDto.class).getBody();
            if (booking == null) throw new BackendException();
            return booking;
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new NotRegisteredException();
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException(id);
        } catch (RestClientException e) {
            log.atError().addKeyValue("endpoint", "PUT /api/bookings/" + id + "/approve").setCause(e).log("backend request failed");
            throw new BackendException();
        }
    }

    public BookingDto rejectBooking(String id, String adminId) {
        try {
            BookingDto booking = restTemplate.exchange(
                    baseUrl + "/api/bookings/" + id + "/reject?adminId=" + adminId,
                    HttpMethod.PUT, null, BookingDto.class).getBody();
            if (booking == null) throw new BackendException();
            return booking;
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new NotRegisteredException();
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException(id);
        } catch (RestClientException e) {
            log.atError().addKeyValue("endpoint", "PUT /api/bookings/" + id + "/reject").setCause(e).log("backend request failed");
            throw new BackendException();
        }
    }

    public void deleteBooking(String id) {
        try {
            restTemplate.delete(baseUrl + "/api/bookings/" + id);
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new NotRegisteredException();
        } catch (RestClientException e) {
            log.atError().addKeyValue("endpoint", "DELETE /api/bookings/" + id).setCause(e).log("backend request failed");
            throw new BackendException();
        }
    }

    public List<PersonDto> getPersons() {
        try {
            PersonDto[] list = restTemplate.getForObject(baseUrl + "/api/persons", PersonDto[].class);
            return list != null ? Arrays.asList(list) : List.of();
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new NotRegisteredException();
        } catch (RestClientException e) {
            log.atError().addKeyValue("endpoint", "GET /api/persons").setCause(e).log("backend request failed");
            throw new BackendException();
        }
    }

    public PersonDto createPerson(PersonCreateRequest request) {
        try {
            PersonDto person = restTemplate.postForObject(baseUrl + "/api/persons", request, PersonDto.class);
            if (person == null) throw new BackendException();
            return person;
        } catch (RestClientException e) {
            log.atError().addKeyValue("endpoint", "POST /api/persons").setCause(e).log("backend request failed");
            throw new BackendException();
        }
    }

    public List<CategoryDto> getCategories() {
        try {
            CategoryDto[] list = restTemplate.getForObject(baseUrl + "/api/categories", CategoryDto[].class);
            return list != null ? Arrays.asList(list) : List.of();
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new NotRegisteredException();
        } catch (RestClientException e) {
            log.atError().addKeyValue("endpoint", "GET /api/categories").setCause(e).log("backend request failed");
            throw new BackendException();
        }
    }
}
