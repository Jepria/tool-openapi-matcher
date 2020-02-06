package org.jepria.tools.matcher;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseStart;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StreamProvider;
import com.github.javaparser.StringProvider;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.jepria.tools.matcher.diffTypes.AnnotationDiff;
import org.jepria.tools.matcher.diffTypes.MatchDiff;
import org.jepria.tools.matcher.diffTypes.MethodDiff;
import org.jepria.tools.matcher.diffTypes.ParamDiff;

class YamlJavaMatcher {

  private ArrayList<String> annotationIgnoreNames = new ArrayList<>();

  public YamlJavaMatcher() {
    annotationIgnoreNames.add("Pattern");
    annotationIgnoreNames.add("Path");
  }

  public List<MethodDiff> match(InputStream base, InputStream spec) throws IOException {
    ParserConfiguration parserConfiguration = new ParserConfiguration().setLexicalPreservationEnabled(true);

    CompilationUnit cuBase = new JavaParser(parserConfiguration).parse(ParseStart.COMPILATION_UNIT, new StreamProvider(base)).getResult().get();
    CompilationUnit cuSpec = new JavaParser(parserConfiguration).parse(ParseStart.COMPILATION_UNIT, new StreamProvider(spec)).getResult().get();
    return matchCu(cuBase, cuSpec);
  }

  public List<MethodDiff> match(String base, String spec) {
    ParserConfiguration parserConfiguration = new ParserConfiguration().setLexicalPreservationEnabled(true);

    CompilationUnit cuBase = new JavaParser(parserConfiguration).parse(ParseStart.COMPILATION_UNIT, new StringProvider(base)).getResult().get();
    CompilationUnit cuSpec = new JavaParser(parserConfiguration).parse(ParseStart.COMPILATION_UNIT, new StringProvider(spec)).getResult().get();

    return matchCu(cuBase, cuSpec);
  }

  private List<MethodDiff> matchCu(CompilationUnit cuBase, CompilationUnit cuSpec) {
    //check @Path annotations in classes
    if (((!cuBase.getTypes().isEmpty()) && !cuSpec.getTypes().isEmpty()) && !cuBase.getTypes().get(0).getAnnotationByName("Path").equals(cuSpec.getTypes().get(0).getAnnotationByName("Path"))) {
      return null;
    }

    List<MethodDeclaration> baseMethods = findMethods(cuBase);
    List<MethodDeclaration> yamlMethods = findMethods(cuSpec);

    // pick up Jersey methods
    List<JerseyMethodDeclaration> baseJerseyMethodsList = JerseyMethodDeclaration.findJerseyMethods(baseMethods);
    List<JerseyMethodDeclaration> yamlJerseyMethodsList = JerseyMethodDeclaration.findJerseyMethods(yamlMethods);

    return compareJerseyMethodsLists(baseJerseyMethodsList, yamlJerseyMethodsList);
  }


  /**
   * @return list of methods in Java file (maybe use CompilationUnit of JavaParser lb)
   */
  private List<MethodDeclaration> findMethods(Node unit) {
    List<MethodDeclaration> result = new ArrayList<MethodDeclaration>();
    for (Node node : unit.getChildNodes()) {
      if (node instanceof MethodDeclaration) {
        result.add((MethodDeclaration) node);
      } else if (node instanceof ClassOrInterfaceDeclaration) {
        result.addAll(findMethods(node));
      }
    }
    return result;
  }

  /**
   * Функция сравнивает все методы из 2-х списков. Считается, что это один и тот же метод, если у них одинаковое название. Поиск всех методов из 2-го списка, которым нет соответствия во 1-м. Для
   * методов, которым нашлось соответствие, печатаем разницу в аннотациях и параметрах
   *
   * @param oldMethodList список методов
   * @param newMethodList список методов
   * @return
   */
  private List<MethodDiff> compareMethodsLists(List<MethodDeclaration> oldMethodList, List<MethodDeclaration> newMethodList) {
    List<MethodDiff> diffList = new ArrayList<>();

    for (MethodDeclaration newMethod : newMethodList) {
      boolean findMath = false;
      for (MethodDeclaration oldMethod : oldMethodList) {
        if (oldMethod.getNameAsString().equals(newMethod.getNameAsString())) {
          findMath = true;
          diffList.add(matchMethods(oldMethod, newMethod));
        }
      }
      if (!findMath) {
        diffList.add(new MethodDiff(MethodDiff.DiffType.ADDED, newMethod));
//                System.out.println("Method " + newMethod.getName() + " added.");
      }
    }

    return diffList.isEmpty() ? null : diffList;
  }

