package cn.model.lexical;

public class tokenStr {
  private int level;// 行数
  private String catagory;
  private String value;

  public tokenStr(int l, String c, String v) {
    this.setLevel(l);
    this.setCatagory(c);
    this.setValue(v);
  }

  public int getLevel() {
    return level;
  }

  public void setLevel(int level) {
    this.level = level;
  }

  public String getCatagory() {
    if(catagory == null){
      return value;
    }
    return catagory;
  }

  public void setCatagory(String catagory) {
    this.catagory = catagory;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "{" + this.level + "  " + this.catagory + "  " + this.value + "}";
  }

}
