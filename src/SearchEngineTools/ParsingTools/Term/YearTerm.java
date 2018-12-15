package SearchEngineTools.ParsingTools.Term;

public class YearTerm extends ATerm {

    private int month;
    private int year;

    /**
     * Constructor For the YearTerm class
     * @param month month of year
     * @param year year
     */
    public YearTerm(int month,int year){
        this.month=month;
        this.year=year;
        isNumber=false;
    }

    @Override
    protected String createTerm() {
        return year+"-"+month;
    }
}
