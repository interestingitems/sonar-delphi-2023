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
import au.com.integradev.delphi.symbol.DelphiNameOccurrence;
import au.com.integradev.delphi.symbol.Qualifiable;
import au.com.integradev.delphi.symbol.QualifiedName;
import au.com.integradev.delphi.symbol.declaration.DelphiNameDeclaration;
import au.com.integradev.delphi.symbol.declaration.TypeNameDeclaration;
import au.com.integradev.delphi.symbol.declaration.TypedDeclaration;
import au.com.integradev.delphi.type.DelphiType;
import au.com.integradev.delphi.type.Type;
import au.com.integradev.delphi.type.Typed;
import java.util.ArrayList;
import java.util.List;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;

public final class NameReferenceNode extends DelphiNode implements Qualifiable, Typed {
  private DelphiNameDeclaration declaration;
  private DelphiNameOccurrence occurrence;
  private List<NameReferenceNode> names;
  private QualifiedName qualifiedName;

  public NameReferenceNode(Token token) {
    super(token);
  }

  public NameReferenceNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public List<NameReferenceNode> flatten() {
    if (names == null) {
      names = new ArrayList<>();
      NameReferenceNode name = this;
      while (name != null) {
        names.add(name);
        name = name.nextName();
      }
    }
    return names;
  }

  public IdentifierNode getIdentifier() {
    return (IdentifierNode) jjtGetChild(0);
  }

  @Override
  public QualifiedName getQualifiedName() {
    if (qualifiedName == null) {
      List<String> nameParts = new ArrayList<>();
      StringBuilder builder = new StringBuilder();

      for (NameReferenceNode part : flatten()) {
        builder.append(part.getIdentifier().getImage());

        GenericArgumentsNode generic = part.getGenericArguments();
        if (generic != null) {
          builder.append(generic.getImage());
        }

        nameParts.add(builder.toString());
        builder.setLength(0);
      }

      qualifiedName = new QualifiedName(nameParts);
    }
    return qualifiedName;
  }

  public GenericArgumentsNode getGenericArguments() {
    Node generic = jjtGetChild(1);
    return (generic instanceof GenericArgumentsNode) ? (GenericArgumentsNode) generic : null;
  }

  public NameReferenceNode prevName() {
    Node parent = jjtGetParent();
    return (parent instanceof NameReferenceNode) ? (NameReferenceNode) parent : null;
  }

  public NameReferenceNode nextName() {
    Node child = jjtGetChild(jjtGetNumChildren() - 1);
    return (child instanceof NameReferenceNode) ? (NameReferenceNode) child : null;
  }

  @Override
  public String getImage() {
    return fullyQualifiedName();
  }

  public void setNameOccurrence(DelphiNameOccurrence occurrence) {
    this.occurrence = occurrence;
  }

  public DelphiNameOccurrence getNameOccurrence() {
    return occurrence;
  }

  public DelphiNameDeclaration getNameDeclaration() {
    if (declaration == null && getNameOccurrence() != null) {
      declaration = getNameOccurrence().getNameDeclaration();
    }
    return declaration;
  }

  @Override
  @NotNull
  public Type getType() {
    NameDeclaration typedDeclaration;

    if (isExplicitArrayConstructorInvocation()) {
      List<NameReferenceNode> flatNames = flatten();
      typedDeclaration = flatNames.get(flatNames.size() - 2).getNameDeclaration();
    } else {
      typedDeclaration = getLastName().getNameDeclaration();
    }

    if (typedDeclaration instanceof TypedDeclaration) {
      return ((Typed) typedDeclaration).getType();
    } else {
      return DelphiType.unknownType();
    }
  }

  public NameReferenceNode getLastName() {
    List<NameReferenceNode> flatNames = flatten();
    return flatNames.get(flatNames.size() - 1);
  }

  public boolean isExplicitArrayConstructorInvocation() {
    List<NameReferenceNode> flatNames = flatten();
    if (flatNames.size() > 1) {
      NameDeclaration arrayDeclaration = flatNames.get(flatNames.size() - 2).getNameDeclaration();
      if (arrayDeclaration instanceof TypeNameDeclaration) {
        Type type = ((Typed) arrayDeclaration).getType();
        return type.isArray() && getLastName().hasImageEqualTo("Create");
      }
    }
    return false;
  }
}