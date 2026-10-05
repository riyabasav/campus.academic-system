package com.campus.events.service;

import com.campus.events.model.Club;
import com.campus.events.model.Event;
import com.campus.events.model.EventRegistration;
import com.campus.events.repository.ClubRepository;
import com.campus.events.repository.EventRegistrationRepository;
import com.campus.events.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final ClubRepository clubRepository;
    private final EmailService emailService;

    @Autowired
    public EventService(EventRepository eventRepository,
                        EventRegistrationRepository registrationRepository,
                        ClubRepository clubRepository,
                        EmailService emailService) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.clubRepository = clubRepository;
        this.emailService = emailService;
    }

    public List<Event> getAllEventsOrderedByDate() {
        return eventRepository.findAllByOrderByEventDateAsc();
    }

    public Optional<Event> getEventById(Long id) {
        return eventRepository.findById(id);
    }

    @Transactional
    public Event saveEvent(Event event) {
        return eventRepository.save(event);
    }

    public List<Club> getAllClubs() {
        return clubRepository.findAll();
    }

    @Transactional
    public Club saveClub(Club club) {
        return clubRepository.save(club);
    }

    @Transactional
    public EventRegistration registerStudentForEvent(Long eventId, String studentName, String studentEmail) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));

        if (event.getRemainingSeats() <= 0) {
            throw new IllegalStateException("Event is already at full capacity");
        }

        String ticketCode = generateTicketCode();

        EventRegistration registration = new EventRegistration(
                studentName,
                studentEmail,
                ticketCode,
                LocalDateTime.now(),
                event
        );

        event.getRegistrations().add(registration);
        EventRegistration savedRegistration = registrationRepository.save(registration);

        // Async email notification
        emailService.sendRegistrationConfirmation(savedRegistration);

        return savedRegistration;
    }

    public Optional<EventRegistration> getRegistrationByTicketCode(String ticketCode) {
        return registrationRepository.findByTicketCode(ticketCode);
    }

    private String generateTicketCode() {
        // Generates unique 8-character digital pass code e.g., "EVT-8F92A1"
        String hex = UUID.randomUUID().toString().replace("-", "").toUpperCase().substring(0, 6);
        return "EVT-" + hex;
    }
}
