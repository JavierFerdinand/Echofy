package com.echofy.echofy.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
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
            List<Map<String, Object>> topTracks = spotifyService.getTopTracks(accessToken);
            int likedSongsCount = spotifyService.getLikedSongsCount(accessToken);
            Map<String, Object> newReleases = spotifyService.getNewReleases(accessToken);

            Map<String, Object> albums = (Map<String, Object>) newReleases.get("albums");
            List<Map<String, Object>> albumItems = (List<Map<String, Object>>) albums.get("items");

            model.addAttribute("user", user);
            model.addAttribute("playlists", playlists.get("items"));
            model.addAttribute("likedSongsCount", likedSongsCount);
            model.addAttribute("topTracks", topTracks);
            model.addAttribute("token", accessToken); // Tambahkan 
            model.addAttribute("newReleases", albumItems);
           

            return "dashboard";
        } catch (Exception e) {
            e.printStackTrace(); // ❗ Tampilkan detail error di console
            model.addAttribute("errorMessage", e.getMessage());
            return "error"; // pastikan kamu punya error.html atau ganti sesuai halaman error-mu
        }
    }

    @GetMapping("/token")
    public String getToken(HttpSession session, Model model) {
        String accessToken = (String) session.getAttribute("access_token");
        System.out.println("Access Token di session: " + accessToken);
        model.addAttribute("token", accessToken);
        return "token";
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

    @GetMapping("/top-tracks")
    public String topTracks(HttpSession session, Model model) {
        String accessToken = (String) session.getAttribute("access_token");
        if (accessToken == null) {
            return "redirect:/login";
        }

        List<Map<String, Object>> topTracks = spotifyService.getTopTracks(accessToken);
        model.addAttribute("topTracks", topTracks);
        return "topTracks";
    }

     @ModelAttribute("playlists")
    public List<Map<String, Object>> playlists(HttpSession session) {
        String accessToken = (String) session.getAttribute("access_token");
        if (accessToken == null) {
            return List.of(); // kosong kalau belum login
        }
        Map<String, Object> playlists = spotifyService.getUserPlaylists(accessToken);
        return (List<Map<String, Object>>) playlists.get("items");
    }

    @PostMapping("/spotify/playlists/add")
public String addPlaylist(@RequestParam String name,
                          @RequestParam(required = false) String description,
                          @RequestParam(required = false, defaultValue = "false") boolean publicPlaylist,
                          HttpSession session) {

    String accessToken = (String) session.getAttribute("access_token");
    String userId = spotifyService.getCurrentUserId(accessToken); // kamu harus buat method ini
    spotifyService.createPlaylist(accessToken, userId, name, description, publicPlaylist);

    return "redirect:/dashboard"; // kembali ke halaman utama
}

@PostMapping("/spotify/playlists/update")
public String updatePlaylistName(@RequestParam String playlistId,
                                 @RequestParam String newName,
                                 HttpSession session) {
    String accessToken = (String) session.getAttribute("access_token");
    if (accessToken != null) {
        spotifyService.updatePlaylistName(accessToken, playlistId, newName);
    }
    return "redirect:/dashboard";
}

@PostMapping("/spotify/playlists/delete")
public String deletePlaylist(@RequestParam String playlistId, HttpSession session) {
    String accessToken = (String) session.getAttribute("access_token");
    if (accessToken != null) {
        spotifyService.unfollowPlaylist(accessToken, playlistId);
    }
    return "redirect:/dashboard";
}
}
