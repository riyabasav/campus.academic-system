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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
    @WithMockUser(username = "Admin", roles = {"ADMIN"})
    void testCreateEventAsAdmin() throws Exception {
        long initialCount = eventRepository.count();

        mockMvc.perform(post("/events")
                        .with(csrf())
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
    void testShowRegistrationFormAnonymousRedirect() throws Exception {
        Event event = eventRepository.findAll().get(0);

        mockMvc.perform(get("/events/" + event.getId() + "/register"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = "alex_student@campus.edu", roles = {"USER"})
    void testShowRegistrationFormAsStudentUser() throws Exception {
        Event event = eventRepository.findAll().get(0);

        mockMvc.perform(get("/events/" + event.getId() + "/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("event"))
                .andExpect(content().string(containsString(event.getClubName())));
    }

    @Test
    @Transactional
    @WithMockUser(username = "jane.doe@campus.edu", roles = {"USER"})
    void testProcessRegistrationAndPreventDuplicateRegistration() throws Exception {
        Event event = new Event(
                "Design Systems Seminar",
                "UI/UX Club",
                "Learn about modern design systems.",
                "Design Lab 101",
                LocalDateTime.now().plusDays(5),
                2
        );
        event = eventRepository.save(event);

        // First registration - Success
        mockMvc.perform(post("/events/" + event.getId() + "/register")
                        .with(csrf())
                        .param("studentName", "Jane Doe")
                        .param("studentEmail", "jane.doe@campus.edu"))
                .andExpect(status().isOk())
                .andExpect(view().name("pass"))
                .andExpect(model().attributeExists("registration"))
                .andExpect(model().attributeExists("event"))
                .andExpect(content().string(containsString("Digital Entry Pass")));

        // Duplicate registration attempt - Redirects with error
        mockMvc.perform(post("/events/" + event.getId() + "/register")
                        .with(csrf())
                        .param("studentName", "Jane Doe")
                        .param("studentEmail", "jane.doe@campus.edu"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("errorMessage", "You have already registered for this event."));
    }
}
