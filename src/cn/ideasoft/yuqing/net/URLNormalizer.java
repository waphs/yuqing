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

package cn.ideasoft.yuqing.net;

import java.net.MalformedURLException;

import org.apache.hadoop.conf.Configurable;

/** Interface used to convert URLs to normal form and optionally perform substitutions */
public interface URLNormalizer extends Configurable {
  
  /* Extension ID */
  public static final String X_POINT_ID = URLNormalizer.class.getName();
  
  /* Interface for URL normalization */
  public String normalize(String urlString, String scope) throws MalformedURLException;

}
