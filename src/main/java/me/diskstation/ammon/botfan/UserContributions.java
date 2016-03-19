/*
 * Copyright 2007 Tobias Knerr.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * Contributors:
 * Tobias Knerr
 *
 */
package me.diskstation.ammon.botfan;

import java.util.Iterator;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import java.util.List;
import net.sourceforge.jwbf.core.actions.RequestBuilder;
import net.sourceforge.jwbf.core.actions.util.HttpAction;
import net.sourceforge.jwbf.mapper.XmlConverter;
import net.sourceforge.jwbf.mapper.XmlElement;
import net.sourceforge.jwbf.mediawiki.ApiRequestBuilder;
import net.sourceforge.jwbf.mediawiki.MediaWiki;
import net.sourceforge.jwbf.mediawiki.actions.queries.BaseQuery;
import net.sourceforge.jwbf.mediawiki.actions.util.MWAction;
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * action class using the MediaWiki-api's "list=imagelinks" and later imageUsage.
 *
 * @author Tobias Knerr
 * @author Thomas Stock
 * @since MediaWiki 1.9.0
 */
public class UserContributions extends BaseQuery<String> {

  private static final Logger log = LoggerFactory.getLogger(ImageUsageFlaggedTitles.class);

  private final int limit = 500;

  private final MediaWikiBot bot;
  
  private final String username;
  
  private final ImmutableList<Integer> namespacesList;
  
  private final int[] namespaces = {MediaWiki.NS_CATEGORY, 
      MediaWiki.NS_CATEGORY_TALK, MediaWiki.NS_HELP, 
      MediaWiki.NS_HELP_TALK, MediaWiki.NS_IMAGES, 
      MediaWiki.NS_IMAGES_TALK, MediaWiki.NS_MAIN,
      MediaWiki.NS_MAIN_TALK, MediaWiki.NS_MEDIAWIKI,
      MediaWiki.NS_MEDIAWIKI_TALK, MediaWiki.NS_META,
      MediaWiki.NS_META_TALK, MediaWiki.NS_TEMPLATE,
      MediaWiki.NS_TEMPLATE_TALK, 102, 103, 106, 107, 108, 109
  };


  
  public UserContributions(MediaWikiBot bot, String username) {
    super(bot);
    this.bot = bot;
    this.username = username;
    namespacesList = MWAction.nullSafeCopyOf(namespaces);
  }

  /**
   * gets the information about a follow-up page from a provided api response. If there is one, a
   * new request is added to msgs by calling generateRequest.
   *
   * @param xml text for parsing
   */
  @Override
  protected Optional<String> parseHasMore(final String xml) {
    return parseXmlHasMore(xml, "usercontribs", "uccontinue", "uccontinue");
  }

  /**
   * picks the article name from a MediaWiki api response.
   *
   * @param s text for parsing
   */
  @Override
  protected ImmutableList<String> parseElements(String s) {
    ImmutableList.Builder<String> titleCollection = ImmutableList.builder();
    Optional<XmlElement> childOpt = XmlConverter.getChildOpt(s, "query", "usercontribs");
    if (childOpt.isPresent()) {
      for (XmlElement element : childOpt.get().getChildren("item")) {
          //List<XmlElement> items = element.getChildren();
          //if (items.size() == 1){
              //XmlElement item = element.get(0);
                titleCollection.add(element.getAttributeValue("title"));
          //}
          
        
      }
    }
    return titleCollection.build();
  }

  @Override
  protected HttpAction prepareNextRequest() {
    RequestBuilder requestBuilder = new ApiRequestBuilder() //
        .action("query") //
        //.paramNewContinue(bot.getVersion()) //
        .formatXml() //
        .param("list", "usercontribs") //
        .param("uclimit", 2) //
        .param("ucnamespace", MediaWiki.urlEncodedNamespace(namespacesList))
        .param("ucstart", MediaWiki.urlEncode("2015-01-01 00:00:00"))
        .param("ucend", MediaWiki.urlEncode("2015-12-31 23:59:59"))
        .param("ucuser", MediaWiki.urlEncode(username))
        .param("ucdir", "newer");

    Optional<String> ilcontinue = nextPageInfoOpt();
    if (ilcontinue.isPresent()) {
      requestBuilder.param("uccontinue", MediaWiki.urlEncode(ilcontinue.get()));
    }
    return requestBuilder.buildGet();

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Iterator<String> copy() {
    return new UserContributions(bot, username);
  }

}
