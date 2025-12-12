package albr.com.tax.exception;

import org.springframework.dao.DataAccessException;

public class ProductCreationException extends Throwable {
    public ProductCreationException(String unableToCreateProduct, DataAccessException ex) {
    }
}
