package SearchEngineTools.ParsingTools;

public class Token {

    private String tokenString;
    private int position;

    public Token(String s){
        this.tokenString = s;
        position = -1;
    }

    public Token(String s, int position){
        tokenString = s;
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public String getTokenString(){
        return tokenString;
    }

    public void setTokenString(String tokenString){
        this.tokenString=tokenString;
    }

    public void setPosition(int position){
        this.position=position;
    }

    public String toString(){
        return "Token: "+tokenString + (position==-1 ? "" : "Position: " + position);
    }

    public boolean equals(Object o){
        if(o!=null && o instanceof Token){
            Token other = (Token) o;
            if(position==(other.getPosition())){
                if(this.tokenString==null){
                    return other.tokenString==null;
                }
                else if(other.tokenString==null){
                    return false;
                }
                else {
                    return this.tokenString.equals(other.tokenString);
                }
            }
        }
        return false;
    }

    public boolean stringsAreEqual(Token other){
        return this.tokenString.equals(tokenString);
    }

    public boolean areInSameLocation(Token other){
        return position==other.position;
    }
}