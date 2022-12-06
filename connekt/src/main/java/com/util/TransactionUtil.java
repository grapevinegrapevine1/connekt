package com.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Service
public class TransactionUtil {

	@Autowired private PlatformTransactionManager transactionManager;
	
	public TransactionStatus getTrans() {
		TransactionDefinition trans = new DefaultTransactionDefinition();
		return transactionManager.getTransaction(trans);
	}
	
	public void commit(TransactionStatus stat) {
		// コミット
		transactionManager.commit(stat);
	}
	
	public void rollback(TransactionStatus stat) {
		// ロールバック
		transactionManager.rollback(stat);
	}
}
