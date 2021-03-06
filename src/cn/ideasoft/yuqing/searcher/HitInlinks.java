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

package cn.ideasoft.yuqing.searcher;

import java.io.IOException;

import org.apache.hadoop.io.Closeable;
import cn.ideasoft.yuqing.crawl.Inlinks;

/** Service that returns information about incoming links to a hit. */
public interface HitInlinks extends Closeable {
  /** Returns the anchors of a hit document. */
  String[] getAnchors(HitDetails details) throws IOException;

  /** Return the inlinks of a hit document. */
  Inlinks getInlinks(HitDetails details) throws IOException;
}
