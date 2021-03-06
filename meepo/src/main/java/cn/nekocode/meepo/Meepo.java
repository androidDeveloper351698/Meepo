/*
 * Copyright 2017. nekocode (nekocode.cn@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.nekocode.meepo;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.nekocode.meepo.adapter.GotoActivityAdapter;
import cn.nekocode.meepo.adapter.GotoAdapter;
import cn.nekocode.meepo.config.Config;
import cn.nekocode.meepo.config.UriConfig;
import cn.nekocode.meepo.parser.DefaultParser;
import cn.nekocode.meepo.parser.Parser;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
public final class Meepo {
    private Config config;
    private Parser parser;
    private GotoAdapter gotoAdapter;


    private Meepo(Builder builder) {
        this.config = builder.config;
        this.parser = builder.parser;
        this.gotoAdapter = builder.gotoAdapter;
    }

    public <T> T create(@NonNull Class<T> routerClass) {
        return (T) Proxy.newProxyInstance(routerClass.getClassLoader(),
                new Class[]{routerClass}, new MeepoInvocationHandler());
    }

    private class MeepoInvocationHandler implements InvocationHandler {
        private final Map<Method, GotoMethod> methodCache = new ConcurrentHashMap<>();


        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return gotoAdapter.goTo(config, loadGoto(method), args);
        }

        private GotoMethod loadGoto(Method method) {
            GotoMethod result = methodCache.get(method);
            if (result != null) return result;

            synchronized (methodCache) {
                result = parser.parseMethod(config, method);
                methodCache.put(method, result);
            }
            return result;
        }
    }

    public static class Builder {
        private Config config;
        private Parser parser;
        private GotoAdapter gotoAdapter;

        public Builder config(@NonNull Config config) {
            this.config = config;
            return this;
        }

        public Builder parser(@Nullable Parser parser) {
            this.parser = parser;
            return this;
        }

        public Builder adapter(@Nullable GotoAdapter gotoAdapter) {
            this.gotoAdapter = gotoAdapter;
            return this;
        }

        public Meepo build() {
            if (config == null) config(new UriConfig());
            if (parser == null) parser(new DefaultParser());
            if (gotoAdapter == null) adapter(new GotoActivityAdapter());
            return new Meepo(this);
        }
    }
}
