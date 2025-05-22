package com.echofy.echofy.service;

import java.util.List;
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
                                "user-library-read",
                                "user-read-recently-played"))  
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

    

   

    

    public int getLikedSongsCount(String accessToken) {
        Map<String, Object> savedTracks = makeSpotifyGetRequest("https://api.spotify.com/v1/me/tracks?limit=1", accessToken);

        if (savedTracks != null && savedTracks.get("items") instanceof List) {
            return ((List<?>) savedTracks.get("items")).size();
        }

        return 0;
    }

    private Map<String, Object> makeSpotifyGetRequest(String url, String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(url, HttpMethod.GET, entity, Map.class).getBody();
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



}
