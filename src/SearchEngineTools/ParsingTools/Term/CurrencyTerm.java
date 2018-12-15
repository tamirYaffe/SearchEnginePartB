package SearchEngineTools.ParsingTools.Term;

import java.util.List;

public class CurrencyTerm extends ATerm {

    protected NumberTerm numberTerm;
    protected String currency;

    /**
     * Value of currency
     * @param numberTerm number that represents currency
     * @param currency name of currency
     */
    public CurrencyTerm(NumberTerm numberTerm, String currency){
        this.numberTerm = numberTerm;
        this.currency = currency;
        isNumber=true;
        addPositions(numberTerm);
    }

    /**
     * String that represents number
     * @return
     */
    protected String getValueTermString(){
        List<Character> numberWithoutDecimal = numberTerm.getNumberWithoutDecimal();
        String afterNumber = "";
        int digitsBeforeDecimal = numberTerm.isWholeNumber() ? numberWithoutDecimal.size() : numberTerm.getLocationOfDecimal();
        if(digitsBeforeDecimal >= 7){
            digitsBeforeDecimal = digitsBeforeDecimal - 6;
            afterNumber = " M";
        }
        int digitsToPrint = digitsBeforeDecimal==numberWithoutDecimal.size() ? numberWithoutDecimal.size() : numberWithoutDecimal.size()+1;
        StringBuilder term = new StringBuilder();
        for (int i = 0, j=0; i < digitsToPrint; i++) {
            if(i==digitsBeforeDecimal){
                term.append(".");
            }
            else {
                term.append(numberWithoutDecimal.get(j));
                j++;
            }
        }

        String beforeNumber = numberTerm.isNegative() ? "-" : "";
        String toReturn = digitsBeforeDecimal==numberWithoutDecimal.size() ? beforeNumber+term.toString()+afterNumber : beforeNumber+removeUnnecessaryDigits(term)+afterNumber;
        return toReturn;
    }

    @Override
    protected String createTerm() {
        return getValueTermString()+" "+currency;
    }

    private static String removeUnnecessaryDigits(CharSequence s){
        int lastNecessaryIndx = s.length()-1;
        boolean necessary = false;
        while (!necessary && lastNecessaryIndx>0){
            if(s.charAt(lastNecessaryIndx)=='0'){
                lastNecessaryIndx--;
            }
            else if(s.charAt(lastNecessaryIndx)=='.'){
                lastNecessaryIndx--;
                necessary=true;
            }
            else {
                necessary = true;
            }
        }
        String toReturn = s.toString();
        return toReturn.substring(0,lastNecessaryIndx+1);
    }

    /**
     * Multiply Term by value
     * @param value
     */
    public void multiply(Value value){
        numberTerm.multiply(value);
    }
}
