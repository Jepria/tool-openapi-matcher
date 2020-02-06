package org.jepria.tools.openapi.matcher;

import java.io.IOException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class MatcherCli {

  public static final  String SPEC_OPT      = "spec";
  private static final String SPEC_OPT_NAME = "specification";

  public static final  String JAVA_PATH_OPT      = "java";
  private static final String JAVA_PATH_OPT_NAME = "java";

  private static Options options;

  static {
    initOptions();
  }

  private static void initOptions() {
    Option specOpt = new Option(SPEC_OPT, SPEC_OPT_NAME, true, "OpenAPI specification");
    specOpt.setArgs(1);
    specOpt.setArgName(SPEC_OPT_NAME);

    Option outputDirOpt = new Option(JAVA_PATH_OPT, JAVA_PATH_OPT_NAME, true, "Output directory");
    outputDirOpt.setArgs(1);
    outputDirOpt.setArgName(JAVA_PATH_OPT_NAME);

    options = new Options();
    options.addOption(specOpt);
    options.addOption(outputDirOpt);
  }

  private static Options getOptions() {
    return options;
  }

  public static void main(String[] args) throws ParseException, IOException {
    match(args);
  }

  public static void match(String[] args) throws ParseException, IOException {
    CommandLine commandLine = new DefaultParser().parse(getOptions(), args);

    String specPath = null;
    String javaPath = null;

    if (commandLine.hasOption(SPEC_OPT)) {
      System.out.println(commandLine.getOptionValue(SPEC_OPT));
      specPath = commandLine.getOptionValue(SPEC_OPT);
    } else {
      System.out.println("You should set OpenAPi specification file.");
      return;
    }

    if (commandLine.hasOption(JAVA_PATH_OPT)) {
      System.out.println(commandLine.getOptionValue(JAVA_PATH_OPT));
      javaPath = commandLine.getOptionValue(JAVA_PATH_OPT);
    } else {
      System.out.println("You should set java file.");
      return;
    }

    AdapterMatcher matcher = new AdapterMatcher();

    matcher.matchAdapter2Spec(javaPath, specPath, System.out);

  }

}
