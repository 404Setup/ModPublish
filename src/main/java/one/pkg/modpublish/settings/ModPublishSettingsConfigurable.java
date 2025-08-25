package one.pkg.modpublish.settings;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

public class ModPublishSettingsConfigurable implements Configurable {

    private ModPublishSettingsComponent component;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "ModPublish: Global Settings";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return component.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        component = new ModPublishSettingsComponent();
        return component.getPanel();
    }

    @Override
    public boolean isModified() {
        ModPublishSettings.State state =
                Objects.requireNonNull(ModPublishSettings.getInstance().getState());
        return !component.getModrinthTokenText().equals(state.getModrinthToken()) ||
                !component.getModrinthTestTokenText().equals(state.getModrinthTestToken()) ||
                !component.getCurseforgeTokenText().equals(state.getCurseforgeToken()) ||
                !component.getCurseforgeStudioTokenText().equals(state.getCurseforgeStudioToken()) ||
                !component.getGithubTokenText().equals(state.getGithubToken()) ||
                !component.getGitlabTokenText().equals(state.getGitlabToken());
    }

    @Override
    public void apply() {
        ModPublishSettings.State state =
                Objects.requireNonNull(ModPublishSettings.getInstance().getState());
        state.updateModrinthToken(component.getModrinthTokenText());
        state.updateModrinthTestToken(component.getModrinthTestTokenText());
        state.updateCurseforgeToken(component.getCurseforgeTokenText());
        state.updateCurseforgeStudioToken(component.getCurseforgeStudioTokenText());
        state.updateGithubToken(component.getGithubTokenText());
        state.updateGitlabToken(component.getGitlabTokenText());
    }

    @Override
    public void reset() {
        ModPublishSettings.State state =
                Objects.requireNonNull(ModPublishSettings.getInstance().getState());
        component.setModrinthTokenText(state.getModrinthToken());
        component.setModrinthTestTokenText(state.getModrinthTestToken());
        component.setCurseforgeTokenText(state.getCurseforgeToken());
        component.setCurseforgeStudioTokenText(state.getCurseforgeStudioToken());
        component.setGithubTokenText(state.getGithubToken());
        component.setGitlabTokenText(state.getGitlabToken());
    }

    @Override
    public void disposeUIResources() {
        component = null;
    }

}