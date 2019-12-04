package org.sonar.plugins.delphi.type;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.Collectors;
import org.sonar.plugins.delphi.type.Type.ArrayConstructorType;

public class DelphiArrayConstructorType extends DelphiType implements ArrayConstructorType {
  private final ImmutableList<Type> elementTypes;

  private DelphiArrayConstructorType(List<Type> elementTypes) {
    super(createImage(elementTypes));
    this.elementTypes = ImmutableList.copyOf(elementTypes);
  }

  public static ArrayConstructorType arrayConstructor(List<Type> elementTypes) {
    return new DelphiArrayConstructorType(elementTypes);
  }

  @Override
  public List<Type> elementTypes() {
    return elementTypes;
  }

  @Override
  public boolean isArrayConstructor() {
    return true;
  }

  private static String createImage(List<Type> elementTypes) {
    return "[" + elementTypes.stream().map(Type::getImage).collect(Collectors.joining(",")) + "]";
  }
}
