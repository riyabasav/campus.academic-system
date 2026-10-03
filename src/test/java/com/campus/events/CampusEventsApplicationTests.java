package com.campus.events;

import com.campus.events.model.Event;
import com.campus.events.model.EventRegistration;
import com.campus.events.repository.EventRegistrationRepository;
import com.campus.events.repository.EventRepository;
import com.campus.events.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CampusEventsApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventRegistrationRepository registrationRepository;

    @Autowired
    private EventService eventService;

    @Test
    void contextLoads() {
        assertThat(eventRepository.count()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void testDashboardPage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("events"))
                .andExpect(model().attributeExists("events"))
                .andExpect(model().attributeExists("newEvent"))
                .andExpect(content().string(containsString("Campus Events")));
    }

    @Test
    void testCreateEvent() throws Exception {
        long initialCount = eventRepository.count();

        mockMvc.perform(post("/events")
                        .param("title", "Robotics Expo")
                        .param("clubName", "Robotics Club")
                        .param("description", "Exhibition of student robotics projects.")
                        .param("location", "Engineering Hall")
                        .param("eventDate", "2026-11-01T10:00")
                        .param("capacity", "30"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        assertThat(eventRepository.count()).isEqualTo(initialCount + 1);
    }

    @Test
    void testShowRegistrationForm() throws Exception {
        Event event = eventRepository.findAll().get(0);

        mockMvc.perform(get("/events/" + event.getId() + "/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("event"))
                .andExpect(content().string(containsString(event.getClubName())));
    }

    @Test
    @Transactional
    void testProcessRegistrationAndPassGeneration() throws Exception {
        Event event = new Event(
                "Design Systems Seminar",
                "UI/UX Club",
                "Learn about modern design systems.",
                "Design Lab 101",
                LocalDateTime.now().plusDays(5),
                2
        );
        event = eventRepository.save(event);

        mockMvc.perform(post("/events/" + event.getId() + "/register")
                        .param("studentName", "Jane Doe")
                        .param("studentEmail", "jane.doe@campus.edu"))
                .andExpect(status().isOk())
                .andExpect(view().name("pass"))
                .andExpect(model().attributeExists("registration"))
                .andExpect(model().attributeExists("event"))
                .andExpect(content().string(containsString("Digital Entry Pass")))
                .andExpect(content().string(containsString("EVT-")));

        Event updatedEvent = eventRepository.findById(event.getId()).orElseThrow();
        assertThat(updatedEvent.getRegisteredCount()).isEqualTo(1);
        assertThat(updatedEvent.getRemainingSeats()).isEqualTo(1);

        EventRegistration reg = updatedEvent.getRegistrations().get(0);
        assertThat(reg.getStudentName()).isEqualTo("Jane Doe");
        assertThat(reg.getStudentEmail()).isEqualTo("jane.doe@campus.edu");
        assertThat(reg.getTicketCode()).startsWith("EVT-");
        assertThat(reg.getTicketCode()).hasSize(10); // "EVT-" (4 chars) + 6 hex chars = 10 chars total (EVT-XXXXXX)
    }
}
