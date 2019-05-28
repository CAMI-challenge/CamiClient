package cami;

import cami.download.SwiftDownload;
import cami.hash.IHashAlgorithm;
import cami.hash.MD5Sum;
import cami.hash.SHA1Sum;
import cami.io.Base;
import cami.upload.BibiS3Upload;
import cami.upload.IUpload;
import cami.validator.CamiIOValidator;
import cami.validator.IValidator;
import org.apache.commons.cli.*;
import org.apache.log4j.PropertyConfigurator;
import java.util.Properties;

import java.io.IOException;

/**
 * CLI for validating, hashing and uploading cami specific files to s3.
 * Downloads data from Swift.
 *
 * @author pbelmann
 */
public class CLI {
    private static final String VERSION_LONG_OPT_NAME = "version";
    private static final String VERSION_DESCRIPTION = "Print the version of the application";
    private static final String VERSION_OPT_NAME = "v";

    private static final String HELP_LONG_OPT_NAME = "help";
    private static final String HELP_DESCRIPTION = "Print the help of the application";
    private static final String HELP_OPT_NAME = "h";

    private static final String DOWNLOAD_SWIFT_ARG_NAME = "linkfile|url> <destination";
    private static final String DOWNLOAD_SWIFT_LONG_OPT_NAME = "download";
    private static final String DOWNLOAD_SWIFT_DESCRIPTION =
            "Downloads data from Swift";
    private static final String DOWNLOAD_SWIFT_OPT_NAME = "d";

    private static final String DOWNLOAD_PATTERN_ARG_NAME = "pattern";
    private static final String DOWNLOAD_PATTERN_LONG_OPT_NAME = "pattern";
    private static final String DOWNLOAD_PATTERN_DESCRIPTION = "Pattern to use for downloading, to select only specific files to be downloaded eg. '-p long' selects only those files with long in the name";
    private static final String DOWNLOAD_PATTERN_OPT_NAME = "p";

    private static final String DOWNLOAD_THREADS_ARG_NAME = "threads";
    private static final String DOWNLOAD_THREADS_LONG_OPT_NAME = "threads";
    private static final String DOWNLOAD_THREADS_DESCRIPTION = "Number of threads to use for downloading";
    private static final String DOWNLOAD_THREADS_OPT_NAME = "t";

    private static final String DOWNLOAD_RETRY_ARG_NAME = "retry";
    private static final String DOWNLOAD_RETRY_LONG_OPT_NAME = "retry";
    private static final String DOWNLOAD_RETRY_DESCRIPTION = "Retry existing files when checksum is incorrect (or not present)";
    private static final String DOWNLOAD_RETRY_OPT_NAME = "r";

    private static final String DOWNLOAD_FORCERETRY_ARG_NAME = "force";
    private static final String DOWNLOAD_FORCERETRY_LONG_OPT_NAME = "force";
    private static final String DOWNLOAD_FORCERETRY_DESCRIPTION = "When specified with -r, retry existing files regardless of checksum";
    private static final String DOWNLOAD_FORCERETRY_OPT_NAME = "f";

    private static final String LIST_SWIFT_ARG_NAME = "url";
    private static final String LIST_SWIFT_LONG_OPT_NAME = "list";
    private static final String LIST_SWIFT_DESCRIPTION = "Retrieves a list the data in public Swift Container";
    private static final String LIST_SWIFT_OPT_NAME = "l";

    private static final String ASSEMBLY_ARG_NAME = "assembly_file";
    private static final String ASSEMBLY_LONG_OPT_NAME = "assemblyFingerprint";
    private static final String ASSEMBLY_DESCRIPTION = "Computes fingerprint of an assembly file.";
    private static final String ASSEMBLY_OPT_NAME = "af";

    private static final String BINNING_ARG_NAME = "binning_file> <extracted_taxnomy_db_path";
    private static final String BINNING_LONG_OPT_NAME = "binningFingerprint";
    private static final String BINNING_DESCRIPTION = "Validates binning file and computes fingerprint" +
            " (download the taxonomy_db from https://data.cami-challenge.org/participate (Databases Section))";
    private static final String BINNING_OPT_NAME = "bf";

    private static final String PROFILING_ARG_NAME = "profiling_file> <extracted_taxnomy_db_path";
    private static final String PROFILING_LONG_OPT_NAME = "profilingFingerprint";
    private static final String PROFILING_DESCRIPTION = "Validates profiling file and computes fingerprint" +
	    "(download the taxonomy_db from https://data.cami-challenge.org/participate (Databases Section))";

    private static final String PROFILING_OPT_NAME = "pf";

    private static final String UPLOAD_ARG_NAME = "linkfile> <file_to_upload";
    private static final String UPLOAD_OPT_LONG_NAME = "upload";
    private static final String UPLOAD_DESCRIPTION =
            "Uploads data to Swift";
    private static final String UPLOAD_OPT_NAME = "u";

