package com.info7255.controller;

import com.info7255.service.AuthorizeService;
import com.info7255.service.MessageQueueService;
import com.info7255.service.PlanService;
import com.info7255.validator.JsonValidator;
import org.apache.commons.codec.digest.DigestUtils;
import org.everit.json.schema.ValidationException;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.validation.Valid;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jagman on Feb, 2020
 **/
@RestController
@RequestMapping(path = "/medicalplan")
public class MedicalPlanController {

    @Autowired
    JsonValidator validator;

    @Autowired
    PlanService planservice ;

    @Autowired
    private AuthorizeService authorizeService;

    Map<String, Object> m = new HashMap<String, Object>();

    @Autowired
    private MessageQueueService messageQueueService;


    @GetMapping(value = "/getToken")
    public ResponseEntity<String> getToken()
            throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

        String token = authorizeService.getToken();
        return new ResponseEntity<String>(token, HttpStatus.CREATED);
    }

    @PostMapping(path = "/plan/", produces = "application/json")
    public ResponseEntity<Object> createPlan(@Valid @RequestBody(required = false) String medicalPlan, @RequestHeader HttpHeaders headers) throws JSONException, Exception {
        m.clear();
        if (medicalPlan == null || medicalPlan.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new JSONObject().put("Error", "Body is Empty.Kindly provide the JSON").toString());
        }

        String returnValue = authorizeService.authorizeToken(headers);
        if ((returnValue != "Valid Token"))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JSONObject().put("Authetication Error: ", returnValue).toString());

        JSONObject json = new JSONObject(medicalPlan);
        try{
            validator.validateJson(json);
        }catch(ValidationException ex){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new JSONObject().put("Error",ex.getErrorMessage()).toString());

        }

        String key = json.get("objectType").toString() + "_" + json.get("objectId").toString();
        if(planservice.checkIfKeyExists(key)){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new JSONObject().put("Message", "Plan already exist").toString());
        }

        String newEtag = planservice.savePlanToRedisAndMQ(json, key);

        return ResponseEntity.ok().eTag(newEtag).body(" {\"message\": \"Created data with key: " + json.get("objectId") + "\" }");

    }


    @GetMapping(path = "/{type}/{objectId}", produces = "application/json")
    public ResponseEntity<Object> getPlan(@RequestHeader HttpHeaders headers, @PathVariable String objectId,@PathVariable String type) throws JSONException, Exception {

        String returnValue = authorizeService.authorizeToken(headers);
        if ((returnValue != "Valid Token"))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JSONObject().put("Authetication Error: ", returnValue).toString());

        if (!planservice.checkIfKeyExists(type + "_" + objectId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new JSONObject().put("Message", "ObjectId does not exist").toString());
        }

        String actualEtag = null;
        if (type.equals("plan")) {
            actualEtag = planservice.getEtag(type + "_" + objectId, "eTag");
            String eTag = headers.getFirst("If-None-Match");
            if (eTag != null && eTag.equals(actualEtag)) {
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(actualEtag).build();
            }
        }

        String key = type + "_" + objectId;
        Map<String, Object> plan = planservice.getPlan(key);

        if (type.equals("plan")) {
            return ResponseEntity.ok().eTag(actualEtag).body(new JSONObject(plan).toString());
        }

        return ResponseEntity.ok().body(new JSONObject(plan).toString());
    }

    @PutMapping(path = "/plan/{objectId}", produces = "application/json")
    public ResponseEntity<Object> updatePlan(@RequestHeader HttpHeaders headers, @Valid @RequestBody String medicalPlan,
                                             @PathVariable String objectId) throws IOException {

        String returnValue = authorizeService.authorizeToken(headers);
        if ((returnValue != "Valid Token"))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JSONObject().put("Authetication Error: ", returnValue).toString());

        JSONObject planObject = new JSONObject(medicalPlan);
        try {
            validator.validateJson(planObject);
        } catch (ValidationException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new JSONObject().put("Validation Error", ex.getMessage()).toString());
        }

        String key = "plan_" + objectId;
        if (!planservice.checkIfKeyExists(key)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new JSONObject().put("Message", "ObjectId does not exist").toString());
        }

        // Get eTag value
        String actualEtag = planservice.getEtag(key, "eTag");
        String eTag = headers.getFirst("If-Match");
        if (eTag != null && !eTag.equals(actualEtag)) {
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).eTag(actualEtag).build();
        }

        String newEtag = planservice.savePlanToRedisAndMQ(planObject, key);

        return ResponseEntity.ok().eTag(newEtag)
                .body(new JSONObject().put("Message: ", "Resource updated successfully").toString());
    }

    @PatchMapping(path = "/plan/{objectId}", produces = "application/json")
    public ResponseEntity<Object> patchPlan(@RequestHeader HttpHeaders headers, @Valid @RequestBody String medicalPlan,
                                            @PathVariable String objectId) throws IOException {

        String returnValue = authorizeService.authorizeToken(headers);
        if ((returnValue != "Valid Token"))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JSONObject().put("Authetication Error: ", returnValue).toString());

        JSONObject planObject = new JSONObject(medicalPlan);

        if (!planservice.checkIfKeyExists("plan_" + objectId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new JSONObject().put("Message", "ObjectId does not exist").toString());
        }

        String key = "plan_" + objectId;
        String newEtag = planservice.savePlanToRedisAndMQ(planObject, key);

        return ResponseEntity.ok().eTag(newEtag)
                .body(new JSONObject().put("Message: ", "Resource updated successfully").toString());
    }

    @DeleteMapping(path = "/plan/{objectId}", produces = "application/json")
    public ResponseEntity<Object> getPlan(@RequestHeader HttpHeaders headers, @PathVariable String objectId){

        String returnValue = authorizeService.authorizeToken(headers);
        if ((returnValue != "Valid Token"))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JSONObject().put("Authetication Error: ", returnValue).toString());

        if (!planservice.checkIfKeyExists("plan"+ "_" + objectId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new JSONObject().put("Message", "ObjectId does not exist").toString());
        }

        planservice.deletePlan("plan" + "_" + objectId);

        //save plan to MQ
        messageQueueService.addToMessageQueue(objectId, true);

        return ResponseEntity
                .noContent()
                .build();


    }


    }
