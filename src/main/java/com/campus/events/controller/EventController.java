package com.campus.events.controller;

import com.campus.events.config.DynamicOAuth2ClientRegistrationRepository;
import com.campus.events.model.Club;
import com.campus.events.model.Event;
import com.campus.events.model.EventRegistration;
import com.campus.events.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
    private final DynamicOAuth2ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    public EventController(EventService eventService, DynamicOAuth2ClientRegistrationRepository clientRegistrationRepository) {
        this.eventService = eventService;
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @GetMapping({"/", "/events"})
    public String dashboard(Model model) {
        model.addAttribute("events", eventService.getAllEventsOrderedByDate());
        model.addAttribute("clubs", eventService.getAllClubs());
        model.addAttribute("newEvent", new Event());
        model.addAttribute("newClub", new Club());
        return "events";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/oauth2/authorization/google")
    public String handleGoogleLoginRedirect() {
        if (!clientRegistrationRepository.isGoogleConfigured()) {
            return "redirect:/oauth2-setup";
        }
        return "redirect:/oauth2/authorization/google";
    }

    @PostMapping("/events")
    public String createEvent(@ModelAttribute("newEvent") Event event, RedirectAttributes redirectAttributes) {
        eventService.saveEvent(event);
        redirectAttributes.addFlashAttribute("successMessage", "Event created successfully!");
        return "redirect:/";
    }

    @PostMapping("/clubs")
    public String createClub(@ModelAttribute("newClub") Club club, RedirectAttributes redirectAttributes) {
        eventService.saveClub(club);
        redirectAttributes.addFlashAttribute("successMessage", "Club created successfully!");
        return "redirect:/";
    }

    @GetMapping("/events/{id}/register")
    public String showRegistrationForm(@PathVariable("id") Long id, Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please sign in with Google to register for campus events.");
            return "redirect:/login";
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));

        if (isAdmin) {
            redirectAttributes.addFlashAttribute("errorMessage", "Administrators are not permitted to register for events.");
            return "redirect:/";
        }

        Event event = eventService.getEventById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event Id:" + id));
        model.addAttribute("event", event);
        return "register";
    }

    @PostMapping("/events/{id}/register")
    public String processRegistration(@PathVariable("id") Long id,
                                      @RequestParam("studentName") String studentName,
                                      @RequestParam("studentEmail") String studentEmail,
                                      Authentication authentication,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please sign in with Google to register for campus events.");
            return "redirect:/login";
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));

        if (isAdmin) {
            redirectAttributes.addFlashAttribute("errorMessage", "Administrators are not permitted to register for events.");
            return "redirect:/";
        }

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
