package com.syed.QuizYa.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;

@Service
public class UrlDiscoveryService {

    /**
     * Automatically determines the base URL for the application.
     * It first tries to query the local Ngrok API to see if an ngrok tunnel is active.
     * If ngrok is running, it returns the public HTTPS url (e.g. https://xyz.ngrok-free.app).
     * If ngrok is NOT running, it falls back to the local Wi-Fi IP address (e.g. http://192.168.1.5:8080).
     */
    public String getBaseUrl(int port) {
        try {
            // 1. Try to ask Ngrok for the URL
            // We create a RestTemplate, which acts like a mini web browser inside our Java code.
            RestTemplate restTemplate = new RestTemplate();
            
            // This is the default local API address where Ngrok reports its status
            String ngrokApiUrl = "http://127.0.0.1:4040/api/tunnels";
            
            // Fetch the JSON response from Ngrok as a raw String
            String jsonResponse = restTemplate.getForObject(ngrokApiUrl, String.class);
            
            if (jsonResponse != null) {
                // Manually parse the JSON string into a tree structure
                ObjectMapper mapper = new ObjectMapper();
                JsonNode response = mapper.readTree(jsonResponse);
                
                // Parse the JSON to find the HTTPS tunnel
                // We check if the answer exists, and if it contains a list called "tunnels"
                if (response.has("tunnels")) {
                    for (JsonNode tunnel : response.get("tunnels")) {
                        // Grab the text next to "public_url"
                        String publicUrl = tunnel.get("public_url").asText();
                        
                        // Ngrok usually gives an HTTP and an HTTPS link. We want the secure HTTPS one.
                        if (publicUrl.startsWith("https")) {
                            // Found it! e.g., https://a1b2.ngrok-free.app
                            return publicUrl; 
                        }
                    }
                }
            }
        } catch (ResourceAccessException e) {
            // Ngrok is not running on port 4040. Proceed to fallback.
            // This happens normally when you aren't using ngrok during development.
            System.out.println("Ngrok not detected. Falling back to local IP.");
        } catch (Exception e) {
            System.out.println("Error parsing Ngrok URL: " + e.getMessage());
        }

        // 2. Fallback to Local IP if Ngrok is closed
        // If we get here, it means Ngrok is either closed or errored out.
        // So, we find the computer's local Wi-Fi IP address instead.
        try {
            String localIp = InetAddress.getLocalHost().getHostAddress();
            return "http://" + localIp + ":" + port; 
        } catch (Exception e) {
            // Plan C: If even the Wi-Fi is broken, just use localhost.
            return "http://localhost:" + port;
        }
    }
}
