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

package cn.ideasoft.yuqing.scoring;

import java.util.HashMap;
import java.util.List;

import org.apache.lucene.document.Document;
import cn.ideasoft.yuqing.crawl.CrawlDatum;
import cn.ideasoft.yuqing.crawl.Inlinks;
import cn.ideasoft.yuqing.parse.Parse;
import cn.ideasoft.yuqing.parse.ParseData;
import cn.ideasoft.yuqing.plugin.Extension;
import cn.ideasoft.yuqing.plugin.ExtensionPoint;
import cn.ideasoft.yuqing.plugin.PluginRuntimeException;
import cn.ideasoft.yuqing.plugin.PluginRepository;
import cn.ideasoft.yuqing.protocol.Content;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.io.Text;

/**
 * Creates and caches {@link ScoringFilter} implementing plugins.
 * 
 * @author Andrzej Bialecki
 */
public class ScoringFilters extends Configured implements ScoringFilter {

  private ScoringFilter[] filters;

  public ScoringFilters(Configuration conf) {
    super(conf);
    String order = conf.get("scoring.filter.order");
    this.filters = (ScoringFilter[]) conf.getObject(ScoringFilter.class.getName());

    if (this.filters == null) {
      String[] orderedFilters = null;
      if (order != null && !order.trim().equals("")) {
        orderedFilters = order.split("\\s+");
      }

      try {
        ExtensionPoint point = PluginRepository.get(conf).getExtensionPoint(ScoringFilter.X_POINT_ID);
        if (point == null) throw new RuntimeException(ScoringFilter.X_POINT_ID + " not found.");
        Extension[] extensions = point.getExtensions();
        HashMap filterMap = new HashMap();
        for (int i = 0; i < extensions.length; i++) {
          Extension extension = extensions[i];
          ScoringFilter filter = (ScoringFilter) extension.getExtensionInstance();
          if (!filterMap.containsKey(filter.getClass().getName())) {
            filterMap.put(filter.getClass().getName(), filter);
          }
        }
        if (orderedFilters == null) {
          conf.setObject(ScoringFilter.class.getName(), filterMap.values().toArray(new ScoringFilter[0]));
        } else {
          ScoringFilter[] filter = new ScoringFilter[orderedFilters.length];
          for (int i = 0; i < orderedFilters.length; i++) {
            filter[i] = (ScoringFilter) filterMap.get(orderedFilters[i]);
          }
          conf.setObject(ScoringFilter.class.getName(), filter);
        }
      } catch (PluginRuntimeException e) {
        throw new RuntimeException(e);
      }
      this.filters = (ScoringFilter[]) conf.getObject(ScoringFilter.class.getName());
    }
    if (this.filters == null || this.filters.length == 0)
      throw new RuntimeException("No scoring plugins - at least one scoring plugin is required!");
  }

  /** Calculate a sort value for Generate. */
  public float generatorSortValue(Text url, CrawlDatum datum, float initSort) throws ScoringFilterException {
    for (int i = 0; i < this.filters.length; i++) {
      initSort = this.filters[i].generatorSortValue(url, datum, initSort);
    }
    return initSort;
  }

  /** Calculate a new initial score, used when adding newly discovered pages. */
  public void initialScore(Text url, CrawlDatum datum) throws ScoringFilterException {
    for (int i = 0; i < this.filters.length; i++) {
      this.filters[i].initialScore(url, datum);
    }
  }

  /** Calculate a new initial score, used when injecting new pages. */
  public void injectedScore(Text url, CrawlDatum datum) throws ScoringFilterException {
    for (int i = 0; i < this.filters.length; i++) {
      this.filters[i].injectedScore(url, datum);
    }
  }

  /** Calculate updated page score during CrawlDb.update(). */
  public void updateDbScore(Text url, CrawlDatum old, CrawlDatum datum, List inlinked) throws ScoringFilterException {
    for (int i = 0; i < this.filters.length; i++) {
      this.filters[i].updateDbScore(url, old, datum, inlinked);
    }
  }

  public void passScoreBeforeParsing(Text url, CrawlDatum datum, Content content) throws ScoringFilterException {
    for (int i = 0; i < this.filters.length; i++) {
      this.filters[i].passScoreBeforeParsing(url, datum, content);
    }
  }
  
  public void passScoreAfterParsing(Text url, Content content, Parse parse) throws ScoringFilterException {
    for (int i = 0; i < this.filters.length; i++) {
      this.filters[i].passScoreAfterParsing(url, content, parse);
    }
  }
  
  public CrawlDatum distributeScoreToOutlink(Text fromUrl, Text toUrl, ParseData parseData, CrawlDatum target, CrawlDatum adjust, int allCount, int validCount) throws ScoringFilterException {
    for (int i = 0; i < this.filters.length; i++) {
      adjust = this.filters[i].distributeScoreToOutlink(fromUrl, toUrl, parseData, target, adjust, allCount, validCount);
    }
    return adjust;
  }

  public float indexerScore(Text url, Document doc, CrawlDatum dbDatum, CrawlDatum fetchDatum, Parse parse, Inlinks inlinks, float initScore) throws ScoringFilterException {
    for (int i = 0; i < this.filters.length; i++) {
      initScore = this.filters[i].indexerScore(url, doc, dbDatum, fetchDatum, parse, inlinks, initScore);
    }
    return initScore;
  }

}