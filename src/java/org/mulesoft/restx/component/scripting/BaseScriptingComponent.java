/*      
 *  RESTx: Sane, simple and effective data publishing and integration. 
 *  
 *  Copyright (C) 2010   MuleSoft Inc.    http://www.mulesoft.com 
 *  
 *  This program is free software: you can redistribute it and/or modify 
 *  it under the terms of the GNU General Public License as published by 
 *  the Free Software Foundation, either version 3 of the License, or 
 *  (at your option) any later version. 
 * 
 *  This program is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 *  GNU General Public License for more details. 
 * 
 *  You should have received a copy of the GNU General Public License 
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package org.mulesoft.restx.component.scripting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.mulesoft.restx.Settings;
import org.mulesoft.restx.component.BaseComponent;
import org.mulesoft.restx.component.api.HTTP;
import org.mulesoft.restx.component.api.Result;
import org.mulesoft.restx.exception.RestxException;
import org.mulesoft.restx.parameter.ParameterType;

public abstract class BaseScriptingComponent extends BaseComponent
{
    private final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();

    private ScriptEngine scriptEngine;

    private File getComponentScriptFile()
    {
        return new File(Settings.getRootDir() + instanceConf.get("path"));
    }

    protected Object evaluateComponent(Bindings bindings) throws RestxException
    {
        try
        {
            return evaluate(bindings, getComponentCodeInputStream());
        }
        catch (final FileNotFoundException fnfe)
        {
            throw new RestxException(fnfe.getMessage());
        }
    }

    protected InputStream getComponentCodeInputStream() throws FileNotFoundException
    {
        return new FileInputStream(getComponentScriptFile());
    }

    protected Object evaluateResource(Bindings bindings, String resourceName) throws RestxException
    {
        return evaluate(bindings, getClass().getResourceAsStream(resourceName));
    }

    private Object evaluate(Bindings bindings, InputStream inputStream) throws RestxException
    {
        try
        {
            final ScriptEngine engine = getScriptEngine();
            return engine.eval(new InputStreamReader(inputStream), bindings);
        }
        catch (final ScriptException se)
        {
            throw new RestxException(se.getMessage());
        }
    }

    protected ScriptEngine getScriptEngine()
    {
        if (scriptEngine == null)
        {
            scriptEngine = newScriptEngine(scriptEngineManager);
        }

        return scriptEngine;
    }

    protected abstract ScriptEngine newScriptEngine(ScriptEngineManager scriptEngineManager);

    protected void addCommonBindings(final Bindings bindings)
    {
        bindings.put("HTTP", new HTTP());
        bindings.put("TYPE", new ParameterType());
        bindings.put("RESULT", new Result(500, "No result provided"));
        bindings.put("RESTx", this);
    }
}
