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
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
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

    @Test
    @DisplayName("Test 1: Event Dashboard Rendering")
    void testDashboardRendering() {
        // Navigate to dashboard
        page.navigate(getBaseUrl() + "/");

        // Verify page title and header
        assertThat(page).hasTitle(Pattern.compile("Campus Events"));
        assertThat(page.locator(".hero-banner")).containsText("Campus Events & Club Activities");

        // Assert that sample seed events (e.g. "AI & Web3 Workshop") are visible
        assertThat(page.getByText("AI & Web3 Workshop")).isVisible();
        assertThat(page.getByText("Annual Hackathon Recruitment")).isVisible();
    }

    @Test
    @DisplayName("Test 2: Create New Campus Event")
    void testCreateNewCampusEvent() {
        page.navigate(getBaseUrl() + "/");

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

        // Assert redirect to dashboard and new event appears in event list
        assertThat(page.getByText("Robotics Bootcamp")).isVisible();
        assertThat(page.getByText("Robotics Club").first()).isVisible();
    }

    @Test
    @DisplayName("Test 3: Student Event Registration & Digital Pass Generation")
    void testStudentRegistrationAndDigitalPass() {
        page.navigate(getBaseUrl() + "/");

        // Locate an event on dashboard and click its "Register" button
        page.locator("a:has-text('Register Now')").first().click();

        // Verify navigation to registration page
        assertThat(page).hasURL(Pattern.compile("/events/\\d+/register"));

        // Fill in student registration form
        page.fill("#studentName", "Riya Basavaraj");
        page.fill("#studentEmail", "riya@example.com");

        // Submit form
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Confirm Registration & Get Pass")).click();

        // Assert navigation to pass page
        assertThat(page.locator(".pass-card")).isVisible();
        assertThat(page.getByText("Campus Digital Entry Pass")).isVisible();

        // Verify student name, ticket code, and print pass button
        assertThat(page.getByText("Riya Basavaraj")).isVisible();
        assertThat(page.locator(".pass-ticket-code")).containsText("EVT-");
        assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Print Digital Pass"))).isVisible();
    }
}
