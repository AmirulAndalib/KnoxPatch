/*
 * KnoxPatch
 * Copyright (C) 2024 Salvo Giangreco
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.mesalabs.knoxpatch.hooks

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog

import com.highcapable.kavaref.KavaRef.Companion.resolve

object KnoxMatrixHooks : YukiBaseHooker() {
    private const val TAG: String = "KnoxMatrixHooks"

    override fun onHook() {
        YLog.debug(msg = "$TAG: onHook: loaded.")

        /* Bypass Root of Trust checks */
        "com.samsung.android.kmxservice.common.util.RootOfTrust".toClassOrNull()?.resolve()?.apply {
            firstMethodOrNull {
                name = "getVerifiedBootState"
                returnType = Int::class
            }?.hook {
                replaceTo(0)
            }

            firstMethodOrNull {
                name = "isDeviceLocked"
                returnType = Boolean::class
            }?.hook {
                replaceToTrue()
            }
        }

        "com.samsung.android.kmxservice.fabrickeystore.keystore.cert.RootOfTrust".toClassOrNull()?.resolve()?.apply {
            firstMethodOrNull {
                name = "getVerifiedBootState"
                returnType = Int::class
            }?.hook {
                replaceTo(0)
            }

            firstMethodOrNull {
                name = "isDeviceLocked"
                returnType = Boolean::class
            }?.hook {
                replaceToTrue()
            }
        }

        "com.samsung.android.kmxservice.sdk.trustchain.util.RootOfTrust".toClassOrNull()?.resolve()?.apply {
            firstMethodOrNull {
                name = "getVerifiedBootState"
                returnType = Int::class
            }?.hook {
                replaceTo(0)
            }

            firstMethodOrNull {
                name = "isDeviceLocked"
                returnType = Boolean::class
            }?.hook {
                replaceToTrue()
            }
        }

        "com.samsung.android.kmxservice.sdk.util.RootOfTrust".toClassOrNull()?.resolve()?.apply {
            firstMethodOrNull {
                name = "getVerifiedBootState"
                returnType = Int::class
            }?.hook {
                replaceTo(0)
            }

            firstMethodOrNull {
                name = "isDeviceLocked"
                returnType = Boolean::class
            }?.hook {
                replaceToTrue()
            }
        }

        /* Bypass Knox Integrity Status checks */
        "com.samsung.android.kmxservice.common.util.IntegrityStatus".toClassOrNull()?.resolve()?.apply {
            firstMethodOrNull {
                name = "getStatus"
                returnType = Int::class
            }?.hook {
                replaceTo(0)
            }

            firstMethodOrNull {
                name = "isNormal"
                returnType = Boolean::class
            }?.hook {
                replaceToTrue()
            }
        }

        "com.samsung.android.kmxservice.fabrickeystore.keystore.cert.IntegrityStatus".toClassOrNull()?.resolve()?.apply {
            firstMethodOrNull {
                name = "isNormal"
                returnType = Boolean::class
            }?.hook {
                replaceToTrue()
            }
        }

        "com.samsung.android.kmxservice.sdk.trustchain.util.IntegrityStatus".toClassOrNull()?.resolve()?.apply {
            firstMethodOrNull {
                name = "getStatus"
                returnType = Int::class
            }?.hook {
                replaceTo(0)
            }

            firstMethodOrNull {
                name = "isNormal"
                returnType = Boolean::class
            }?.hook {
                replaceToTrue()
            }
        }

        "com.samsung.android.kmxservice.sdk.util.IntegrityStatus".toClassOrNull()?.resolve()?.apply {
            firstMethodOrNull {
                name = "getStatus"
                returnType = Int::class
            }?.hook {
                replaceTo(0)
            }

            firstMethodOrNull {
                name = "isNormal"
                returnType = Boolean::class
            }?.hook {
                replaceToTrue()
            }
        }
    }

}
