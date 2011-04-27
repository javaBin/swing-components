package no.java.swing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class WeakReferencePropertyChangeListener implements PropertyChangeListener {
    private final WeakReference<PropertyChangeListener> ref;

    public WeakReferencePropertyChangeListener(PropertyChangeListener listener) {
        ref = new WeakReference<PropertyChangeListener>(listener);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        PropertyChangeListener listener = ref.get();
        if (listener != null) {
            listener.propertyChange(evt);
        }
    }
}
