package org.jepria.tools.matcher;


import static org.jepria.tools.matcher.diffTypes.MethodDiff.DiffType.ADDED;
import static org.jepria.tools.matcher.diffTypes.MethodDiff.DiffType.CHANGED;
import static org.jepria.tools.matcher.diffTypes.MethodDiff.DiffType.DELETED;
import static org.jepria.tools.matcher.diffTypes.MethodDiff.DiffType.TYPE_CHANGED;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import org.jepria.tools.matcher.diffTypes.AnnotationDiff;
import org.jepria.tools.matcher.diffTypes.MatchDiff;
import org.jepria.tools.matcher.diffTypes.MethodDiff;
import org.jepria.tools.matcher.diffTypes.ParamDiff;

//TODO: need refactoring!
public class MatchingResultParser {

  private BufferedWriter outWriter;

  private enum SeverityLevel {
    INFO,
    WARNING,
    ERROR
  }

  private SeverityLevel methodAddedSeverityLevel       = SeverityLevel.ERROR;
  private SeverityLevel methodDeletedSeverityLevel     = SeverityLevel.ERROR;
  private SeverityLevel methodChangedSeverityLevel     = SeverityLevel.ERROR;
  private SeverityLevel paramsChangedSeverityLevel     = SeverityLevel.ERROR;
  private SeverityLevel paramsAddedSeverityLevel       = SeverityLevel.ERROR;
  private SeverityLevel paramsDeletedSeverityLevel     = SeverityLevel.ERROR;
  private SeverityLevel annotationAddedSeverityLevel   = SeverityLevel.ERROR;
  private SeverityLevel annotationDeletedSeverityLevel = SeverityLevel.ERROR;
  private SeverityLevel annotationChangedSeverityLevel = SeverityLevel.ERROR;

  private SeverityLevel currentSeverityLevel = SeverityLevel.ERROR;

  public MatchingResultParser(OutputStream outStream) {
    this.outWriter = new BufferedWriter(new OutputStreamWriter(outStream));
  }

  public void parse(List<MethodDiff> matchingResult) throws IOException {
    if (null == matchingResult || 0 == matchingResult.size()) {
      return;
    }
    outWriter.write("Parsing results: ");
    outWriter.newLine();
    for (MethodDiff methodDiff : matchingResult) {
      String errorLine = String.valueOf(methodDiff.getMethod().getBegin().get());
      if (DELETED.equals(methodDiff.getType()) && !isHide(methodDeletedSeverityLevel)) {
        outWriter.write(errorLine + ": ");
        outWriter.write(methodDeletedSeverityLevel.toString() + ": Method diff " + methodDiff.getMethod().getNameAsString() + " " + "method deleted");
        outWriter.newLine();
        outWriter.newLine();
      }
      if (ADDED.equals(methodDiff.getType()) && !isHide(methodAddedSeverityLevel)) {
        outWriter.write(errorLine + ": ");
        outWriter.write(methodAddedSeverityLevel.toString() + ": Need implement new method " + methodDiff.getMethod().getNameAsString() + ".");
        outWriter.newLine();
        outWriter.newLine();
      }
      if (CHANGED.equals(methodDiff.getType()) && !isHide(methodChangedSeverityLevel)) {
        outWriter.write(errorLine + ": ");
        outWriter.write(methodChangedSeverityLevel.toString() + ": method changed " + methodDiff.getMethod().getDeclarationAsString());
        outWriter.newLine();
        if (null != methodDiff.getAnnotationsDiff() && !methodDiff.getAnnotationsDiff().isEmpty()) {
          for (AnnotationDiff annotationDiff : methodDiff.getAnnotationsDiff()) {
            if (MatchDiff.DiffType.ADDED.equals(annotationDiff.getType()) && !isHide(annotationDeletedSeverityLevel)) {
              outWriter.write(annotationAddedSeverityLevel.toString() + ": need add new annotation " + annotationDiff.getNode().toString());
              outWriter.newLine();
            }
            if (MatchDiff.DiffType.DELETED.equals(annotationDiff.getType()) && !isHide(annotationAddedSeverityLevel)) {
              outWriter.write(annotationAddedSeverityLevel.toString() + ": found extra annotation " + annotationDiff.getNode().toString());
              outWriter.newLine();
            }
            if (MatchDiff.DiffType.CHANGED.equals(annotationDiff.getType()) && !isHide(annotationChangedSeverityLevel)) {
              outWriter.write(annotationChangedSeverityLevel.toString() + ": maybe wrong annotation " + annotationDiff.getNode().toString());
              outWriter.newLine();
            }
          }
        }
        if (null != methodDiff.getParamsDiff() && !methodDiff.getParamsDiff().isEmpty()) {
          for (ParamDiff paramDiff : methodDiff.getParamsDiff()) {
            if (MatchDiff.DiffType.ADDED.equals(paramDiff.getType()) && !isHide(paramsAddedSeverityLevel)) {
              outWriter.write(paramsAddedSeverityLevel.toString() + ": need add new parameter in method " + paramDiff.getNode().toString());
              outWriter.newLine();
            }
            if (MatchDiff.DiffType.DELETED.equals(paramDiff.getType()) && !isHide(paramsDeletedSeverityLevel)) {
              outWriter.write(paramsDeletedSeverityLevel.toString() + ": found extra parameter " + paramDiff.getNode().toString());
              outWriter.newLine();
            }
            if (TYPE_CHANGED.equals(paramDiff.getType()) && !isHide(paramsChangedSeverityLevel)) {
              outWriter.write(paramsChangedSeverityLevel.toString() + ": parameter type changed " + paramDiff.getNode().toString());
              outWriter.newLine();
            }
          }
        }
        outWriter.newLine();
      }
    }
    outWriter.flush();
  }

  private boolean isHide(SeverityLevel level) {
    return (level.ordinal() < currentSeverityLevel.ordinal());
  }

  public void setCurrentSeverityLevel(SeverityLevel currentSeverityLevel) {
    this.currentSeverityLevel = currentSeverityLevel;
  }
}
