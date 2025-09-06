/*
 * Copyright (C) 2025 404Setup (https://github.com/404Setup)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package one.pkg.modpublish.settings.properties;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import one.pkg.modpublish.data.internel.Info;

import javax.swing.*;

@SuppressWarnings("unused")
public enum PID {
    ModrinthModID("modpublish.modrinth.modid", false), ModrinthTestModID("modpublish.modrinth.testModId", false),
    ModrinthToken("modpublish.modrinth.token", true), ModrinthTestToken("modpublish.modrinth.testToken", true),
    CurseForgeToken("modpublish.curseforge.token", true), CurseForgeStudioToken("modpublish.curseforge.studioToken", true),
    CurseForgeModID("modpublish.curseforge.modid", false),
    GithubToken("modpublish.github.token", true), GithubRepo("modpublish.github.repo", false), GithubBranch("modpublish.github.branch", false),
    CommonVersionFormat("modpublish.common.versionFormat", false);

    public final String id;
    public final boolean protect;

    PID(String id, boolean protect) {
        this.id = id;
        this.protect = protect;
    }

    public String get(Project project) {
        return get(Properties.getPropertiesComponent(project));
    }

    public String get(PropertiesComponent properties) {
        return properties.getValue(id, "");
    }

    public Info getProtect(Project project) {
        return getProtect(PropertiesComponent.getInstance(project));
    }

    public Info getProtect(PropertiesComponent properties) {
        return Properties.getProtectValue(properties, this);
    }

    public void set(Project project, String data) {
        set(PropertiesComponent.getInstance(project), data);
    }

    public void set(PropertiesComponent properties, String data) {
        if (protect) Properties.setProtectValue(properties, id, data);
        else properties.setValue(id, data);
    }

    public void set(Project project, JTextField component) {
        set(project, component.getText());
    }

    public void set(PropertiesComponent properties, JTextField component) {
        set(properties, component.getText());
    }
}
