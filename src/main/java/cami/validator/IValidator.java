package cami.validator;

import java.io.IOException;

import cami.io.Base.ParseException;

public interface IValidator {

	public void validateBinning(String path, String taxonomyDbPath) throws ParseException, IOException;

	public void validateProfiling(String path, String taxonomyDbPath) throws ParseException, IOException;
}
