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
package au.com.integradev.delphi.checks;

import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.checks.verifier.CheckVerifier;
import org.junit.jupiter.api.Test;

class UnusedGlobalVariableCheckTest {
  @Test
  void testUsedGlobalConstantShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedGlobalVariableCheck())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("var")
                .appendDecl("  Foo: Integer;")
                .appendImpl("procedure SetFoo;")
                .appendImpl("begin")
                .appendImpl("  Foo := 123;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testUnusedGlobalConstantShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedGlobalVariableCheck())
        .onFile(
            new DelphiTestUnitBuilder() //
                .appendDecl("var")
                .appendDecl("  Foo: Integer; // Noncompliant"))
        .verifyIssues();
  }

  @Test
  void testUnusedAutoCreateFormVarShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new UnusedGlobalVariableCheck())
        .withSearchPathUnit(createVclForms())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendDecl("uses")
                .appendDecl("  Vcl.Forms;")
                .appendDecl("type")
                .appendDecl("  TFooForm = class(TForm)")
                .appendDecl("  end;")
                .appendDecl("var")
                .appendDecl("  Foo: TFooForm;"))
        .verifyNoIssues();
  }

  private static DelphiTestUnitBuilder createVclForms() {
    return new DelphiTestUnitBuilder()
        .unitName("Vcl.Forms")
        .appendDecl("uses")
        .appendDecl("  System.Classes;")
        .appendDecl("type")
        .appendDecl("TForm = class(TComponent)")
        .appendDecl("end;");
  }
}
