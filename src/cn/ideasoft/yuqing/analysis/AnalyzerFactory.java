/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.ideasoft.yuqing.analysis;

// Commons Logging imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// Nutch imports
import cn.ideasoft.yuqing.plugin.Extension;
import cn.ideasoft.yuqing.plugin.ExtensionPoint;
import cn.ideasoft.yuqing.plugin.PluginRuntimeException;
import cn.ideasoft.yuqing.plugin.PluginRepository;
import org.apache.hadoop.conf.Configuration;


/**
 * Creates and caches {@link YuQingAnalyzer} plugins.
 *
 * @author J&eacute;r&ocirc;me Charron
 */
public class AnalyzerFactory {

  private final static String KEY = AnalyzerFactory.class.getName();
  
  public final static Log LOG = LogFactory.getLog(KEY);

  
  private YuQingAnalyzer DEFAULT_ANALYZER;
  
  private ExtensionPoint extensionPoint;
  private Configuration conf;

  public AnalyzerFactory (Configuration conf) {
      DEFAULT_ANALYZER = new YuQingDocumentAnalyzer(conf);
      this.conf = conf;
      this.extensionPoint = PluginRepository.get(conf).getExtensionPoint(YuQingAnalyzer.X_POINT_ID);
      if(this.extensionPoint == null) {
          throw new RuntimeException("x point " + YuQingAnalyzer.X_POINT_ID +
          " not found.");
      }
  }

  public static AnalyzerFactory get(Configuration conf) {
    AnalyzerFactory factory = (AnalyzerFactory) conf.getObject(KEY);
    if (factory == null) {
      factory = new AnalyzerFactory(conf);
      conf.setObject(KEY, factory);
    }
    return factory;
  }
  
  /**
   * Returns the appropriate {@link YuQingAnalyzer analyzer} implementation
   * given a language code.
   *
   * <p>NutchAnalyzer extensions should define the attribute "lang". The first
   * plugin found whose "lang" attribute equals the specified lang parameter is
   * used. If none match, then the {@link YuQingDocumentAnalyzer} is used.
   */
  public YuQingAnalyzer get(String lang) {

    YuQingAnalyzer analyzer = DEFAULT_ANALYZER;
    Extension extension = getExtension(lang);
    if (extension != null) {
        try {
            analyzer = (YuQingAnalyzer) extension.getExtensionInstance();
        } catch (PluginRuntimeException pre) {
            analyzer = DEFAULT_ANALYZER;
        }
    }
    return analyzer;
  }

  private Extension getExtension(String lang) {

    if (lang == null) { return null; }
    Extension extension = (Extension) this.conf.getObject(lang);
    if (extension == null) {
      extension = findExtension(lang);
      if (extension != null) {
        this.conf.setObject(lang, extension);
      }
    }
    return extension;
  }

  private Extension findExtension(String lang) {

    if (lang != null) {
      Extension[] extensions = this.extensionPoint.getExtensions();
      for (int i=0; i<extensions.length; i++) {
        if (lang.equals(extensions[i].getAttribute("lang"))) {
          return extensions[i];
        }
      }
    }
    return null;
  }

  /** 
   * Method used by unit test
   */
  protected YuQingAnalyzer getDefault() {
    return DEFAULT_ANALYZER;
  }

}
