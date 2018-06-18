package cami;

import cami.download.SwiftDownload;
import cami.hash.IHashAlgorithm;
import cami.hash.MD5Sum;
import cami.io.Base;
import cami.upload.BibiS3Upload;
import cami.upload.IUpload;
import cami.validator.CamiIOValidator;
import cami.validator.IValidator;
import org.apache.commons.cli.*;

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

    private static final String DOWNLOAD_SWIFT_ARG_NAME = "url destination pattern threads";
    private static final String DOWNLOAD_SWIFT_LONG_OPT_NAME = "download";
    private static final String DOWNLOAD_SWIFT_DESCRIPTION = "Downloads data from Swift. You have to provide a pattern for downloading files. A simple '.' would download everything. Number of Threads is optional. Default: 10";
    private static final String DOWNLOAD_SWIFT_OPT_NAME = "d";

    private static final String LIST_SWIFT_ARG_NAME = "url";
    private static final String LIST_SWIFT_LONG_OPT_NAME = "list";
    private static final String LIST_SWIFT_DESCRIPTION = "Retrieves a list the data in Swift Container";
    private static final String LIST_SWIFT_OPT_NAME = "l";

    private static final String ASSEMBLY_ARG_NAME = "assembly_file";
    private static final String ASSEMBLY_LONG_OPT_NAME = "assemblyFingerprint";
    private static final String ASSEMBLY_DESCRIPTION = "Computes fingerprint of an assembly file.";
    private static final String ASSEMBLY_OPT_NAME = "af";

    private static final String BINNING_ARG_NAME = "binning_file extracted_taxnomy_db_path";
    private static final String BINNING_LONG_OPT_NAME = "binningFingerprint";
    private static final String BINNING_DESCRIPTION = "Validates binning file and computes fingerprint."+
                        " (download the taxonomy_db from https://data.cami-challenge.org/participate (Databases Section))";
    private static final String BINNING_OPT_NAME = "bf";

    private static final String PROFILING_ARG_NAME = "profiling_file extracted_taxnomy_db_path";
    private static final String PROFILING_LONG_OPT_NAME = "profilingFingerprint";
    private static final String PROFILING_DESCRIPTION = "Validates profiling file and computes fingerprint." +
            "(download the taxonomy_db from https://data.cami-challenge.org/participate (Databases Section))";
    private static final String PROFILING_OPT_NAME = "pf";

    private static final String UPLOAD_ARG_NAME = "credentials_file file_to_upload";
    private static final String UPLOAD_OPT_LONG_NAME = "upload";
    private static final String UPLOAD_DESCRIPTION =
            "You can get the credentials file from the cami website."
                    + "File to upload is the assembly, binning or profiling file you want to upload.";
    private static final String UPLOAD_OPT_NAME = "u";

    private static final String NOT_ENOUGH_PARAMETER = "Please provide the parameters: %s ";

    public static final String VERSION = "1.5.0";
    public static final String USAGE = "java -jar camiClient.jar";

    private IValidator validator;
    private IHashAlgorithm algorithm;
    private IUpload upload;

    public CLI(IValidator validator, IUpload upload, IHashAlgorithm algorithm) {
        this.validator = validator;
        this.algorithm = algorithm;
        this.upload = upload;
    }

    @SuppressWarnings("static-access")
    public void processCommandLine(String[] commandLine) throws IOException, Base.ParseException, ParseException {
        Options options = new Options();

        Option validateBinning = OptionBuilder.withArgName(BINNING_ARG_NAME)
                .withLongOpt(BINNING_LONG_OPT_NAME)
                .hasArgs(2)
                .withDescription(BINNING_DESCRIPTION)
                .create(BINNING_OPT_NAME);

        Option download = OptionBuilder.withArgName(DOWNLOAD_SWIFT_ARG_NAME)
                .withLongOpt(DOWNLOAD_SWIFT_LONG_OPT_NAME)
                .hasArgs(4)
                .withDescription(DOWNLOAD_SWIFT_DESCRIPTION)
                .create(DOWNLOAD_SWIFT_OPT_NAME);

        Option list = OptionBuilder.withArgName(LIST_SWIFT_ARG_NAME)
                .withLongOpt(LIST_SWIFT_LONG_OPT_NAME)
                .hasArgs(1)
                .withDescription(LIST_SWIFT_DESCRIPTION)
                .create(LIST_SWIFT_OPT_NAME);

        Option validateProfiling = OptionBuilder
                .withArgName(PROFILING_ARG_NAME)
                .withLongOpt(PROFILING_LONG_OPT_NAME)
                .hasArgs(2)
                .withDescription(PROFILING_DESCRIPTION)
                .create(PROFILING_OPT_NAME);

        Option validateAssembly = OptionBuilder.withArgName(ASSEMBLY_ARG_NAME)
                .withLongOpt(ASSEMBLY_LONG_OPT_NAME).hasArg(true)
                .withDescription(ASSEMBLY_DESCRIPTION)
                .create(ASSEMBLY_OPT_NAME);

        Option upload = OptionBuilder.withArgName(UPLOAD_ARG_NAME)
                .withLongOpt(UPLOAD_OPT_LONG_NAME).hasArgs(2)
                .withValueSeparator(' ').withDescription(UPLOAD_DESCRIPTION)
                .create(UPLOAD_OPT_NAME);

        options.addOption(validateProfiling);
        options.addOption(validateBinning);
        options.addOption(validateAssembly);
        options.addOption(upload);
        options.addOption(download);
        options.addOption(list);
        options.addOption(OptionBuilder.withLongOpt(HELP_LONG_OPT_NAME)
                .withDescription(HELP_DESCRIPTION)
                .create(HELP_OPT_NAME));
        options.addOption(OptionBuilder.withLongOpt(VERSION_LONG_OPT_NAME)
                .withDescription(VERSION_DESCRIPTION)
                .create(VERSION_OPT_NAME));


        CommandLineParser parser = new BasicParser();
        CommandLine line = null;

        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(200);
        String header = "Validates and uploads binning, profiling and assembly files. " +
                "Downloads data from Swift.\n";
        String footer = "\n";

        try {
            line = parser.parse(options, commandLine);

        } catch (ParseException ex) {
            formatter.printHelp(USAGE, header, options, footer, true);
            throw ex;
        }

        String path = null;
        if (line.hasOption(BINNING_OPT_NAME)
                || line.hasOption(BINNING_LONG_OPT_NAME)) {
            path = line.getOptionValue(BINNING_OPT_NAME);
            String args[] = line.getOptionValues(BINNING_OPT_NAME);
            if(args.length == 2) {
                validator.validateBinning(args[0], args[1]);
                computeFingerprint(path);
            }else{
                throw new IOException(String.format(NOT_ENOUGH_PARAMETER, BINNING_ARG_NAME));
            }
        } else if (line.hasOption(PROFILING_OPT_NAME)
                || line.hasOption(PROFILING_LONG_OPT_NAME)) {
            String args[] = line.getOptionValues(PROFILING_OPT_NAME);
            if(args.length ==2) {
                validator.validateProfiling(args[0], args[1]);
                computeFingerprint(args[0]);
            }else{
                throw new IOException(String.format(NOT_ENOUGH_PARAMETER, PROFILING_ARG_NAME));
            }
        } else if (line.hasOption(ASSEMBLY_OPT_NAME)
                || line.hasOption(ASSEMBLY_LONG_OPT_NAME)) {
            path = line.getOptionValue(ASSEMBLY_OPT_NAME);
            computeFingerprint(path);
        } else if (line.hasOption(UPLOAD_OPT_NAME)
                || line.hasOption(UPLOAD_OPT_LONG_NAME)) {
            String[] args = line.getOptionValues(UPLOAD_OPT_NAME);
            if(args.length == 2) {
                String credentialsFile = args[0];
                String fileToUpload = args[1];

                try {
                    this.upload.upload(fileToUpload, credentialsFile);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }else{
                throw new IOException(String.format(NOT_ENOUGH_PARAMETER, UPLOAD_ARG_NAME));
            }
        } else if (line.hasOption(DOWNLOAD_SWIFT_OPT_NAME)
                        || line.hasOption(DOWNLOAD_SWIFT_LONG_OPT_NAME)) {

            String[] args = line.getOptionValues(DOWNLOAD_SWIFT_OPT_NAME);
            if (args.length == 3 || args.length == 4) {
                String source = args[0];
                String destination = args[1];
                String regex = args[2];
                int threads = 10;
                if (args.length == 4) {
                    threads = Integer.parseInt(args[3]);
                }

                try {
                    if (source.endsWith("/")) {
                        source = source.replaceFirst(".$", "");
                    }
                    SwiftDownload swiftDownload = new SwiftDownload();
                    swiftDownload.downloadAll(source, destination, regex, threads);
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            } else {
                throw new IOException(String.format(NOT_ENOUGH_PARAMETER, DOWNLOAD_SWIFT_ARG_NAME));
            }
        } else if (line.hasOption(LIST_SWIFT_OPT_NAME)
                || line.hasOption(LIST_SWIFT_LONG_OPT_NAME)){

            String[] args = line.getOptionValues(LIST_SWIFT_OPT_NAME);
            if (args.length == 1) {
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
            } else {
                throw new IOException(String.format(NOT_ENOUGH_PARAMETER, LIST_SWIFT_ARG_NAME));
            }
        } else if (line.hasOption(VERSION_LONG_OPT_NAME)
                || line.hasOption(VERSION_OPT_NAME)) {
            System.out.println("Version:" + VERSION);
        } else if (line.hasOption(HELP_LONG_OPT_NAME)
                || line.hasOption(HELP_OPT_NAME)) {
            formatter.printHelp(USAGE, header, options, footer, true);
        } else {
                formatter.printHelp(USAGE, header, options, footer, true);
        }
    }

    private void computeFingerprint(String path) {
        System.out.println("Validation finished");
        System.out.println("Starting to compute fingerprint");
        System.out.println("Use this fingerprint in the cami website: "
                + algorithm.getFingerprint(path));
    }

    public static void main(String[] args) {
        CLI uploader = new CLI(new CamiIOValidator(), new BibiS3Upload(), new MD5Sum());
        try {
            uploader.processCommandLine(args);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (Base.ParseException e) {
            System.err.println(e.getMessage());
        } catch (ParseException e) {
            System.err.println(e.getMessage());
        }
    }
}
