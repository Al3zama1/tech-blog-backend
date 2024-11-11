package com.selflearntech.tech_blog_backend.test_utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selflearntech.tech_blog_backend.exception.ErrorResponse;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ResponseBodyMatchers {
    private final ObjectMapper  objectMapper = new ObjectMapper();

    public <T>ResultMatcher containsObjectAsJson(Object expectedObject, Class<T> targetClass) {
        return mvcResult -> {
            String json = mvcResult.getResponse().getContentAsString();
            T actualObject = objectMapper.readValue(json, targetClass);
            assertThat(actualObject).isEqualTo(expectedObject);
        };
    }

    public ResultMatcher containsError(String expectedFieldName, String expectedMessage) {
        return mvcResult -> {
            String json = mvcResult.getResponse().getContentAsString();
            ErrorResponse errorResponse = objectMapper.readValue(json, ErrorResponse.class);
            List<ErrorResponse.ValidationError> fieldErrors = errorResponse.getErrors().stream()
                    .filter(fieldError -> fieldError.field().equals(expectedFieldName))
                    .filter(fieldError -> fieldError.message().equals(expectedMessage))
                    .toList();

            assertThat(fieldErrors)
                    .hasSize(1);
        };
    }

    public static ResponseBodyMatchers responseBody() {
        return new ResponseBodyMatchers();
    }
}
