package com.echofy.echofy.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class SpotifyService {

    @Value("${spotify.clientId}")
    private String clientId;

    @Value("${spotify.clientSecret}")
    private String clientSecret;

    @Value("${spotify.redirectUri}")
    private String redirectUri;

    private final String authorizeUrl = "https://accounts.spotify.com/authorize";
    private final String tokenUrl = "https://accounts.spotify.com/api/token";

    public String buildAuthorizeUrl() {
        return UriComponentsBuilder.fromHttpUrl(authorizeUrl)
                .queryParam("client_id", clientId)
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", String.join(" ",
                    "user-read-private",
                    "user-read-email",
                    "playlist-read-private",
                    "playlist-read-collaborative",
                    "user-top-read",
                    "user-library-read"))
                .build().toUriString();
    }

    public String getAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientId, clientSecret);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("code", code);
        params.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        Map<String, Object> responseBody = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, Map.class).getBody();

        if (responseBody != null) {
            return (String) responseBody.get("access_token");
        }
        return null;
    }

    public Map<String, Object> getUserProfile(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        return restTemplate.exchange("https://api.spotify.com/v1/me", HttpMethod.GET, entity, Map.class).getBody();
    }

    public Map<String, Object> getUserPlaylists(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        return restTemplate.exchange("https://api.spotify.com/v1/me/playlists?limit=50", HttpMethod.GET, entity, Map.class).getBody();
    }

    public Map<String, Object> getTopArtists(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        return restTemplate.exchange("https://api.spotify.com/v1/me/top/artists?limit=10", HttpMethod.GET, entity, Map.class).getBody();
    }

    public int getLikedSongsCount(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        Map<String, Object> savedTracks = restTemplate.exchange("https://api.spotify.com/v1/me/tracks?limit=1", HttpMethod.GET, entity, Map.class).getBody();

        if (savedTracks != null && savedTracks.get("items") instanceof java.util.List) {
            return ((java.util.List<?>) savedTracks.get("items")).size();
        }

        return 0;
    }
        

}
