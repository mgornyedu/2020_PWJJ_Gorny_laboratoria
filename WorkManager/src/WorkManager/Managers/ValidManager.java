package WorkManager.Managers;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;

/**
 * Klasa abstrakcyjna walidacji
 */
public abstract class ValidManager{
    /**
     * Konstruktor menadżera walidacji
     * @param targetProperty właściwosć do której ma być zapisany rezultat walidacji
     */
    public ValidManager(BooleanProperty targetProperty) {
        TargetProperty = targetProperty;
    }

    /**
     * Pole przechoujące właściwosć do której ma być zapisany rezultat walidacji
     */
    private BooleanProperty TargetProperty;

    /**
     * Funkcja abstrakcyjna dla walidacji
     * @return rezultat walidacji
     */
    protected abstract boolean Valid();

    /**
     * Przypięcie właściwości wyzwalających
     * @param object Właściwość wyzwalająca
     */
    public void AppendMonitoringProperty(Observable object){
        object.addListener((e)-> TargetProperty.setValue(!Valid()));
    }
}
