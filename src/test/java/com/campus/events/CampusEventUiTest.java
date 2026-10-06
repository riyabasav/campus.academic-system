package com.campus.events;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CampusEventUiTest {

    @LocalServerPort
    private int port;

    private static Playwright playwright;
    private static Browser browser;

    private BrowserContext context;
    private Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        try {
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        } catch (Exception e) {
            try {
                browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setChannel("msedge").setHeadless(true));
            } catch (Exception ex1) {
                try {
                    browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setChannel("chrome").setHeadless(true));
                } catch (Exception ex2) {
                    throw new RuntimeException("Could not launch Playwright browser. " + e.getMessage(), e);
                }
            }
        }
    }

    @AfterAll
    static void closeBrowser() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeEach
    void createContextAndPage() {
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    void closeContext() {
        if (context != null) {
            context.close();
        }
    }

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    private void loginAsAdmin() {
        page.navigate(getBaseUrl() + "/login");
        page.fill("#username", "Admin");
        page.fill("#password", "despu@admin123");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign In as Admin")).click();
        assertThat(page).hasURL(getBaseUrl() + "/");
    }

    @Test
    @DisplayName("Test 1: Event Dashboard Rendering")
    void testDashboardRendering() {
        page.navigate(getBaseUrl() + "/");

        assertThat(page).hasTitle(Pattern.compile("Campus Events"));
        assertThat(page.locator(".hero-banner")).containsText("Campus Events & Club Activities");
        assertThat(page.getByText("AI & Web3 Workshop")).isVisible();
        assertThat(page.getByText("Annual Hackathon Recruitment")).isVisible();

        // Verify Register button is disabled for anonymous users
        assertThat(page.getByText("Register Now (Sign in required)").first()).isDisabled();
    }

    @Test
    @DisplayName("Test 2: Admin Login and Create New Campus Event & Club")
    void testAdminCreateNewCampusEventAndClub() {
        // Sign in as Admin
        loginAsAdmin();

        // Verify Admin cannot register button is disabled
        assertThat(page.getByText("Admin Cannot Register").first()).isDisabled();

        // Create a new Club
        page.fill("#cName", "Robotics Club");
        page.fill("#cDesc", "Building autonomous robotics");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Club")).click();

        assertThat(page.getByText("Robotics Club").first()).isVisible();

        // Generate future date formatted as yyyy-MM-ddTHH:mm
        String futureDateTime = LocalDateTime.now().plusDays(15).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

        // Fill out Create Event form
        page.locator("#title").fill("Robotics Bootcamp");
        page.locator("#clubName").fill("Robotics Club");
        page.locator("#eventDate").fill(futureDateTime);
        page.locator("#location").fill("Lab 302");
        page.locator("#capacity").fill("50");
        page.locator("#description").fill("Hands-on robotics building workshop for beginners.");

        // Click Submit / Publish Event
        page.locator("button[type='submit']:has-text('Publish Event')").click();

        // Assert new event appears in event list
        assertThat(page.getByText("Robotics Bootcamp")).isVisible();
    }

    @Test
    @DisplayName("Test 3: Anonymous User Registration Attempts Redirects To Login")
    void testAnonymousRegistrationRedirectsToLogin() {
        page.navigate(getBaseUrl() + "/events/1/register");
        assertThat(page).hasURL(Pattern.compile("/login"));
    }
}