  /**
   * Сверяем аннотации и параметры методов
   *
   * @param oldMethod Старый метод
   * @param newMethod Новый метод
   */
  private MethodDiff matchMethods(MethodDeclaration oldMethod, MethodDeclaration newMethod) {
    MethodDiff diff = null;

    List<AnnotationDiff> annotationsDiff = compareAnnotationList(oldMethod.getAnnotations(), newMethod.getAnnotations());
    List<ParamDiff>      paramsDiff      = compareParameterList(oldMethod.getParameters(), newMethod.getParameters());
    if (null != annotationsDiff || null != paramsDiff) {
      diff = new MethodDiff(MethodDiff.DiffType.CHANGED, newMethod);
      diff.setAnnotationsDiff(annotationsDiff);
      diff.setParamsDiff(paramsDiff);
    }

    return diff;
  }

  /**
   * Функция сравнивает списки аннотаций. Все новые аннотации должны быть добавлены. А вот старые могут и не мешать?
   *
   * @param oldAnnotationList Старый список аннотаций.
   * @param newAnnotationList Новые аннотации.
   * @return
   */
  private List<AnnotationDiff> compareAnnotationList(NodeList<AnnotationExpr> oldAnnotationList, NodeList<AnnotationExpr> newAnnotationList) {
    List<AnnotationDiff> annotationDiffList = new ArrayList<>();
    for (AnnotationExpr annotationOld : oldAnnotationList) {

      if (isIgnoredAnnotation(annotationOld)) {
        continue;
      }

      boolean isContains = false;

      for (AnnotationExpr annotationNew : newAnnotationList) {
        if (annotationOld.getName().equals(annotationNew.getName())) {
          isContains = true;
          if (!compareAnnotation(annotationOld, annotationNew)) {
            annotationDiffList.add(new AnnotationDiff(MatchDiff.DiffType.CHANGED, annotationNew));
//                        System.err.println(annotationOld.getBegin().get() + ": " + "Annotation " + annotationOld + " changed to " + annotationNew + ".");
          }
          break;
        }
      }

      if (!isContains) {
//                System.err.println(annotationOld.getBegin().get() + ": " + "Annotation " + annotationOld + " deleted.");
        annotationDiffList.add(new AnnotationDiff(MatchDiff.DiffType.DELETED, annotationOld)); // возможно это не нужно
      }
    }

    for (AnnotationExpr annotationNew : newAnnotationList) {

      if (isIgnoredAnnotation(annotationNew)) {
        continue;
      }

      boolean isContains = false;
      for (AnnotationExpr annotationOld : oldAnnotationList) {
        if (annotationOld.getName().equals(annotationNew.getName())) {
          isContains = true;
          break;
        }
      }
      if (!isContains) {
//                System.err.println(annotationNew.getBegin().get() + ": " + "Annotation " + annotationNew + " added.");
        annotationDiffList.add(new AnnotationDiff(MatchDiff.DiffType.ADDED, annotationNew));
      }
    }
    return annotationDiffList.isEmpty() ? null : annotationDiffList;  // нет смысла возвращать пустой список
  }

  private Boolean compareAnnotation(AnnotationExpr oldAnnotation, AnnotationExpr newAnnotation) {
    boolean result = false;
    if (oldAnnotation.equals(newAnnotation)) {
      result = true;
    }
    return result;
  }

