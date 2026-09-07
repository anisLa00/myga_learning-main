package com.myga.learning.backend.backend;

import com.myga.learning.backend.backend.dto.AnnouncementRequest;
import com.myga.learning.backend.backend.models.AnnouncementTarget;
import com.myga.learning.backend.backend.models.Notification;
import com.myga.learning.backend.backend.models.NotificationType;
import com.myga.learning.backend.backend.models.Role;
import com.myga.learning.backend.backend.models.User;
import com.myga.learning.backend.backend.repositories.NotificationRepository;
import com.myga.learning.backend.backend.repositories.UserRepository;
import com.myga.learning.backend.backend.service.AnnouncementService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AnnouncementNotificationIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private AnnouncementService announcementService;
    @Autowired private PasswordEncoder passwordEncoder;

    private static final String ADMIN_EMAIL = "admin@myga.local";
    private static final String PARENT_A = "ann.parentA@test.local";
    private static final String PARENT_B = "ann.parentB@test.local";

    private Long parentAUserId;
    private Long notificationOfBId;

    @BeforeEach
    void seed() {
        User a = saveUser(PARENT_A, Role.PARENT);
        User b = saveUser(PARENT_B, Role.PARENT);
        parentAUserId = a.getId();

        Notification n = new Notification();
        n.setRecipient(b);
        n.setType(NotificationType.GENERAL);
        n.setTitle("Seeded");
        n.setMessage("For parent B");
        n.setCreatedAt(LocalDateTime.now());
        notificationOfBId = notificationRepository.save(n).getId();
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void announcementToParentsNotifiesParentAccounts() {
        authenticateAs(ADMIN_EMAIL, "ADMIN");

        AnnouncementRequest request = new AnnouncementRequest();
        request.setTitle("Parent meeting");
        request.setMessage("Meeting on Friday.");
        request.setTarget(AnnouncementTarget.PARENTS);
        announcementService.create(request);

        List<Notification> parentA =
                notificationRepository.findByRecipient_IdOrderByCreatedAtDesc(parentAUserId);
        assertTrue(parentA.stream().anyMatch(n -> n.getType() == NotificationType.NEW_ANNOUNCEMENT),
                "Parent A should have received a NEW_ANNOUNCEMENT notification");
    }

    @Test
    @WithMockUser(username = PARENT_A, roles = "PARENT")
    void parentCannotMarkAnotherUsersNotificationRead() throws Exception {
        mockMvc.perform(put("/api/notifications/" + notificationOfBId + "/read"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = PARENT_B, roles = "PARENT")
    void parentCanReadAndMarkOwnNotification() throws Exception {
        mockMvc.perform(get("/api/notifications/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(put("/api/notifications/" + notificationOfBId + "/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }

    private void authenticateAs(String email, String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))));
    }

    private User saveUser(String email, Role role) {
        User u = new User();
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode("password123"));
        u.setRole(role);
        u.setEnabled(true);
        return userRepository.save(u);
    }
}
