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

package one.pkg.modpublish.data.network.curseforge;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@SuppressWarnings("unused")
@Data
public class CurseForgePublishResult {
    /**
     * The uploaded file ID
     */
    @SerializedName("id")
    private Integer id;

    public CurseForgePublishResult() {
    }

    public CurseForgePublishResult(Integer id) {
        this.id = id;
    }

    /**
     * Creates a successful response object
     *
     * @param fileId the file ID
     * @return CurseForgePublishResult object
     */
    public static CurseForgePublishResult success(Integer fileId) {
        return new CurseForgePublishResult(fileId);
    }

    /**
     * Creates a failure response object
     *
     * @return CurseForgePublishResult object with null ID
     */
    public static CurseForgePublishResult failure() {
        return new CurseForgePublishResult();
    }

    /**
     * Checks if the response is valid (contains a valid ID)
     *
     * @return true if ID is not null and greater than 0
     */
    public boolean isValid() {
        return id != null && id > 0;
    }

    /**
     * Checks if the upload was successful
     *
     * @return true if there is a valid ID
     */
    public boolean isSuccess() {
        return isValid();
    }

    /**
     * Gets the file ID as a string
     *
     * @return string representation of file ID, or empty string if ID is null
     */
    public String getIdAsString() {
        return id != null ? id.toString() : "";
    }

}