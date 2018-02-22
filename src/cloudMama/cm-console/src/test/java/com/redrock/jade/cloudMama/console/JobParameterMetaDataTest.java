package com.redrock.jade.cloudMama.console;

import org.junit.Test;

import static org.junit.Assert.*;

public class JobParameterMetaDataTest {
    @Parameter(isRequired = true, defaultValue = "random")
    public String               stringField;

    @Parameter
    public int                  integerField;

    @Parameter
    public JobParameterMetaData unsupportedField;

    @Test
    public void initialization_stringParameter_succeeds() throws Exception {
        JobParameterMetaData metaData =
                new JobParameterMetaData(getClass().getDeclaredField("stringField"));

        assertEquals(JobParameterType.STRING, metaData.getType());
        assertEquals(true, metaData.isRequired());
        assertEquals("random", metaData.getDefaultValue());
    }

    @Test
    public void initialization_integerParameter_succeeds() throws Exception {
        JobParameterMetaData metaData =
                new JobParameterMetaData(getClass().getDeclaredField("integerField"));

        assertEquals(JobParameterType.INTEGER, metaData.getType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void initialization_unsupportedParameter_throws() throws Exception {
        new JobParameterMetaData(getClass().getDeclaredField("unsupportedField"));
    }

    @Test
    public void setValue_string_succeeds() throws Exception {
        JobParameterMetaData metaData =
                new JobParameterMetaData(getClass().getDeclaredField("stringField"));

        stringField = null;
        metaData.setValue(this, "test");
        assertEquals("test", stringField);
    }

    @Test
    public void setValue_integer_succeeds() throws Exception {
        JobParameterMetaData metaData =
                new JobParameterMetaData(getClass().getDeclaredField("integerField"));

        integerField = 0;
        metaData.setValue(this, "7");
        assertEquals(7, integerField);
    }
}