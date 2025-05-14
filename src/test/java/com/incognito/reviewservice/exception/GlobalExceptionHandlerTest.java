package com.incognito.reviewservice.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private WebRequest webRequest;

    @Mock
    private HttpServletRequest httpServletRequest;

    @BeforeEach
    void setUp() {
        // Mock WebRequest to return a URI
        when(webRequest.getDescription(false)).thenReturn("uri=/test/path");
        // Mock HttpServletRequest for ServletWebRequest if needed by specific handlers, though WebRequest is often enough
    }

    @Test
    void testHandleMethodArgumentNotValidException() {
        // Arrange
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "fieldName", "defaultMessage");
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));

        // Create a MethodParameter that can respond to getExecutable() and others if needed by the exception's getMessage()
        MethodParameter parameter = mock(MethodParameter.class);
        // It's often the constructor or a specific method that is the executable.
        // For the purpose of getMessage() in MethodArgumentNotValidException, 
        // a basic mock that doesn't return null for getExecutable() might be enough, 
        // or one that returns a mock Executable.
        java.lang.reflect.Method mockMethod = mock(java.lang.reflect.Method.class);
        when(parameter.getExecutable()).thenReturn(mockMethod);
        when(mockMethod.toGenericString()).thenReturn("mockMethodSignature"); // Provide a string for the method signature

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);
        HttpHeaders headers = new HttpHeaders();
        HttpStatusCode status = HttpStatus.BAD_REQUEST;

        // Act
        ResponseEntity<Object> responseEntity = globalExceptionHandler.handleMethodArgumentNotValid(ex, headers, status, webRequest);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) responseEntity.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.get("status"));
        assertEquals("Validation Error", body.get("error"));
        assertTrue(body.containsKey("timestamp"));
        assertEquals("/test/path", body.get("path"));
        @SuppressWarnings("unchecked")
        List<String> messages = (List<String>) body.get("messages");
        assertNotNull(messages);
        assertEquals(1, messages.size());
        assertEquals("fieldName: defaultMessage", messages.get(0));
    }

    @Test
    void testHandleResourceNotFoundException() {
        // Arrange
        ResourceNotFoundException ex = new ResourceNotFoundException("Resource was not found");

        // Act
        ResponseEntity<Object> responseEntity = globalExceptionHandler.handleResourceNotFoundException(ex, webRequest);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) responseEntity.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.NOT_FOUND.value(), body.get("status"));
        assertEquals("Not Found", body.get("error"));
        assertEquals("Resource was not found", body.get("message"));
        assertTrue(body.containsKey("timestamp"));
        assertEquals("/test/path", body.get("path"));
    }

    @Test
    void testHandleHttpMessageNotReadableException() {
        // Arrange
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Could not read JSON", mock(org.springframework.http.HttpInputMessage.class));
        HttpHeaders headers = new HttpHeaders();
        HttpStatusCode status = HttpStatus.BAD_REQUEST;


        // Act
        ResponseEntity<Object> responseEntity = globalExceptionHandler.handleHttpMessageNotReadable(ex, headers, status, webRequest);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) responseEntity.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.get("status"));
        assertEquals("Malformed JSON", body.get("error"));
        assertEquals("The request body is not valid JSON or cannot be processed.", body.get("message"));
        assertTrue(body.containsKey("timestamp"));
        assertEquals("/test/path", body.get("path"));
    }

    @Test
    void testHandleCustomBadRequestException() { // Renamed test method for clarity
        // Arrange
        // Use the custom BadRequestException from your application
        com.incognito.reviewservice.exception.BadRequestException ex = 
            new com.incognito.reviewservice.exception.BadRequestException("This was a custom bad request");

        // Act
        // Call the correct handler method for your custom BadRequestException
        ResponseEntity<Object> responseEntity = globalExceptionHandler.handleBadRequestException(ex, webRequest);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) responseEntity.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.get("status"));
        assertEquals("Bad Request", body.get("error"));
        assertEquals("This was a custom bad request", body.get("message"));
        assertTrue(body.containsKey("timestamp"));
        assertEquals("/test/path", body.get("path"));
    }
    
    @Test
    void testHandleGenericException() {
        // Arrange
        Exception ex = new Exception("Some generic error");

        // Act
        ResponseEntity<Object> responseEntity = globalExceptionHandler.handleAllOtherExceptions(ex, webRequest);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) responseEntity.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), body.get("status"));
        assertEquals("Internal Server Error", body.get("error"));
        assertEquals("An unexpected error occurred. Please try again later.", body.get("message"));
        assertTrue(body.containsKey("timestamp"));
        assertEquals("/test/path", body.get("path"));
    }
}
