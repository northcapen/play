package controllers;

import models.Customer;
import models.IIN;
import models.CustomerKotlin;
import models.AccountKotlin;
import org.apache.commons.lang.StringUtils;
import play.mvc.Controller;

public class Application extends Controller {

    public static void index() {
        Customer customer = new Customer(StringUtils.upperCase("Betty"));
        CustomerKotlin customerKotlin = new CustomerKotlin("Thomas", new IIN("123"));
        AccountKotlin accountKotlin = new AccountKotlin();
        String output = customer.name + ". " + customerKotlin.name() + ". " + accountKotlin;
        render(output);
    }
}
