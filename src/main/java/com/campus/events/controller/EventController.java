package com.campus.events.controller;

import com.campus.events.model.Event;
import com.campus.events.model.EventRegistration;
import com.campus.events.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class EventController {

    private final EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping({"/", "/events"})
    public String dashboard(Model model) {
        model.addAttribute("events", eventService.getAllEventsOrderedByDate());
        model.addAttribute("newEvent", new Event());
        return "events";
    }

    @PostMapping("/events")
    public String createEvent(@ModelAttribute("newEvent") Event event, RedirectAttributes redirectAttributes) {
        eventService.saveEvent(event);
        redirectAttributes.addFlashAttribute("successMessage", "Event created successfully!");
        return "redirect:/";
    }

    @GetMapping("/events/{id}/register")
    public String showRegistrationForm(@PathVariable("id") Long id, Model model) {
        Event event = eventService.getEventById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event Id:" + id));
        model.addAttribute("event", event);
        return "register";
    }

    @PostMapping("/events/{id}/register")
    public String processRegistration(@PathVariable("id") Long id,
                                      @RequestParam("studentName") String studentName,
                                      @RequestParam("studentEmail") String studentEmail,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        try {
            EventRegistration registration = eventService.registerStudentForEvent(id, studentName, studentEmail);
            model.addAttribute("registration", registration);
            model.addAttribute("event", registration.getEvent());
            return "pass";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/events/" + id + "/register";
        }
    }
}