    private static final String NOT_ENOUGH_PARAMETER = "Please provide the parameters: %s ";

    public static final String VERSION = "1.8.0";
    private static final String DOWNLOAD_USAGE = "java -jar camiClient.jar -d <linkfile|url> <destination> [-p <pattern >] [-t <threads>] [-r [-f]]";
    private static final String LIST_USAGE = "java -jar camiClient.jar -l <url>";
    private static final String BINNING_USAGE = "java -jar camiClient.jar -bf <binning_file> <extracted_taxnomy_db_path>";
    private static final String PROFILING_USAGE = "java -jar camiClient.jar -pf <profiling_file> <extracted_taxnomy_db_path>";
    private static final String ASSEMBLY_USAGE = "java -jar camiClient.jar -af <assembly_file>";
    private static final String UPLOAD_USAGE = "java -jar camiClient.jar -u <linkfile> <file_to_upload>";
    private static final String VERSION_USAGE = "java -jar camiClient.jar -v";
    private static final String HELP_USAGE = "java -jar camiClient.jar -h";

    private final IValidator validator;
    private final IHashAlgorithm algorithm;
    private final IHashAlgorithm downloadAlgorithm;
    private final IUpload upload;

    public CLI(IValidator validator, IUpload upload, IHashAlgorithm algorithm, IHashAlgorithm downloadAlgorithm) {
        this.validator = validator;
        this.algorithm = algorithm;
        this.downloadAlgorithm = downloadAlgorithm;
        this.upload = upload;
    }

    public void processCommandLine(String[] commandLine) throws IOException, Base.ParseException, ParseException {
	for (int i = 0; i < commandLine.length; i++) {
		commandLine[i] = commandLine[i].replace(' ', 'ᴥ');
	}
        Options options = buildCommandLineOptions();
        CommandLineParser parser = new BasicParser();
        CommandLine line;
        try {
            line = parser.parse(options, commandLine);
        } catch (ParseException ex) {
            printHelp(options);
            throw ex;
        }
        if (line.hasOption(BINNING_OPT_NAME) || line.hasOption(BINNING_LONG_OPT_NAME)) {
            runValidateBinning(line);
        } else if (line.hasOption(PROFILING_OPT_NAME) || line.hasOption(PROFILING_LONG_OPT_NAME)) {
            runProfiling(line);
        } else if (line.hasOption(ASSEMBLY_OPT_NAME) || line.hasOption(ASSEMBLY_LONG_OPT_NAME)) {
            runValidateAssembly(line);
        } else if (line.hasOption(UPLOAD_OPT_NAME) || line.hasOption(UPLOAD_OPT_LONG_NAME)) {
            runUpload(line);
        } else if (line.hasOption(DOWNLOAD_SWIFT_OPT_NAME) || line.hasOption(DOWNLOAD_SWIFT_LONG_OPT_NAME)) {
            runDownload(line);
        } else if (line.hasOption(LIST_SWIFT_OPT_NAME) || line.hasOption(LIST_SWIFT_LONG_OPT_NAME)) {
            runList(line);
        } else if (line.hasOption(VERSION_LONG_OPT_NAME) || line.hasOption(VERSION_OPT_NAME)) {
            System.out.println("Version:" + VERSION);
        } else {
            printHelp(options);
        }
    }

    private void runValidateBinning(CommandLine line) throws IOException, Base.ParseException {
        String path = line.getOptionValue(BINNING_OPT_NAME);
        String[] args = line.getOptionValues(BINNING_OPT_NAME);
        if (args.length != 2) {
            throw new IOException(String.format(NOT_ENOUGH_PARAMETER, BINNING_ARG_NAME));
        }
        String binningFile = args[0];
        binningFile = binningFile.replace('ᴥ', ' ');
        String extractPath = args[1];
        extractPath = extractPath.replace('ᴥ', ' ');
        validator.validateBinning(binningFile, extractPath);
        computeFingerprint(binningFile);
    }

    private void runProfiling(CommandLine line) throws IOException, Base.ParseException {
        String[] args = line.getOptionValues(PROFILING_OPT_NAME);
        if (args.length != 2) {
            throw new IOException(String.format(NOT_ENOUGH_PARAMETER, PROFILING_ARG_NAME));
        }
        String profilingFile = args[0];
        profilingFile = profilingFile.replace('ᴥ', ' ');
        String extractPath = args[1];
        extractPath = extractPath.replace('ᴥ', ' ');
        validator.validateProfiling(profilingFile, extractPath);
        computeFingerprint(profilingFile);
    }

    private void runValidateAssembly(CommandLine line) {
        String path = line.getOptionValue(ASSEMBLY_OPT_NAME);
        String assemblyFile = path;
        assemblyFile = assemblyFile.replace('ᴥ', ' ');
        computeFingerprint(assemblyFile);
    }

