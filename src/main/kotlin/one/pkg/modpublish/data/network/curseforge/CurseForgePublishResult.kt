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
package one.pkg.modpublish.data.network.curseforge

import com.google.gson.annotations.SerializedName

@Suppress("unused")
data class CurseForgePublishResult(
    /**
     * The uploaded file ID
     */
    @SerializedName("id")
    var id: Int? = null
) {
    constructor() : this(null)

    /**
     * Checks if the response is valid (contains a valid ID)
     *
     * @return true if ID is not null and greater than 0
     */
    val isValid: Boolean
        get() = id != null && id!! > 0

    /**
     * Checks if the upload was successful
     *
     * @return true if there is a valid ID
     */
    val isSuccess: Boolean
        get() = this.isValid

    /**
     * Gets the file ID as a string
     *
     * @return string representation of file ID, or empty string if ID is null
     */
    val idAsString: String
        get() = id?.toString() ?: ""

    companion object {
        /**
         * Creates a successful response object
         *
         * @param fileId the file ID
         * @return CurseForgePublishResult object
         */
        fun success(fileId: Int): CurseForgePublishResult {
            return CurseForgePublishResult(fileId)
        }

        /**
         * Creates a failure response object
         *
         * @return CurseForgePublishResult object with null ID
         */
        fun failure(): CurseForgePublishResult {
            return CurseForgePublishResult()
        }
    }
}