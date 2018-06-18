package cami.validator;

import java.io.IOException;

import cami.io.Base.ParseException;

public interface IValidator {
    void validateBinning(String path, String taxonomyDbPath) throws ParseException, IOException;

    void validateProfiling(String path, String taxonomyDbPath) throws ParseException, IOException;
}
