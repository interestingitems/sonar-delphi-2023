package org.sonar.plugins.delphi.symbol.resolve;

import static java.util.function.Predicate.not;

import java.util.List;
import org.sonar.plugins.delphi.symbol.declaration.ParameterDeclaration;
import org.sonar.plugins.delphi.type.Type;

public interface Invocable {

  String getName();

  List<ParameterDeclaration> getParameters();

  Type getReturnType();

  boolean isCallable();

  boolean isClassInvocable();

  default int getParametersCount() {
    return getParameters().size();
  }

  default int getRequiredParametersCount() {
    return (int)
        getParameters().stream().filter(not(ParameterDeclaration::hasDefaultValue)).count();
  }

  default ParameterDeclaration getParameter(int index) {
    return getParameters().get(index);
  }

  default boolean hasSameParameterTypes(Invocable other) {
    if (getParametersCount() != other.getParametersCount()) {
      return false;
    }

    for (int i = 0; i < getParametersCount(); ++i) {
      if (!getParameter(i).getType().is(other.getParameter(i).getType())) {
        return false;
      }
    }

    return true;
  }
}