  /**
   * Функция сравнивает списки параметров. Лишних параметров быть не должно. Все новые параметры должны быть. У параметров имеет смысл порядок. Сравнение необходимо делать просто по порядку. Параметры
   * подлежат сравнению, если у них одинаковый порядковый номер.
   *
   * @param oldParams параметры, которые были
   * @param newParams параметры, к которым необходимо привести старые
   * @return
   */
  private List<ParamDiff> compareParameterList(NodeList<Parameter> oldParams, NodeList<Parameter> newParams) {
    List<ParamDiff> paramDiffArrayList = new ArrayList<>();

    for (int i = 0; (i < newParams.size()) && (i < oldParams.size()); i++) {
      ParamDiff paramDiff = compareParameter(oldParams.get(i), newParams.get(i));
      if (null != paramDiff) {
        paramDiffArrayList.add(paramDiff);
      }
    }

    if (newParams.size() > oldParams.size()) {
      for (int i = oldParams.size(); i < newParams.size(); i++) {
        paramDiffArrayList.add(new ParamDiff(MatchDiff.DiffType.ADDED, newParams.get(i)));
      }
    } else if (oldParams.size() > newParams.size()) {
      for (int i = newParams.size(); i < oldParams.size(); i++) {
        paramDiffArrayList.add(new ParamDiff(MatchDiff.DiffType.DELETED, oldParams.get(i)));
      }
    }

    return paramDiffArrayList.isEmpty() ? null : paramDiffArrayList;
  }

  /**
   * Сравниваем параметры. Название у параметров не имеет значения. Если тип параметра не совпадает, то аннотации сравнивать уже нет смысла. Сравниваем тип параметров и аннотации.
   *
   * @param oldParameter
   * @param newParameter
   * @return
   */
  private ParamDiff compareParameter(Parameter oldParameter, Parameter newParameter) {
    ParamDiff paramDiff = null;
    if (!oldParameter.getType().equals(newParameter.getType())) {
//            System.err.println(oldParameter.getParentNode().get().getBegin().get() + ": " + ((MethodDeclaration) oldParameter.getParentNode().get()).getName() + "Parameters type changed");
      paramDiff = new ParamDiff(MatchDiff.DiffType.TYPE_CHANGED, newParameter);
    } else {
      List<AnnotationDiff> annotationDiffList = compareAnnotationList(oldParameter.getAnnotations(), newParameter.getAnnotations());
      if (null != annotationDiffList) {
        paramDiff = new ParamDiff(MatchDiff.DiffType.CHANGED, newParameter);
        paramDiff.setAnnotationsDiff(annotationDiffList);
      }
    }
    return paramDiff;
  }

  /**
   * Функция сравнивает все методы из 2-х списков Методы считаются одинаковыми, если у них одинаковое значение аннотации @Path и у них одинаковая аннотация типа http запроса (например @GET и
   *
   * @param oldMethodList список методов
   * @param newMethodList список методов
   * @return
   * @Path("/path") - есть у обоих) Печатаем все методы из 2-го списка, которым нет соответствия во 1-м. Для методов, которым нашлось соответствие, печатаем разницу в аннотациях и параметрах
   */
  private List<MethodDiff> compareJerseyMethodsLists(List<JerseyMethodDeclaration> oldMethodList, List<JerseyMethodDeclaration> newMethodList) {
    List<MethodDiff> diffList = new ArrayList<>();

    for (JerseyMethodDeclaration newMethod : newMethodList) {
      boolean findMatch = false;
      for (JerseyMethodDeclaration oldMethod : oldMethodList) {
        if (newMethod.isSameMethod(oldMethod)) {
          findMatch = true;
          MethodDiff methodDiff = matchMethods(oldMethod.methodDeclaration, newMethod.methodDeclaration);
          if (null != methodDiff) {
            diffList.add(methodDiff);
          }
        }
      }
      if (!findMatch) {
        diffList.add(new MethodDiff(MethodDiff.DiffType.ADDED, newMethod.methodDeclaration));
//                System.out.println("Method " + newMethod.methodDeclaration.getNameAsString() + " added.");
      }
    }

    return diffList;
  }

  boolean isIgnoredAnnotation(AnnotationExpr annotationExpr) {
    return annotationIgnoreNames.contains(annotationExpr.getName().getIdentifier());
  }

}
