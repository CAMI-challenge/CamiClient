package cami.validator;

import cami.io.Base.ParseException;
import cami.io.Binning;
import cami.io.concat.ConcatProfilingIter;

import java.io.File;
import java.io.IOException;

public class CamiIOValidator implements IValidator {
    public void validateBinning(String path, String taxonomyDbPath) throws ParseException, IOException {
        File file = new File(path);
        File db = new File(taxonomyDbPath);
        filesExist(file, db, path);
        Binning.ValidatingReader reader = new Binning.ValidatingReader(file.getAbsolutePath(), taxonomyDbPath, true);
        while ((reader.readRow()) != null) {
        }
        reader.close();
    }

    public void validateProfiling(String path, String taxonomyDbPath) throws IOException, ParseException {
        File file = new File(path);
        File db = new File(taxonomyDbPath);
        filesExist(file, db, path);
        filesExist(file, db, path);
        ConcatProfilingIter profilingIter = new ConcatProfilingIter(file.getPath(), taxonomyDbPath, true);
        while ((profilingIter.readRow()) != null) {
        }
        profilingIter.close();
    }

    private void filesExist(File file, File db, String path) throws IOException {
        if (!file.exists()) {
            throw new IOException("File " + path + " does not exist.");
        }
        if (!db.exists()) {
            throw new IOException("Database " + path + " does not exist.");
        }
    }
}
