/*
 * Sonar Delphi Plugin
 * Copyright (C) 2015 Fabricio Colombo
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

class ManualEnumeratorLoopCheckTest {
  private DelphiTestUnitBuilder enumeratorUnit() {
    return new DelphiTestUnitBuilder()
        .unitName("MyEnumerable")
        .appendDecl("type")
        .appendDecl("  TEnumerator = class(TInterfacedObject, System.IEnumerator)")
        .appendDecl("    function GetCurrent: TObject;")
        .appendDecl("    function MoveNext: Boolean;")
        .appendDecl("    procedure Reset;")
        .appendDecl("    property Current: TObject read GetCurrent;")
        .appendDecl("  end;")
        .appendDecl("  TEnumerable = class(TInterfacedObject, System.IEnumerable)")
        .appendDecl("    FCount: Integer;")
        .appendDecl("    property Count: Integer read FCount;")
        .appendDecl("    function GetEnumerator: TEnumerator;")
        .appendDecl("  end;");
  }

  private DelphiTestUnitBuilder enumeratorUnitMultiLevel() {
    return new DelphiTestUnitBuilder()
        .unitName("MyEnumerable")
        .appendDecl("type")
        .appendDecl("  TEnumerator = class(TInterfacedObject, System.IEnumerator)")
        .appendDecl("    function GetCurrent: TObject;")
        .appendDecl("    function MoveNext: Boolean;")
        .appendDecl("    procedure Reset;")
        .appendDecl("    property Current: TObject read GetCurrent;")
        .appendDecl("  end;")
        .appendDecl("  TIntermediateEnumerable = class")
        .appendDecl("    FCount: Integer;")
        .appendDecl("    property Count: Integer read FCount;")
        .appendDecl("  end;")
        .appendDecl("  TEnumerable = class(TInterfacedObject, System.IEnumerable)")
        .appendDecl("    function GetEnumerator: TEnumerator;")
        .appendDecl("  end;");
  }

  private DelphiTestUnitBuilder implicitEnumeratorUnit() {
    return new DelphiTestUnitBuilder()
        .unitName("MyImplicitEnumerable")
        .appendDecl("type")
        .appendDecl("  TEnumerator = class")
        .appendDecl("    function GetCurrent: TObject;")
        .appendDecl("    function MoveNext: Boolean;")
        .appendDecl("    procedure Reset;")
        .appendDecl("    property Current: TObject read GetCurrent;")
        .appendDecl("  end;")
        .appendDecl("  TEnumerable = class")
        .appendDecl("    FCount: Integer;")
        .appendDecl("    property Count: Integer read FCount;")
        .appendDecl("    function GetEnumerator: TEnumerator;")
        .appendDecl("  end;");
  }

  @Test
  void testIndexBasedForLoopWithoutUseOfIndexShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ManualEnumeratorLoopCheck())
        .withSearchPathUnit(enumeratorUnit())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  MyEnumerable;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("  E: TEnumerable;")
                .appendImpl("begin")
                .appendImpl("  for I := 0 to E.Count - 1 do begin")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testIndexBasedForLoopWithHardCastShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ManualEnumeratorLoopCheck())
        .withSearchPathUnit(enumeratorUnit())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  MyEnumerable;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("  E: TEnumerable;")
                .appendImpl("  O: TObject;")
                .appendImpl("begin")
                .appendImpl("  for I := 0 to E.Count - 1 do begin")
                .appendImpl("    O := TObject(E[I]);")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testIndexBasedForLoopWithSoftCastShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ManualEnumeratorLoopCheck())
        .withSearchPathUnit(enumeratorUnit())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  MyEnumerable;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("  E: TEnumerable;")
                .appendImpl("  O: TObject;")
                .appendImpl("begin")
                .appendImpl("  for I := 0 to E.Count - 1 do begin")
                .appendImpl("    O := E[I] as TObject;")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testIndexBasedForLoopOverExplicitEnumeratorShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ManualEnumeratorLoopCheck())
        .withSearchPathUnit(enumeratorUnit())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  MyEnumerable;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("  E: TEnumerable;")
                .appendImpl("  O: TObject;")
                .appendImpl("begin")
                .appendImpl("  for I := 0 to E.Count - 1 do begin")
                .appendImpl("    O := E[I];")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssueOnLine(15);
  }

  @Test
  void testIndexBasedForLoopOverImplicitEnumeratorShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ManualEnumeratorLoopCheck())
        .withSearchPathUnit(implicitEnumeratorUnit())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  MyImplicitEnumerable;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("  E: TEnumerable;")
                .appendImpl("  O: TObject;")
                .appendImpl("begin")
                .appendImpl("  for I := 0 to E.Count - 1 do begin")
                .appendImpl("    O := E[I];")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssueOnLine(15);
  }

  @Test
  void testIndexIsUsedShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ManualEnumeratorLoopCheck())
        .withSearchPathUnit(enumeratorUnit())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  MyEnumerable;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("  E: TEnumerable;")
                .appendImpl("  O: TObject;")
                .appendImpl("begin")
                .appendImpl("  for I := 0 to E.Count - 1 do begin")
                .appendImpl("    O := E[I];")
                .appendImpl("    WriteLn(I);")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testIndexBasedForLoopWithAssignmentToGlobalShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ManualEnumeratorLoopCheck())
        .withSearchPathUnit(enumeratorUnit())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  MyEnumerable;")
                .appendImpl("var")
                .appendImpl("  Global: TObject;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("  E: TEnumerable;")
                .appendImpl("begin")
                .appendImpl("  for I := 0 to E.Count - 1 do begin")
                .appendImpl("    Global := E[I];")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testIndexBasedForLoopWithAssignmentToComplexVarShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ManualEnumeratorLoopCheck())
        .withSearchPathUnit(enumeratorUnit())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  MyEnumerable;")
                .appendImpl("type")
                .appendImpl("  TObjWrapper = class")
                .appendImpl("    O: TObject;")
                .appendImpl("  end;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  W: TObjWrapper;")
                .appendImpl("  I: Integer;")
                .appendImpl("  E: TEnumerable;")
                .appendImpl("begin")
                .appendImpl("  for I := 0 to E.Count - 1 do begin")
                .appendImpl("    W.O := E[I];")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testInlineVarIndexBasedForLoopShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ManualEnumeratorLoopCheck())
        .withSearchPathUnit(enumeratorUnit())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  MyEnumerable;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  E: TEnumerable;")
                .appendImpl("  O: TObject;")
                .appendImpl("begin")
                .appendImpl("  for var I := 0 to E.Count - 1 do begin")
                .appendImpl("    O := E[I];")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssueOnLine(14);
  }

  @Test
  void testInlineVarObjectIndexBasedForLoopShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ManualEnumeratorLoopCheck())
        .withSearchPathUnit(enumeratorUnit())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  MyEnumerable;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("  E: TEnumerable;")
                .appendImpl("begin")
                .appendImpl("  for I := 0 to E.Count - 1 do begin")
                .appendImpl("    var O := E[I];")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssueOnLine(14);
  }

  @Test
  void testInlineVarObjectOutsideLoopIndexBasedForLoopShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ManualEnumeratorLoopCheck())
        .withSearchPathUnit(enumeratorUnit())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  MyEnumerable;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("  E: TEnumerable;")
                .appendImpl("begin")
                .appendImpl("  var O := nil;")
                .appendImpl("  for I := 0 to E.Count - 1 do begin")
                .appendImpl("    O := E[I];")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssueOnLine(15);
  }

  @Test
  void testCountAndEnumeratorAtDifferentLevelsOfHierarchyShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ManualEnumeratorLoopCheck())
        .withSearchPathUnit(enumeratorUnitMultiLevel())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  MyEnumerable;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  I: Integer;")
                .appendImpl("  E: TEnumerable;")
                .appendImpl("begin")
                .appendImpl("  var O := nil;")
                .appendImpl("  for I := 0 to E.Count - 1 do begin")
                .appendImpl("    O := E[I];")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssueOnLine(15);
  }

  @Test
  void testEnumeratorAccessedIndirectlyShouldAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ManualEnumeratorLoopCheck())
        .withSearchPathUnit(enumeratorUnit())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  MyEnumerable;")
                .appendImpl("type")
                .appendImpl("  TWrapper = class")
                .appendImpl("    E: TEnumerable;")
                .appendImpl("  end;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  O: TObject;")
                .appendImpl("  I: Integer;")
                .appendImpl("  W: TWrapper;")
                .appendImpl("begin")
                .appendImpl("  for I := 0 to W.E.Count - 1 do begin")
                .appendImpl("    O := W.E[I];")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyIssueOnLine(19);
  }

  @Test
  void testUnMatchingTypesShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ManualEnumeratorLoopCheck())
        .withSearchPathUnit(enumeratorUnit())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  MyEnumerable;")
                .appendImpl("type")
                .appendImpl("  TWrapper = class")
                .appendImpl("    E: TEnumerable;")
                .appendImpl("    function Count: Integer;")
                .appendImpl("  end;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  E: TEnumerable;")
                .appendImpl("  O: TObject;")
                .appendImpl("  I: Integer;")
                .appendImpl("  W: TWrapper;")
                .appendImpl("begin")
                .appendImpl("  for I := 0 to E.Count - 1 do begin")
                .appendImpl("    O := W.E[I];")
                .appendImpl("  end;")
                .appendImpl("")
                .appendImpl("  for I := 0 to W.E.Count - 1 do begin")
                .appendImpl("    O := E[I];")
                .appendImpl("  end;")
                .appendImpl("")
                .appendImpl("  for I := 0 to W.Count - 1 do begin")
                .appendImpl("    O := E[I];")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }

  @Test
  void testAssignmentNotFirstLoopBodyStatementShouldNotAddIssue() {
    CheckVerifier.newVerifier()
        .withCheck(new ManualEnumeratorLoopCheck())
        .withSearchPathUnit(enumeratorUnit())
        .onFile(
            new DelphiTestUnitBuilder()
                .appendImpl("uses")
                .appendImpl("  MyEnumerable;")
                .appendImpl("procedure Test;")
                .appendImpl("var")
                .appendImpl("  E: TEnumerable;")
                .appendImpl("  O: TObject;")
                .appendImpl("  I: Integer;")
                .appendImpl("begin")
                .appendImpl("  for I := 0 to E.Count - 1 do begin")
                .appendImpl("    Assert(1 < 2);")
                .appendImpl("    O := E[I];")
                .appendImpl("  end;")
                .appendImpl("end;"))
        .verifyNoIssues();
  }
}
