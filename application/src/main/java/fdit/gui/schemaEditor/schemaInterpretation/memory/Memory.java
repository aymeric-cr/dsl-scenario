package fdit.gui.schemaEditor.schemaInterpretation.memory;

import fdit.metamodel.recording.Recording;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class Memory {

    private final Map<String, Constant> constantMap = newHashMap();
    private final MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
    private boolean replayAttack = false;
    private Recording targetedRecording = null;

    public Memory() throws NoSuchAlgorithmException {
    }

    public void addConstant(final Constant constant) {
        messageDigest.update(constant.getName().getBytes());
        constantMap.putIfAbsent(
                new String(messageDigest.digest()), constant);
    }

    public Constant getConstant(final String key) {
        messageDigest.update(key.getBytes());
        return constantMap.get(new String(messageDigest.digest()));
    }

    public Collection<Constant> getConstants() {
        return constantMap.values();
    }

    public void clear() {
        constantMap.clear();
        replayAttack = false;
        targetedRecording = null;
    }

    public boolean isReplayAttack() {
        return replayAttack;
    }

    public void setReplayAttack(boolean replayAttack) {
        this.replayAttack = replayAttack;
    }

    public Recording getTargetedRecording() {
        return targetedRecording;
    }

    public void setTargetedRecording(Recording targetedRecording) {
        this.targetedRecording = targetedRecording;
    }
}