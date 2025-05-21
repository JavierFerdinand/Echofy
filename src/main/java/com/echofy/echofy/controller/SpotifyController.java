package com.echofy.echofy.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.echofy.echofy.service.SpotifyService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class SpotifyController {

    private final SpotifyService spotifyService;

    public SpotifyController(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }

    @GetMapping("/")
    public String homePage() {
        return "index";
    }

    @GetMapping("/login")
    public void login(HttpServletResponse response) throws IOException {
        String url = spotifyService.buildAuthorizeUrl();
        response.sendRedirect(url);
    }

    @GetMapping("/callback")
    public String callback(@RequestParam("code") String code, HttpSession session) {
        String accessToken = spotifyService.getAccessToken(code);
        if (accessToken != null) {
            session.setAttribute("access_token", accessToken);
            return "redirect:/dashboard";
        }
        return "redirect:/?error";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String accessToken = (String) session.getAttribute("access_token");
        if (accessToken == null) {
            return "redirect:/login";
        }

        try {
            Map<String, Object> user = spotifyService.getUserProfile(accessToken);
            Map<String, Object> playlists = spotifyService.getUserPlaylists(accessToken);
            Map<String, Object> topArtists = spotifyService.getTopArtists(accessToken);
            int likedSongsCount = spotifyService.getLikedSongsCount(accessToken);

            model.addAttribute("user", user);
            model.addAttribute("playlists", playlists.get("items"));
            model.addAttribute("topArtists", topArtists.get("items"));
            model.addAttribute("likedSongsCount", likedSongsCount);

            return "dashboard";
        } catch (Exception e) {
            return "redirect:/?error=api";
        }
    }
    
    @GetMapping("/search")
public String searchTracks(@RequestParam("query") String query, HttpSession session, Model model) {
    String token = (String) session.getAttribute("access_token");

    if (token == null) {
        return "redirect:/login";
    }

    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    HttpEntity<String> entity = new HttpEntity<>(headers);

    String url = UriComponentsBuilder
        .fromHttpUrl("https://api.spotify.com/v1/search")
        .queryParam("q", query)
        .queryParam("type", "track")
        .queryParam("limit", 10)
        .build().toUriString();

    try {
        Map<String, Object> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class).getBody();
        Map<String, Object> tracks = (Map<String, Object>) response.get("tracks");

        model.addAttribute("tracks", tracks.get("items"));
        model.addAttribute("query", query);

        return "search-results";
    } catch (Exception e) {
        return "redirect:/?error=search";
    }
}

}
