package com.ipiecoles.eval_batch.dbExport;

import com.ipiecoles.eval_batch.dto.CommuneTXT;
import com.ipiecoles.eval_batch.model.Commune;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.ItemWriteListener;

import java.util.List;

/**
 * Classe qui implémente les interfaces "ItemReadListener" et "ItemWriteListener".
 * On compte entre-autres, les méthodes "beforeRead()", "afterRead()" et "beforeWrite()".
 */
public class CommuneDBItemListener implements ItemReadListener<Commune>, ItemWriteListener<CommuneTXT> {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void beforeRead() {
        logger.info("Before Read DB Import");
    }

    @Override
    public void afterRead(Commune item) {
        logger.info("After Read DB Import => " + item.toString());
    }

    @Override
    public void onReadError(Exception ex) {
        logger.error("On Read Error DB Import => " + ex.getMessage());
    }

    @Override
    public void beforeWrite(List<? extends CommuneTXT> items) {
        logger.info("Before Write DB Import");
//        logger.info("Before Write DB Import => " + items.toString());
    }

    @Override
    public void afterWrite(List<? extends CommuneTXT> items) {
        logger.info("After Write DB Import => " + items.toString());
    }

    @Override
    public void onWriteError(Exception exception, List<? extends CommuneTXT> items) {
        logger.error("On Write Error DB Import => " + exception.getMessage() + " " + items.toString());
    }
}
