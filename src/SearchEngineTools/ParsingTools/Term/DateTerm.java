package SearchEngineTools.ParsingTools.Term;

public class DateTerm extends ATerm {

    private int day;
    private int month;

    /**
     * Constructor for DateTerm class
     * @param month
     * @param day
     */
    public DateTerm (int month, int day){
        this.day = day;
        this.month = month;
        isNumber=false;
    }


    @Override
    protected String createTerm() {
        return month+"-"+day;
    }
}
