/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.web.invoker;
import net.hasor.core.AppContext;
import net.hasor.test.actions.ParamsCallAction;
import net.hasor.test.actions.SpecialTypeCallAction;
import net.hasor.test.actions.args.*;
import net.hasor.web.Invoker;
import net.hasor.web.WebApiBinder;
import net.hasor.web.WebModule;
import net.hasor.test.beans.params.*;
import org.junit.Test;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigInteger;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//
public class CallerParamsTest extends AbstractWeb30BinderDataTest {
    @Test
    public void queryParamTest() throws Throwable {
        AppContext appContext = hasor.build((WebModule) apiBinder -> {
            apiBinder.tryCast(WebApiBinder.class).loadMappingTo(QueryArgsAction.class);
        });
        //
        List<InMappingDef> definitions = appContext.findBindingBean(InMappingDef.class);
        Invoker invoker = newInvoker(definitions.get(0), mockRequest("post", new URL("http://www.hasor.net/query_param.do?byteParam=123&bigInteger=321"), appContext), appContext);
        InvokerCaller caller = new InvokerCaller(() -> invoker, null);
        Object o = caller.invoke(null).get();
        assert o instanceof Map;
        assert (Byte) ((Map) o).get("byteParam") == (byte) 123;
        assert ((BigInteger) ((Map) o).get("bigInteger")).longValue() == 321;
    }
    @Test
    public void pathParamTest() throws Throwable {
        AppContext appContext = hasor.build((WebModule) apiBinder -> {
            apiBinder.tryCast(WebApiBinder.class).loadMappingTo(PathArgsAction.class);
        });
        //
        List<InMappingDef> definitions = appContext.findBindingBean(InMappingDef.class);
        Invoker invoker = newInvoker(definitions.get(0), mockRequest("post", new URL("http://www.hasor.net/123/321/path_param.do"), appContext), appContext);
        InvokerCaller caller = new InvokerCaller(() -> invoker, null);
        Object o = caller.invoke(null).get();
        assert o instanceof Map;
        assert (Byte) ((Map) o).get("byteParam") == (byte) 123;
        assert ((Float) ((Map) o).get("floatParam")).longValue() == 321;
    }
    @Test
    public void cookieParamTest() throws Throwable {
        AppContext appContext = hasor.build((WebModule) apiBinder -> {
            apiBinder.tryCast(WebApiBinder.class).loadMappingTo(CookieArgsAction.class);
        });
        //
        Cookie[] cookies = new Cookie[] {//
                new Cookie("byteParam", "123"),//
                new Cookie("floatParam", "321"),//
        };
        //
        List<InMappingDef> definitions = appContext.findBindingBean(InMappingDef.class);
        Invoker invoker = newInvoker(definitions.get(0), mockRequest("post", new URL("http://www.hasor.net/cookie_param.do"), appContext, cookies, null), appContext);
        InvokerCaller caller = new InvokerCaller(() -> invoker, null);
        Object o = caller.invoke(null).get();
        assert o instanceof Map;
        assert (Byte) ((Map) o).get("byteParam") == (byte) 123;
        assert ((Float) ((Map) o).get("floatParam")).longValue() == 321;
    }
    @Test
    public void attrParamTest() throws Throwable {
        AppContext appContext = hasor.build((WebModule) apiBinder -> {
            apiBinder.tryCast(WebApiBinder.class).loadMappingTo(AttributeArgsAction.class);
        });
        //
        HttpServletRequest request = mockRequest("post", new URL("http://www.hasor.net/attr_param.do"), appContext);
        request.setAttribute("byteParam", 123);
        request.setAttribute("floatParam", 321);
        //
        List<InMappingDef> definitions = appContext.findBindingBean(InMappingDef.class);
        Invoker invoker = newInvoker(definitions.get(0), request, appContext);
        InvokerCaller caller = new InvokerCaller(() -> invoker, null);
        Object o = caller.invoke(null).get();
        assert o instanceof Map;
        assert (Byte) ((Map) o).get("byteParam") == (byte) 123;
        assert ((Float) ((Map) o).get("floatParam")).longValue() == 321;
    }
    @Test
    public void headerParamTest() throws Throwable {
        AppContext appContext = hasor.build((WebModule) apiBinder -> {
            apiBinder.bindType(Map.class).nameWith("http-header").toInstance(new HashMap<String, String>() {{
                put("byteParam", "123");
                put("floatParam", "321");
            }});
            //
            apiBinder.tryCast(WebApiBinder.class).loadMappingTo(HeaderArgsAction.class);
        });
        //
        HttpServletRequest request = mockRequest("post", new URL("http://www.hasor.net/header_param.do"), appContext);
        //
        List<InMappingDef> definitions = appContext.findBindingBean(InMappingDef.class);
        Invoker invoker = newInvoker(definitions.get(0), request, appContext);
        InvokerCaller caller = new InvokerCaller(() -> invoker, null);
        Object o = caller.invoke(null).get();
        assert o instanceof Map;
        assert (Byte) ((Map) o).get("byteParam") == (byte) 123;
        assert ((Float) ((Map) o).get("floatParam")).longValue() == 321;
    }
    @Test
    public void requestParamTest() throws Throwable {
        AppContext appContext = hasor.build((WebModule) apiBinder -> {
            apiBinder.tryCast(WebApiBinder.class).loadMappingTo(RequestArgsAction.class);
        });
        //
        Map<String, String> requestMap = new HashMap<String, String>() {{
            put("byteParam", "123");
            put("floatParam", "321");
        }};
        HttpServletRequest request = mockRequest("post", new URL("http://www.hasor.net/req_param.do?intParam=111"), appContext, null, requestMap);
        //
        List<InMappingDef> definitions = appContext.findBindingBean(InMappingDef.class);
        Invoker invoker = newInvoker(definitions.get(0), request, appContext);
        InvokerCaller caller = new InvokerCaller(() -> invoker, null);
        Object o = caller.invoke(null).get();
        assert o instanceof Map;
        assert (Byte) ((Map) o).get("byteParam") == (byte) 123;
        assert ((Float) ((Map) o).get("floatParam")).longValue() == 321;
        assert ((Integer) ((Map) o).get("intParam")).longValue() == 111;
    }
    @Test
    public void beanParamTest() throws Throwable {
        AppContext appContext = hasor.build((WebModule) apiBinder -> {
            apiBinder.tryCast(WebApiBinder.class).loadMappingTo(ParamsCallAction.class);
        });
        //
        Map<String, String> requestMap = new HashMap<String, String>() {{
            put("byteParam", "123");
            put("floatParam", "321");
        }};
        HttpServletRequest request = mockRequest("post", new URL("http://www.hasor.net/bean_param.do?intParam=111"), appContext, null, requestMap);
        //
        List<InMappingDef> definitions = appContext.findBindingBean(InMappingDef.class);
        Invoker invoker = newInvoker(definitions.get(0), request, appContext);
        InvokerCaller caller = new InvokerCaller(() -> invoker, null);
        Object o = caller.invoke(null).get();
        assert o instanceof Map;
        assert (Byte) ((Map) o).get("byteParam") == (byte) 123;
        assert ((Float) ((Map) o).get("floatParam")).longValue() == 321;
        assert ((Integer) ((Map) o).get("intParam")).longValue() == 111;
    }
    @Test
    public void specialParamTest() throws Throwable {
        AppContext appContext = hasor.build((WebModule) apiBinder -> {
            apiBinder.tryCast(WebApiBinder.class).loadMappingTo(SpecialTypeCallAction.class);
        });
        //
        HttpServletRequest request = mockRequest("post", new URL("http://www.hasor.net/special_param.do"), appContext);
        //
        List<InMappingDef> definitions = appContext.findBindingBean(InMappingDef.class);
        Invoker invoker = newInvoker(definitions.get(0), request, appContext);
        InvokerCaller caller = new InvokerCaller(() -> invoker, null);
        Object o = caller.invoke(null).get();
        assert o instanceof Map;
        assert ((Map) o).get("request") instanceof HttpServletRequest;
        assert ((Map) o).get("response") instanceof HttpServletResponse;
        assert ((Map) o).get("session") instanceof HttpSession;
        assert ((Map) o).get("invoker") instanceof Invoker;
        assert ((Map) o).get("listData") == null;
    }
}