/*
 * Copyright 2008-2009 the original 赵永春(zyc@hasor.net).
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
package net.hasor.rsf.center.web;
import java.io.IOException;
import net.hasor.mvc.api.MappingTo;
import net.hasor.mvc.api.Params;
import net.hasor.mvc.api.Valid;
import net.hasor.mvc.support.AbstractWebController;
import net.hasor.rsf.center.domain.form.OffLineForm;
/**
 * 
 * @version : 2015年5月5日
 * @author 赵永春(zyc@hasor.net)
 */
@MappingTo("/apis/offline")
public class OffLine extends AbstractWebController {
    public void execute(@Valid("Access") @Params OffLineForm offLineForm) throws IOException {
        getResponse().getWriter().write("abc");
        this.getContextMap().put("var", "abc");
        System.out.println("/apis/offline");
    }
}