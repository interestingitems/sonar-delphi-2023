/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
import javax.annotation.Nullable;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodParametersNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodReturnTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.ProcedureTypeHeadingNode;

public final class ProcedureTypeHeadingNodeImpl extends DelphiNodeImpl
    implements ProcedureTypeHeadingNode {
  public ProcedureTypeHeadingNodeImpl(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public String getImage() {
    return super.getImage() + getParameterSignature();
  }

  private String getParameterSignature() {
    MethodParametersNode parameters = getMethodParametersNode();
    return parameters != null ? parameters.getImage() : "";
  }

  @Override
  @Nullable
  public MethodParametersNode getMethodParametersNode() {
    DelphiNode node = getChild(0);
    return (node instanceof MethodParametersNode) ? (MethodParametersNode) node : null;
  }

  @Override
  @Nullable
  public MethodReturnTypeNode getMethodReturnTypeNode() {
    DelphiNode node = getChild(hasMethodParametersNode() ? 1 : 0);
    return (node instanceof MethodReturnTypeNode) ? (MethodReturnTypeNode) node : null;
  }

  @Override
  public boolean hasMethodParametersNode() {
    return getMethodParametersNode() != null;
  }
}
