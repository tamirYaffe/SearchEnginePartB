package SearchEngineTools.ParsingTools.Term;

public class FractionTerm extends ATerm {

    private NumberTerm numerator;
    private NumberTerm denominator;

    NumberTerm getDenominator() {
        return denominator;
    }

    NumberTerm getNumerator() {
        return numerator;
    }

    /**
     * Constructor for Fraction Term Clas
     * @param numerator numerator
     * @param denominator denominator
     */
    public FractionTerm(NumberTerm numerator, NumberTerm denominator){
        this.numerator = numerator;
        this.denominator = denominator;
        isNumber = true;
        addPositions(numerator);
    }


    @Override
    protected String createTerm() {
        return numerator.getTerm()+"/"+denominator.getTerm();
    }
}
