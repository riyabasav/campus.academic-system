package com.campus.events.controller;

import com.campus.events.config.DynamicOAuth2ClientRegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OAuth2SetupController {

    private final DynamicOAuth2ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    public OAuth2SetupController(DynamicOAuth2ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @GetMapping("/oauth2-setup")
    public String showSetupPage(Model model) {
        model.addAttribute("isConfigured", clientRegistrationRepository.isGoogleConfigured());
        return "oauth2-setup";
    }

    @PostMapping("/oauth2-setup")
    public String saveOAuth2Credentials(@RequestParam("clientId") String clientId,
                                        @RequestParam("clientSecret") String clientSecret,
                                        RedirectAttributes redirectAttributes) {
        if (clientId == null || clientId.trim().isEmpty() || clientId.contains("YOUR_GOOGLE_CLIENT_ID")) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please enter a valid Google Client ID from Google Cloud Console.");
            return "redirect:/oauth2-setup";
        }

        clientRegistrationRepository.updateGoogleCredentials(clientId, clientSecret);
        redirectAttributes.addFlashAttribute("successMessage", "Google OAuth2 credentials updated successfully! Initiating Google Sign In...");
        return "redirect:/oauth2/authorization/google";
    }
}
