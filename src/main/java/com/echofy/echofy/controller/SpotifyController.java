package com.echofy.echofy.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class SpotifyController {

    @Value("${spotify.clientId}")
    private String clientId;

    @Value("${spotify.clientSecret}")
    private String clientSecret;

    @Value("${spotify.redirectUri}")
    private String redirectUri;

    private final String authorizeUrl = "https://accounts.spotify.com/authorize";
    private final String tokenUrl = "https://accounts.spotify.com/api/token";
    @GetMapping("/")
    public String homePage() {
        return "index";
        }
        
    @GetMapping("/login")
    public void login(HttpServletResponse response) throws IOException {
        String url = UriComponentsBuilder.fromHttpUrl(authorizeUrl)
                .queryParam("client_id", clientId)
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", "user-read-private user-read-email")
                .queryParam("scope", "user-read-private user-read-email playlist-read-private playlist-read-collaborative")
                .build().toUriString();

        response.sendRedirect(url);
    }

   @GetMapping("/callback")
    public String callback(@RequestParam("code") String code, HttpSession session) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
    headers.setBasicAuth(clientId, clientSecret);
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "authorization_code");
    params.add("code", code);
    params.add("redirect_uri", redirectUri);

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

    ResponseEntity<Map> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, Map.class);
    Map<String, Object> responseBody = response.getBody();

    if (responseBody != null && responseBody.get("access_token") != null) {
        String accessToken = (String) responseBody.get("access_token");
        session.setAttribute("access_token", accessToken);
        return "redirect:/dashboard";
    }

    return "redirect:/?error";
    }

   @GetMapping("/dashboard")
public String dashboard(HttpSession session, Model model) {
    String token = (String) session.getAttribute("access_token");

    if (token == null) {
        return "redirect:/login";
    }

    RestTemplate restTemplate = new RestTemplate();

    // Header dengan Bearer token
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    HttpEntity<String> entity = new HttpEntity<>(headers);

    // Ambil profil user
    ResponseEntity<Map> userResponse = restTemplate.exchange(
        "https://api.spotify.com/v1/me",
        HttpMethod.GET,
        entity,
        Map.class
    );
    model.addAttribute("user", userResponse.getBody());

    // Ambil playlist user
    ResponseEntity<Map> playlistResponse = restTemplate.exchange(
        "https://api.spotify.com/v1/me/playlists",
        HttpMethod.GET,
        entity,
        Map.class
    );

    // Ambil array dari "items" (playlist list)
    Map<String, Object> playlistBody = playlistResponse.getBody();
    if (playlistBody != null) {
        model.addAttribute("playlists", playlistBody.get("items"));
    }

    return "dashboard";
}




}
 