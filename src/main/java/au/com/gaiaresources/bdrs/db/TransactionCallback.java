package au.com.gaiaresources.bdrs.db;

import org.springframework.transaction.TransactionStatus;

public abstract class TransactionCallback<C> implements
        org.springframework.transaction.support.TransactionCallback {

    public abstract C doInTransaction(TransactionStatus status);
}
