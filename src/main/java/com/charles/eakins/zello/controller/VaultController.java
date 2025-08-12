package com.charles.eakins.zello.controller;

import com.charles.eakins.zello.config.ZelloApiConfig;
import com.charles.eakins.zello.model.HistoryResponse;
import com.charles.eakins.zello.model.MediaResponse;
import com.charles.eakins.zello.model.ProcessedMessage;
import com.charles.eakins.zello.service.ZelloApiService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class VaultController {

    private static final Logger log = LoggerFactory.getLogger(VaultController.class);
    private final ZelloApiService zelloService;
    private final ZelloApiConfig zelloApiConfig;

    @Value("${app.target-channel}")
    private String targetChannel;
    @Value("${app.messages-per-page}")
    private int messagesPerPage;
    @Value("${app.display-timezone}")
    private String displayTimezone;
    @Value("${app.session-timeout-seconds}")
    private int sessionTimeoutSeconds;
    @Value("${app.session-warning-seconds}")
    private int sessionWarningSeconds;
    @Value("${app.auto-refresh-seconds}")
    private int autoRefreshSeconds;

    public VaultController(ZelloApiService zelloService, ZelloApiConfig zelloApiConfig) {
        this.zelloService = zelloService;
        this.zelloApiConfig = zelloApiConfig;
    }

    private boolean credentialsAreHardcoded() {
        return zelloApiConfig.getUsername() != null && !zelloApiConfig.getUsername().isEmpty() &&
                zelloApiConfig.getPassword() != null && !zelloApiConfig.getPassword().isEmpty();
    }

    private boolean isUserAuthenticated(HttpSession session) {
        if (credentialsAreHardcoded()) { return true; }
        return session.getAttribute("user_username") != null;
    }

    private String getSid(HttpSession session) {
        String sid = (String) session.getAttribute("zelloSid");
        if (sid == null) {
            log.info("No SID in session. Re-authenticating...");
            if (credentialsAreHardcoded()) { sid = zelloService.authenticate(); }
            else {
                String username = (String) session.getAttribute("user_username");
                String password = (String) session.getAttribute("user_password");
                if (username != null && password != null) { sid = zelloService.authenticate(username, password); }
            }
            if (sid != null) { session.setAttribute("zelloSid", sid); }
        }
        return sid;
    }

    @GetMapping("/login")
    public String loginPage() { return "login"; }

    @PostMapping("/login")
    public String handleLogin(@RequestParam String username, @RequestParam String password, HttpSession session, RedirectAttributes redirectAttributes) {
        String sid = zelloService.authenticate(username, password);
        if (sid != null) {
            session.setAttribute("user_username", username);
            session.setAttribute("user_password", password);
            session.setAttribute("zelloSid", sid);
            session.setMaxInactiveInterval(sessionTimeoutSeconds);
            return "redirect:/";
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid username or password.");
            return "redirect:/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) { session.invalidate(); return "redirect:/login"; }

    private void addAppConfigToModel(Model model) {
        model.addAttribute("session_timeout_seconds", sessionTimeoutSeconds);
        model.addAttribute("session_warning_seconds", sessionWarningSeconds);
        model.addAttribute("auto_refresh_seconds", autoRefreshSeconds);
    }

    @GetMapping({"/", "/page/{page_num}", "/calendar/{year}/{month}"})
    public String messageVault(@PathVariable(required = false) Integer page_num, @PathVariable(required = false) Integer year, @PathVariable(required = false) Integer month, Model model, HttpSession session) {
        if (!isUserAuthenticated(session)) { return "redirect:/login"; }
        String sid = getSid(session);
        if (sid == null) { model.addAttribute("error", "Could not authenticate with Zello."); return "error"; }

        int currentPage = (page_num == null) ? 1 : page_num;
        int startIndex = (currentPage - 1) * messagesPerPage;
        HistoryResponse history = zelloService.getMessages(sid, targetChannel, startIndex, messagesPerPage, null, null);
        Map<String, String> userMap = zelloService.getUserDisplayNameMap(sid);

        processMessagesAndAddToModel(model, history, userMap);
        addAppConfigToModel(model);
        model.addAttribute("page_num", currentPage);
        model.addAttribute("total_pages", (history != null && history.getTotal() > 0) ? (int) Math.ceil((double) history.getTotal() / messagesPerPage) : 0);
        model.addAttribute("is_day_view", false);
        model.addAttribute("isTodayView", false);

        LocalDate calDate = (year != null && month != null) ? LocalDate.of(year, month, 1) : LocalDate.now();
        addCalendarDataToModel(model, calDate.getYear(), calDate.getMonthValue());
        return "index";
    }

    @GetMapping("/date/{year}/{month}/{day}")
    public String dayView(@PathVariable int year, @PathVariable int month, @PathVariable int day, Model model, HttpSession session) {
        if (!isUserAuthenticated(session)) { return "redirect:/login"; }
        String sid = getSid(session);
        if (sid == null) { model.addAttribute("error", "Could not authenticate with Zello."); return "error"; }

        ZoneId displayZoneId = ZoneId.of(displayTimezone);
        LocalDate requestedDate = LocalDate.of(year, month, day);
        long startTs = requestedDate.atStartOfDay(displayZoneId).toEpochSecond();
        long endTs = requestedDate.plusDays(1).atStartOfDay(displayZoneId).toEpochSecond();

        HistoryResponse history = zelloService.getMessages(sid, targetChannel, 0, 1000, startTs, endTs);
        Map<String, String> userMap = zelloService.getUserDisplayNameMap(sid);

        processMessagesAndAddToModel(model, history, userMap);
        addAppConfigToModel(model);
        model.addAttribute("is_day_view", true);
        model.addAttribute("viewing_date", requestedDate);

        boolean isTodayView = requestedDate.equals(LocalDate.now(displayZoneId));
        model.addAttribute("isTodayView", isTodayView);

        addCalendarDataToModel(model, year, month);
        return "index";
    }

    @GetMapping("/play_audio/{mediaKey}")
    public ResponseEntity<Resource> playAudio(@PathVariable String mediaKey, HttpSession session) throws IOException {
        String sid = getSid(session);
        if (sid == null) { return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); }
        try {
            long startTime = System.currentTimeMillis(); long timeout = 40000; MediaResponse mediaInfo;
            while (System.currentTimeMillis() - startTime < timeout) {
                mediaInfo = zelloService.getMediaInfo(sid, mediaKey);
                if (mediaInfo == null) { log.warn("Null media info for key: {}. Retrying...", mediaKey); Thread.sleep(2000); continue; }
                String status = mediaInfo.getStatus();
                if ("OK".equals(status)) {
                    InputStreamResource audioResource = zelloService.getMediaResource(mediaInfo.getUrl(), sid);
                    return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(audioResource);
                } else if ("Waiting".equals(status) || "Working".equals(status)) { log.info("Media not ready. Status: {}, Progress: {}%. Waiting...", status, mediaInfo.getProgress()); Thread.sleep(2000);
                } else { log.error("Unhandled Zello status for media: {}", status); return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); }
            }
            log.warn("Timeout waiting for media key: {}", mediaKey); return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).build();
        } catch (Exception e) { log.error("Generic error fetching media for key {}:", mediaKey, e); return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); }
    }

    @GetMapping("/api/ping")
    public ResponseEntity<Void> pingSession() { return ResponseEntity.ok().build(); }

    @GetMapping("/api/messages/latest")
    @ResponseBody
    public ResponseEntity<List<ProcessedMessage>> getLatestMessages(
            @RequestParam("since") long lastTimestamp,
            @RequestParam(value = "day", required = false) String day, // ADDED: Optional day parameter
            HttpSession session) {

        if (!isUserAuthenticated(session)) { return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); }
        String sid = getSid(session);
        if (sid == null) { return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); }

        long startTs = lastTimestamp + 1;
        Long endTs = null; // Default to no end time

        // FIX: If the 'day' parameter is provided, enforce the date boundary
        if (day != null && !day.isEmpty()) {
            try {
                LocalDate requestedDate = LocalDate.parse(day);
                ZoneId displayZoneId = ZoneId.of(displayTimezone);
                endTs = requestedDate.plusDays(1).atStartOfDay(displayZoneId).toEpochSecond();
                log.info("API Refresh: Filtering for day {}, with end timestamp {}", day, endTs);
            } catch (DateTimeException e) {
                log.warn("API Refresh: Received invalid day parameter: {}", day);
                // Proceed without the end timestamp if the date is invalid
            }
        }

        HistoryResponse history = zelloService.getMessages(sid, targetChannel, 0, 100, startTs, endTs);
        if (history == null || history.getMessages() == null || history.getMessages().isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        Map<String, String> userMap = zelloService.getUserDisplayNameMap(sid);
        ZoneId displayZoneId = ZoneId.of(displayTimezone);
        List<ProcessedMessage> processed = history.getMessages().stream()
                .map(msg -> new ProcessedMessage(userMap.getOrDefault(msg.getSender(), msg.getSender()), Instant.ofEpochSecond(msg.getTimestamp()).atZone(displayZoneId).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), msg.getMediaKey(), msg.getTranscription(), msg.getTimestamp()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(processed);
    }

    private void processMessagesAndAddToModel(Model model, HistoryResponse history, Map<String, String> userMap) {
        ZoneId displayZoneId = ZoneId.of(displayTimezone);
        List<ProcessedMessage> processedMessages = new ArrayList<>();
        if (history != null && history.getMessages() != null) {
            processedMessages = history.getMessages().stream()
                    .map(msg -> new ProcessedMessage(userMap.getOrDefault(msg.getSender(), msg.getSender()), Instant.ofEpochSecond(msg.getTimestamp()).atZone(displayZoneId).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), msg.getMediaKey(), msg.getTranscription(), msg.getTimestamp()))
                    .collect(Collectors.toList());
        }
        model.addAttribute("messages", processedMessages); model.addAttribute("channel", targetChannel);
    }

    private void addCalendarDataToModel(Model model, int year, int month) {
        ZoneId displayZoneId = ZoneId.of(displayTimezone);
        model.addAttribute("today", LocalDate.now(displayZoneId));
        model.addAttribute("cal_date", LocalDate.of(year, month, 1));
        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
        model.addAttribute("prev_month_date", firstDayOfMonth.minusMonths(1)); model.addAttribute("next_month_date", firstDayOfMonth.plusMonths(1));
        List<List<Integer>> weeks = new ArrayList<>(); LocalDate current = firstDayOfMonth;
        int dayOfWeekOfFirst = current.getDayOfWeek().getValue() % 7; List<Integer> currentWeek = new ArrayList<>();
        for (int i = 0; i < dayOfWeekOfFirst; i++) { currentWeek.add(0); }
        while (current.getMonthValue() == month) {
            currentWeek.add(current.getDayOfMonth());
            if (currentWeek.size() == 7) { weeks.add(currentWeek); currentWeek = new ArrayList<>(); }
            current = current.plusDays(1);
        }
        if (!currentWeek.isEmpty()) { while(currentWeek.size() < 7) { currentWeek.add(0); } weeks.add(currentWeek); }
        model.addAttribute("calendar_weeks", weeks);
    }
}