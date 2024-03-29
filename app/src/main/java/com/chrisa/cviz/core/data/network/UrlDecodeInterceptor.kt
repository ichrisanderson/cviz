/*
 * Copyright 2020 Chris Anderson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chrisa.cviz.core.data.network

import okhttp3.Interceptor
import okhttp3.Response

class UrlDecodeInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val replacedUrl = originalRequest.url.toString()
            .replace("%3D", "=")
            .replace("%3A", ":")
            .replace("%2C", ",")
            .replace("%26", "&")
        val request = originalRequest.newBuilder().url(replacedUrl).build()
        return chain.proceed(request)
    }
}
