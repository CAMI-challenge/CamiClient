package cami;

import static org.junit.Assert.*;

import cami.io.Base;
import cami.upload.BibiS3Upload;
import cami.hash.MD5Sum;
import cami.validator.CamiIOValidator;
import org.apache.commons.cli.ParseException;
import org.junit.Test;

import java.io.IOException;

public class CliTest {

	//@Test
	public void testValidBinning() {
		String[] args = new String[]{"-bf","src/test/resources/binning.txt"};

		CLI uploader = new CLI(new CamiIOValidator(), new BibiS3Upload(),
				new MD5Sum());

		Exception ex = null;

		try {
			uploader.processCommandLine(args);
		} catch (IOException e) {
			ex = e;
			System.err.println(e.getMessage());
		} catch (Base.ParseException e) {
			ex = e;
			System.err.println(e.getMessage());
		} catch (ParseException e) {
			ex = e;
			System.err.println(e.getMessage());
		}
		assertEquals(ex,null);
	}


//	@Test
	public void testValidProfiling() {
		String[] args = new String[]{"-pf","src/test/resources/profiling.txt",
				"/home/belmann/Desktop/master/web/neotax/neotax/currentTaxdb/taxdb"};

		CLI uploader = new CLI(new CamiIOValidator(), new BibiS3Upload(),
				new MD5Sum());

		Exception ex = null;

		try {
			uploader.processCommandLine(args);
		} catch (IOException e) {
			ex = e;
			System.err.println(e.getMessage());
		} catch (Base.ParseException e) {
			ex = e;
			System.err.println(e.getMessage());
		} catch (ParseException e) {
			ex = e;
			System.err.println(e.getMessage());
		}
		assertEquals(ex,null);
	}
}
