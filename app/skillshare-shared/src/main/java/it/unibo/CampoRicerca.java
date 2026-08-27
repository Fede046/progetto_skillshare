package it.unibo;

/**
 * Campi dell'annuncio su cui e' possibile effettuare la ricerca testuale.
 * Usato lato client per selezionare in quali campi cercare la query
 * e lato server per applicare il filtro.
 *
 * Nota: AUTORE fa riferimento al nome completo dell'autore (nomeAutore),
 * valorizzato dal MarketplaceServiceImpl dopo l'arricchimento dei dati,
 * NON al campo idUtente.
 */
public enum CampoRicerca {
    TITOLO,
    COMPETENZA,
    CONTROPRESTAZIONE,
    AUTORE
}
