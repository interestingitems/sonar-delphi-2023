unit MethodResolution;

interface

{** documented class **}
type
  TFoo = class(TObject)
  public
    procedure Bar;
  end;

  TMetaFoo = class of TFoo;

implementation

procedure Test(Baz: TMetaFoo);
begin
  Baz.Bar;
end;

end.