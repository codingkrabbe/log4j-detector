package com.mergebase.log4j;

import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;

public class IntegrationTest {

    @Test
    @Ignore("needs /samples directory to be setup")
    public void testEnd2EndIntegrationAgainstSamples() throws IOException {
        File file = samplesDir();
        PrintStream original = System.out;
        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PrintStream printStream = new PrintStream(baos, true, StandardCharsets.UTF_8.name())) {
            System.setOut(printStream);

            Log4JDetector.main(new String[]{file.getAbsolutePath()});
            String probe = baos.toString(StandardCharsets.UTF_8.name());
            original.print(probe);
            verify(probe);
        } finally {
            System.setOut(original);
        }
        
    }

    private void verify(String probe) {
        probe = probe.replaceAll("\\\\", "/");
        List<String> lines = Arrays.asList(probe.split("\n"));

        verifyFalsePositives(lines);
        verifyHealthyPositives(lines);
        verifyMitigated(lines);
        verifyVulnerable(lines);
        verifyEndOfLife(lines);
    }
    
    private void verifyFalsePositives(List<String> lines) {
        verifyFolderMatches(lines, "/good_v2x_jars/log4j-over-slf/", 72, "__FALSE_POSITIVE__");
    }

    private void verifyHealthyPositives(List<String> lines) {
        verifyFolderMatches(lines, "/good_v2x_jars/log4j-core/", 3, "__UP_TO_DATE__");
    }

    private void verifyMitigated(List<String> lines) {
        verifyFolderMatches(lines, "/mitigations/", 2, "(__MITIGATED_BY_");
    }

    private void verifyVulnerable(List<String> lines) {
        verifyFolderMatches(lines, "/evil_v2x_jars/", 49, "__CVE_2021_45105__");
    }

    private void verifyEndOfLife(List<String> lines) {
        verifyFolderMatches(lines, "/evil_v1x_jars/", 4, "contains Log4J-1.x");
    }

    private void verifyFolderMatches(List<String> lines, String folder, int matchCount, String tag) {
        List<String> shouldBeFalse = lines.stream().filter(string -> string.contains(folder)).collect(Collectors.toList());
        assertThat(shouldBeFalse.size(), is(matchCount));
        for (String match : shouldBeFalse) {
            assertThat(match, containsString(tag));
        }
    }

    private File samplesDir() {
        String relPath = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        // TODO: Automatically synchronize jfrog repository (needs to be public)
        File targetDir = new File(relPath+"../../samples");
        if(!targetDir.exists()) {
            System.out.println("jfrog rt dl --url https://captain.rtf.siemens.net/artifactory/ --user $GID --access-token $API_KEY utils-log4j-samples/ .");
            throw new IllegalStateException("Please make sure the samples directory exists and contains our samples.");
        }
        return targetDir;
    }
}
