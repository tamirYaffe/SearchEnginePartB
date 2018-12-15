package Test;

import eu.fayder.restcountries.v1.domain.Country;
import eu.fayder.restcountries.v1.rest.CountryService;

public class MergeBlocksTest {
    public static void main(String[] args) {
        for(Country country: CountryService.getInstance().getAll())
            System.out.println(country.getCapital());
    }
}
