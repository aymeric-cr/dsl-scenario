package fdit.gui.zoneEditor.tabs.zone;

import fdit.gui.application.FditManager;
import fdit.gui.application.FditManagerListener;
import fdit.gui.application.commands.delete.DeletionUtils;
import fdit.gui.utils.ThreadSafeBooleanProperty;
import fdit.gui.utils.ThreadSafeStringProperty;
import fdit.metamodel.element.Directory;
import fdit.metamodel.element.FditElement;
import fdit.metamodel.zone.Zone;
import fdit.storage.nameChecker.CheckResult;
import fdit.tools.i18n.MessageTranslator;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import org.codefx.libfx.listener.handle.ListenerHandle;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.storage.nameChecker.FditElementNameChecker.checkNewFditElementNameValidity;
import static fdit.storage.nameChecker.FditElementNameChecker.checkRenameElementValidity;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;
import static java.lang.Double.parseDouble;
import static java.lang.Math.abs;
import static org.codefx.libfx.listener.handle.ListenerHandles.createAttached;
import static org.codefx.libfx.listener.handle.ListenerHandles.createFor;

public abstract class ZoneEditionModel {

    protected static final MessageTranslator TRANSLATOR = createMessageTranslator(ZoneEditionModel.class);

    protected final UUID zoneId;
    protected final StringProperty zoneName = new ThreadSafeStringProperty(TRANSLATOR.getMessage("name.newZone"));
    protected final StringProperty errorMessage = new ThreadSafeStringProperty("");
    protected final Collection<ListenerHandle> listenerHandles = newArrayList();
    private final StringProperty altitudeLowerBound = new ThreadSafeStringProperty("0");
    private final StringProperty altitudeUpperBound = new ThreadSafeStringProperty("0");
    private final BooleanProperty saveButtonEnabled = new ThreadSafeBooleanProperty(true);
    private final BooleanProperty deleteButtonEnabled = new ThreadSafeBooleanProperty(false);
    private final Collection<ZoneEditionModelListener> listeners = newArrayList();
    protected Zone editedZone;

    ZoneEditionModel(final UUID zoneId) {
        this.zoneId = zoneId;
        listenerHandles.add(createAttached(zoneNameProperty(),
                (observable, oldValue, newValue) -> updateOkButton()));
        listenerHandles.add(createAttached(altitudeLowerBoundProperty(),
                (observable, oldValue, newValue) -> updateOkButton()));
        listenerHandles.add(createAttached(altitudeUpperBoundProperty(),
                (observable, oldValue, newValue) -> updateOkButton()));
        listenerHandles.add(createFor(FDIT_MANAGER, createFditManagerListener())
                .onAttach(FditManager::addListener)
                .onDetach(FditManager::removeListener)
                .buildAttached());
    }

    public void addListener(final ZoneEditionModelListener listener) {
        listeners.add(listener);
    }

    public void removeListener(final ZoneEditionModelListener listener) {
        listeners.remove(listener);
    }

    public UUID getZoneId() {
        return zoneId;
    }

    public String getZoneIdAsString() {
        return String.valueOf(abs(zoneId.hashCode()));
    }

    public String getZoneName() {
        return zoneName.get();
    }

    void setZoneName(final String zoneName) {
        this.zoneName.set(zoneName);
    }

    public Zone getEditedZone() {
        return editedZone;
    }

    void setEditedZone(final Zone editedZone) {
        this.editedZone = editedZone;
        updateButtons();
    }

    public final StringProperty zoneNameProperty() {
        return zoneName;
    }

    StringProperty errorMessageProperty() {
        return errorMessage;
    }

    String getAltitudeLowerBound() {
        return altitudeLowerBound.get();
    }

    void setAltitudeLowerBound(final String altitudeLowerBound) {
        this.altitudeLowerBound.set(altitudeLowerBound);
    }

    StringProperty altitudeLowerBoundProperty() {
        return altitudeLowerBound;
    }

