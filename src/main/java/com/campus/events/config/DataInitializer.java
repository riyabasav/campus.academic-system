package com.campus.events.config;

import com.campus.events.model.Club;
import com.campus.events.model.Event;
import com.campus.events.repository.ClubRepository;
import com.campus.events.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initDatabase(EventRepository eventRepository, ClubRepository clubRepository) {
        return args -> {
            if (clubRepository.count() == 0) {
                logger.info("Seeding initial campus clubs...");
                clubRepository.save(new Club("Tech Club", "Fostering technology, AI, and software engineering innovation."));
                clubRepository.save(new Club("Coding Society", "Promoting competitive programming, hackathons, and open source development."));
                clubRepository.save(new Club("Arts & Culture Club", "Celebrating music, performing arts, and visual design on campus."));
                logger.info("Sample campus clubs successfully seeded.");
            }

            if (eventRepository.count() == 0) {
                logger.info("Seeding initial campus events...");

                Event event1 = new Event(
                        "AI & Web3 Workshop",
                        "Tech Club",
                        "Explore the intersection of Artificial Intelligence and Web3 technologies with hands-on coding sessions and expert panels.",
                        "Innovation Hub, Room 301",
                        LocalDateTime.now().plusDays(3).withHour(14).withMinute(0).withSecond(0).withNano(0),
                        50
                );

                Event event2 = new Event(
                        "Annual Hackathon Recruitment",
                        "Coding Society",
                        "Join us for the annual hackathon team formation and recruitment drive. Network with fellow developers and designers!",
                        "Student Union Main Hall",
                        LocalDateTime.now().plusDays(7).withHour(17).withMinute(30).withSecond(0).withNano(0),
                        100
                );

                Event event3 = new Event(
                        "Campus Music & Art Night",
                        "Arts & Culture Club",
                        "An evening of live student musical performances, acoustic jams, and digital art exhibitions.",
                        "Campus Amphitheater",
                        LocalDateTime.now().plusDays(10).withHour(18).withMinute(0).withSecond(0).withNano(0),
                        75
                );

                eventRepository.save(event1);
                eventRepository.save(event2);
                eventRepository.save(event3);

                logger.info("Sample campus events successfully seeded.");
            }
        };
    }
}
