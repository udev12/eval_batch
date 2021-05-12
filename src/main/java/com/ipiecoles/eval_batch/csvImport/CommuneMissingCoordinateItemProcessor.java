package com.ipiecoles.eval_batch.csvImport;

import com.ipiecoles.eval_batch.model.Commune;
import com.ipiecoles.eval_batch.utils.OpenStreetMapUtils;
import org.springframework.batch.item.ItemProcessor;

import java.util.Map;

/**
 * Classe qui implémente l'interface "ItemProcessor".
 */
public class CommuneMissingCoordinateItemProcessor implements ItemProcessor<Commune, Commune> {
    /**
     * La méthode "process" crée la latitude et la longitude.
     *
     * @param item : objet de type "Commune".
     * @return objet de type "Commune".
     * @throws Exception : échec requête GET.
     */
    @Override
    public Commune process(Commune item) throws Exception {
        Map<String, Double> coordinatesOSM = OpenStreetMapUtils.getInstance().getCoordinates(
                item.getNom() + " " + item.getCodePostal());
        if (coordinatesOSM != null && coordinatesOSM.size() == 2) {
            item.setLongitude(coordinatesOSM.get("lon"));
            item.setLatitude(coordinatesOSM.get("lat"));
            return item;
        }
        return null;
    }
}
