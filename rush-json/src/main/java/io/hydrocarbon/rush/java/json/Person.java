package io.hydrocarbon.rush.java.json;

import lombok.Data;

/**
 * @author Zou Zhenfeng
 * @since 2024-05-15
 */
@Data
public class Person {

    private String name;

    private Integer age;

    private double height;

    private boolean married;

    private Person spouse;
}
