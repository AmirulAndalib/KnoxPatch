/*
 * KnoxPatch
 * Copyright (C) 2023 Salvo Giangreco
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

import android.content.Context
import android.os.Build

import java.lang.reflect.Member
import java.security.cert.Certificate

import de.robv.android.xposed.XposedBridge

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog

import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve

import com.highcapable.kavaref.extension.ArrayClass

import io.mesalabs.knoxpatch.utils.BuildUtils
import io.mesalabs.knoxpatch.utils.Constants

object SystemHooks : YukiBaseHooker()  {
    private const val TAG: String = "SystemHooks"

    override fun onHook() {
        YLog.debug(msg = "$TAG: onHook: loaded.")

        /* Fix Secure Folder/Work profile */
        val sepVersion: Int = BuildUtils.getSEPVersion()
        if (sepVersion >= Constants.ONEUI_3_0) {
            applySAKHooks()
        } else if (sepVersion >= Constants.ONEUI_1_0) {
            applyTIMAHooks()
        }

        /* Disable KnoxGuard support */
        applyKGHooks()

        /* Disable ASKS */
        applySPHooks()
    }

    private fun applySAKHooks() {
        if (Build.VERSION.SDK_INT >= 35) {
            "com.samsung.android.security.keystore.AttestParameterSpec".toClass().resolve()
                .constructor {  }
                .hookAll {
                    after {
                        instance.asResolver()
                            .firstField {
                                name = "mVerifiableIntegrity"
                                type = Boolean::class
                            }.set(true)
                    }
                }
        }

        if (Build.VERSION.SDK_INT >= 31) {
            "com.android.server.knox.dar.DarManagerService".toClass().resolve().apply {
                firstMethod {
                    name = "checkDeviceIntegrity"
                    parameters(ArrayClass(Certificate::class))
                    returnType = Boolean::class
                }.hook {
                    replaceToTrue()
                }

                firstMethod {
                    name = "isDeviceRootKeyInstalled"
                    emptyParameters()
                    returnType = Boolean::class
                }.hook {
                    replaceToTrue()
                }
            }
        } else {
            "com.android.server.pm.PersonaManagerService".toClass().resolve()
                .firstMethod {
                    name = "isKnoxKeyInstallable"
                    emptyParameters()
                    returnType = Boolean::class
                }.hook {
                    replaceToTrue()
                }
        }
    }

    private fun applyTIMAHooks() {
        "com.android.server.pm.PersonaServiceHelper".toClass().resolve()
            .firstMethod {
                name = "isTimaAvailable"
                parameters(Context::class)
                returnType = Boolean::class
            }.hook {
                replaceToTrue()
            }

        if (Build.VERSION.SDK_INT >= 29) {
            "com.android.server.SdpManagerService\$LocalService".toClass().resolve()
                .firstMethod {
                    name = "isKnoxKeyInstallable"
                    emptyParameters()
                    returnType = Boolean::class
                }.hook {
                    replaceToTrue()
                }
        }

        "com.android.server.locksettings.SyntheticPasswordManager".toClass().resolve()
            .firstMethod {
                name = "isUnifiedKeyStoreSupported"
                emptyParameters()
                returnType = Boolean::class
            }.hook {
                replaceToTrue()
            }

        findAndDeoptimizeMethod("com.android.server.locksettings.LockSettingsService",
            "verifyToken")
        findAndDeoptimizeMethod("com.android.server.locksettings.LockSettingsService\$VirtualLock",
            "doVerifyCredential")
        findAndDeoptimizeMethod("com.android.server.locksettings.SyntheticPasswordManager",
            "createSyntheticPasswordBlobSpecific")
        findAndDeoptimizeMethod("com.android.server.locksettings.SyntheticPasswordManager",
            "destroySPBlobKey")
    }

    private fun applyKGHooks() {
        if (Build.VERSION.SDK_INT < 35) {
            "com.samsung.android.knoxguard.service.KnoxGuardService".toClass().resolve()
                .firstConstructor {
                    parameters(Context::class)
                }.hook {
                    before {
                        UnsupportedOperationException("KnoxGuard is unsupported").throwToApp()
                    }
                }
        }

        if (Build.VERSION.SDK_INT >= 30) {
            "com.samsung.android.knoxguard.service.KnoxGuardSeService".toClass().resolve()
                .firstConstructor {
                    parameters(Context::class)
                }.hook {
                    before {
                        UnsupportedOperationException("KnoxGuard is unsupported").throwToApp()
                    }
                }
        }
    }

    private fun applySPHooks() {
        "android.os.SystemProperties".toClass().resolve().apply {
            firstMethod {
                name = "get"
                parameters(String::class)
                returnType = String::class
            }.hook {
                before {
                    val key: String = args(0).string()

                    if (key == "ro.build.official.release") {
                        result = "false"
                    }
                }
            }

            firstMethod {
                name = "get"
                parameters(String::class, String::class)
                returnType = String::class
            }.hook {
                before {
                    val key: String = args(0).string()
                    val def: String = args(1).string()

                    if (key == "ro.build.official.release") {
                        result = def
                    }
                }
            }
        }
    }

    private fun findAndDeoptimizeMethod(className: String,
                                        methodName: String) {
        try {
            val clz: Class<*> = Class.forName(className, false, appClassLoader)
            for (m in clz.declaredMethods) {
                if (methodName == m.name) {
                    YLog.debug(msg = "$TAG: findAndDeoptimizeMethod: $m")
                    XposedBridge::class.resolve()
                        .firstMethod {
                            name = "deoptimizeMethod"
                            parameters(Member::class)
                        }.of(null).invoke(m)
                }
            }
        } catch (e: Throwable) {
            YLog.error(msg = "$TAG: findAndDeoptimizeMethod: $e")
        }
    }

}
