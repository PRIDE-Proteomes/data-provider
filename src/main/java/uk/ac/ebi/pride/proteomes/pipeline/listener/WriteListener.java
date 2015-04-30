package uk.ac.ebi.pride.proteomes.pipeline.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.ItemWriteListener;
import uk.ac.ebi.pride.proteomes.db.core.api.protein.Protein;

import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: noedelta
 * Date: 14/10/2013
 * Time: 23:03
 */
public class WriteListener implements ItemWriteListener<Protein> {

	private static final Log log = LogFactory.getLog(WriteListener.class);


	int itemWritten = 0;

	public void beforeWrite(List items) {
		log.info("BeforeWriteListener - " + items.size() + " - " + itemWritten);
	}

	public void afterWrite(List items) {
		itemWritten += items.size();
		log.info("AfterWriteListener - " + items.size() + " - " + itemWritten);
	}

	public void onWriteError(Exception exception, List items) {
		log.error("OnWriteErrorWriteListener - " + Arrays.toString(items.toArray()) + " " + exception.getMessage());
		log.error(exception.getStackTrace());

	}
}
