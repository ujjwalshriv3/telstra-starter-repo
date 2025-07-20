package au.com.telstra.simcardactivator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class SimActivationController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private SimActivationRecordRepository repository;

    @GetMapping("/")
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("SIM Activation Microservice is running!");
    }

    @PostMapping("/activate")
    public ResponseEntity<String> activateSim(@RequestBody SimActivationRequest request) {

        // 1. Prepare request to actuator
        Map<String, String> actuatorRequest = new HashMap<>();
        actuatorRequest.put("iccid", request.getIccid());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(actuatorRequest, headers);

        try {
            // 2. Call actuator service
            ResponseEntity<ActuatorResponse> response = restTemplate.postForEntity(
                    "http://localhost:8444/actuate", entity, ActuatorResponse.class
            );

            boolean success = response.getBody().isSuccess();
            System.out.println("Activation success: " + success);

            // 3. Save record to database
            SimActivationRecord record = new SimActivationRecord();
            record.setIccid(request.getIccid());
            record.setCustomerEmail(request.getCustomerEmail());
            record.setSuccess(success);
            record.setTimestamp(LocalDateTime.now());

            repository.save(record);  // 📝 Save to DB

            return ResponseEntity.ok("Activation success: " + success);

        } catch (Exception e) {
            System.out.println("Error during activation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Activation failed");
        }
    }
}
