/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2021 Tuya Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tuya.appsdk.sample;

import android.app.Application;

import com.thingclips.sdk.os.ThingOSUser;
import com.thingclips.smart.android.base.provider.ApiUrlProvider;
import com.thingclips.smart.android.network.ThingSmartNetWork;
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk;
import com.thingclips.smart.sdk.ThingSdk;

/**
 * Base Application
 *
 * @author chuanfeng <a href="mailto:developer@tuya.com"/>
 * @since 2021/2/9 10:41 AM
 */
public final class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ThingSdk.setDebugMode(true);
        ThingSdk.init(this);
        ThingSmartNetWork.mSdkVersion = "1.3.0";
        ThingOptimusSdk.init(this);
    }
}