package com.github.quorum.utils;

import lombok.extern.slf4j.Slf4j;
import org.web3j.codegen.TruffleJsonFunctionWrapperGenerator;

import java.io.*;
import java.util.Arrays;

@Slf4j
public class WrappersGenerator {

    private static final String PATH_TO_COMPILED_CONTRACTS = "src/main/solidity/build/contracts";
    private static final String PATH_FOR_OUTPUT = "src/main/java";
    private static final String PACKAGE = "com.github.quorum.component.wrappers";

    public static void main(String[] args) {
        compileContracts();

        final File directory = new File(PATH_TO_COMPILED_CONTRACTS);
        final String[] solidityFiles = directory.list((file, name) -> name.endsWith(".json") && !name.startsWith("Migrations"));

        Arrays.stream(solidityFiles)
                .map(name -> name.substring(0, name.length() - 5))
                .forEach(WrappersGenerator::generate);
    }

    private static void compileContracts() {
        final File tempScript = createTempScript();
        try {
            final Process process = new ProcessBuilder("bash", tempScript.toString()).inheritIO().start();
            process.waitFor();
        } catch (NullPointerException ex) {
            log.error("[GENERATOR] script does not exists: {}", ex);
        } catch (IOException | InterruptedException ex) {
            log.error("[GENERATOR] exception while executing command: {}", ex);
        } finally {
            if (tempScript != null) {
                tempScript.delete();
            }
        }
    }

    private static File createTempScript() {
        try {
            final File tempScript = File.createTempFile("compile_contracts.sh", null);
            final Writer streamWriter = new OutputStreamWriter(new FileOutputStream(tempScript));
            PrintWriter printWriter = new PrintWriter(streamWriter);

            printWriter.println("#!/bin/bash");
            printWriter.println("cd src/main/solidity");
            printWriter.println("truffle compile");
            printWriter.close();

            return tempScript;

        } catch (Exception ex) {
            log.error("[GENERATOR] exception while creating temp script: {}", ex);
            return null;
        }
    }

    private static void generate(final String fileName) {
        final String[] parameters = new String[] {
                "--solidityTypes",
                PATH_TO_COMPILED_CONTRACTS + "/" + fileName + ".json",
                "-o",
                PATH_FOR_OUTPUT,
                "-p",
                PACKAGE
        };

        try {
            TruffleJsonFunctionWrapperGenerator.main(parameters);
        } catch (Exception ex) {
            log.error("[GENERATOR] exception while generating wrappers", ex);
        }
    }
}
