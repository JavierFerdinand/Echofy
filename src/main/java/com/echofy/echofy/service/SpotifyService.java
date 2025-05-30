package com.echofy.echofy.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
                        "user-library-read",
                        "user-read-recently-played",
                        "playlist-modify-public",
                        "playlist-modify-private"))
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
        return makeSpotifyGetRequest("https://api.spotify.com/v1/me", accessToken);
    }

    public Map<String, Object> getUserPlaylists(String accessToken) {
        return makeSpotifyGetRequest("https://api.spotify.com/v1/me/playlists?limit=50", accessToken);
    }

    private Map<String, Object> makeSpotifyGetRequest(String url, String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class
        ).getBody();
    }

    public Map<String, Object> getTopArtists(String accessToken) {
        String url = "https://api.spotify.com/v1/me/top/artists?limit=10";
        return makeSpotifyGetRequest(url, accessToken);
    }

    public int getLikedSongsCount(String accessToken) {
        Map<String, Object> savedTracks = makeSpotifyGetRequest("https://api.spotify.com/v1/me/tracks?limit=1", accessToken);

        if (savedTracks != null && savedTracks.get("items") instanceof List) {
            return ((List<?>) savedTracks.get("items")).size();
        }

        return 0;
    }

    public List<Map<String, Object>> getTopTracks(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);  // Kirim token sebagai Bearer
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = "https://api.spotify.com/v1/me/top/tracks?limit=10";

        Map<String, Object> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class).getBody();
        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
        return items;
    }

    public Map<String, Object> getNewReleases(String accessToken) {
        return makeSpotifyGetRequest(
                "https://api.spotify.com/v1/browse/new-releases?limit=10&country=ID",
                accessToken
        );

    }

    public String getCurrentUserId(String accessToken) {
        String url = "https://api.spotify.com/v1/me";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return (String) response.getBody().get("id");
        } else {
            throw new RuntimeException("Gagal mengambil user ID dari Spotify");
        }
    }

    public void createPlaylist(String accessToken, String userId, String name, String description, boolean publicPlaylist) {
        String url = "https://api.spotify.com/v1/users/" + userId + "/playlists";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("description", description);
        body.put("public", publicPlaylist);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Gagal membuat playlist: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Terjadi kesalahan saat membuat playlist", e);
        }
    }

    public void updatePlaylistName(String accessToken, String playlistId, String newName) {
    String url = "https://api.spotify.com/v1/playlists/" + playlistId;

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    headers.setContentType(MediaType.APPLICATION_JSON);

    Map<String, String> body = new HashMap<>();
    body.put("name", newName);

    HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

    RestTemplate restTemplate = new RestTemplate();
    restTemplate.exchange(url, HttpMethod.PUT, request, Void.class);
}

public void unfollowPlaylist(String accessToken, String playlistId) {
    String url = "https://api.spotify.com/v1/playlists/" + playlistId + "/followers";

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<Void> request = new HttpEntity<>(headers);

    RestTemplate restTemplate = new RestTemplate();
    restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
}
}
