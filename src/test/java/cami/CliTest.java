package cami;

import static org.junit.Assert.*;

import cami.io.Base;
import cami.upload.BibiS3Upload;
import cami.hash.MD5Sum;
import cami.validator.CamiIOValidator;
import org.apache.commons.cli.ParseException;

import java.io.IOException;

public class CliTest {
    //@Test
    public void testValidBinning() {
        runCommandLine(new String[]{
                "-bf",
                "src/test/resources/binning.txt"
        });
    }

    //@Test
    public void testValidProfiling() {
        runCommandLine(new String[]{
                "-pf",
                "src/test/resources/profiling.txt",
                "/home/belmann/Desktop/master/web/neotax/neotax/currentTaxdb/taxdb"
        });
    }

    private void runCommandLine(String[] args) {
        Exception ex = null;
        CLI cli = new CLI(new CamiIOValidator(), new BibiS3Upload(), new MD5Sum());
        try {
            cli.processCommandLine(args);
        } catch (IOException | Base.ParseException | ParseException e) {
            ex = e;
            System.err.println(e.getMessage());
        }
        assertEquals(ex, null);
    }
}
