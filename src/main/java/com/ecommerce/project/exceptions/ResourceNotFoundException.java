package com.ecommerce.project.exceptions;


public class ResourceNotFoundException extends RuntimeException {


    String resourceName;

    String field;

    String filedName;

    Long fieldId;

    public ResourceNotFoundException(String resourceName, String field, String filedName) {
        super(String.format("%s not found with %s: %S", resourceName, field, filedName));
        this.resourceName = resourceName;
        this.field = field;
        this.filedName = filedName;
    }

    public ResourceNotFoundException() {
    }

    public ResourceNotFoundException(String resourceName, String field, Long fieldId) {
        super(String.format("%s not found with %s: %S", resourceName, field, fieldId));

        this.resourceName = resourceName;
        this.fieldId = fieldId;
        this.field = field;
    }


}