    private void runUpload(CommandLine line) throws IOException {
        String[] args = line.getOptionValues(UPLOAD_OPT_NAME);
        if (args.length != 2) {
            throw new IOException(String.format(NOT_ENOUGH_PARAMETER, UPLOAD_ARG_NAME));
        }
        String linkFile = args[0];
	linkFile = linkFile.replace('ᴥ', ' ');
        String fileToUpload = args[1];
	fileToUpload = fileToUpload.replace('ᴥ', ' ');
        try {
            this.upload.upload(fileToUpload, linkFile);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void runDownload(CommandLine line) throws IOException {
        String[] args = line.getOptionValues(DOWNLOAD_SWIFT_OPT_NAME);
        if (args.length != 2) {
            throw new IOException(String.format(NOT_ENOUGH_PARAMETER, DOWNLOAD_SWIFT_ARG_NAME));
        }

        String source = args[0];
        String destination = args[1];
	source = source.replace('ᴥ', ' ');
	destination = destination.replace('ᴥ', ' ');
	String regex = ".";
	int threads = 10;
	boolean retry = false;
	boolean forceRetry = false;

        if (line.hasOption(DOWNLOAD_PATTERN_OPT_NAME)) {
		args = line.getOptionValues(DOWNLOAD_PATTERN_OPT_NAME);
		if (args.length != 1) {
		    throw new IOException(String.format(NOT_ENOUGH_PARAMETER, DOWNLOAD_PATTERN_ARG_NAME));
		}

		regex = args[0];
	}

        if (line.hasOption(DOWNLOAD_THREADS_OPT_NAME)) {
		args = line.getOptionValues(DOWNLOAD_THREADS_OPT_NAME);
		if (args.length != 1) {
		    throw new IOException(String.format(NOT_ENOUGH_PARAMETER, DOWNLOAD_THREADS_ARG_NAME));
		}

		threads = Integer.parseInt(args[0]);
	}

        if (line.hasOption(DOWNLOAD_RETRY_OPT_NAME)) {
		args = line.getOptionValues(DOWNLOAD_RETRY_OPT_NAME);

		retry = true;
	}

        if (line.hasOption(DOWNLOAD_FORCERETRY_OPT_NAME)) {
		args = line.getOptionValues(DOWNLOAD_FORCERETRY_OPT_NAME);

		forceRetry = true;
	}

        try {
            if (source.endsWith("/")) {
                source = source.replaceFirst(".$", "");
            }
            SwiftDownload swiftDownload = new SwiftDownload();
            swiftDownload.downloadAll(source, destination, regex, threads, retry, forceRetry, downloadAlgorithm);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }


    private void runList(CommandLine line) throws IOException {
        String[] args = line.getOptionValues(LIST_SWIFT_OPT_NAME);
        if (args.length != 1) {
            throw new IOException(String.format(NOT_ENOUGH_PARAMETER, LIST_SWIFT_ARG_NAME));
        }
        String source = args[0];
        try {
            if (source.endsWith("/")) {
                source = source.replaceFirst(".$", "");
            }
            SwiftDownload swiftDownload = new SwiftDownload();
            System.out.println(swiftDownload.list(source));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }


    @SuppressWarnings("static-access")
    private static Options buildCommandLineOptions() {
        Options options = new Options();
        // Download Swift option
        options.addOption(OptionBuilder.withArgName(DOWNLOAD_SWIFT_ARG_NAME)
                .withLongOpt(DOWNLOAD_SWIFT_LONG_OPT_NAME)
                .hasArgs(2)
                .withDescription(DOWNLOAD_SWIFT_DESCRIPTION)
                .create(DOWNLOAD_SWIFT_OPT_NAME));
        // Download sub arguments
        options.addOption(OptionBuilder.withArgName(DOWNLOAD_PATTERN_ARG_NAME)
                .withLongOpt(DOWNLOAD_PATTERN_LONG_OPT_NAME)
                .hasArg()
                .withDescription(DOWNLOAD_PATTERN_DESCRIPTION)
                .create(DOWNLOAD_PATTERN_OPT_NAME));
        options.addOption(OptionBuilder.withArgName(DOWNLOAD_THREADS_ARG_NAME)
                .withLongOpt(DOWNLOAD_THREADS_LONG_OPT_NAME)
                .hasArg()
                .withDescription(DOWNLOAD_THREADS_DESCRIPTION)
                .create(DOWNLOAD_THREADS_OPT_NAME));
        options.addOption(OptionBuilder.withArgName(DOWNLOAD_RETRY_ARG_NAME)
                .withLongOpt(DOWNLOAD_RETRY_LONG_OPT_NAME)
                .withDescription(DOWNLOAD_RETRY_DESCRIPTION)
                .create(DOWNLOAD_RETRY_OPT_NAME));
        options.addOption(OptionBuilder.withArgName(DOWNLOAD_FORCERETRY_ARG_NAME)
                .withLongOpt(DOWNLOAD_FORCERETRY_LONG_OPT_NAME)
                .withDescription(DOWNLOAD_FORCERETRY_DESCRIPTION)
                .create(DOWNLOAD_FORCERETRY_OPT_NAME));
        // Profiling option
        options.addOption(OptionBuilder.withArgName(PROFILING_ARG_NAME)
                .withLongOpt(PROFILING_LONG_OPT_NAME)
                .hasArgs(2)
                .withDescription(PROFILING_DESCRIPTION)
                .create(PROFILING_OPT_NAME));
        // Validate binning option
        options.addOption(OptionBuilder.withArgName(BINNING_ARG_NAME)
                .withLongOpt(BINNING_LONG_OPT_NAME)
                .hasArgs(2)
                .withDescription(BINNING_DESCRIPTION)
                .create(BINNING_OPT_NAME));
        // Validate assembly option
        options.addOption(OptionBuilder.withArgName(ASSEMBLY_ARG_NAME)
                .withLongOpt(ASSEMBLY_LONG_OPT_NAME)
                .hasArg(true)
                .withDescription(ASSEMBLY_DESCRIPTION)
                .create(ASSEMBLY_OPT_NAME));
        // Upload option
        options.addOption(OptionBuilder.withArgName(UPLOAD_ARG_NAME)
                .withLongOpt(UPLOAD_OPT_LONG_NAME)
                .hasArgs(2)
                .withValueSeparator(' ')
                .withDescription(UPLOAD_DESCRIPTION)
                .create(UPLOAD_OPT_NAME));
        // List Swift option
        options.addOption(OptionBuilder.withArgName(LIST_SWIFT_ARG_NAME)
                .withLongOpt(LIST_SWIFT_LONG_OPT_NAME)
                .hasArgs(1)
                .withDescription(LIST_SWIFT_DESCRIPTION)
                .create(LIST_SWIFT_OPT_NAME));
        // Help option
        options.addOption(OptionBuilder.withLongOpt(HELP_LONG_OPT_NAME)
                .withDescription(HELP_DESCRIPTION)
                .create(HELP_OPT_NAME));
        // Version option
        options.addOption(OptionBuilder.withLongOpt(VERSION_LONG_OPT_NAME)
                .withDescription(VERSION_DESCRIPTION)
                .create(VERSION_OPT_NAME));
        return options;
    }

    private void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(150);
        String downloadHeader = "DOWNLOAD - downloads data from OpenStack\n\n";
        String listHeader = "LIST - lists data from public Swift storage\n\n";
        String binningHeader = "BINNING - calculates binning header\n\n";
        String profilingHeader = "PROFILING - calculates profiling header\n\n";
        String assemblyHeader = "ASSEMBLY - calculates assembly header\n\n";
        String uploadHeader = "UPLOAD - uploads data to OpenStack\n\n";
        String versionHeader = "VERSION - shows client version\n\n";
        String helpHeader = "HELP - shows this message\n\n";
        String footer = "";
        Options blankOptions = new Options();

	System.out.println("\n");
        formatter.printHelp(DOWNLOAD_USAGE, downloadHeader, blankOptions, footer, false);
        formatter.printHelp(LIST_USAGE, listHeader, blankOptions, footer, false);
        formatter.printHelp(BINNING_USAGE, binningHeader, blankOptions, footer, false);
        formatter.printHelp(PROFILING_USAGE, profilingHeader, blankOptions, footer, false);
        formatter.printHelp(ASSEMBLY_USAGE, assemblyHeader, blankOptions, footer, false);
        formatter.printHelp(UPLOAD_USAGE, uploadHeader, blankOptions, footer, false);
        formatter.printHelp(VERSION_USAGE, versionHeader, blankOptions, footer, false);
        formatter.printHelp(HELP_USAGE, helpHeader, options, footer, false);
    }

    private void computeFingerprint(String path) {
        System.out.println("Validation finished");
        System.out.println("Starting to compute fingerprint");
        System.out.println("Use this fingerprint in the cami website: " + algorithm.getFingerprint(path));
    }

    public static void main(String[] args) {

	Properties prop = new Properties();
	prop.setProperty("log4j.rootLogger", "WARN");
	PropertyConfigurator.configure(prop);

        CLI uploader = new CLI(new CamiIOValidator(), new BibiS3Upload(), new MD5Sum(), new SHA1Sum());
        try {
            uploader.processCommandLine(args);
        } catch (IOException | Base.ParseException | ParseException e) {
            System.err.println(e.getMessage());
        }
    }
}