    String getAltitudeUpperBound() {
        return altitudeUpperBound.get();
    }

    void setAltitudeUpperBound(final String altitudeUpperBound) {
        this.altitudeUpperBound.set(altitudeUpperBound);
    }

    StringProperty altitudeUpperBoundProperty() {
        return altitudeUpperBound;
    }

    BooleanProperty saveButtonEnabledProperty() {
        return saveButtonEnabled;
    }

    boolean getSaveButtonEnabled() {
        return saveButtonEnabled.get();
    }

    BooleanProperty deleteButtonEnabledProperty() {
        return deleteButtonEnabled;
    }

    boolean isDeleteButtonEnabled() {
        return deleteButtonEnabled.get();
    }

    protected abstract void save();

    void delete() throws IOException {
        DeletionUtils.delete(editedZone);
    }

    public boolean validate() {
        final String newZoneName = getZoneName();
        final CheckResult checkResult;
        final File rootFile = FDIT_MANAGER.getRootFile();
        if (editedZone != null) {
            checkResult = checkRenameElementValidity(editedZone, rootFile, newZoneName);
        } else {
            checkResult = checkNewFditElementNameValidity(newZoneName, getZoneFolder(), rootFile, Zone.class);
        }
        if (checkResult.checkFailed()) {
            errorMessage.setValue(checkResult.getMessage());
            return false;
        }
        final Double altitudeUpperBound;
        final Double altitudeLowerBound;
        try {
            altitudeUpperBound = parseDouble(this.altitudeUpperBound.get());
            altitudeLowerBound = parseDouble(this.altitudeLowerBound.get());
        } catch (final NumberFormatException e) {
            errorMessage.setValue(TRANSLATOR.getMessage("error.altitude.badFormat"));
            return false;
        }
        if (altitudeLowerBound < 0 || altitudeUpperBound < 0) {
            errorMessage.setValue(TRANSLATOR.getMessage("error.altitude.negative"));
            return false;
        }
        if (altitudeUpperBound < altitudeLowerBound) {
            errorMessage.setValue(TRANSLATOR.getMessage("error.altitude.lowerSupUpper"));
            return false;
        }
        errorMessage.setValue("");
        return true;
    }

    protected final void updateButtons() {
        updateOkButton();
        updateDeleteButton();
    }

    private void updateDeleteButton() {
        deleteButtonEnabled.setValue(editedZone != null);
    }

    protected void updateOkButton() {
        saveButtonEnabled.setValue(validate() && hasDataChanged());
    }

    @SuppressWarnings("RedundantIfStatement")
    protected boolean hasDataChanged() {
        if (editedZone == null) {
            return true;
        }
        if (!editedZone.getName().equals(getZoneName())) {
            return true;
        }
        return false;
    }

    protected void fireZoneUpdated() {
        listeners.forEach(ZoneEditionModelListener::zoneUpdated);
    }

    private FditManagerListener createFditManagerListener() {
        return new FditManagerListener() {
            @Override
            public void elementAdded(final FditElement createdElement) {
                if (createdElement instanceof Zone && ((Zone) createdElement).getId().equals(zoneId)) {
                    editedZone = (Zone) createdElement;
                    updateButtons();
                }
            }

            @Override
            public void elementEdited(final FditElement elementEdited) {
                if (elementEdited == editedZone) {
                    restoreModel();
                    updateButtons();
                }
            }

            @Override
            public void elementRemoved(final FditElement removedElement) {
                if (removedElement == editedZone) {
                    editedZone = null;
                    updateButtons();
                }
            }
        };
    }

    protected abstract void restoreModel();

    private Directory getZoneFolder() {
        if (editedZone != null) {
            return editedZone.getFather();
        }
        return FDIT_MANAGER.getRoot();
    }

    void closeModel() {
        for (final ListenerHandle listenerHandle : listenerHandles) {
            listenerHandle.detach();
        }
        listenerHandles.clear();
    }

    public interface ZoneEditionModelListener {
        void zoneUpdated();
    }
}
