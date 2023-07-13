/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package au.com.integradev.delphi.antlr.ast.node;

import au.com.integradev.delphi.antlr.ast.visitors.DelphiParserVisitor;
import au.com.integradev.delphi.type.factory.TypeFactoryImpl;
import au.com.integradev.delphi.type.parameter.FormalParameter;
import java.util.Collections;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.AnonymousMethodNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodParametersNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodReturnTypeNode;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodKind;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

public final class AnonymousMethodNodeImpl extends ExpressionNodeImpl
    implements AnonymousMethodNode {
  private String image;
  private MethodKind methodKind;

  public AnonymousMethodNodeImpl(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public MethodParametersNode getMethodParametersNode() {
    return getFirstChildOfType(MethodParametersNode.class);
  }

  @Override
  public MethodReturnTypeNode getReturnTypeNode() {
    return getFirstChildOfType(MethodReturnTypeNode.class);
  }

  @Override
  public Type getReturnType() {
    return getReturnTypeNode().getTypeNode().getType();
  }

  @Override
  public MethodKind getMethodKind() {
    if (methodKind == null) {
      methodKind = MethodKind.fromTokenType(getTokenType());
    }
    return methodKind;
  }

  @Override
  public boolean isFunction() {
    return getMethodKind() == MethodKind.FUNCTION;
  }

  @Override
  public String getImage() {
    if (image == null) {
      image = getToken().getImage() + getParameterSignature() + getReturnTypeSignature();
    }
    return image;
  }

  private String getParameterSignature() {
    MethodParametersNode parameters = getMethodParametersNode();
    return parameters != null ? getMethodParametersNode().getImage() : "";
  }

  private String getReturnTypeSignature() {
    MethodReturnTypeNode returnType = getReturnTypeNode();
    return returnType != null ? (" : " + getReturnTypeNode().getTypeNode().getImage()) : "";
  }

  @Override
  @Nonnull
  protected Type createType() {
    MethodParametersNode parameters = getMethodParametersNode();
    MethodReturnTypeNode returnTypeNode = getReturnTypeNode();

    return ((TypeFactoryImpl) getTypeFactory())
        .anonymous(
            parameters == null
                ? Collections.emptyList()
                : parameters.getParameters().stream()
                    .map(FormalParameter::create)
                    .collect(Collectors.toUnmodifiableList()),
            returnTypeNode == null
                ? TypeFactory.voidType()
                : returnTypeNode.getTypeNode().getType());
  }
}
