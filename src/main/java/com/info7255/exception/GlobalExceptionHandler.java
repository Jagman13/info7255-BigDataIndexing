package com.info7255.exception;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by jagman on Feb, 2020
 **/
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler
{
    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
    	String detail= ex.getLocalizedMessage();
        return new ResponseEntity<Object>(new JSONObject().put("Server Error", detail).toString(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @ExceptionHandler(JSONException.class)
    public final ResponseEntity<Object> handleResourceNotFoundException(JSONException ex, WebRequest request) {
        List<String> details = new ArrayList<>();
        details.add(ex.getLocalizedMessage());
        return new ResponseEntity<Object>(new JSONObject().put("JsonException: ", details).toString(), HttpStatus.BAD_REQUEST);
    }
 
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        List<String> details = new ArrayList<>();
        for(ObjectError error : ex.getBindingResult().getAllErrors()) {
            details.add(error.getDefaultMessage());
        }
        return new ResponseEntity<Object>(new JSONObject().put("Validation Error", details).toString(), HttpStatus.BAD_REQUEST);
    }
    
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
    	String detail= ex.getLocalizedMessage();
        return new ResponseEntity<Object>(new JSONObject().put("Validation Error", detail).toString(), HttpStatus.BAD_REQUEST);
    }
    
    @Override
    protected ResponseEntity<Object> handleMissingPathVariable(MissingPathVariableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
//    	List<String> details = new ArrayList<>();
//        for(ObjectError error : ex.get) {
//            details.add(error.getDefaultMessage());
//        }
//        RestApiError error = new RestApiError("Validation Failed", details);
        return new ResponseEntity<Object>("Please enter all fields", HttpStatus.BAD_REQUEST);
    }
}
