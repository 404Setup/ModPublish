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
        return !component.getModrinthTokenText().equals(state.getModrinthToken().data()) ||
                !component.getModrinthTestTokenText().equals(state.getModrinthTestToken().data()) ||
                !component.getCurseforgeTokenText().equals(state.getCurseforgeToken().data()) ||
                !component.getCurseforgeStudioTokenText().equals(state.getCurseforgeStudioToken().data()) ||
                !component.getGithubTokenText().equals(state.getGithubToken().data()) ||
                !component.getGitlabTokenText().equals(state.getGitlabToken().data());
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
        component.setModrinthTokenText(state.getModrinthToken().data());
        component.setModrinthTestTokenText(state.getModrinthTestToken().data());
        component.setCurseforgeTokenText(state.getCurseforgeToken().data());
        component.setCurseforgeStudioTokenText(state.getCurseforgeStudioToken().data());
        component.setGithubTokenText(state.getGithubToken().data());
        component.setGitlabTokenText(state.getGitlabToken().data());
    }

    @Override
    public void disposeUIResources() {
        component = null;
    }

}