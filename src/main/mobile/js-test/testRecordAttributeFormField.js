// jasmine test

describe("testAttributeValueFormField", function() {
    it("should always pass", function() {
        expect(true);
    });
  
    it("should assign the attribute id", function() {
        var attr = {
            id: "hello"
        };
        var recAttr = new bdrs.mobile.attribute.AttributeValueFormField(attr);
        expect(recAttr.getAttributeInputSelector()).toEqual("#record-attr-hello");
    });
});