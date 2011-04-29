package au.com.gaiaresources.bdrs.db;

import org.springframework.transaction.TransactionStatus;

public class TransactionCallbackWithSuccessOrFail extends TransactionCallback<Boolean> {
    @Override
    public Boolean doInTransaction(TransactionStatus status) {
        return Boolean.TRUE;
    }
}
