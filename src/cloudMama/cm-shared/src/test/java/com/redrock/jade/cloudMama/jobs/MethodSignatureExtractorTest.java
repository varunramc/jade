package com.redrock.jade.cloudMama.jobs;

import com.redrock.jade.cloudMama.jobs.stubs.MethodSignatureTargetStub;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class MethodSignatureExtractorTest {

    @Test
    public void extract_simpleMethod_methodSignatureExtracted() throws Exception {
        MethodSignature signature =
                MethodSignatureExtractor.extract(MethodSignatureTargetStub.class, MethodSignatureTargetStub::simple);

        Method method = signature.getMethod(MethodSignatureTargetStub.class);
        assertEquals("simple", method.getName());
    }

    @Test
    public void extract_overloadedMethod_correctMethodSignatureExtracted() throws Exception {
        MethodSignature signature =
                MethodSignatureExtractor.extract(MethodSignatureTargetStub.class, instance -> instance.duplicate(5));

        Method method = signature.getMethod(MethodSignatureTargetStub.class);
        assertEquals("duplicate", method.getName());
        assertEquals(1, method.getParameterCount());
        assertEquals(int.class, method.getParameterTypes()[0]);
    }
}